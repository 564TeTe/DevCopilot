package com.devcopilot.domain.model;

import com.devcopilot.domain.enums.MessageRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_message")
public class AiMessage extends BaseEntity {

    @Column(nullable = false)
    private Long sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MessageRole role;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    protected AiMessage() {
    }

    public AiMessage(Long sessionId, MessageRole role, String content) {
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public MessageRole getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
