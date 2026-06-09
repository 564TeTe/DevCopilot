import { Bot, MessageSquarePlus, RefreshCw, SendHorizontal, UserRound } from 'lucide-react';
import { FormEvent, useEffect, useMemo, useState } from 'react';
import { api, streamAsk } from '../api';
import type { AiMessage, AiSession, ChatMode, KnowledgeBase } from '../types';

interface ChatPanelProps {
  projectId?: number;
  knowledgeBases: KnowledgeBase[];
  notify: (message: string) => void;
}

export function ChatPanel({ projectId, knowledgeBases, notify }: ChatPanelProps) {
  const [sessions, setSessions] = useState<AiSession[]>([]);
  const [selectedSessionId, setSelectedSessionId] = useState<number | undefined>();
  const [messages, setMessages] = useState<AiMessage[]>([]);
  const [title, setTitle] = useState('知识库问答');
  const [mode, setMode] = useState<ChatMode>('KNOWLEDGE');
  const [knowledgeBaseId, setKnowledgeBaseId] = useState<number | undefined>();
  const [content, setContent] = useState('DevCopilot 支持哪些能力？');
  const [creating, setCreating] = useState(false);
  const [streaming, setStreaming] = useState(false);

  const projectSessions = useMemo(
    () => sessions.filter((session) => !projectId || session.projectId === projectId),
    [sessions, projectId]
  );

  useEffect(() => {
    if (!knowledgeBaseId && knowledgeBases.length > 0) {
      setKnowledgeBaseId(knowledgeBases[0].id);
    }
  }, [knowledgeBases, knowledgeBaseId]);

  useEffect(() => {
    void loadSessions();
  }, []);

  useEffect(() => {
    if (!selectedSessionId && projectSessions.length > 0) {
      setSelectedSessionId(projectSessions[0].id);
    }
  }, [projectSessions, selectedSessionId]);

  useEffect(() => {
    if (selectedSessionId) {
      void loadHistory(selectedSessionId);
    } else {
      setMessages([]);
    }
  }, [selectedSessionId]);

  async function loadSessions() {
    try {
      setSessions(await api.listSessions());
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '会话加载失败');
    }
  }

  async function loadHistory(sessionId = selectedSessionId) {
    if (!sessionId) {
      return;
    }
    try {
      setMessages(await api.history(sessionId));
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '消息加载失败');
    }
  }

  async function createSession(event: FormEvent) {
    event.preventDefault();
    if (!projectId) {
      notify('请先选择项目');
      return;
    }
    setCreating(true);
    try {
      const session = await api.createSession({
        projectId,
        title,
        mode,
        knowledgeBaseId: mode === 'KNOWLEDGE' ? knowledgeBaseId : undefined
      });
      await loadSessions();
      setSelectedSessionId(session.id);
      notify('会话已创建');
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '创建失败');
    } finally {
      setCreating(false);
    }
  }

  async function send(event: FormEvent) {
    event.preventDefault();
    if (!selectedSessionId || !content.trim()) {
      notify('请选择会话并输入问题');
      return;
    }
    const question = content.trim();
    setContent('');
    const userMessage: AiMessage = {
      id: -Date.now(),
      sessionId: selectedSessionId,
      role: 'USER',
      content: question,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    const assistantMessage: AiMessage = {
      id: -Date.now() - 1,
      sessionId: selectedSessionId,
      role: 'ASSISTANT',
      content: '',
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    setMessages((items) => [...items, userMessage, assistantMessage]);
    setStreaming(true);
    try {
      await streamAsk(selectedSessionId, question, {
        onDelta: (value) => {
          setMessages((items) =>
            items.map((item) =>
              item.id === assistantMessage.id ? { ...item, content: item.content + value } : item
            )
          );
        },
        onError: (message) => notify(message)
      });
      await loadHistory(selectedSessionId);
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '发送失败');
    } finally {
      setStreaming(false);
    }
  }

  return (
    <section className="view-grid chat-grid">
      <div className="panel">
        <div className="panel-heading">
          <div>
            <h2>会话</h2>
            <span>{projectSessions.length} 个</span>
          </div>
          <button className="icon-button" onClick={loadSessions} type="button" title="刷新">
            <RefreshCw size={18} />
          </button>
        </div>
        <form className="form-stack compact" onSubmit={createSession}>
          <label>
            标题
            <input value={title} onChange={(event) => setTitle(event.target.value)} required />
          </label>
          <label>
            模式
            <select value={mode} onChange={(event) => setMode(event.target.value as ChatMode)}>
              <option value="GENERAL">通用</option>
              <option value="KNOWLEDGE">知识库</option>
              <option value="CODE">代码</option>
            </select>
          </label>
          {mode === 'KNOWLEDGE' && (
            <label>
              知识库
              <select
                value={knowledgeBaseId ?? ''}
                onChange={(event) => setKnowledgeBaseId(Number(event.target.value) || undefined)}
              >
                <option value="">未选择</option>
                {knowledgeBases.map((kb) => (
                  <option key={kb.id} value={kb.id}>
                    {kb.name}
                  </option>
                ))}
              </select>
            </label>
          )}
          <button className="primary-button" disabled={creating || !projectId} type="submit">
            <MessageSquarePlus size={16} />
            {creating ? '创建中' : '创建会话'}
          </button>
        </form>
        <div className="list-block">
          {projectSessions.map((session) => (
            <button
              className={`row-button ${session.id === selectedSessionId ? 'selected' : ''}`}
              key={session.id}
              onClick={() => setSelectedSessionId(session.id)}
              type="button"
            >
              <Bot size={18} />
              <span>
                <strong>{session.title}</strong>
                <small>{session.mode}</small>
              </span>
              <em>#{session.id}</em>
            </button>
          ))}
          {projectSessions.length === 0 && <div className="empty-state">暂无会话</div>}
        </div>
      </div>

      <div className="panel wide-panel chat-panel">
        <div className="panel-heading">
          <div>
            <h2>AI 问答</h2>
            <span>{selectedSessionId ? `会话 #${selectedSessionId}` : '未选择'}</span>
          </div>
          <button className="icon-button" onClick={() => void loadHistory()} type="button" title="刷新">
            <RefreshCw size={18} />
          </button>
        </div>
        <div className="message-list">
          {messages.map((message) => (
            <div className={`message ${message.role.toLowerCase()}`} key={message.id}>
              <span className="message-avatar">{message.role === 'USER' ? <UserRound size={16} /> : <Bot size={16} />}</span>
              <p>{message.content || '...'}</p>
            </div>
          ))}
          {messages.length === 0 && <div className="empty-state">暂无消息</div>}
        </div>
        <form className="chat-input" onSubmit={send}>
          <textarea value={content} onChange={(event) => setContent(event.target.value)} rows={3} />
          <button className="primary-button icon-text" disabled={streaming || !selectedSessionId} type="submit">
            <SendHorizontal size={17} />
            {streaming ? '生成中' : '发送'}
          </button>
        </form>
      </div>
    </section>
  );
}
