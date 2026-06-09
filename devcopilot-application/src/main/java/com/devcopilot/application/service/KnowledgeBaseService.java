package com.devcopilot.application.service;

import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.model.KnowledgeBase;
import com.devcopilot.domain.repository.KnowledgeBaseRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final ProjectService projectService;

    public KnowledgeBaseService(KnowledgeBaseRepository knowledgeBaseRepository, ProjectService projectService) {
        this.knowledgeBaseRepository = knowledgeBaseRepository;
        this.projectService = projectService;
    }

    @Transactional
    public KnowledgeBase create(Long userId, Long projectId, String name, String description) {
        projectService.getOwned(projectId, userId);
        return knowledgeBaseRepository.save(new KnowledgeBase(projectId, name, description));
    }

    @Transactional(readOnly = true)
    public List<KnowledgeBase> listByProject(Long userId, Long projectId) {
        projectService.getOwned(projectId, userId);
        return knowledgeBaseRepository.findByProjectIdOrderByIdDesc(projectId);
    }

    @Transactional(readOnly = true)
    public KnowledgeBase getOwned(Long knowledgeBaseId, Long userId) {
        KnowledgeBase knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "知识库不存在"));
        projectService.getOwned(knowledgeBase.getProjectId(), userId);
        return knowledgeBase;
    }
}
