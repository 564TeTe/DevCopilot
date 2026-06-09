export type ChatMode = 'GENERAL' | 'KNOWLEDGE' | 'CODE';
export type MessageRole = 'USER' | 'ASSISTANT' | 'SYSTEM';
export type TaskStatus = 'PENDING' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'CANCELED';

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface AuthResult {
  userId: number;
  username: string;
  displayName: string;
  token: string;
}

export interface DevProject {
  id: number;
  createdAt: string;
  updatedAt: string;
  ownerId: number;
  name: string;
  description?: string;
  status: string;
}

export interface KnowledgeBase {
  id: number;
  createdAt: string;
  updatedAt: string;
  projectId: number;
  name: string;
  description?: string;
  status: string;
}

export interface KnowledgeDocument {
  id: number;
  createdAt: string;
  updatedAt: string;
  knowledgeBaseId: number;
  fileName: string;
  contentType?: string;
  storagePath: string;
  status: string;
  totalChunks: number;
}

export interface DocumentUploadResult {
  documentId: number;
  taskId: number;
  fileName: string;
}

export interface AsyncTask {
  id: number;
  createdAt: string;
  updatedAt: string;
  taskType: string;
  businessType: string;
  businessId: number;
  status: TaskStatus;
  progress: number;
  message?: string;
  payload?: string;
  errorDetail?: string;
}

export interface AiSession {
  id: number;
  createdAt: string;
  updatedAt: string;
  projectId: number;
  userId: number;
  title: string;
  mode: ChatMode;
  knowledgeBaseId?: number;
}

export interface AiMessage {
  id: number;
  createdAt: string;
  updatedAt: string;
  sessionId: number;
  role: MessageRole;
  content: string;
}

export interface RepositoryCreateResult {
  repositoryId: number;
  taskId: number;
  name: string;
}

export interface CodeRepository {
  id: number;
  createdAt: string;
  updatedAt: string;
  projectId: number;
  name: string;
  cloneUrl: string;
  defaultBranch: string;
  status: string;
  indexedFiles: number;
}

export interface PrAnalysisCreateResult {
  analysisId: number;
  taskId: number;
  title: string;
}

export interface PrAnalysis {
  id: number;
  createdAt: string;
  updatedAt: string;
  projectId: number;
  repositoryId?: number;
  title: string;
  sourceBranch?: string;
  targetBranch?: string;
  diffContent: string;
  status: TaskStatus;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  summary?: string;
  report?: string;
}

export interface RecentTask {
  taskId: number;
  label: string;
  type: string;
  createdAt: string;
}
