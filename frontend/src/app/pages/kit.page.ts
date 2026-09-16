import { Component, inject, signal } from '@angular/core';
import { ApiService } from '../core/api.service';
import { apiError } from '../core/auth.interceptor';
import { I18nService } from '../core/i18n.service';
import { KitDocument, RuleFile, emptyRules, normalizeKit, normalizeRules, rulesToPayload } from '../core/models';
import { ToastService } from '../core/toast.service';
import { KitEditor } from './kit-editor';

export { normalizeKit } from '../core/models';

@Component({
  selector: 'app-kit',
  imports: [KitEditor],
  templateUrl: './kit.page.html'
})
export class KitPage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  kit = signal<KitDocument | null>(null);
  rulesHu = signal<RuleFile>(emptyRules('hu'));
  rulesEn = signal<RuleFile>(emptyRules('en'));

  constructor() {
    this.api.myKit().subscribe({
      next: (view) => {
        this.rulesHu.set(normalizeRules(view.rulesHu, 'hu'));
        this.rulesEn.set(normalizeRules(view.rulesEn, 'en'));
        this.kit.set(normalizeKit(view.kit));
      },
      error: (err) => this.toast.show(apiError(err))
    });
  }

  save(): void {
    const current = this.kit();
    if (!current) {
      return;
    }
    this.api.saveMyKit(current, rulesToPayload(this.rulesHu()), rulesToPayload(this.rulesEn())).subscribe({
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
