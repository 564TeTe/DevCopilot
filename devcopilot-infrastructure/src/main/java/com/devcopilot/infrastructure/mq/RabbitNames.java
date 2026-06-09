package com.devcopilot.infrastructure.mq;

public final class RabbitNames {

    public static final String TASK_EXCHANGE = "devcopilot.task.exchange";

    public static final String DOCUMENT_QUEUE = "devcopilot.task.document";
    public static final String CODE_QUEUE = "devcopilot.task.code";
    public static final String PR_QUEUE = "devcopilot.task.pr";

    public static final String DOCUMENT_ROUTING_KEY = "task.document.parse";
    public static final String CODE_ROUTING_KEY = "task.code.index";
    public static final String PR_ROUTING_KEY = "task.pr.analysis";

    private RabbitNames() {
    }
}
