import { Injectable, computed, signal } from '@angular/core';
import { Locale, STRINGS, Strings } from './i18n';

@Injectable({ providedIn: 'root' })
export class I18nService {
  readonly locale = signal<Locale>(localStorage.getItem('notif.locale') === 'en' ? 'en' : 'hu');
  readonly t = computed(() => STRINGS[this.locale()] as Strings);

  constructor() {
    document.documentElement.lang = this.locale();
  }

  toggle(): void {
    const next: Locale = this.locale() === 'hu' ? 'en' : 'hu';
    this.locale.set(next);
    localStorage.setItem('notif.locale', next);
    document.documentElement.lang = next;
  }
}
