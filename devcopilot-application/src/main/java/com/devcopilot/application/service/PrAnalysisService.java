package com.devcopilot.application.service;

import com.devcopilot.application.dto.PrAnalysisCreateResult;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.enums.TaskType;
import com.devcopilot.domain.model.AsyncTask;
import com.devcopilot.domain.model.CodeRepository;
import com.devcopilot.domain.model.PrAnalysis;
import com.devcopilot.domain.repository.PrAnalysisRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrAnalysisService {

    private final PrAnalysisRepository prAnalysisRepository;
    private final ProjectService projectService;
    private final CodeRepositoryService codeRepositoryService;
    private final TaskService taskService;

    public PrAnalysisService(PrAnalysisRepository prAnalysisRepository, ProjectService projectService,
                             CodeRepositoryService codeRepositoryService, TaskService taskService) {
        this.prAnalysisRepository = prAnalysisRepository;
        this.projectService = projectService;
        this.codeRepositoryService = codeRepositoryService;
        this.taskService = taskService;
    }

    @Transactional
    public PrAnalysisCreateResult create(Long userId, Long projectId, Long repositoryId, String title,
                                         String sourceBranch, String targetBranch, String diffContent) {
        projectService.getOwned(projectId, userId);
        if (repositoryId != null) {
            CodeRepository repository = codeRepositoryService.getOwned(userId, repositoryId);
            if (!repository.getProjectId().equals(projectId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "代码仓库不属于该项目");
            }
        }
        PrAnalysis analysis = prAnalysisRepository.save(
                new PrAnalysis(projectId, repositoryId, title, sourceBranch, targetBranch, diffContent));
        AsyncTask task = taskService.createAndPublish(TaskType.PR_ANALYSIS, "PR_ANALYSIS", analysis.getId(), null);
        return new PrAnalysisCreateResult(analysis.getId(), task.getId(), title);
    }

    @Transactional(readOnly = true)
    public PrAnalysis getOwned(Long userId, Long analysisId) {
        PrAnalysis analysis = prAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "PR 分析不存在"));
        projectService.getOwned(analysis.getProjectId(), userId);
        return analysis;
    }

    @Transactional(readOnly = true)
    public List<PrAnalysis> listByProject(Long userId, Long projectId) {
        projectService.getOwned(projectId, userId);
        return prAnalysisRepository.findByProjectIdOrderByIdDesc(projectId);
    }
}
