import { Code2, Play, RefreshCw } from 'lucide-react';
import { FormEvent, useEffect, useState } from 'react';
import { api } from '../api';
import type { CodeRepository, RecentTask } from '../types';

interface CodePanelProps {
  projectId?: number;
  repositories: CodeRepository[];
  onRepositoriesChange: (repositories: CodeRepository[]) => void;
  onAddTask: (task: RecentTask) => void;
  notify: (message: string) => void;
}

export function CodePanel({ projectId, repositories, onRepositoriesChange, onAddTask, notify }: CodePanelProps) {
  const [name, setName] = useState('DevCopilot 后端');
  const [cloneUrl, setCloneUrl] = useState('C:\\Users\\st\\Documents\\DevCopilot 智能协作平台');
  const [defaultBranch, setDefaultBranch] = useState('main');
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    void load();
  }, [projectId]);

  async function load() {
    if (!projectId) {
      onRepositoriesChange([]);
      return;
    }
    setLoading(true);
    try {
      onRepositoriesChange(await api.listRepositories(projectId));
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '代码仓库加载失败');
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
      const result = await api.createRepository({ projectId, name, cloneUrl, defaultBranch });
      onAddTask({
        taskId: result.taskId,
        label: `代码索引: ${result.name}`,
        type: 'CODE_INDEX',
        createdAt: new Date().toISOString()
      });
      notify('代码索引任务已创建');
      window.setTimeout(() => void load(), 1500);
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '创建失败');
    } finally {
      setSaving(false);
    }
  }

  async function triggerIndex(repository: CodeRepository) {
    try {
      const task = await api.triggerRepositoryIndex(repository.id);
      onAddTask({
        taskId: task.id,
        label: `代码索引: ${repository.name}`,
        type: 'CODE_INDEX',
        createdAt: new Date().toISOString()
      });
      notify('索引任务已触发');
      window.setTimeout(() => void load(), 1500);
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '触发失败');
    }
  }

  return (
    <section className="view-grid code-grid">
      <div className="panel">
        <div className="panel-heading">
          <div>
            <h2>代码仓库</h2>
            <span>索引源</span>
          </div>
          <Code2 size={20} />
        </div>
        <form className="form-stack" onSubmit={submit}>
          <label>
            名称
            <input value={name} onChange={(event) => setName(event.target.value)} required />
          </label>
          <label>
            仓库地址
            <input value={cloneUrl} onChange={(event) => setCloneUrl(event.target.value)} required />
          </label>
          <label>
            默认分支
            <input value={defaultBranch} onChange={(event) => setDefaultBranch(event.target.value)} />
          </label>
          <button className="primary-button" disabled={saving || !projectId} type="submit">
            {saving ? '提交中' : '创建并索引'}
          </button>
        </form>
      </div>

      <div className="panel wide-panel">
        <div className="panel-heading">
          <div>
            <h2>索引记录</h2>
            <span>{repositories.length} 个</span>
          </div>
          <button className="icon-button" onClick={load} type="button" title="刷新">
            <RefreshCw size={18} />
          </button>
        </div>
        <div className="table-list">
          <div className="table-head">
            <span>名称</span>
            <span>状态</span>
            <span>文件</span>
            <span>操作</span>
          </div>
          {repositories.map((repository) => (
            <div className="table-row" key={repository.id}>
              <span className="clip">{repository.name}</span>
              <span className={`status status-${repository.status.toLowerCase()}`}>{repository.status}</span>
              <span>{repository.indexedFiles}</span>
              <button className="icon-button" onClick={() => void triggerIndex(repository)} type="button" title="重新索引">
                <Play size={16} />
              </button>
            </div>
          ))}
          {!loading && repositories.length === 0 && <div className="empty-state">暂无仓库</div>}
          {loading && <div className="empty-state">加载中</div>}
        </div>
      </div>
    </section>
  );
}
