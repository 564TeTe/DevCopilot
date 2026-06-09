package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.KnowledgeDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {

    List<KnowledgeDocument> findByKnowledgeBaseIdOrderByIdDesc(Long knowledgeBaseId);
}
