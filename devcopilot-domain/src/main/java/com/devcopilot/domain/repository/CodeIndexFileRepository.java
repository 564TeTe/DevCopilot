package com.devcopilot.domain.repository;

import com.devcopilot.domain.model.CodeIndexFile;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeIndexFileRepository extends JpaRepository<CodeIndexFile, Long> {

    void deleteByRepositoryId(Long repositoryId);

    List<CodeIndexFile> findTop10ByRepositoryIdOrderByIdDesc(Long repositoryId);
}
