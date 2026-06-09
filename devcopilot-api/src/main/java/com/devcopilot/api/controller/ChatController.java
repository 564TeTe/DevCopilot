package com.devcopilot.api.controller;

import com.devcopilot.api.support.CurrentUserSupport;
import com.devcopilot.application.service.ChatService;
import com.devcopilot.common.response.ApiResponse;
import com.devcopilot.domain.enums.ChatMode;
import com.devcopilot.domain.model.AiMessage;
import com.devcopilot.domain.model.AiSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/chat")
public class ChatController extends CurrentUserSupport {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/sessions")
    public ApiResponse<AiSession> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return ApiResponse.ok(chatService.createSession(
                currentUserId(), request.projectId(), request.title(), request.mode(), request.knowledgeBaseId()));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<AiSession>> listSessions() {
        return ApiResponse.ok(chatService.listMine(currentUserId()));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<AiMessage>> history(@PathVariable Long sessionId) {
        return ApiResponse.ok(chatService.history(currentUserId(), sessionId));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<AiMessage> ask(@PathVariable Long sessionId, @Valid @RequestBody AskRequest request) {
        return ApiResponse.ok(chatService.ask(currentUserId(), sessionId, request.content()));
    }

    @PostMapping(value = "/sessions/{sessionId}/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAsk(@PathVariable Long sessionId, @Valid @RequestBody AskRequest request) {
        return chatService.streamAsk(currentUserId(), sessionId, request.content());
    }

    public record CreateSessionRequest(
            @NotNull Long projectId,
            String title,
            @NotNull ChatMode mode,
            Long knowledgeBaseId
    ) {
    }

    public record AskRequest(@NotBlank String content) {
    }
}
