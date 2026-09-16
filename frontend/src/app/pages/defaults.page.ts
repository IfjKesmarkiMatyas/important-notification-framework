import { Component, inject, signal } from '@angular/core';
import { ApiService } from '../core/api.service';
import { apiError } from '../core/auth.interceptor';
import { I18nService } from '../core/i18n.service';
import { KitDocument, RuleFile, emptyRules, normalizeKit, normalizeRules, rulesToPayload } from '../core/models';
import { ToastService } from '../core/toast.service';
import { KitEditor } from './kit-editor';

@Component({
  selector: 'app-defaults',
  imports: [KitEditor],
  templateUrl: './defaults.page.html'
})
export class DefaultsPage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  kit = signal<KitDocument | null>(null);
  rulesHu = signal<RuleFile>(emptyRules('hu'));
  rulesEn = signal<RuleFile>(emptyRules('en'));

  constructor() {
    this.api.defaults().subscribe({
      next: (view) => {
        this.kit.set(normalizeKit(view.kit));
        this.rulesHu.set(normalizeRules(view.rulesHu, 'hu'));
        this.rulesEn.set(normalizeRules(view.rulesEn, 'en'));
      },
      error: (err) => this.toast.show(apiError(err))
    });
  }

  save(): void {
    const current = this.kit();
    if (!current) {
      return;
    }
    this.api.saveDefaults(current, rulesToPayload(this.rulesHu()), rulesToPayload(this.rulesEn())).subscribe({
      next: (view) => {
        this.kit.set(normalizeKit(view.kit));
        this.rulesHu.set(normalizeRules(view.rulesHu, 'hu'));
        this.rulesEn.set(normalizeRules(view.rulesEn, 'en'));
        this.toast.show(this.i18n.t().saved);
      },
      error: (err) => this.toast.show(apiError(err))
    });
  }
}
