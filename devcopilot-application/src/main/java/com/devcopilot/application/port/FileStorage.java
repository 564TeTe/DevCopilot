package com.devcopilot.application.port;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorage {

    String save(String originalFilename, byte[] content) throws IOException;

    Path resolve(String storagePath);
}
