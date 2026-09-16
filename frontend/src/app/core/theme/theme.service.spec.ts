import { ThemeService } from './theme.service';

describe('ThemeService', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.removeAttribute('data-theme');
    spyOn(window, 'matchMedia').and.returnValue({
      matches: false,
      media: '',
      onchange: null,
      addListener() {},
      removeListener() {},
      addEventListener() {},
      removeEventListener() {},
      dispatchEvent() {
        return false;
      }
    } as MediaQueryList);
  });

  it('defaults to dark when the OS is not light', () => {
    const theme = new ThemeService();
    expect(theme.theme()).toBe('dark');
    expect(document.documentElement.dataset['theme']).toBe('dark');
  });

  it('toggles to light and persists', () => {
    const theme = new ThemeService();
    theme.toggle();
    expect(theme.theme()).toBe('light');
    expect(localStorage.getItem('notif.theme')).toBe('light');
    expect(document.documentElement.dataset['theme']).toBe('light');
  });
});
