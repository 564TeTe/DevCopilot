package com.devcopilot.domain.model;

import com.devcopilot.domain.enums.RepositoryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "code_repository")
public class CodeRepository extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 1024)
    private String cloneUrl;

    @Column(nullable = false, length = 128)
    private String defaultBranch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RepositoryStatus status;

    @Column(nullable = false)
    private int indexedFiles;

    protected CodeRepository() {
    }

    public CodeRepository(Long projectId, String name, String cloneUrl, String defaultBranch) {
        this.projectId = projectId;
        this.name = name;
        this.cloneUrl = cloneUrl;
        this.defaultBranch = defaultBranch;
        this.status = RepositoryStatus.CREATED;
        this.indexedFiles = 0;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public RepositoryStatus getStatus() {
        return status;
    }

    public int getIndexedFiles() {
        return indexedFiles;
    }

    public void markIndexing() {
        this.status = RepositoryStatus.INDEXING;
    }

    public void markReady(int indexedFiles) {
        this.status = RepositoryStatus.READY;
        this.indexedFiles = indexedFiles;
    }

    public void markFailed() {
        this.status = RepositoryStatus.FAILED;
    }
}
