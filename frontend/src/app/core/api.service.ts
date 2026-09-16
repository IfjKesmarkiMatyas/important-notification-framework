import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Channel,
  DefaultsView,
  DeliveryJobView,
  KitDocument,
  KitView,
  SessionUser,
  ScrapeEventDetail,
  ScrapeEventView,
  ScrapeSourceView,
  UserView
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  login(email: string, password: string): Observable<SessionUser> {
    return this.http.post<SessionUser>('/api/auth/login', { email, password });
  }

  peekInvite(token: string): Observable<{ email: string }> {
    return this.http.get<{ email: string }>(`/api/auth/invite/${token}`);
  }

  acceptInvite(token: string, displayName: string, password: string): Observable<SessionUser> {
    return this.http.post<SessionUser>(`/api/auth/invite/${token}/accept`, { displayName, password });
  }

  me(): Observable<UserView> {
    return this.http.get<UserView>('/api/me');
  }

  myKit(): Observable<KitView> {
    return this.http.get<KitView>('/api/me/kit');
  }

  saveMyKit(kit: KitDocument, rulesHu: Record<string, unknown>, rulesEn: Record<string, unknown>): Observable<KitView> {
    return this.http.put<KitView>('/api/me/kit', { kit, rulesHu, rulesEn });
  }

  listUsers(): Observable<UserView[]> {
    return this.http.get<UserView[]>('/api/admin/users');
  }

  invite(email: string, locale: string): Observable<{ userId: string; deliveryJobId: string }> {
    return this.http.post<{ userId: string; deliveryJobId: string }>('/api/admin/users/invite', {
      email,
      locale
    });
  }

  resend(id: string, locale: string): Observable<{ userId: string; deliveryJobId: string }> {
    return this.http.post<{ userId: string; deliveryJobId: string }>(
      `/api/admin/users/${id}/resend-invite`,
      { locale }
    );
  }

  revoke(id: string): Observable<UserView> {
    return this.http.post<UserView>(`/api/admin/users/${id}/revoke`, {});
  }

  deactivate(id: string): Observable<UserView> {
    return this.http.post<UserView>(`/api/admin/users/${id}/deactivate`, {});
  }

  userKit(id: string): Observable<KitView> {
    return this.http.get<KitView>(`/api/admin/users/${id}/kit`);
  }

  saveUserKit(
    id: string,
    kit: KitDocument,
    rulesHu: Record<string, unknown>,
    rulesEn: Record<string, unknown>
  ): Observable<KitView> {
    return this.http.put<KitView>(`/api/admin/users/${id}/kit`, { kit, rulesHu, rulesEn });
  }

  jobs(): Observable<DeliveryJobView[]> {
    return this.http.get<DeliveryJobView[]>('/api/admin/delivery/jobs');
  }

  testDelivery(channel: Channel, recipient: string): Observable<{ jobId: string }> {
    return this.http.post<{ jobId: string }>('/api/admin/delivery/test', { channel, recipient });
  }

  defaults(): Observable<DefaultsView> {
    return this.http.get<DefaultsView>('/api/admin/defaults');
  }

  saveDefaults(
    kit: KitDocument,
    rulesHu: Record<string, unknown>,
    rulesEn: Record<string, unknown>
  ): Observable<DefaultsView> {
    return this.http.put<DefaultsView>('/api/admin/defaults', { kit, rulesHu, rulesEn });
  }

  scrapeSources(): Observable<ScrapeSourceView[]> {
    return this.http.get<ScrapeSourceView[]>('/api/admin/scrape/sources');
  }

  runScrape(sourceId: string): Observable<unknown> {
    return this.http.post('/api/admin/scrape/runs', { sourceId });
  }

  scrapeEvents(family?: string, sourceId?: string): Observable<ScrapeEventView[]> {
    const params: Record<string, string> = {};
    if (family && family !== 'all') {
      params['family'] = family;
    }
    if (sourceId && sourceId !== 'all') {
      params['sourceId'] = sourceId;
    }
    return this.http.get<ScrapeEventView[]>('/api/admin/scrape/events', { params });
  }

  scrapeEvent(id: string): Observable<ScrapeEventDetail> {
    return this.http.get<ScrapeEventDetail>(`/api/admin/scrape/events/${id}`);
  }

  exportScrapeEvents(family?: string, sourceId?: string): Observable<Record<string, unknown>[]> {
    const params: Record<string, string> = {};
    if (family && family !== 'all') {
      params['family'] = family;
    }
    if (sourceId && sourceId !== 'all') {
      params['sourceId'] = sourceId;
    }
    return this.http.get<Record<string, unknown>[]>('/api/admin/scrape/events/export', { params });
  }
}
