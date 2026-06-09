import { DatabaseZap, FileUp, RefreshCw } from 'lucide-react';
import { FormEvent, useEffect, useMemo, useState } from 'react';
import { api } from '../api';
import type { KnowledgeBase, KnowledgeDocument, RecentTask } from '../types';

interface KnowledgePanelProps {
  projectId?: number;
  knowledgeBases: KnowledgeBase[];
  onRefreshKnowledgeBases: () => Promise<void>;
  onAddTask: (task: RecentTask) => void;
  notify: (message: string) => void;
}

export function KnowledgePanel({
  projectId,
  knowledgeBases,
  onRefreshKnowledgeBases,
  onAddTask,
  notify
}: KnowledgePanelProps) {
  const [name, setName] = useState('研发知识库');
  const [description, setDescription] = useState('项目文档、接口说明、故障排查记录');
  const [selectedKbId, setSelectedKbId] = useState<number | undefined>();
  const [documents, setDocuments] = useState<KnowledgeDocument[]>([]);
  const [file, setFile] = useState<File | null>(null);
  const [loadingDocs, setLoadingDocs] = useState(false);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);

  const selectedKb = useMemo(
    () => knowledgeBases.find((item) => item.id === selectedKbId),
    [knowledgeBases, selectedKbId]
  );

  useEffect(() => {
    if (!selectedKbId && knowledgeBases.length > 0) {
      setSelectedKbId(knowledgeBases[0].id);
    }
    if (selectedKbId && !knowledgeBases.some((item) => item.id === selectedKbId)) {
      setSelectedKbId(knowledgeBases[0]?.id);
    }
  }, [knowledgeBases, selectedKbId]);

  useEffect(() => {
    if (selectedKbId) {
      void loadDocuments(selectedKbId);
    } else {
      setDocuments([]);
    }
  }, [selectedKbId]);

  async function createKnowledgeBase(event: FormEvent) {
    event.preventDefault();
    if (!projectId) {
      notify('请先创建或选择项目');
      return;
    }
    setSaving(true);
    try {
      await api.createKnowledgeBase({ projectId, name, description });
      await onRefreshKnowledgeBases();
      notify('知识库已创建');
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '创建失败');
    } finally {
      setSaving(false);
    }
  }

  async function loadDocuments(knowledgeBaseId = selectedKbId) {
    if (!knowledgeBaseId) {
      return;
    }
    setLoadingDocs(true);
    try {
      setDocuments(await api.listDocuments(knowledgeBaseId));
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '文档加载失败');
    } finally {
      setLoadingDocs(false);
    }
  }

  async function uploadDocument(event: FormEvent) {
    event.preventDefault();
    if (!selectedKbId || !file) {
      notify('请选择知识库和文件');
      return;
    }
    setUploading(true);
    try {
      const result = await api.uploadDocument(selectedKbId, file);
      onAddTask({
        taskId: result.taskId,
        label: `文档解析: ${result.fileName}`,
        type: 'DOCUMENT_PARSE',
        createdAt: new Date().toISOString()
      });
      setFile(null);
      notify('文档已上传');
      await loadDocuments(selectedKbId);
      window.setTimeout(() => void loadDocuments(selectedKbId), 3000);
    } catch (ex) {
      notify(ex instanceof Error ? ex.message : '上传失败');
    } finally {
      setUploading(false);
    }
  }

  return (
    <section className="view-grid knowledge-grid">
      <div className="panel">
        <div className="panel-heading">
          <div>
            <h2>知识库</h2>
            <span>{knowledgeBases.length} 个</span>
          </div>
          <button className="icon-button" onClick={onRefreshKnowledgeBases} type="button" title="刷新">
            <RefreshCw size={18} />
          </button>
        </div>
        <form className="form-stack compact" onSubmit={createKnowledgeBase}>
          <label>
            名称
            <input value={name} onChange={(event) => setName(event.target.value)} required />
          </label>
          <label>
            描述
            <textarea value={description} onChange={(event) => setDescription(event.target.value)} rows={3} />
          </label>
          <button className="primary-button" disabled={saving || !projectId} type="submit">
            {saving ? '创建中' : '创建知识库'}
          </button>
        </form>
        <div className="list-block">
          {knowledgeBases.map((kb) => (
            <button
              className={`row-button ${kb.id === selectedKbId ? 'selected' : ''}`}
              key={kb.id}
              onClick={() => setSelectedKbId(kb.id)}
              type="button"
            >
              <DatabaseZap size={18} />
              <span>
                <strong>{kb.name}</strong>
                <small>{kb.description || '无描述'}</small>
              </span>
              <em>{kb.status}</em>
            </button>
          ))}
          {knowledgeBases.length === 0 && <div className="empty-state">暂无知识库</div>}
        </div>
      </div>

      <div className="panel wide-panel">
        <div className="panel-heading">
          <div>
            <h2>文档</h2>
            <span>{selectedKb?.name || '未选择'}</span>
          </div>
          <button className="icon-button" onClick={() => void loadDocuments()} type="button" title="刷新">
            <RefreshCw size={18} />
          </button>
        </div>
        <form className="upload-row" onSubmit={uploadDocument}>
          <label className="file-input">
            <FileUp size={18} />
            <input
              onChange={(event) => setFile(event.target.files?.[0] ?? null)}
              type="file"
              accept=".txt,.md,.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.json,.xml,.yml,.yaml"
            />
            <span>{file?.name || '选择文件'}</span>
          </label>
          <button className="primary-button" disabled={uploading || !selectedKbId || !file} type="submit">
            {uploading ? '上传中' : '上传'}
          </button>
        </form>
        <div className="table-list">
          <div className="table-head">
            <span>文件</span>
            <span>状态</span>
            <span>切片</span>
            <span>时间</span>
          </div>
          {documents.map((document) => (
            <div className="table-row" key={document.id}>
              <span className="clip">{document.fileName}</span>
              <span className={`status status-${document.status.toLowerCase()}`}>{document.status}</span>
              <span>{document.totalChunks}</span>
              <span>{formatTime(document.updatedAt)}</span>
            </div>
          ))}
          {!loadingDocs && documents.length === 0 && <div className="empty-state">暂无文档</div>}
          {loadingDocs && <div className="empty-state">加载中</div>}
        </div>
      </div>
    </section>
  );
}

function formatTime(value: string) {
  return new Date(value).toLocaleString();
}
