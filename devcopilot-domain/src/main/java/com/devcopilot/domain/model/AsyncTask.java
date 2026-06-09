package com.devcopilot.domain.model;

import com.devcopilot.domain.enums.TaskStatus;
import com.devcopilot.domain.enums.TaskType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "async_task")
public class AsyncTask extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private TaskType taskType;

    @Column(nullable = false, length = 64)
    private String businessType;

    @Column(nullable = false)
    private Long businessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TaskStatus status;

    @Column(nullable = false)
    private int progress;

    @Column(length = 1024)
    private String message;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String payload;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String errorDetail;

    protected AsyncTask() {
    }

    public AsyncTask(TaskType taskType, String businessType, Long businessId, String payload) {
        this.taskType = taskType;
        this.businessType = businessType;
        this.businessId = businessId;
        this.payload = payload;
        this.status = TaskStatus.PENDING;
        this.progress = 0;
        this.message = "等待调度";
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public String getBusinessType() {
        return businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }

    public String getMessage() {
        return message;
    }

    public String getPayload() {
        return payload;
    }

    public String getErrorDetail() {
        return errorDetail;
    }

    public void markRunning(String message) {
        this.status = TaskStatus.RUNNING;
        this.progress = Math.max(this.progress, 5);
        this.message = message;
        this.errorDetail = null;
    }

    public void updateProgress(int progress, String message) {
        this.progress = Math.max(0, Math.min(100, progress));
        this.message = message;
    }

    public void markSuccess(String message) {
        this.status = TaskStatus.SUCCESS;
        this.progress = 100;
        this.message = message;
        this.errorDetail = null;
    }

    public void markFailed(String message, String errorDetail) {
        this.status = TaskStatus.FAILED;
        this.message = message;
        this.errorDetail = errorDetail;
    }
}
