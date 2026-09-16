import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-logo',
  template: `
    <span class="logo" [class.lg]="size === 'lg'">
      <svg viewBox="0 0 32 32" aria-hidden="true" focusable="false">
        <circle cx="16" cy="17" r="9.2" fill="none" stroke="currentColor" stroke-width="1.6" opacity="0.32" />
        <circle cx="16" cy="17" r="5.7" fill="none" stroke="currentColor" stroke-width="2" />
        <circle cx="16" cy="17" r="2.25" fill="currentColor" />
        <circle cx="23.4" cy="8.6" r="2.2" fill="currentColor" />
      </svg>
      @if (word) {
        <span class="word">Notif</span>
      }
    </span>
  `,
  styles: `
    .logo {
      display: inline-flex;
      align-items: center;
      gap: 10px;
      color: var(--ink-accent);
      line-height: 1;
    }
    svg {
      width: 28px;
      height: 28px;
      display: block;
      flex: none;
    }
    .word {
      font-weight: 600;
      font-size: 18px;
      letter-spacing: -0.03em;
      color: var(--ink-text);
    }
    .lg svg {
      width: 40px;
      height: 40px;
    }
    .lg .word {
      font-size: 28px;
    }
  `
})
export class Logo {
  @Input() size: 'sm' | 'lg' = 'sm';
  @Input() word = true;
}
