package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.AiSession;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiSessionRepository extends JpaRepository<AiSession, Long> {

    List<AiSession> findByUserIdOrderByIdDesc(Long userId);
}
