package com.devcopilot.application.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TextChunker {

    private static final int TARGET_CHARS = 900;
    private static final int OVERLAP_CHARS = 120;

    public List<String> chunk(String text) {
        String normalized = normalize(text);
        List<String> chunks = new ArrayList<>();
        if (normalized.isBlank()) {
            return chunks;
        }
        int cursor = 0;
        while (cursor < normalized.length()) {
            int end = Math.min(cursor + TARGET_CHARS, normalized.length());
            int boundary = findBoundary(normalized, cursor, end);
            chunks.add(normalized.substring(cursor, boundary).trim());
            if (boundary >= normalized.length()) {
                break;
            }
            cursor = Math.max(0, boundary - OVERLAP_CHARS);
        }
        return chunks;
    }

    public int estimateTokens(String chunk) {
        if (chunk == null || chunk.isBlank()) {
            return 0;
        }
        return Math.max(1, chunk.length() / 2);
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[\\t ]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private int findBoundary(String text, int start, int proposedEnd) {
        if (proposedEnd >= text.length()) {
            return text.length();
        }
        for (int i = proposedEnd; i > start + 200; i--) {
            char ch = text.charAt(i - 1);
            if (ch == '\n' || ch == '。' || ch == '.' || ch == ';' || ch == '；') {
                return i;
            }
        }
        return proposedEnd;
    }
}
