import { emptyKit, normalizeKit, normalizeRules } from './models';

describe('emptyKit', () => {
  it('defaults email on and other channels off', () => {
    const kit = emptyKit('ada@notif.local');
    expect(kit.version).toBe(1);
    expect(kit.preferences.email).toBe('ada@notif.local');
    expect(kit.preferences.channels).toEqual({ email: true, slack: false, pushover: false });
    expect(kit.preferences.slack.mode).toBe('chatbot');
    expect(kit.interests).toEqual([]);
  });
});

describe('normalizeRules', () => {
  it('keeps when/level rows and defaults locale', () => {
    const file = normalizeRules(
      { rules: [{ when: 'breaking', level: 'high' }, { extra: true }] },
      'hu'
    );
    expect(file.locale).toBe('hu');
    expect(file.rules[0]).toEqual({ when: 'breaking', level: 'high' });
    expect(file.rules[1].level).toBe('medium');
  });
});

describe('normalizeKit', () => {
  it('clones interests so the form can mutate a copy', () => {
    const source = emptyKit();
    source.interests.push({ type: 'market', instrument: 'eth', movePercent: 3 });
    const copy = normalizeKit(source);
    copy.interests[0].instrument = 'btc';
    expect(source.interests[0].instrument).toBe('eth');
  });
});
