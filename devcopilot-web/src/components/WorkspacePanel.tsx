import { FolderKanban, Plus, RefreshCw } from 'lucide-react';
import { FormEvent, useState } from 'react';
import type { DevProject, KnowledgeBase, RecentTask } from '../types';

interface WorkspacePanelProps {
  projects: DevProject[];
  selectedProjectId?: number;
  knowledgeBases: KnowledgeBase[];
  recentTasks: RecentTask[];
  onSelectProject: (projectId: number) => void;
  onCreateProject: (input: { name: string; description?: string }) => Promise<void>;
  onRefresh: () => Promise<void>;
}

export function WorkspacePanel({
  projects,
  selectedProjectId,
  knowledgeBases,
  recentTasks,
  onSelectProject,
  onCreateProject,
  onRefresh
}: WorkspacePanelProps) {
  const [name, setName] = useState('DevCopilot 演示项目');
  const [description, setDescription] = useState('研发知识库、代码索引、PR 分析与 AI 问答');
  const [saving, setSaving] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSaving(true);
    try {
      await onCreateProject({ name, description });
      setName('');
      setDescription('');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="view-grid workspace-grid">
      <div className="panel">
        <div className="panel-heading">
          <div>
            <h2>项目空间</h2>
            <span>{projects.length} 个项目</span>
          </div>
          <button className="icon-button" onClick={onRefresh} type="button" title="刷新">
            <RefreshCw size={18} />
          </button>
        </div>
        <div className="list-block">
          {projects.map((project) => (
            <button
              className={`row-button ${project.id === selectedProjectId ? 'selected' : ''}`}
              key={project.id}
              onClick={() => onSelectProject(project.id)}
              type="button"
            >
              <FolderKanban size={18} />
              <span>
                <strong>{project.name}</strong>
                <small>{project.description || '无描述'}</small>
              </span>
              <em>{project.status}</em>
            </button>
          ))}
          {projects.length === 0 && <div className="empty-state">暂无项目</div>}
        </div>
      </div>

      <div className="panel">
        <div className="panel-heading">
          <div>
            <h2>创建项目</h2>
            <span>研发空间</span>
          </div>
          <Plus size={20} />
        </div>
        <form className="form-stack" onSubmit={submit}>
          <label>
            项目名称
            <input value={name} onChange={(event) => setName(event.target.value)} required />
          </label>
          <label>
            描述
            <textarea value={description} onChange={(event) => setDescription(event.target.value)} rows={4} />
          </label>
          <button className="primary-button" disabled={saving} type="submit">
            {saving ? '创建中' : '创建项目'}
          </button>
        </form>
      </div>

      <div className="metric-strip">
        <div className="metric">
          <strong>{projects.length}</strong>
          <span>项目</span>
        </div>
        <div className="metric">
          <strong>{knowledgeBases.length}</strong>
          <span>知识库</span>
        </div>
        <div className="metric">
          <strong>{recentTasks.length}</strong>
          <span>最近任务</span>
        </div>
      </div>
    </section>
  );
}
