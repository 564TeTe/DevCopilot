package com.devcopilot.api.controller;

import com.devcopilot.api.support.CurrentUserSupport;
import com.devcopilot.application.dto.PrAnalysisCreateResult;
import com.devcopilot.application.service.PrAnalysisService;
import com.devcopilot.common.response.ApiResponse;
import com.devcopilot.domain.model.PrAnalysis;
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
@RequestMapping("/api/pr-analysis")
public class PrAnalysisController extends CurrentUserSupport {

    private final PrAnalysisService prAnalysisService;

    public PrAnalysisController(PrAnalysisService prAnalysisService) {
        this.prAnalysisService = prAnalysisService;
    }

    @PostMapping
    public ApiResponse<PrAnalysisCreateResult> create(@Valid @RequestBody CreatePrAnalysisRequest request) {
        return ApiResponse.ok(prAnalysisService.create(
                currentUserId(),
                request.projectId(),
                request.repositoryId(),
                request.title(),
                request.sourceBranch(),
                request.targetBranch(),
                request.diffContent()
        ));
    }

    @GetMapping
    public ApiResponse<List<PrAnalysis>> list(@RequestParam Long projectId) {
        return ApiResponse.ok(prAnalysisService.listByProject(currentUserId(), projectId));
    }

    @GetMapping("/{id}")
    public ApiResponse<PrAnalysis> get(@PathVariable Long id) {
        return ApiResponse.ok(prAnalysisService.getOwned(currentUserId(), id));
    }

    public record CreatePrAnalysisRequest(
            @NotNull Long projectId,
            Long repositoryId,
            @NotBlank String title,
            String sourceBranch,
            String targetBranch,
            @NotBlank String diffContent
    ) {
    }
}
