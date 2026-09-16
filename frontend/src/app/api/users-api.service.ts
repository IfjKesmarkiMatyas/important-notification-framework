import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { InviteResponse, UserView } from '../models';

@Injectable({ providedIn: 'root' })
export class UsersApiService {
  private readonly http = inject(HttpClient);

  list(): Observable<UserView[]> {
    return this.http.get<UserView[]>('/api/admin/users');
  }

  invite(email: string, locale: string): Observable<InviteResponse> {
    return this.http.post<InviteResponse>('/api/admin/users/invite', { email, locale });
  }

  resend(id: string, locale: string): Observable<InviteResponse> {
    return this.http.post<InviteResponse>(`/api/admin/users/${id}/resend-invite`, { locale });
  }

  revoke(id: string): Observable<UserView> {
    return this.http.post<UserView>(`/api/admin/users/${id}/revoke`, {});
  }

  deactivate(id: string): Observable<UserView> {
    return this.http.post<UserView>(`/api/admin/users/${id}/deactivate`, {});
  }
}
