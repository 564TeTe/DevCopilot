package com.devcopilot.application.service;

import com.devcopilot.application.dto.DocumentUploadResult;
import com.devcopilot.application.port.FileStorage;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.enums.TaskType;
import com.devcopilot.domain.model.AsyncTask;
import com.devcopilot.domain.model.KnowledgeDocument;
import com.devcopilot.domain.repository.KnowledgeDocumentRepository;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeBaseService knowledgeBaseService;
    private final FileStorage fileStorage;
    private final TaskService taskService;

    public DocumentService(KnowledgeDocumentRepository documentRepository, KnowledgeBaseService knowledgeBaseService,
                           FileStorage fileStorage, TaskService taskService) {
        this.documentRepository = documentRepository;
        this.knowledgeBaseService = knowledgeBaseService;
        this.fileStorage = fileStorage;
        this.taskService = taskService;
    }

    @Transactional
    public DocumentUploadResult upload(Long userId, Long knowledgeBaseId, String originalFilename, String contentType, byte[] content) {
        knowledgeBaseService.getOwned(knowledgeBaseId, userId);
        try {
            String storagePath = fileStorage.save(originalFilename, content);
            KnowledgeDocument document = documentRepository.save(
                    new KnowledgeDocument(knowledgeBaseId, originalFilename, contentType, storagePath));
            AsyncTask task = taskService.createAndPublish(TaskType.DOCUMENT_PARSE, "DOCUMENT", document.getId(), null);
            return new DocumentUploadResult(document.getId(), task.getId(), originalFilename);
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "文件保存失败");
        }
    }

    @Transactional(readOnly = true)
    public KnowledgeDocument getOwned(Long userId, Long documentId) {
        KnowledgeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "文档不存在"));
        knowledgeBaseService.getOwned(document.getKnowledgeBaseId(), userId);
        return document;
    }

    @Transactional(readOnly = true)
    public List<KnowledgeDocument> listByKnowledgeBase(Long userId, Long knowledgeBaseId) {
        knowledgeBaseService.getOwned(knowledgeBaseId, userId);
        return documentRepository.findByKnowledgeBaseIdOrderByIdDesc(knowledgeBaseId);
    }
}
