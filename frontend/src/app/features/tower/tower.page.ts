import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DecisionApiService } from '../../api/decision-api.service';
import { ScrapeApiService } from '../../api/scrape-api.service';
import { apiError } from '../../core/auth/auth.interceptor';
import { I18nService } from '../../core/i18n/i18n.service';
import { ToastService } from '../../core/toast/toast.service';
import {
  DecisionResult,
  DecisionSwitchView,
  GoldenScore,
  ScrapeEventDetail,
  ScrapeEventView,
  ScrapeHealth,
  ScrapeSourceView
} from '../../models';

@Component({
  selector: 'app-tower',
  imports: [FormsModule],
  templateUrl: './tower.page.html',
  styleUrl: './tower.page.css'
})
export class TowerPage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(ScrapeApiService);
  private readonly decisions = inject(DecisionApiService);
  private readonly toast = inject(ToastService);

  sources = signal<ScrapeSourceView[]>([]);
  events = signal<ScrapeEventView[]>([]);
  selected = signal<ScrapeEventDetail | null>(null);
  trail = signal<DecisionResult[]>([]);
  engineSwitch = signal<DecisionSwitchView | null>(null);
  golden = signal<GoldenScore | null>(null);
  family = 'all';
  sourceId = 'all';
  busy = signal(false);

  constructor() {
    this.reload();
  }

  reload(): void {
    this.api.sources().subscribe({
      next: (sources) => this.sources.set(sources),
      error: (err) => this.toast.show(apiError(err))
    });
    this.api.events(this.family, this.sourceId).subscribe({
      next: (events) => this.events.set(events),
      error: (err) => this.toast.show(apiError(err))
    });
    this.decisions.switchView().subscribe({
      next: (view) => this.engineSwitch.set(view),
      error: (err) => this.toast.show(apiError(err))
    });
  }

  setMode(mode: string): void {
    this.busy.set(true);
    this.decisions.setSwitch(mode).subscribe({
      next: (view) => {
        this.busy.set(false);
        this.engineSwitch.set(view);
      },
      error: (err) => {
        this.busy.set(false);
        this.toast.show(apiError(err));
      }
    });
  }

  run(sourceId: string): void {
    this.busy.set(true);
    this.api.run(sourceId).subscribe({
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
    this.selected.set({ ...event, rawIntakeId: null, rawExcerpt: '' });
    this.loadTrail(event.id);
    this.api.event(event.id).subscribe({
      next: (detail) => this.selected.set(detail),
      error: () => {
        /* list payload already shown */
      }
    });
  }

  decide(): void {
    const detail = this.selected();
    if (!detail) {
      return;
    }
    this.busy.set(true);
    this.decisions.decide(detail.id).subscribe({
      next: (rows) => {
        this.busy.set(false);
        this.trail.set(rows);
      },
      error: (err) => {
        this.busy.set(false);
        this.toast.show(apiError(err));
      }
    });
  }

  score(): void {
    this.busy.set(true);
    this.decisions.score().subscribe({
      next: (score) => {
        this.busy.set(false);
        this.golden.set(score);
      },
      error: (err) => {
        this.busy.set(false);
        this.toast.show(apiError(err));
      }
    });
  }

  export(): void {
    this.api.export(this.family, this.sourceId).subscribe({
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

  formatF1(score: GoldenScore | null): string {
    if (!score || score.f1 == null) {
      return '—';
    }
    return score.f1.toFixed(2);
  }

  familyLine(score: GoldenScore): string {
    return (score.byFamily ?? [])
      .map((slice) => `${slice.name} F₁ ${slice.f1 == null ? '—' : slice.f1.toFixed(2)}`)
      .join(' · ');
  }

  private loadTrail(eventId: string): void {
    this.decisions.trail(eventId).subscribe({
      next: (rows) => this.trail.set(rows),
      error: (err) => this.toast.show(apiError(err))
    });
  }
}
