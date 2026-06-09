package com.devcopilot.application.service;

import com.devcopilot.application.dto.RepositoryCreateResult;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.enums.TaskType;
import com.devcopilot.domain.model.AsyncTask;
import com.devcopilot.domain.model.CodeRepository;
import com.devcopilot.domain.repository.CodeRepositoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeRepositoryService {

    private final CodeRepositoryRepository codeRepositoryRepository;
    private final ProjectService projectService;
    private final TaskService taskService;

    public CodeRepositoryService(CodeRepositoryRepository codeRepositoryRepository, ProjectService projectService,
                                 TaskService taskService) {
        this.codeRepositoryRepository = codeRepositoryRepository;
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @Transactional
    public RepositoryCreateResult create(Long userId, Long projectId, String name, String cloneUrl, String defaultBranch) {
        projectService.getOwned(projectId, userId);
        String branch = defaultBranch == null || defaultBranch.isBlank() ? "main" : defaultBranch;
        CodeRepository repository = codeRepositoryRepository.save(new CodeRepository(projectId, name, cloneUrl, branch));
        AsyncTask task = taskService.createAndPublish(TaskType.CODE_INDEX, "CODE_REPOSITORY", repository.getId(), null);
        return new RepositoryCreateResult(repository.getId(), task.getId(), name);
    }

    @Transactional
    public AsyncTask triggerIndex(Long userId, Long repositoryId) {
        CodeRepository repository = getOwned(userId, repositoryId);
        return taskService.createAndPublish(TaskType.CODE_INDEX, "CODE_REPOSITORY", repository.getId(), null);
    }

    @Transactional(readOnly = true)
    public List<CodeRepository> listByProject(Long userId, Long projectId) {
        projectService.getOwned(projectId, userId);
        return codeRepositoryRepository.findByProjectIdOrderByIdDesc(projectId);
    }

    @Transactional(readOnly = true)
    public CodeRepository getOwned(Long userId, Long repositoryId) {
        CodeRepository repository = codeRepositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "代码仓库不存在"));
        projectService.getOwned(repository.getProjectId(), userId);
        return repository;
    }
}
