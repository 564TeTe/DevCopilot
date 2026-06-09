package com.devcopilot.application.service;

import com.devcopilot.application.dto.VectorSearchResult;
import com.devcopilot.application.port.AiClient;
import com.devcopilot.application.port.VectorStore;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.enums.ChatMode;
import com.devcopilot.domain.enums.MessageRole;
import com.devcopilot.domain.model.AiMessage;
import com.devcopilot.domain.model.AiSession;
import com.devcopilot.domain.model.CodeIndexFile;
import com.devcopilot.domain.model.CodeRepository;
import com.devcopilot.domain.model.DocumentChunk;
import com.devcopilot.domain.repository.AiMessageRepository;
import com.devcopilot.domain.repository.AiSessionRepository;
import com.devcopilot.domain.repository.CodeIndexFileRepository;
import com.devcopilot.domain.repository.CodeRepositoryRepository;
import com.devcopilot.domain.repository.DocumentChunkRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ChatService {

    private final AiSessionRepository sessionRepository;
    private final AiMessageRepository messageRepository;
    private final DocumentChunkRepository chunkRepository;
    private final CodeRepositoryRepository codeRepositoryRepository;
    private final CodeIndexFileRepository codeIndexFileRepository;
    private final ProjectService projectService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final AiClient aiClient;
    private final TaskExecutor taskExecutor;

    public ChatService(AiSessionRepository sessionRepository, AiMessageRepository messageRepository,
                       DocumentChunkRepository chunkRepository, CodeRepositoryRepository codeRepositoryRepository,
                       CodeIndexFileRepository codeIndexFileRepository, ProjectService projectService,
                       KnowledgeBaseService knowledgeBaseService, EmbeddingService embeddingService,
                       VectorStore vectorStore, AiClient aiClient, TaskExecutor taskExecutor) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.chunkRepository = chunkRepository;
        this.codeRepositoryRepository = codeRepositoryRepository;
        this.codeIndexFileRepository = codeIndexFileRepository;
        this.projectService = projectService;
        this.knowledgeBaseService = knowledgeBaseService;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.aiClient = aiClient;
        this.taskExecutor = taskExecutor;
    }

    @Transactional
    public AiSession createSession(Long userId, Long projectId, String title, ChatMode mode, Long knowledgeBaseId) {
        projectService.getOwned(projectId, userId);
        if (mode == ChatMode.KNOWLEDGE && knowledgeBaseId != null) {
            knowledgeBaseService.getOwned(knowledgeBaseId, userId);
        }
        String safeTitle = title == null || title.isBlank() ? "新会话" : title;
        return sessionRepository.save(new AiSession(projectId, userId, safeTitle, mode, knowledgeBaseId));
    }

    @Transactional(readOnly = true)
    public List<AiSession> listMine(Long userId) {
        return sessionRepository.findByUserIdOrderByIdDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<AiMessage> history(Long userId, Long sessionId) {
        getOwnedSession(userId, sessionId);
        return messageRepository.findBySessionIdOrderByIdAsc(sessionId);
    }

    @Transactional
    public AiMessage ask(Long userId, Long sessionId, String question) {
        AiSession session = getOwnedSession(userId, sessionId);
        messageRepository.save(new AiMessage(sessionId, MessageRole.USER, question));
        List<String> contexts = retrieveContexts(session, question);
        String answer = String.join("", aiClient.streamAnswer(question, contexts));
        return messageRepository.save(new AiMessage(sessionId, MessageRole.ASSISTANT, answer));
    }

    public SseEmitter streamAsk(Long userId, Long sessionId, String question) {
        AiSession session = getOwnedSession(userId, sessionId);
        messageRepository.save(new AiMessage(sessionId, MessageRole.USER, question));
        SseEmitter emitter = new SseEmitter(120_000L);
        taskExecutor.execute(() -> {
            StringBuilder answer = new StringBuilder();
            try {
                List<String> contexts = retrieveContexts(session, question);
                for (String token : aiClient.streamAnswer(question, contexts)) {
                    answer.append(token);
                    emitter.send(SseEmitter.event().name("delta").data(token));
                    Thread.sleep(35L);
                }
                AiMessage saved = messageRepository.save(new AiMessage(sessionId, MessageRole.ASSISTANT, answer.toString()));
                emitter.send(SseEmitter.event().name("done").data(saved.getId()));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(ex.getMessage()));
                } catch (IOException ignored) {
                    // The client may already have disconnected.
                }
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    private AiSession getOwnedSession(Long userId, Long sessionId) {
        AiSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "会话不存在"));
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该会话");
        }
        return session;
    }

    private List<String> retrieveContexts(AiSession session, String question) {
        if (session.getMode() == ChatMode.KNOWLEDGE && session.getKnowledgeBaseId() != null) {
            return retrieveKnowledgeContexts(session.getKnowledgeBaseId(), question);
        }
        if (session.getMode() == ChatMode.CODE) {
            return retrieveCodeContexts(session.getProjectId());
        }
        return List.of();
    }

    private List<String> retrieveKnowledgeContexts(Long knowledgeBaseId, String question) {
        String queryEmbedding = embeddingService.embed(question);
        List<VectorSearchResult> hits = vectorStore.searchKnowledgeBase(knowledgeBaseId, queryEmbedding, 5);
        if (!hits.isEmpty()) {
            return hits.stream().map(VectorSearchResult::content).toList();
        }
        return chunkRepository.findTop5ByKnowledgeBaseIdOrderByIdDesc(knowledgeBaseId)
                .stream()
                .map(DocumentChunk::getContent)
                .toList();
    }

    private List<String> retrieveCodeContexts(Long projectId) {
        List<String> contexts = new ArrayList<>();
        for (CodeRepository repository : codeRepositoryRepository.findByProjectIdOrderByIdDesc(projectId)) {
            List<CodeIndexFile> files = codeIndexFileRepository.findTop10ByRepositoryIdOrderByIdDesc(repository.getId());
            for (CodeIndexFile file : files) {
                contexts.add("文件: " + file.getFilePath() + "\n" + file.getSummary());
            }
            if (contexts.size() >= 10) {
                break;
            }
        }
        return contexts;
    }
}
