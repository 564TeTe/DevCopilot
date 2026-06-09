import { GitPullRequest, RefreshCw, ShieldAlert } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { api } from '../api';
import type { CodeRepository, PrAnalysis, RecentTask } from '../types';

interface PrPanelProps {
  projectId?: number;
  repositories: CodeRepository[];
  onAddTask: (task: RecentTask) => void;
  notify: (message: string) => void;
}

export function PrPanel({ projectId, repositories, onAddTask, notify }: PrPanelProps) {
  const [items, setItems] = useState<PrAnalysis[]>([]);
  const [title, setTitle] = useState('权限校验改造');
  const [repositoryId, setRepositoryId] = useState<number | undefined>();
  const [sourceBranch, setSourceBranch] = useState('feature/auth');
  const [targetBranch, setTargetBranch] = useState('main');
  const [diffContent, setDiffContent] = useState(defaultDiff);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    void load();
  }, [projectId]);

  async function load() {
    if (!projectId) {
      setItems([]);
      return;
    }
    setLoading(true);
    try {
      setItems(await api.listPrAnalyses(projectId));
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : 'PR 分析加载失败');
    } finally {
      setLoading(false);
    }
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!projectId) {
      notify('请先选择项目');
      return;
    }
    setSaving(true);
    try {
      const result = await api.createPrAnalysis({
        projectId,
        repositoryId,
        title,
        sourceBranch,
        targetBranch,
        diffContent
      });
      onAddTask({
        taskId: result.taskId,
        label: `PR 分析: ${result.title}`,
        type: 'PR_ANALYSIS',
        createdAt: new Date().toISOString()
      });
      notify('PR 分析任务已创建');
      window.setTimeout(() => void load(), 1500);
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '创建失败');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="view-grid pr-grid">
      <div className="panel">
        <div className="panel-heading">
          <div>
            <h2>创建分析</h2>
            <span>PR diff</span>
          </div>
          <GitPullRequest size={20} />
        </div>
        <form className="form-stack" onSubmit={submit}>
          <label>
            标题
            <input value={title} onChange={(event) => setTitle(event.target.value)} required />
          </label>
          <label>
            代码仓库
            <select value={repositoryId ?? ''} onChange={(event) => setRepositoryId(Number(event.target.value) || undefined)}>
              <option value="">未关联</option>
              {repositories.map((repository) => (
                <option key={repository.id} value={repository.id}>
                  {repository.name}
                </option>
              ))}
            </select>
          </label>
          <div className="two-fields">
            <label>
              源分支
              <input value={sourceBranch} onChange={(event) => setSourceBranch(event.target.value)} />
            </label>
            <label>
              目标分支
              <input value={targetBranch} onChange={(event) => setTargetBranch(event.target.value)} />
            </label>
          </div>
          <label>
            Diff
            <textarea value={diffContent} onChange={(event) => setDiffContent(event.target.value)} rows={10} required />
          </label>
          <button className="primary-button" disabled={saving || !projectId} type="submit">
            {saving ? '提交中' : '提交分析'}
          </button>
        </form>
      </div>

      <div className="panel wide-panel">
        <div className="panel-heading">
          <div>
            <h2>分析记录</h2>
            <span>{items.length} 条</span>
          </div>
          <button className="icon-button" onClick={load} type="button" title="刷新">
            <RefreshCw size={18} />
          </button>
        </div>
        <div className="analysis-list">
          {items.map((item) => (
            <article className="analysis-item" key={item.id}>
              <div className="analysis-title">
                <ShieldAlert size={18} />
                <strong>{item.title}</strong>
                <span className={`risk risk-${item.riskLevel.toLowerCase()}`}>{item.riskLevel}</span>
                <em>{item.status}</em>
              </div>
              <p>{item.summary || '等待分析'}</p>
              {item.report && <pre>{item.report}</pre>}
            </article>
          ))}
          {!loading && items.length === 0 && <div className="empty-state">暂无分析</div>}
          {loading && <div className="empty-state">加载中</div>}
        </div>
      </div>
    </section>
  );
}

const defaultDiff = `diff --git a/src/AuthService.java b/src/AuthService.java
+ String token = request.getHeader("Authorization");
+ if (token == null) {
+     throw new RuntimeException("missing token");
+ }
- return true;`;
