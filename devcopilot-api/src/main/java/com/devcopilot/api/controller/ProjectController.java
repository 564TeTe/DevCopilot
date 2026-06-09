package com.devcopilot.api.controller;

import com.devcopilot.api.support.CurrentUserSupport;
import com.devcopilot.application.service.ProjectService;
import com.devcopilot.common.response.ApiResponse;
import com.devcopilot.domain.model.DevProject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController extends CurrentUserSupport {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ApiResponse<DevProject> create(@Valid @RequestBody CreateProjectRequest request) {
        return ApiResponse.ok(projectService.create(currentUserId(), request.name(), request.description()));
    }

    @GetMapping
    public ApiResponse<List<DevProject>> listMine() {
        return ApiResponse.ok(projectService.listMine(currentUserId()));
    }

    public record CreateProjectRequest(@NotBlank String name, String description) {
    }
}
