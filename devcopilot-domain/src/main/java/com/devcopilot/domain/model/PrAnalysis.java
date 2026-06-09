package com.devcopilot.domain.model;

import com.devcopilot.domain.enums.PrRiskLevel;
import com.devcopilot.domain.enums.TaskStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "pr_analysis")
public class PrAnalysis extends BaseEntity {

    @Column(nullable = false)
    private Long projectId;

    private Long repositoryId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 128)
    private String sourceBranch;

    @Column(length = 128)
    private String targetBranch;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String diffContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PrRiskLevel riskLevel;

    @Column(length = 1024)
    private String summary;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String report;

    protected PrAnalysis() {
    }

    public PrAnalysis(Long projectId, Long repositoryId, String title, String sourceBranch, String targetBranch, String diffContent) {
        this.projectId = projectId;
        this.repositoryId = repositoryId;
        this.title = title;
        this.sourceBranch = sourceBranch;
        this.targetBranch = targetBranch;
        this.diffContent = diffContent;
        this.status = TaskStatus.PENDING;
        this.riskLevel = PrRiskLevel.LOW;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getRepositoryId() {
        return repositoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public String getDiffContent() {
        return diffContent;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public PrRiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getSummary() {
        return summary;
    }

    public String getReport() {
        return report;
    }

    public void markRunning() {
        this.status = TaskStatus.RUNNING;
    }

    public void markSuccess(PrRiskLevel riskLevel, String summary, String report) {
        this.status = TaskStatus.SUCCESS;
        this.riskLevel = riskLevel;
        this.summary = summary;
        this.report = report;
    }

    public void markFailed(String errorMessage) {
        this.status = TaskStatus.FAILED;
        this.summary = errorMessage;
    }
}
