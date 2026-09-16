import { I18nService } from './i18n.service';

describe('I18nService', () => {
  beforeEach(() => localStorage.clear());

  it('defaults to Hungarian and toggles to English', () => {
    const i18n = new I18nService();
    expect(i18n.locale()).toBe('hu');
    expect(i18n.t().login).toBe('Belépés');
    i18n.toggle();
    expect(i18n.locale()).toBe('en');
    expect(i18n.t().login).toBe('Sign in');
    expect(localStorage.getItem('notif.locale')).toBe('en');
  });
});
