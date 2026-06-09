package com.devcopilot.application.port;

import java.nio.file.Path;

public interface TextExtractor {

    String extract(Path file);
}
