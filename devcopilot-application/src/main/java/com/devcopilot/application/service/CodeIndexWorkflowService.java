package com.devcopilot.application.service;

import com.devcopilot.application.port.VectorStore;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.model.CodeIndexFile;
import com.devcopilot.domain.model.CodeRepository;
import com.devcopilot.domain.repository.CodeIndexFileRepository;
import com.devcopilot.domain.repository.CodeRepositoryRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class CodeIndexWorkflowService {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "java", "kt", "kts", "js", "ts", "tsx", "jsx", "py", "go", "rs", "sql", "md", "yml", "yaml", "xml", "json"
    );
    private static final Set<String> SKIPPED_DIRS = Set.of(".git", "target", "node_modules", "dist", "build", ".idea", ".gradle");
    private static final int MAX_FILES = 300;
    private static final long MAX_FILE_BYTES = 220_000L;

    private final CodeRepositoryRepository repositoryRepository;
    private final CodeIndexFileRepository indexFileRepository;
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final TaskService taskService;

    public CodeIndexWorkflowService(CodeRepositoryRepository repositoryRepository, CodeIndexFileRepository indexFileRepository,
                                    EmbeddingService embeddingService, VectorStore vectorStore, TaskService taskService) {
        this.repositoryRepository = repositoryRepository;
        this.indexFileRepository = indexFileRepository;
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.taskService = taskService;
    }

    public void index(Long taskId, Long repositoryId) {
        CodeRepository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "代码仓库不存在"));
        try {
            taskService.markRunning(taskId, "开始准备代码仓库");
            repository.markIndexing();
            repositoryRepository.save(repository);

            Path root = resolveRepositoryRoot(repository);
            indexFileRepository.deleteByRepositoryId(repository.getId());
            taskService.updateProgress(taskId, 20, "代码仓库准备完成，开始扫描文件");

            List<Path> files;
            try (Stream<Path> stream = Files.walk(root)) {
                files = stream.filter(Files::isRegularFile)
                        .filter(this::isSupportedSource)
                        .filter(this::isSmallEnough)
                        .limit(MAX_FILES)
                        .toList();
            }

            int indexed = 0;
            for (Path file : files) {
                String content = readFile(file);
                if (content.isBlank()) {
                    continue;
                }
                String relativePath = root.relativize(file).toString().replace('\\', '/');
                String language = languageOf(file);
                String summary = summarize(relativePath, language, content);
                CodeIndexFile saved = indexFileRepository.save(new CodeIndexFile(
                        repository.getId(), relativePath, language, sha256(content), summary));
                vectorStore.upsertCodeFile(saved.getId(), repository.getId(), relativePath, embeddingService.embed(summary), summary);
                indexed++;
                int progress = 25 + (int) ((indexed / (double) Math.max(1, files.size())) * 70);
                taskService.updateProgress(taskId, progress, "代码索引进度 " + indexed + "/" + files.size());
            }

            repository.markReady(indexed);
            repositoryRepository.save(repository);
            taskService.markSuccess(taskId, "代码索引完成，共索引 " + indexed + " 个文件");
        } catch (Exception ex) {
            repository.markFailed();
            repositoryRepository.save(repository);
            taskService.markFailed(taskId, "代码索引失败", ex);
            throw new IllegalStateException(ex);
        }
    }

    private Path resolveRepositoryRoot(CodeRepository repository) throws IOException, InterruptedException {
        Path directPath = Path.of(repository.getCloneUrl());
        if (Files.isDirectory(directPath)) {
            return directPath;
        }
        Path target = Path.of("data", "repos", "repository-" + repository.getId()).toAbsolutePath();
        deleteDirectory(target);
        Files.createDirectories(target.getParent());
        Process process = new ProcessBuilder("git", "clone", "--depth", "1", "--branch",
                repository.getDefaultBranch(), repository.getCloneUrl(), target.toString())
                .redirectErrorStream(true)
                .start();
        boolean finished = process.waitFor(Duration.ofSeconds(90).toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Git clone timed out");
        }
        if (process.exitValue() != 0) {
            throw new IOException("Git clone failed with exit code " + process.exitValue());
        }
        return target;
    }

    private void deleteDirectory(Path target) throws IOException {
        if (!Files.exists(target)) {
            return;
        }
        try (Stream<Path> stream = Files.walk(target)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }

    private boolean isSupportedSource(Path file) {
        for (Path part : file) {
            if (SKIPPED_DIRS.contains(part.toString())) {
                return false;
            }
        }
        return SUPPORTED_EXTENSIONS.contains(languageOf(file));
    }

    private boolean isSmallEnough(Path file) {
        try {
            return Files.size(file) <= MAX_FILE_BYTES;
        } catch (IOException ex) {
            return false;
        }
    }

    private String languageOf(Path file) {
        String name = file.getFileName().toString();
        int index = name.lastIndexOf('.');
        return index < 0 ? "text" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String readFile(Path file) {
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            return "";
        }
    }

    private String summarize(String filePath, String language, String content) {
        String preview = content.length() > 1600 ? content.substring(0, 1600) : content;
        return "语言: " + language + "\n路径: " + filePath + "\n摘要材料:\n" + preview;
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
