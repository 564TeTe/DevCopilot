package com.devcopilot.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "code_index_file")
public class CodeIndexFile extends BaseEntity {

    @Column(nullable = false)
    private Long repositoryId;

    @Column(nullable = false, length = 1024)
    private String filePath;

    @Column(nullable = false, length = 64)
    private String language;

    @Column(nullable = false, length = 64)
    private String contentHash;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String summary;

    protected CodeIndexFile() {
    }

    public CodeIndexFile(Long repositoryId, String filePath, String language, String contentHash, String summary) {
        this.repositoryId = repositoryId;
        this.filePath = filePath;
        this.language = language;
        this.contentHash = contentHash;
        this.summary = summary;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getLanguage() {
        return language;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getSummary() {
        return summary;
    }
}
