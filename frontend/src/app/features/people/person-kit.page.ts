import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { KitApiService } from '../../api/kit-api.service';
import { apiError } from '../../core/auth/auth.interceptor';
import { I18nService } from '../../core/i18n/i18n.service';
import { ToastService } from '../../core/toast/toast.service';
import { KitDocument, RuleFile, emptyRules, normalizeKit, normalizeRules, rulesToPayload } from '../../models';
import { KitEditor } from '../../shared/kit-editor/kit-editor';

@Component({
  selector: 'app-person-kit',
  imports: [KitEditor],
  templateUrl: './person-kit.page.html'
})
export class PersonKitPage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(KitApiService);
  private readonly toast = inject(ToastService);
  private readonly route = inject(ActivatedRoute);

  kit = signal<KitDocument | null>(null);
  rulesHu = signal<RuleFile>(emptyRules('hu'));
  rulesEn = signal<RuleFile>(emptyRules('en'));
  private userId = this.route.snapshot.paramMap.get('id') ?? '';

  constructor() {
    this.api.forUser(this.userId).subscribe({
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
    this.api
      .saveForUser(this.userId, current, rulesToPayload(this.rulesHu()), rulesToPayload(this.rulesEn()))
      .subscribe({
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
