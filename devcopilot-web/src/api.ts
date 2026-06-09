import type {
  AiMessage,
  AiSession,
  ApiResponse,
  AsyncTask,
  AuthResult,
  ChatMode,
  CodeRepository,
  DevProject,
  DocumentUploadResult,
  KnowledgeBase,
  KnowledgeDocument,
  PrAnalysis,
  PrAnalysisCreateResult,
  RepositoryCreateResult
} from './types';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

let token = localStorage.getItem('devcopilot_token') ?? '';

export function setAuthToken(nextToken: string) {
  token = nextToken;
  localStorage.setItem('devcopilot_token', nextToken);
}

export function clearAuthToken() {
  token = '';
  localStorage.removeItem('devcopilot_token');
  localStorage.removeItem('devcopilot_user');
}

export function currentToken() {
  return token;
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (!(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  const text = await response.text();
  const payload = text ? (JSON.parse(text) as ApiResponse<T>) : null;
  if (!response.ok || !payload || payload.code !== 0) {
    throw new Error(payload?.message || response.statusText || '请求失败');
  }
  return payload.data;
}

export const api = {
  register(input: { username: string; password: string; displayName?: string }) {
    return request<AuthResult>('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(input)
    });
  },

  login(input: { username: string; password: string }) {
    return request<AuthResult>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(input)
    });
  },

  createProject(input: { name: string; description?: string }) {
    return request<DevProject>('/api/projects', {
      method: 'POST',
      body: JSON.stringify(input)
    });
  },

  listProjects() {
    return request<DevProject[]>('/api/projects');
  },

  createKnowledgeBase(input: { projectId: number; name: string; description?: string }) {
    return request<KnowledgeBase>('/api/knowledge-bases', {
      method: 'POST',
      body: JSON.stringify(input)
    });
  },

  listKnowledgeBases(projectId: number) {
    return request<KnowledgeBase[]>(`/api/knowledge-bases?projectId=${projectId}`);
  },

  listDocuments(knowledgeBaseId: number) {
    return request<KnowledgeDocument[]>(`/api/documents?knowledgeBaseId=${knowledgeBaseId}`);
  },

  uploadDocument(knowledgeBaseId: number, file: File) {
    const form = new FormData();
    form.append('knowledgeBaseId', String(knowledgeBaseId));
    form.append('file', file);
    return request<DocumentUploadResult>('/api/documents/upload', {
      method: 'POST',
      body: form
    });
  },

  getTask(taskId: number) {
    return request<AsyncTask>(`/api/tasks/${taskId}`);
  },

  createSession(input: { projectId: number; title?: string; mode: ChatMode; knowledgeBaseId?: number }) {
    return request<AiSession>('/api/chat/sessions', {
      method: 'POST',
      body: JSON.stringify(input)
    });
  },

  listSessions() {
    return request<AiSession[]>('/api/chat/sessions');
  },

  history(sessionId: number) {
    return request<AiMessage[]>(`/api/chat/sessions/${sessionId}/messages`);
  },

  ask(sessionId: number, content: string) {
    return request<AiMessage>(`/api/chat/sessions/${sessionId}/messages`, {
      method: 'POST',
      body: JSON.stringify({ content })
    });
  },

  createRepository(input: { projectId: number; name: string; cloneUrl: string; defaultBranch?: string }) {
    return request<RepositoryCreateResult>('/api/code-repositories', {
      method: 'POST',
      body: JSON.stringify(input)
    });
  },

  listRepositories(projectId: number) {
    return request<CodeRepository[]>(`/api/code-repositories?projectId=${projectId}`);
  },

  triggerRepositoryIndex(repositoryId: number) {
    return request<AsyncTask>(`/api/code-repositories/${repositoryId}/index`, {
      method: 'POST'
    });
  },

  createPrAnalysis(input: {
    projectId: number;
    repositoryId?: number;
    title: string;
    sourceBranch?: string;
    targetBranch?: string;
    diffContent: string;
  }) {
    return request<PrAnalysisCreateResult>('/api/pr-analysis', {
      method: 'POST',
      body: JSON.stringify(input)
    });
  },

  listPrAnalyses(projectId: number) {
    return request<PrAnalysis[]>(`/api/pr-analysis?projectId=${projectId}`);
  }
};

export async function streamAsk(
  sessionId: number,
  content: string,
  handlers: { onDelta: (value: string) => void; onDone?: (messageId: string) => void; onError?: (message: string) => void }
) {
  const headers = new Headers({ 'Content-Type': 'application/json' });
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  const response = await fetch(`${API_BASE}/api/chat/sessions/${sessionId}/messages/stream`, {
    method: 'POST',
    headers,
    body: JSON.stringify({ content })
  });
  if (!response.ok || !response.body) {
    throw new Error(response.statusText || '流式请求失败');
  }

  const decoder = new TextDecoder();
  const reader = response.body.getReader();
  let buffer = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    const events = buffer.split('\n\n');
    buffer = events.pop() ?? '';
    for (const event of events) {
      const parsed = parseSseEvent(event);
      if (parsed.name === 'delta') {
        handlers.onDelta(parsed.data);
      } else if (parsed.name === 'done') {
        handlers.onDone?.(parsed.data);
      } else if (parsed.name === 'error') {
        handlers.onError?.(parsed.data);
      }
    }
  }
}

function parseSseEvent(raw: string) {
  let name = 'message';
  const data: string[] = [];
  for (const line of raw.split('\n')) {
    if (line.startsWith('event:')) {
      name = line.slice('event:'.length).trim();
    } else if (line.startsWith('data:')) {
      data.push(line.slice('data:'.length).trimStart());
    }
  }
  return { name, data: data.join('\n') };
}
