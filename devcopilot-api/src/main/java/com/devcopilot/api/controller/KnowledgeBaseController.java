package com.devcopilot.api.controller;

import com.devcopilot.api.support.CurrentUserSupport;
import com.devcopilot.application.service.KnowledgeBaseService;
import com.devcopilot.common.response.ApiResponse;
import com.devcopilot.domain.model.KnowledgeBase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/knowledge-bases")
public class KnowledgeBaseController extends CurrentUserSupport {

    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseController(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping
    public ApiResponse<KnowledgeBase> create(@Valid @RequestBody CreateKnowledgeBaseRequest request) {
        return ApiResponse.ok(knowledgeBaseService.create(
                currentUserId(), request.projectId(), request.name(), request.description()));
    }

    @GetMapping
    public ApiResponse<List<KnowledgeBase>> list(@RequestParam Long projectId) {
        return ApiResponse.ok(knowledgeBaseService.listByProject(currentUserId(), projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBase> get(@PathVariable Long id) {
        return ApiResponse.ok(knowledgeBaseService.getOwned(id, currentUserId()));
    }

    public record CreateKnowledgeBaseRequest(@NotNull Long projectId, @NotBlank String name, String description) {
    }
}
