import { Injectable, computed, signal } from '@angular/core';
import { SessionUser } from '../../models';

const KEY = 'notif.session';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly session = signal<SessionUser | null>(readSession());

  readonly user = computed(() => this.session());
  readonly token = computed(() => this.session()?.token ?? null);
  readonly isLoggedIn = computed(() => !!this.session()?.token);
  readonly isAdmin = computed(() => this.session()?.role === 'ADMIN');

  setSession(user: SessionUser): void {
    this.session.set(user);
    localStorage.setItem(KEY, JSON.stringify(user));
  }

  clear(): void {
    this.session.set(null);
    localStorage.removeItem(KEY);
  }
}

function readSession(): SessionUser | null {
  try {
    const raw = localStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as SessionUser) : null;
  } catch {
    return null;
  }
}
