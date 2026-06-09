package com.devcopilot.infrastructure.storage;

import com.devcopilot.application.port.FileStorage;
import com.devcopilot.infrastructure.config.StorageProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(StorageProperties storageProperties) {
        this.root = Path.of(storageProperties.getRoot()).toAbsolutePath().normalize();
    }

    @Override
    public String save(String originalFilename, byte[] content) throws IOException {
        LocalDate now = LocalDate.now();
        Path directory = root.resolve(Path.of(String.valueOf(now.getYear()), String.valueOf(now.getMonthValue()), String.valueOf(now.getDayOfMonth())));
        Files.createDirectories(directory);
        String safeName = sanitize(originalFilename);
        String storedName = UUID.randomUUID() + "-" + safeName;
        Path target = directory.resolve(storedName).normalize();
        Files.write(target, content);
        return root.relativize(target).toString().replace('\\', '/');
    }

    @Override
    public Path resolve(String storagePath) {
        return root.resolve(storagePath).normalize();
    }

    private String sanitize(String originalFilename) {
        String name = originalFilename == null || originalFilename.isBlank() ? "document.txt" : originalFilename;
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}
