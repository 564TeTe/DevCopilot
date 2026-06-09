package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.AiMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    List<AiMessage> findBySessionIdOrderByIdAsc(Long sessionId);
}
