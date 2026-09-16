import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DecisionResult, DecisionSwitchView, GoldenScore } from '../models';

@Injectable({ providedIn: 'root' })
export class DecisionApiService {
  private readonly http = inject(HttpClient);

  switchView(): Observable<DecisionSwitchView> {
    return this.http.get<DecisionSwitchView>('/api/admin/decision/switch');
  }

  setSwitch(mode: string): Observable<DecisionSwitchView> {
    return this.http.put<DecisionSwitchView>('/api/admin/decision/switch', { mode });
  }

  trail(eventId: string): Observable<DecisionResult[]> {
    return this.http.get<DecisionResult[]>(`/api/admin/decision/events/${eventId}`);
  }

  decide(eventId: string): Observable<DecisionResult[]> {
    return this.http.post<DecisionResult[]>(`/api/admin/decision/events/${eventId}/decide`, {});
  }

  score(): Observable<GoldenScore> {
    return this.http.post<GoldenScore>('/api/admin/decision/score', {});
  }
}
