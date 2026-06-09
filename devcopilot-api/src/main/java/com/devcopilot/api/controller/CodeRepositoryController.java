package com.devcopilot.api.controller;

import com.devcopilot.api.support.CurrentUserSupport;
import com.devcopilot.application.dto.RepositoryCreateResult;
import com.devcopilot.application.service.CodeRepositoryService;
import com.devcopilot.common.response.ApiResponse;
import com.devcopilot.domain.model.AsyncTask;
import com.devcopilot.domain.model.CodeRepository;
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
@RequestMapping("/api/code-repositories")
public class CodeRepositoryController extends CurrentUserSupport {

    private final CodeRepositoryService codeRepositoryService;

    public CodeRepositoryController(CodeRepositoryService codeRepositoryService) {
        this.codeRepositoryService = codeRepositoryService;
    }

    @PostMapping
    public ApiResponse<RepositoryCreateResult> create(@Valid @RequestBody CreateRepositoryRequest request) {
        return ApiResponse.ok(codeRepositoryService.create(
                currentUserId(),
                request.projectId(),
                request.name(),
                request.cloneUrl(),
                request.defaultBranch()
        ));
    }

    @GetMapping
    public ApiResponse<List<CodeRepository>> list(@RequestParam Long projectId) {
        return ApiResponse.ok(codeRepositoryService.listByProject(currentUserId(), projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<CodeRepository> get(@PathVariable Long id) {
        return ApiResponse.ok(codeRepositoryService.getOwned(currentUserId(), id));
    }

    @PostMapping("/{id}/index")
    public ApiResponse<AsyncTask> triggerIndex(@PathVariable Long id) {
        return ApiResponse.ok(codeRepositoryService.triggerIndex(currentUserId(), id));
    }

    public record CreateRepositoryRequest(
            @NotNull Long projectId,
            @NotBlank String name,
            @NotBlank String cloneUrl,
            String defaultBranch
    ) {
    }
}
