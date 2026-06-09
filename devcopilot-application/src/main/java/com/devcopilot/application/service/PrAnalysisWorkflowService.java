package com.devcopilot.application.service;

import com.devcopilot.application.port.AiClient;
import com.devcopilot.common.exception.BusinessException;
import com.devcopilot.common.exception.ErrorCode;
import com.devcopilot.domain.enums.PrRiskLevel;
import com.devcopilot.domain.model.PrAnalysis;
import com.devcopilot.domain.repository.PrAnalysisRepository;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class PrAnalysisWorkflowService {

    private final PrAnalysisRepository prAnalysisRepository;
    private final AiClient aiClient;
    private final TaskService taskService;

    public PrAnalysisWorkflowService(PrAnalysisRepository prAnalysisRepository, AiClient aiClient, TaskService taskService) {
        this.prAnalysisRepository = prAnalysisRepository;
        this.aiClient = aiClient;
        this.taskService = taskService;
    }

    public void analyze(Long taskId, Long analysisId) {
        PrAnalysis analysis = prAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "PR 分析不存在"));
        try {
            taskService.markRunning(taskId, "开始分析 PR diff");
            analysis.markRunning();
            prAnalysisRepository.save(analysis);

            taskService.updateProgress(taskId, 35, "识别变更规模与风险关键词");
            PrRiskLevel riskLevel = riskLevel(analysis.getDiffContent());
            String summary = summary(analysis.getDiffContent(), riskLevel);

            taskService.updateProgress(taskId, 70, "生成审查报告");
            String report = aiClient.complete("请作为资深代码评审助手生成 PR 分析报告", buildPrompt(analysis, riskLevel, summary));
            analysis.markSuccess(riskLevel, summary, report);
            prAnalysisRepository.save(analysis);
            taskService.markSuccess(taskId, "PR 分析完成，风险等级 " + riskLevel);
        } catch (Exception ex) {
            analysis.markFailed(ex.getMessage());
            prAnalysisRepository.save(analysis);
            taskService.markFailed(taskId, "PR 分析失败", ex);
            throw new IllegalStateException(ex);
        }
    }

    private PrRiskLevel riskLevel(String diffContent) {
        String diff = diffContent == null ? "" : diffContent.toLowerCase(Locale.ROOT);
        int changedLines = diff.split("\n").length;
        boolean criticalKeyword = diff.contains("drop table")
                || diff.contains("delete from")
                || diff.contains("password")
                || diff.contains("secret")
                || diff.contains("token")
                || diff.contains("权限")
                || diff.contains("鉴权");
        if (criticalKeyword || changedLines > 500) {
            return PrRiskLevel.HIGH;
        }
        if (changedLines > 120 || diff.contains("@transactional") || diff.contains("rabbit") || diff.contains("redis")) {
            return PrRiskLevel.MEDIUM;
        }
        return PrRiskLevel.LOW;
    }

    private String summary(String diffContent, PrRiskLevel riskLevel) {
        int added = 0;
        int removed = 0;
        String diff = diffContent == null ? "" : diffContent;
        for (String line : diff.split("\n")) {
            if (line.startsWith("+") && !line.startsWith("+++")) {
                added++;
            } else if (line.startsWith("-") && !line.startsWith("---")) {
                removed++;
            }
        }
        return "风险等级: " + riskLevel + "，新增 " + added + " 行，删除 " + removed + " 行。";
    }

    private String buildPrompt(PrAnalysis analysis, PrRiskLevel riskLevel, String summary) {
        return "标题: " + analysis.getTitle()
                + "\n源分支: " + analysis.getSourceBranch()
                + "\n目标分支: " + analysis.getTargetBranch()
                + "\n" + summary
                + "\n请输出: 1. 主要变更 2. 潜在风险 3. 建议测试点 4. 合并建议"
                + "\nDiff:\n" + limit(analysis.getDiffContent(), 8000)
                + "\n风险等级: " + riskLevel;
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
