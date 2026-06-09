import type { AuthResult, RecentTask } from './types';

const USER_KEY = 'devcopilot_user';
const TASK_KEY = 'devcopilot_recent_tasks';

export function readStoredUser(): AuthResult | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as AuthResult;
  } catch {
    return null;
  }
}

export function saveStoredUser(user: AuthResult) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function readRecentTasks(): RecentTask[] {
  const raw = localStorage.getItem(TASK_KEY);
  if (!raw) {
    return [];
  }
  try {
    return JSON.parse(raw) as RecentTask[];
  } catch {
    return [];
  }
}

export function saveRecentTasks(tasks: RecentTask[]) {
  localStorage.setItem(TASK_KEY, JSON.stringify(tasks));
}
