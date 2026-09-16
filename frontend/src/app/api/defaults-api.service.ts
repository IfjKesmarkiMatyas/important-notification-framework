import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DefaultsView, KitDocument } from '../models';

@Injectable({ providedIn: 'root' })
export class DefaultsApiService {
  private readonly http = inject(HttpClient);

  get(): Observable<DefaultsView> {
    return this.http.get<DefaultsView>('/api/admin/defaults');
  }

  save(
    kit: KitDocument,
    rulesHu: Record<string, unknown>,
    rulesEn: Record<string, unknown>
  ): Observable<DefaultsView> {
    return this.http.put<DefaultsView>('/api/admin/defaults', { kit, rulesHu, rulesEn });
  }
}
