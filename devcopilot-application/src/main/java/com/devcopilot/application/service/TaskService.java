package com.devcopilot.application.service;

import com.devcopilot.application.dto.TaskMessage;
import com.devcopilot.application.port.AsyncTaskPublisher;
import com.devcopilot.application.port.TaskStatusCache;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.enums.TaskStatus;
import com.devcopilot.domain.enums.TaskType;
import com.devcopilot.domain.model.AsyncTask;
import com.devcopilot.domain.repository.AsyncTaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final AsyncTaskRepository taskRepository;
    private final AsyncTaskPublisher taskPublisher;
    private final TaskStatusCache taskStatusCache;

    public TaskService(AsyncTaskRepository taskRepository, AsyncTaskPublisher taskPublisher, TaskStatusCache taskStatusCache) {
        this.taskRepository = taskRepository;
        this.taskPublisher = taskPublisher;
        this.taskStatusCache = taskStatusCache;
    }

    @Transactional
    public AsyncTask createAndPublish(TaskType taskType, String businessType, Long businessId, String payload) {
        AsyncTask task = taskRepository.save(new AsyncTask(taskType, businessType, businessId, payload));
        cache(task);
        taskPublisher.publish(new TaskMessage(task.getId(), taskType, businessId));
        return task;
    }

    @Transactional(readOnly = true)
    public AsyncTask get(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "任务不存在"));
    }

    @Transactional(readOnly = true)
    public List<AsyncTask> listByBusiness(String businessType, Long businessId) {
        return taskRepository.findByBusinessTypeAndBusinessIdOrderByIdDesc(businessType, businessId);
    }

    @Transactional
    public void markRunning(Long taskId, String message) {
        AsyncTask task = getForUpdate(taskId);
        task.markRunning(message);
        cache(task);
    }

    @Transactional
    public void updateProgress(Long taskId, int progress, String message) {
        AsyncTask task = getForUpdate(taskId);
        task.updateProgress(progress, message);
        cache(task);
    }

    @Transactional
    public void markSuccess(Long taskId, String message) {
        AsyncTask task = getForUpdate(taskId);
        task.markSuccess(message);
        cache(task);
    }

    @Transactional
    public void markFailed(Long taskId, String message, Throwable throwable) {
        AsyncTask task = getForUpdate(taskId);
        task.markFailed(message, throwable == null ? null : throwable.getMessage());
        cache(task);
    }

    public String cachedStatus(Long taskId) {
        return taskStatusCache.get(taskId).orElseGet(() -> {
        AsyncTask task = get(taskId);
        return serialize(task);
        });
    }

    private AsyncTask getForUpdate(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "任务不存在"));
    }

    private void cache(AsyncTask task) {
        taskStatusCache.put(task.getId(), serialize(task));
    }

    private String serialize(AsyncTask task) {
        TaskStatus status = task.getStatus();
        return "{\"taskId\":" + task.getId()
                + ",\"status\":\"" + status + "\""
                + ",\"progress\":" + task.getProgress()
                + ",\"message\":\"" + escape(task.getMessage()) + "\"}";
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
