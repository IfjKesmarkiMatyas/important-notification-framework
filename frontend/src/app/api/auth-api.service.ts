import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { InvitePeek, SessionUser, UserView } from '../models';

@Injectable({ providedIn: 'root' })
export class AuthApiService {
  private readonly http = inject(HttpClient);

  login(email: string, password: string): Observable<SessionUser> {
    return this.http.post<SessionUser>('/api/auth/login', { email, password });
  }

  peekInvite(token: string): Observable<InvitePeek> {
    return this.http.get<InvitePeek>(`/api/auth/invite/${token}`);
  }

  acceptInvite(token: string, displayName: string, password: string): Observable<SessionUser> {
    return this.http.post<SessionUser>(`/api/auth/invite/${token}/accept`, { displayName, password });
  }

  me(): Observable<UserView> {
    return this.http.get<UserView>('/api/me');
  }
}
