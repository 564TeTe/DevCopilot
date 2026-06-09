package com.devcopilot.application.service;

import com.devcopilot.application.port.FileStorage;
import com.devcopilot.application.port.TextExtractor;
import com.devcopilot.application.port.VectorStore;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.model.DocumentChunk;
import com.devcopilot.domain.model.KnowledgeDocument;
import com.devcopilot.domain.repository.DocumentChunkRepository;
import com.devcopilot.domain.repository.KnowledgeDocumentRepository;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentWorkflowService {

    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final FileStorage fileStorage;
    private final TextExtractor textExtractor;
    private final TextChunker textChunker;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final TaskService taskService;

    public DocumentWorkflowService(KnowledgeDocumentRepository documentRepository, DocumentChunkRepository chunkRepository,
                                   FileStorage fileStorage, TextExtractor textExtractor, TextChunker textChunker,
                                   EmbeddingService embeddingService, VectorStore vectorStore, TaskService taskService) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.fileStorage = fileStorage;
        this.textExtractor = textExtractor;
        this.textChunker = textChunker;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.taskService = taskService;
    }

    public void parseAndIndex(Long taskId, Long documentId) {
        KnowledgeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "文档不存在"));
        try {
            taskService.markRunning(taskId, "开始解析文档");
            document.markParsing();
            documentRepository.save(document);

            Path file = fileStorage.resolve(document.getStoragePath());
            String text = textExtractor.extract(file);
            List<String> chunks = textChunker.chunk(text);
            taskService.updateProgress(taskId, 35, "文本解析完成，开始切片");

            chunkRepository.deleteByDocumentId(document.getId());
            document.markIndexing();
            documentRepository.save(document);
            for (int i = 0; i < chunks.size(); i++) {
                String chunkContent = chunks.get(i);
                DocumentChunk chunk = chunkRepository.save(new DocumentChunk(
                        document.getId(),
                        document.getKnowledgeBaseId(),
                        i,
                        chunkContent,
                        textChunker.estimateTokens(chunkContent)
                ));
                String embedding = embeddingService.embed(chunkContent);
                vectorStore.upsertDocumentChunk(chunk.getId(), document.getKnowledgeBaseId(), document.getId(), embedding, chunkContent);
                int progress = 40 + (int) (((i + 1) / (double) Math.max(1, chunks.size())) * 55);
                taskService.updateProgress(taskId, progress, "向量化进度 " + (i + 1) + "/" + chunks.size());
            }

            document.markReady(chunks.size());
            documentRepository.save(document);
            taskService.markSuccess(taskId, "文档解析与向量入库完成");
        } catch (Exception ex) {
            document.markFailed();
            documentRepository.save(document);
            taskService.markFailed(taskId, "文档解析失败", ex);
            throw ex;
        }
    }
}
