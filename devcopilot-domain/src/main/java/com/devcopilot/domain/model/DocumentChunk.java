package com.devcopilot.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_chunk")
public class DocumentChunk extends BaseEntity {

    @Column(nullable = false)
    private Long documentId;

    @Column(nullable = false)
    private Long knowledgeBaseId;

    @Column(nullable = false)
    private int chunkIndex;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false)
    private int tokenCount;

    protected DocumentChunk() {
    }

    public DocumentChunk(Long documentId, Long knowledgeBaseId, int chunkIndex, String content, int tokenCount) {
        this.documentId = documentId;
        this.knowledgeBaseId = knowledgeBaseId;
        this.chunkIndex = chunkIndex;
        this.content = content;
        this.tokenCount = tokenCount;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Long getKnowledgeBaseId() {
        return knowledgeBaseId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getContent() {
        return content;
    }

    public int getTokenCount() {
        return tokenCount;
    }
}
