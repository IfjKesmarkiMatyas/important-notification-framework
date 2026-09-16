import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ScrapeEventDetail, ScrapeEventView, ScrapeSourceView } from '../models';

@Injectable({ providedIn: 'root' })
export class ScrapeApiService {
  private readonly http = inject(HttpClient);

  sources(): Observable<ScrapeSourceView[]> {
    return this.http.get<ScrapeSourceView[]>('/api/admin/scrape/sources');
  }

  run(sourceId: string): Observable<unknown> {
    return this.http.post('/api/admin/scrape/runs', { sourceId });
  }

  events(family?: string, sourceId?: string): Observable<ScrapeEventView[]> {
    return this.http.get<ScrapeEventView[]>('/api/admin/scrape/events', {
      params: scrapeParams(family, sourceId)
    });
  }

  event(id: string): Observable<ScrapeEventDetail> {
    return this.http.get<ScrapeEventDetail>('/api/admin/scrape/event-details', {
      params: { eventId: id }
    });
  }

  export(family?: string, sourceId?: string): Observable<Record<string, unknown>[]> {
    return this.http.get<Record<string, unknown>[]>('/api/admin/scrape/events/export', {
      params: scrapeParams(family, sourceId)
    });
  }
}

function scrapeParams(family?: string, sourceId?: string): Record<string, string> {
  const params: Record<string, string> = {};
  if (family && family !== 'all') {
    params['family'] = family;
  }
  if (sourceId && sourceId !== 'all') {
    params['sourceId'] = sourceId;
  }
  return params;
}
