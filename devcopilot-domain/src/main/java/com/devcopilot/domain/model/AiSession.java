package com.devcopilot.domain.model;

import com.devcopilot.domain.enums.ChatMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_session")
public class AiSession extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 128)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ChatMode mode;

    private Long knowledgeBaseId;

    protected AiSession() {
    }

    public AiSession(Long projectId, Long userId, String title, ChatMode mode, Long knowledgeBaseId) {
        this.projectId = projectId;
        this.userId = userId;
        this.title = title;
        this.mode = mode;
        this.knowledgeBaseId = knowledgeBaseId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public ChatMode getMode() {
        return mode;
    }

    public Long getKnowledgeBaseId() {
        return knowledgeBaseId;
    }
}
