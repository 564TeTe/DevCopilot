package com.devcopilot.infrastructure.vector;

import com.devcopilot.application.dto.VectorSearchResult;
import com.devcopilot.application.port.VectorStore;
import com.devcopilot.infrastructure.config.VectorStoreProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

@Component
public class PostgresVectorStore implements VectorStore {

    private static final Logger log = LoggerFactory.getLogger(PostgresVectorStore.class);

    private final JdbcTemplate jdbcTemplate;
    private final boolean enabled;

    public PostgresVectorStore(VectorStoreProperties properties) {
        this.enabled = properties.isEnabled() && properties.getUrl() != null && !properties.getUrl().isBlank();
        if (enabled) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUrl(properties.getUrl());
            dataSource.setUsername(properties.getUsername());
            dataSource.setPassword(properties.getPassword());
            this.jdbcTemplate = new JdbcTemplate(dataSource);
        } else {
            this.jdbcTemplate = null;
        }
    }

    @Override
    public void upsertDocumentChunk(Long chunkId, Long knowledgeBaseId, Long documentId, String embedding, String content) {
        if (!enabled) {
            return;
        }
        try {
            jdbcTemplate.update("""
                    insert into kb_chunk_embedding (chunk_id, knowledge_base_id, document_id, embedding, content)
                    values (?, ?, ?, cast(? as vector), ?)
                    on conflict (chunk_id) do update set
                        knowledge_base_id = excluded.knowledge_base_id,
                        document_id = excluded.document_id,
                        embedding = excluded.embedding,
                        content = excluded.content,
                        updated_at = now()
                    """, chunkId, knowledgeBaseId, documentId, embedding, content);
        } catch (Exception ex) {
            log.warn("Failed to upsert document vector, chunkId={}", chunkId, ex);
        }
    }

    @Override
    public List<VectorSearchResult> searchKnowledgeBase(Long knowledgeBaseId, String embedding, int limit) {
        if (!enabled) {
            return List.of();
        }
        try {
            return jdbcTemplate.query("""
                    select chunk_id, content, 1 - (embedding <=> cast(? as vector)) as score
                    from kb_chunk_embedding
                    where knowledge_base_id = ?
                    order by embedding <=> cast(? as vector)
                    limit ?
                    """,
                    (rs, rowNum) -> new VectorSearchResult(rs.getLong("chunk_id"), rs.getString("content"), rs.getDouble("score")),
                    embedding, knowledgeBaseId, embedding, limit);
        } catch (Exception ex) {
            log.warn("Failed to search knowledge vectors, knowledgeBaseId={}", knowledgeBaseId, ex);
            return List.of();
        }
    }

    @Override
    public void upsertCodeFile(Long fileId, Long repositoryId, String filePath, String embedding, String summary) {
        if (!enabled) {
            return;
        }
        try {
            jdbcTemplate.update("""
                    insert into code_file_embedding (file_id, repository_id, file_path, embedding, summary)
                    values (?, ?, ?, cast(? as vector), ?)
                    on conflict (file_id) do update set
                        repository_id = excluded.repository_id,
                        file_path = excluded.file_path,
                        embedding = excluded.embedding,
                        summary = excluded.summary,
                        updated_at = now()
                    """, fileId, repositoryId, filePath, embedding, summary);
        } catch (Exception ex) {
            log.warn("Failed to upsert code vector, fileId={}", fileId, ex);
        }
    }
}
