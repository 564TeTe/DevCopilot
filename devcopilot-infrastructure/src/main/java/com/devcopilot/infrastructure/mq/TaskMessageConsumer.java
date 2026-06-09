package com.devcopilot.infrastructure.mq;

import com.devcopilot.application.dto.TaskMessage;
import com.devcopilot.application.service.CodeIndexWorkflowService;
import com.devcopilot.application.service.DocumentWorkflowService;
import com.devcopilot.application.service.PrAnalysisWorkflowService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TaskMessageConsumer {

    private final DocumentWorkflowService documentWorkflowService;
    private final CodeIndexWorkflowService codeIndexWorkflowService;
    private final PrAnalysisWorkflowService prAnalysisWorkflowService;

    public TaskMessageConsumer(DocumentWorkflowService documentWorkflowService,
                               CodeIndexWorkflowService codeIndexWorkflowService,
                               PrAnalysisWorkflowService prAnalysisWorkflowService) {
        this.documentWorkflowService = documentWorkflowService;
        this.codeIndexWorkflowService = codeIndexWorkflowService;
        this.prAnalysisWorkflowService = prAnalysisWorkflowService;
    }

    @RabbitListener(queues = RabbitNames.DOCUMENT_QUEUE)
    public void onDocumentTask(TaskMessage message) {
        documentWorkflowService.parseAndIndex(message.taskId(), message.businessId());
    }

    @RabbitListener(queues = RabbitNames.CODE_QUEUE)
    public void onCodeTask(TaskMessage message) {
        codeIndexWorkflowService.index(message.taskId(), message.businessId());
    }

    @RabbitListener(queues = RabbitNames.PR_QUEUE)
    public void onPrTask(TaskMessage message) {
        prAnalysisWorkflowService.analyze(message.taskId(), message.businessId());
    }
}
