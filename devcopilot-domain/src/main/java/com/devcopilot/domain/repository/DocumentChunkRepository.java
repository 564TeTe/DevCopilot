package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.DocumentChunk;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    void deleteByDocumentId(Long documentId);

    List<DocumentChunk> findTop5ByKnowledgeBaseIdOrderByIdDesc(Long knowledgeBaseId);
}
