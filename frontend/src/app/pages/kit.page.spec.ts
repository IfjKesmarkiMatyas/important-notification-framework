import { normalizeKit } from './kit.page';

describe('normalizeKit', () => {
  it('fills missing channel flags and keeps interests', () => {
    const kit = normalizeKit({
      version: 2,
      preferences: {
        channels: { email: false } as never,
        email: 'ada@notif.local',
        slack: { mode: 'chatbot', userId: 'U1' },
        pushoverUserKey: 'key'
      },
      interests: [{ type: 'market', instrument: 'bitcoin', movePercent: 5 }]
    });

    expect(kit.version).toBe(2);
    expect(kit.preferences.channels).toEqual({ email: false, slack: false, pushover: false });
    expect(kit.preferences.slack.userId).toBe('U1');
    expect(kit.interests).toHaveSize(1);
  });

  it('returns a default kit for null input', () => {
    const kit = normalizeKit(null);
    expect(kit.preferences.channels.email).toBeTrue();
    expect(kit.interests).toEqual([]);
  });
});
