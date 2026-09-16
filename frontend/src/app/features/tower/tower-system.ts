import { Component, inject, input, output } from '@angular/core';
import { I18nService } from '../../core/i18n/i18n.service';
import { DecisionSwitchView, GoldenScore, ScrapeSourceView } from '../../models';
import { familyLine, formatF1, formatWhen, healthLabel } from './tower-format';

@Component({
  selector: 'app-tower-system',
  templateUrl: './tower-system.html',
  styleUrl: './tower-system.css'
})
export class TowerSystem {
  readonly i18n = inject(I18nService);
  readonly sources = input.required<ScrapeSourceView[]>();
  readonly engine = input<DecisionSwitchView | null>(null);
  readonly golden = input<GoldenScore | null>(null);
  readonly busy = input(false);
  readonly mode = output<string>();
  readonly run = output<string>();
  readonly score = output<void>();

  protected readonly healthLabel = healthLabel;
  formatWhen = (iso: string | null) => formatWhen(iso, this.i18n.locale());
  formatF1 = formatF1;
  familyLine = familyLine;
}
