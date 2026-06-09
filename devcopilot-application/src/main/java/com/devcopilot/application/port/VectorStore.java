package com.devcopilot.application.port;

import com.devcopilot.application.dto.VectorSearchResult;
import java.util.List;

public interface VectorStore {

    void upsertDocumentChunk(Long chunkId, Long knowledgeBaseId, Long documentId, String embedding, String content);

    List<VectorSearchResult> searchKnowledgeBase(Long knowledgeBaseId, String embedding, int limit);

    void upsertCodeFile(Long fileId, Long repositoryId, String filePath, String embedding, String summary);
}
