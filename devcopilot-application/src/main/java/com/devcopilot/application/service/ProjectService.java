package com.devcopilot.application.service;

import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.model.DevProject;
import com.devcopilot.domain.repository.DevProjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final DevProjectRepository projectRepository;

    public ProjectService(DevProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional
    public DevProject create(Long userId, String name, String description) {
        return projectRepository.save(new DevProject(userId, name, description));
    }

    @Transactional(readOnly = true)
    public List<DevProject> listMine(Long userId) {
        return projectRepository.findByOwnerIdOrderByIdDesc(userId);
    }

    @Transactional(readOnly = true)
    public DevProject getOwned(Long projectId, Long userId) {
        DevProject project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "项目不存在"));
        if (!project.getOwnerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权访问该项目");
        }
        return project;
    }
}
