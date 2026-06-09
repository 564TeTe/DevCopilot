package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.DevProject;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevProjectRepository extends JpaRepository<DevProject, Long> {

    List<DevProject> findByOwnerIdOrderByIdDesc(Long ownerId);
}
