package com.devcopilot.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextChunkerTest {

    @Test
    void shouldSplitLongTextIntoReadableChunks() {
        TextChunker chunker = new TextChunker();
        String text = "这是一个用于知识库切片的段落。".repeat(180);

        List<String> chunks = chunker.chunk(text);

        assertThat(chunks).hasSizeGreaterThan(1);
        assertThat(chunks).allMatch(chunk -> !chunk.isBlank());
        assertThat(chunker.estimateTokens(chunks.get(0))).isGreaterThan(0);
    }
}
