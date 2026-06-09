package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.KnowledgeBase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    List<KnowledgeBase> findByProjectIdOrderByIdDesc(Long projectId);
}
