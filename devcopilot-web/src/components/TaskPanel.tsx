import { Activity, RefreshCw } from 'lucide-react';
import { useEffect, useState } from 'react';
import { api } from '../api';
import type { AsyncTask, RecentTask } from '../types';

interface TaskPanelProps {
  recentTasks: RecentTask[];
  notify: (message: string) => void;
}

export function TaskPanel({ recentTasks, notify }: TaskPanelProps) {
  const [tasks, setTasks] = useState<Record<number, AsyncTask>>({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    void refreshAll();
  }, [recentTasks]);

  async function refreshAll() {
    if (recentTasks.length === 0) {
      setTasks({});
      return;
    }
    setLoading(true);
    try {
      const loaded = await Promise.all(recentTasks.map((task) => api.getTask(task.taskId).catch(() => null)));
      const next: Record<number, AsyncTask> = {};
      loaded.forEach((task) => {
        if (task) {
          next[task.id] = task;
        }
      });
      setTasks(next);
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '任务刷新失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="panel full-panel">
      <div className="panel-heading">
        <div>
          <h2>任务中心</h2>
          <span>{recentTasks.length} 个最近任务</span>
        </div>
        <button className="icon-button" onClick={refreshAll} type="button" title="刷新">
          <RefreshCw size={18} />
        </button>
      </div>
      <div className="task-list">
        {recentTasks.map((record) => {
          const task = tasks[record.taskId];
          return (
            <article className="task-item" key={`${record.type}-${record.taskId}`}>
              <div className="task-title">
                <Activity size={18} />
                <strong>{record.label}</strong>
                <span>{record.type}</span>
              </div>
              <div className="progress-bar">
                <i style={{ width: `${task?.progress ?? 0}%` }} />
              </div>
              <div className="task-meta">
                <span>{task?.status ?? 'PENDING'}</span>
                <span>{task?.message ?? '等待刷新'}</span>
                <span>{new Date(record.createdAt).toLocaleString()}</span>
              </div>
            </article>
          );
        })}
        {!loading && recentTasks.length === 0 && <div className="empty-state">暂无任务</div>}
        {loading && <div className="empty-state">刷新中</div>}
      </div>
    </section>
  );
}
