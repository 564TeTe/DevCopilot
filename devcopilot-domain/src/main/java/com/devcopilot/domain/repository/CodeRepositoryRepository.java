package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.CodeRepository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeRepositoryRepository extends JpaRepository<CodeRepository, Long> {

    List<CodeRepository> findByProjectIdOrderByIdDesc(Long projectId);
}
