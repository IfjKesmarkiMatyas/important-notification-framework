import { Component, inject, signal } from '@angular/core';
import { DefaultsApiService } from '../../api/defaults-api.service';
import { apiError } from '../../core/auth/auth.interceptor';
import { I18nService } from '../../core/i18n/i18n.service';
import { ToastService } from '../../core/toast/toast.service';
import { KitDocument, RuleFile, emptyRules, normalizeKit, normalizeRules, rulesToPayload } from '../../models';
import { KitEditor } from '../../shared/kit-editor/kit-editor';

@Component({
  selector: 'app-defaults',
  imports: [KitEditor],
  templateUrl: './defaults.page.html'
})
export class DefaultsPage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(DefaultsApiService);
  private readonly toast = inject(ToastService);

  kit = signal<KitDocument | null>(null);
  rulesHu = signal<RuleFile>(emptyRules('hu'));
  rulesEn = signal<RuleFile>(emptyRules('en'));

  constructor() {
    this.api.get().subscribe({
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
    this.api.save(current, rulesToPayload(this.rulesHu()), rulesToPayload(this.rulesEn())).subscribe({
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
