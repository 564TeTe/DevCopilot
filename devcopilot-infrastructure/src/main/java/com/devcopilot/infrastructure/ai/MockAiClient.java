package com.devcopilot.infrastructure.ai;

import com.devcopilot.application.port.AiClient;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockAiClient implements AiClient {

    @Override
    public List<String> streamAnswer(String question, List<String> contexts) {
        StringBuilder answer = new StringBuilder();
        answer.append("我已根据当前上下文完成分析。\n\n");
        if (contexts == null || contexts.isEmpty()) {
            answer.append("没有检索到专属知识库上下文，因此先给出通用研发建议：")
                    .append("请确认问题背景、相关模块、输入输出和失败日志，再定位实现路径。\n\n");
        } else {
            answer.append("检索到 ").append(contexts.size()).append(" 段相关上下文，核心依据如下：\n");
            for (int i = 0; i < Math.min(3, contexts.size()); i++) {
                answer.append("- 片段 ").append(i + 1).append(": ")
                        .append(compact(contexts.get(i), 180)).append("\n");
            }
            answer.append("\n");
        }
        answer.append("针对你的问题「").append(question).append("」，建议按以下顺序处理：\n")
                .append("1. 先确认业务目标和接口边界。\n")
                .append("2. 再检查数据模型、异步任务状态和缓存一致性。\n")
                .append("3. 最后补充必要的回归测试和异常场景验证。");
        return split(answer.toString(), 24);
    }

    @Override
    public String complete(String instruction, String content) {
        return instruction + "\n\n"
                + "主要结论:\n"
                + "- 变更已完成结构化分析，可进入人工复核。\n"
                + "- 需要重点关注鉴权、事务边界、缓存一致性和异步任务失败补偿。\n\n"
                + "建议测试点:\n"
                + "- 覆盖正常提交、空 diff、大规模 diff 和高风险关键词场景。\n"
                + "- 验证 RabbitMQ 消费失败后的任务状态是否正确落库。\n"
                + "- 验证 Redis 缓存丢失时接口是否仍可从 MySQL 查询任务状态。\n\n"
                + "输入摘要:\n" + compact(content, 1200);
    }

    private List<String> split(String value, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int cursor = 0;
        while (cursor < value.length()) {
            int end = Math.min(cursor + chunkSize, value.length());
            chunks.add(value.substring(cursor, end));
            cursor = end;
        }
        return chunks;
    }

    private String compact(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }
}
