import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { KitDocument, KitView } from '../models';

@Injectable({ providedIn: 'root' })
export class KitApiService {
  private readonly http = inject(HttpClient);

  mine(): Observable<KitView> {
    return this.http.get<KitView>('/api/me/kit');
  }

  saveMine(
    kit: KitDocument,
    rulesHu: Record<string, unknown>,
    rulesEn: Record<string, unknown>
  ): Observable<KitView> {
    return this.http.put<KitView>('/api/me/kit', { kit, rulesHu, rulesEn });
  }

  forUser(id: string): Observable<KitView> {
    return this.http.get<KitView>(`/api/admin/users/${id}/kit`);
  }

  saveForUser(
    id: string,
    kit: KitDocument,
    rulesHu: Record<string, unknown>,
    rulesEn: Record<string, unknown>
  ): Observable<KitView> {
    return this.http.put<KitView>(`/api/admin/users/${id}/kit`, { kit, rulesHu, rulesEn });
  }
}
