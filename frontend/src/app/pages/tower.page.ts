import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { apiError } from '../core/auth.interceptor';
import { I18nService } from '../core/i18n.service';
import { ScrapeEventDetail, ScrapeEventView, ScrapeHealth, ScrapeSourceView } from '../core/models';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-tower',
  imports: [FormsModule],
  templateUrl: './tower.page.html',
  styleUrl: './tower.page.css'
})
export class TowerPage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  sources = signal<ScrapeSourceView[]>([]);
  events = signal<ScrapeEventView[]>([]);
  selected = signal<ScrapeEventDetail | null>(null);
  family = 'all';
  sourceId = 'all';
  busy = signal(false);

  constructor() {
    this.reload();
  }

  reload(): void {
    this.api.scrapeSources().subscribe({
      next: (sources) => this.sources.set(sources),
      error: (err) => this.toast.show(apiError(err))
    });
    this.api.scrapeEvents(this.family, this.sourceId).subscribe({
      next: (events) => this.events.set(events),
      error: (err) => this.toast.show(apiError(err))
    });
  }

  run(sourceId: string): void {
    this.busy.set(true);
    this.api.runScrape(sourceId).subscribe({
      next: () => {
        this.busy.set(false);
        this.reload();
      },
      error: (err) => {
        this.busy.set(false);
        this.toast.show(apiError(err));
      }
    });
  }

  open(event: ScrapeEventView): void {
    this.api.scrapeEvent(event.id).subscribe({
      next: (detail) => this.selected.set(detail),
      error: (err) => this.toast.show(apiError(err))
    });
  }

  export(): void {
    this.api.exportScrapeEvents(this.family, this.sourceId).subscribe({
      next: (payload) => {
        const blob = new Blob([JSON.stringify(payload, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'normalized-events.json';
        link.click();
        URL.revokeObjectURL(url);
      },
      error: (err) => this.toast.show(apiError(err))
    });
  }

  healthLabel(health: ScrapeHealth): string {
    switch (health) {
      case 'ok':
        return this.i18n.t().healthOk;
      case 'empty':
        return this.i18n.t().healthEmpty;
      case 'silent':
        return this.i18n.t().healthSilent;
      case 'error':
        return this.i18n.t().healthError;
      default:
        return this.i18n.t().healthIdle;
    }
  }

  meta(event: ScrapeEventView): string {
    const payload = event.payload ?? {};
    if (event.family === 'disaster') {
      return `${this.i18n.t().magnitude} ${payload['magnitude'] ?? '—'} · ${payload['place'] ?? ''}`;
    }
    if (event.family === 'market') {
      return `${payload['instrument'] ?? ''} · ${payload['movePercent'] ?? 0}%`;
    }
    const topics = Array.isArray(payload['topics']) ? payload['topics'].join(', ') : '';
    return topics;
  }
}
