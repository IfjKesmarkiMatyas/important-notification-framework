import { Component, computed, inject, input, output } from '@angular/core';
import { I18nService } from '../../core/i18n/i18n.service';
import { DecisionResult, ScrapeEventDetail } from '../../models';
import { eventMeta, formatWhen, outcomeLabel } from './tower-format';

@Component({
  selector: 'app-tower-story',
  templateUrl: './tower-story.html',
  styleUrl: './tower-story.css'
})
export class TowerStory {
  readonly i18n = inject(I18nService);
  readonly detail = input<ScrapeEventDetail | null>(null);
  readonly trail = input<DecisionResult[]>([]);
  readonly busy = input(false);
  readonly decide = output<void>();

  eventMeta = eventMeta;
  formatWhen = (iso: string) => formatWhen(iso, this.i18n.locale());
  outcomeLabel = outcomeLabel;

  readonly fired = computed(() => this.trail().filter((row) => row.outcome === 'fire').length);
  readonly skipped = computed(() => this.trail().filter((row) => row.outcome === 'no').length);
}
