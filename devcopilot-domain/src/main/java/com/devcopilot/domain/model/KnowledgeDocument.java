package com.devcopilot.domain.model;

import com.devcopilot.domain.enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "knowledge_document")
public class KnowledgeDocument extends BaseEntity {

    @Column(nullable = false)
    private Long knowledgeBaseId;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 128)
    private String contentType;

    @Column(nullable = false, length = 1024)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DocumentStatus status;

    @Column(nullable = false)
    private int totalChunks;

    protected KnowledgeDocument() {
    }

    public KnowledgeDocument(Long knowledgeBaseId, String fileName, String contentType, String storagePath) {
        this.knowledgeBaseId = knowledgeBaseId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.storagePath = storagePath;
        this.status = DocumentStatus.UPLOADED;
        this.totalChunks = 0;
    }

    public Long getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public void markParsing() {
        this.status = DocumentStatus.PARSING;
    }

    public void markIndexing() {
        this.status = DocumentStatus.INDEXING;
    }

    public void markReady(int totalChunks) {
        this.status = DocumentStatus.READY;
        this.totalChunks = totalChunks;
    }

    public void markFailed() {
        this.status = DocumentStatus.FAILED;
    }
}
