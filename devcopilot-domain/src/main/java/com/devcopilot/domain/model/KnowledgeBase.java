package com.devcopilot.domain.model;

import com.devcopilot.domain.enums.ResourceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "knowledge_base")
public class KnowledgeBase extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 1024)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ResourceStatus status;

    protected KnowledgeBase() {
    }

    public KnowledgeBase(Long projectId, String name, String description) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.status = ResourceStatus.ACTIVE;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ResourceStatus getStatus() {
        return status;
    }
}
