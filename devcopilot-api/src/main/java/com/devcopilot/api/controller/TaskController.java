package com.devcopilot.api.controller;

import com.devcopilot.api.support.CurrentUserSupport;
import com.devcopilot.application.service.TaskService;
import com.devcopilot.common.response.ApiResponse;
import com.devcopilot.domain.model.AsyncTask;
import java.io.IOException;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/tasks")
public class TaskController extends CurrentUserSupport {

    private final TaskService taskService;
    private final TaskExecutor taskExecutor;

    public TaskController(TaskService taskService, TaskExecutor taskExecutor) {
        this.taskService = taskService;
        this.taskExecutor = taskExecutor;
    }

    @GetMapping("/{id}")
    public ApiResponse<AsyncTask> get(@PathVariable Long id) {
        currentUserId();
        return ApiResponse.ok(taskService.get(id));
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable Long id) {
        currentUserId();
        SseEmitter emitter = new SseEmitter(90_000L);
        taskExecutor.execute(() -> {
            try {
                for (int i = 0; i < 90; i++) {
                    String status = taskService.cachedStatus(id);
                    emitter.send(SseEmitter.event().name("progress").data(status));
                    if (status.contains("\"SUCCESS\"") || status.contains("\"FAILED\"") || status.contains("\"CANCELED\"")) {
                        break;
                    }
                    Thread.sleep(1000L);
                }
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            } catch (Exception ex) {
                try {
                    emitter.send(SseEmitter.event().name("error").data(ex.getMessage()));
                } catch (IOException ignored) {
                    // The client may already have disconnected.
                }
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }
}
