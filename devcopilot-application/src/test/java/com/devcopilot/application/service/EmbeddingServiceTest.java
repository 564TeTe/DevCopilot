package com.devcopilot.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmbeddingServiceTest {

    @Test
    void shouldCreatePgvectorCompatibleLiteral() {
        EmbeddingService embeddingService = new EmbeddingService();

        String embedding = embeddingService.embed("Spring Boot Redis RabbitMQ 知识库问答");

        assertThat(embedding).startsWith("[").endsWith("]");
        assertThat(embedding.substring(1, embedding.length() - 1).split(",")).hasSize(64);
    }
}
