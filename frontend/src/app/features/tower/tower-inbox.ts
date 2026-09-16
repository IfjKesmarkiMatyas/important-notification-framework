import { Component, inject, input, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { I18nService } from '../../core/i18n/i18n.service';
import { ScrapeEventView, ScrapeSourceView } from '../../models';
import { eventMeta, formatWhen } from './tower-format';

@Component({
  selector: 'app-tower-inbox',
  imports: [FormsModule],
  templateUrl: './tower-inbox.html',
  styleUrl: './tower-inbox.css'
})
export class TowerInbox {
  readonly i18n = inject(I18nService);
  readonly events = input.required<ScrapeEventView[]>();
  readonly sources = input.required<ScrapeSourceView[]>();
  readonly selectedId = input<string | null>(null);
  readonly family = input('all');
  readonly sourceId = input('all');
  readonly familyChange = output<string>();
  readonly sourceIdChange = output<string>();
  readonly open = output<ScrapeEventView>();
  readonly download = output<void>();

  eventMeta = eventMeta;
  formatWhen = (iso: string) => formatWhen(iso, this.i18n.locale());
}
