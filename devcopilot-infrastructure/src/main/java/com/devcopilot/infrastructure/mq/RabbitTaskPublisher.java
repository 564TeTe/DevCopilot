package com.devcopilot.infrastructure.mq;

import com.devcopilot.application.dto.TaskMessage;
import com.devcopilot.application.port.AsyncTaskPublisher;
import com.devcopilot.domain.enums.TaskType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class RabbitTaskPublisher implements AsyncTaskPublisher {

    private final RabbitTemplate rabbitTemplate;

    public RabbitTaskPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(TaskMessage message) {
        rabbitTemplate.convertAndSend(RabbitNames.TASK_EXCHANGE, routingKey(message.taskType()), message);
    }

    private String routingKey(TaskType taskType) {
        return switch (taskType) {
            case DOCUMENT_PARSE -> RabbitNames.DOCUMENT_ROUTING_KEY;
            case CODE_INDEX -> RabbitNames.CODE_ROUTING_KEY;
            case PR_ANALYSIS -> RabbitNames.PR_ROUTING_KEY;
        };
    }
}
