import {
  Bot,
  Boxes,
  DatabaseZap,
  FolderKanban,
  GitPullRequest,
  Layers3,
  LogOut,
  Radar,
  RefreshCw
} from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { api, clearAuthToken, currentToken } from './api';
import { AuthPanel } from './components/AuthPanel';
import { ChatPanel } from './components/ChatPanel';
import { CodePanel } from './components/CodePanel';
import { KnowledgePanel } from './components/KnowledgePanel';
import { PrPanel } from './components/PrPanel';
import { TaskPanel } from './components/TaskPanel';
import { WorkspacePanel } from './components/WorkspacePanel';
import { readRecentTasks, readStoredUser, saveRecentTasks } from './storage';
import type { AuthResult, CodeRepository, DevProject, KnowledgeBase, RecentTask } from './types';

type ViewKey = 'workspace' | 'knowledge' | 'chat' | 'pr' | 'code' | 'tasks';

const navItems: Array<{ key: ViewKey; label: string; icon: typeof FolderKanban }> = [
  { key: 'workspace', label: '工作台', icon: FolderKanban },
  { key: 'knowledge', label: '知识库', icon: DatabaseZap },
  { key: 'chat', label: 'AI 会话', icon: Bot },
  { key: 'pr', label: 'PR 分析', icon: GitPullRequest },
  { key: 'code', label: '代码索引', icon: Boxes },
  { key: 'tasks', label: '任务中心', icon: Radar }
];

export function App() {
  const [user, setUser] = useState<AuthResult | null>(() => (currentToken() ? readStoredUser() : null));
  const [view, setView] = useState<ViewKey>('workspace');
  const [projects, setProjects] = useState<DevProject[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<number | undefined>(() => {
    const raw = localStorage.getItem('devcopilot_selected_project');
    return raw ? Number(raw) : undefined;
  });
  const [knowledgeBases, setKnowledgeBases] = useState<KnowledgeBase[]>([]);
  const [repositories, setRepositories] = useState<CodeRepository[]>([]);
  const [recentTasks, setRecentTasks] = useState<RecentTask[]>(readRecentTasks);
  const [notice, setNotice] = useState('');
  const [loadingProjects, setLoadingProjects] = useState(false);

  const selectedProject = useMemo(
    () => projects.find((project) => project.id === selectedProjectId),
    [projects, selectedProjectId]
  );

  useEffect(() => {
    if (user) {
      void loadProjects();
    }
  }, [user]);

  useEffect(() => {
    if (selectedProjectId) {
      localStorage.setItem('devcopilot_selected_project', String(selectedProjectId));
      void loadKnowledgeBases(selectedProjectId);
      void loadRepositories(selectedProjectId);
    } else {
      setKnowledgeBases([]);
      setRepositories([]);
    }
  }, [selectedProjectId]);

  function notify(message: string) {
    setNotice(message);
    window.clearTimeout(Number(window.localStorage.getItem('devcopilot_notice_timer') || 0));
    const timer = window.setTimeout(() => setNotice(''), 2600);
    window.localStorage.setItem('devcopilot_notice_timer', String(timer));
  }

  async function loadProjects() {
    setLoadingProjects(true);
    try {
      const items = await api.listProjects();
      setProjects(items);
      const stillExists = selectedProjectId && items.some((project) => project.id === selectedProjectId);
      if (!stillExists) {
        setSelectedProjectId(items[0]?.id);
      }
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '项目加载失败');
    } finally {
      setLoadingProjects(false);
    }
  }

  async function createProject(input: { name: string; description?: string }) {
    try {
      const project = await api.createProject(input);
      setSelectedProjectId(project.id);
      await loadProjects();
      notify('项目已创建');
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '创建失败');
    }
  }

  async function loadKnowledgeBases(projectId = selectedProjectId) {
    if (!projectId) {
      setKnowledgeBases([]);
      return;
    }
    try {
      setKnowledgeBases(await api.listKnowledgeBases(projectId));
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '知识库加载失败');
    }
  }

  async function loadRepositories(projectId = selectedProjectId) {
    if (!projectId) {
      setRepositories([]);
      return;
    }
    try {
      setRepositories(await api.listRepositories(projectId));
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '代码仓库加载失败');
    }
  }

  function addTask(task: RecentTask) {
    setRecentTasks((items) => {
      const next = [task, ...items.filter((item) => item.taskId !== task.taskId)].slice(0, 24);
      saveRecentTasks(next);
      return next;
    });
  }

  function logout() {
    clearAuthToken();
    setUser(null);
    setProjects([]);
    setKnowledgeBases([]);
    setRepositories([]);
    setSelectedProjectId(undefined);
  }

  if (!user) {
    return <AuthPanel onAuthed={setUser} />;
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-row sidebar-brand">
          <span className="brand-mark">
            <Layers3 size={22} />
          </span>
          <div>
            <h1>DevCopilot</h1>
            <p>智能协作平台</p>
          </div>
        </div>
        <nav>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <button className={view === item.key ? 'active' : ''} key={item.key} onClick={() => setView(item.key)}>
                <Icon size={18} />
                {item.label}
              </button>
            );
          })}
        </nav>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div>
            <h2>{navItems.find((item) => item.key === view)?.label}</h2>
            <span>{selectedProject?.name || '未选择项目'}</span>
          </div>
          <div className="top-actions">
            <select
              value={selectedProjectId ?? ''}
              onChange={(event) => setSelectedProjectId(Number(event.target.value) || undefined)}
            >
              <option value="">选择项目</option>
              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.name}
                </option>
              ))}
            </select>
            <button className="icon-button" onClick={() => void loadProjects()} type="button" title="刷新项目">
              <RefreshCw size={18} />
            </button>
            <span className="user-pill">{user.displayName || user.username}</span>
            <button className="icon-button" onClick={logout} type="button" title="退出">
              <LogOut size={18} />
            </button>
          </div>
        </header>

        {notice && <div className="toast">{notice}</div>}
        {loadingProjects && <div className="loading-line" />}

        <div className="content-area">
          {view === 'workspace' && (
            <WorkspacePanel
              projects={projects}
              selectedProjectId={selectedProjectId}
              knowledgeBases={knowledgeBases}
              recentTasks={recentTasks}
              onSelectProject={setSelectedProjectId}
              onCreateProject={createProject}
              onRefresh={loadProjects}
            />
          )}
          {view === 'knowledge' && (
            <KnowledgePanel
              projectId={selectedProjectId}
              knowledgeBases={knowledgeBases}
              onRefreshKnowledgeBases={() => loadKnowledgeBases()}
              onAddTask={addTask}
              notify={notify}
            />
          )}
          {view === 'chat' && <ChatPanel projectId={selectedProjectId} knowledgeBases={knowledgeBases} notify={notify} />}
          {view === 'pr' && (
            <PrPanel projectId={selectedProjectId} repositories={repositories} onAddTask={addTask} notify={notify} />
          )}
          {view === 'code' && (
            <CodePanel
              projectId={selectedProjectId}
              repositories={repositories}
              onRepositoriesChange={setRepositories}
              onAddTask={addTask}
              notify={notify}
            />
          )}
          {view === 'tasks' && <TaskPanel recentTasks={recentTasks} notify={notify} />}
        </div>
      </main>
    </div>
  );
}
