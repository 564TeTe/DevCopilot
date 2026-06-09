import { KeyRound, LogIn, UserPlus } from 'lucide-react';
import { FormEvent, useState } from 'react';
import { api, setAuthToken } from '../api';
import { saveStoredUser } from '../storage';
import type { AuthResult } from '../types';

interface AuthPanelProps {
  onAuthed: (user: AuthResult) => void;
}

export function AuthPanel({ onAuthed }: AuthPanelProps) {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [username, setUsername] = useState('demo');
  const [password, setPassword] = useState('demo123456');
  const [displayName, setDisplayName] = useState('Demo User');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  async function submit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError('');
    try {
      const user =
        mode === 'login'
          ? await api.login({ username, password })
          : await api.register({ username, password, displayName });
      setAuthToken(user.token);
      saveStoredUser(user);
      onAuthed(user);
    } catch (ex) {
      setError(ex instanceof Error ? ex.message : '认证失败');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-screen">
      <section className="auth-panel">
        <div className="brand-row">
          <span className="brand-mark">
            <KeyRound size={22} />
          </span>
          <div>
            <h1>DevCopilot</h1>
            <p>智能协作平台</p>
          </div>
        </div>

        <div className="segmented">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')} type="button">
            <LogIn size={16} />
            登录
          </button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')} type="button">
            <UserPlus size={16} />
            注册
          </button>
        </div>

        <form className="form-stack" onSubmit={submit}>
          <label>
            用户名
            <input value={username} onChange={(event) => setUsername(event.target.value)} required />
          </label>
          <label>
            密码
            <input
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              type="password"
              minLength={6}
              required
            />
          </label>
          {mode === 'register' && (
            <label>
              昵称
              <input value={displayName} onChange={(event) => setDisplayName(event.target.value)} />
            </label>
          )}
          {error && <div className="inline-error">{error}</div>}
          <button className="primary-button wide" type="submit" disabled={loading}>
            {loading ? '处理中' : mode === 'login' ? '登录' : '创建账号'}
          </button>
        </form>
      </section>
    </main>
  );
}
