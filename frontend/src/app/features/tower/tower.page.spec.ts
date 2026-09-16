import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ScrapeEventView } from '../../models';
import { TowerPage } from './tower.page';

describe('TowerPage', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MatSnackBar, useValue: { open: () => undefined } }
      ]
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function page(): TowerPage {
    const instance = TestBed.runInInjectionContext(() => new TowerPage());
    http.expectOne('/api/admin/scrape/sources').flush([]);
    http.expectOne('/api/admin/scrape/events').flush([]);
    http.expectOne('/api/admin/decision/switch').flush({ mode: 'native', openaiConfigured: false });
    return instance;
  }

  it('formats disaster and market cards from payload facts', () => {
    const component = page();
    const quake: ScrapeEventView = {
      id: '1',
      family: 'disaster',
      sourceId: 'usgs',
      externalId: 'us1',
      occurredAt: '2026-09-16T12:00:00Z',
      ingestedAt: '2026-09-16T12:01:00Z',
      locale: 'en',
      headline: '6.4 — Tokyo',
      summary: 'Tokyo',
      canonicalUrl: 'https://earthquake.usgs.gov/',
      payload: { magnitude: 6.4, place: 'Tokyo' }
    };
    const market: ScrapeEventView = {
      ...quake,
      id: '2',
      family: 'market',
      sourceId: 'coingecko',
      payload: { instrument: 'bitcoin', movePercent: 8 }
    };
    expect(component.meta(quake)).toContain('6.4');
    expect(component.meta(market)).toContain('bitcoin');
    expect(component.healthLabel('silent')).toBe('Hallgat');
  });
});
