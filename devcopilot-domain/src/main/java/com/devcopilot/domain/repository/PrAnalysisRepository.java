package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.PrAnalysis;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrAnalysisRepository extends JpaRepository<PrAnalysis, Long> {

    List<PrAnalysis> findByProjectIdOrderByIdDesc(Long projectId);
}
