import { Component, inject } from '@angular/core';
import { I18nService } from '../core/i18n.service';
import { ThemeService } from '../core/theme.service';

@Component({
  selector: 'app-theme-toggle',
  template: `
    <button
      class="btn ghost icon"
      type="button"
      (click)="theme.toggle()"
      [attr.aria-label]="label()"
      [title]="label()"
    >
      @if (theme.theme() === 'dark') {
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="12" cy="12" r="4" fill="none" stroke="currentColor" stroke-width="1.8" />
          <path
            d="M12 3v2.2M12 18.8V21M4.9 4.9l1.6 1.6M17.5 17.5l1.6 1.6M3 12h2.2M18.8 12H21M4.9 19.1l1.6-1.6M17.5 6.5l1.6-1.6"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linecap="round"
          />
        </svg>
      } @else {
        <svg viewBox="0 0 24 24" aria-hidden="true">
          <path
            d="M16.8 13.2A6.2 6.2 0 0 1 10.8 7 6.4 6.4 0 1 0 16.8 13.2Z"
            fill="none"
            stroke="currentColor"
            stroke-width="1.8"
            stroke-linejoin="round"
          />
        </svg>
      }
    </button>
  `,
  styles: `
    :host {
      display: inline-flex;
    }
    svg {
      width: 16px;
      height: 16px;
      display: block;
    }
  `
})
export class ThemeToggle {
  readonly theme = inject(ThemeService);
  private readonly i18n = inject(I18nService);

  label(): string {
    return this.theme.theme() === 'dark' ? this.i18n.t().themeToLight : this.i18n.t().themeToDark;
  }
}
