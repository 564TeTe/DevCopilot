package com.devcopilot.application.port;

import java.util.Optional;

public interface TaskStatusCache {

    void put(Long taskId, String statusJson);

    Optional<String> get(Long taskId);
}
