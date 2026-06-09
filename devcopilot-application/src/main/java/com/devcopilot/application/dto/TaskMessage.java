package com.devcopilot.application.dto;

import com.devcopilot.domain.enums.TaskType;

public record TaskMessage(Long taskId, TaskType taskType, Long businessId) {
}
