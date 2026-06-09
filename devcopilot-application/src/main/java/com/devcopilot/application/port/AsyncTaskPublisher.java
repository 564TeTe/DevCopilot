package com.devcopilot.application.port;

import com.devcopilot.application.dto.TaskMessage;

public interface AsyncTaskPublisher {

    void publish(TaskMessage message);
}
