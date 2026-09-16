export interface KitView {
  userId: string;
  kit: KitDocument;
  rulesHu: Record<string, unknown>;
  rulesEn: Record<string, unknown>;
}

export interface KitDocument {
  version: number;
  preferences: {
    channels: { email: boolean; slack: boolean; pushover: boolean };
    email: string;
    slack: { mode: string; userId: string };
    pushoverUserKey: string;
  };
  interests: Interest[];
}

export interface Interest {
  type: 'disaster' | 'market' | 'breaking' | string;
  kind?: string;
  minMagnitude?: number;
  instrument?: string;
  movePercent?: number;
  topics?: string[];
}

export type RuleLevel = 'critical' | 'high' | 'medium' | 'low' | string;

export interface Rule {
  when: string;
  level: RuleLevel;
}

export interface RuleFile {
  locale: string;
  rules: Rule[];
}

export interface DefaultsView {
  kit: KitDocument;
  rulesHu: Record<string, unknown>;
  rulesEn: Record<string, unknown>;
  updatedAt: string;
}

export function emptyKit(email = ''): KitDocument {
  return {
    version: 1,
    preferences: {
      channels: { email: true, slack: false, pushover: false },
      email,
      slack: { mode: 'chatbot', userId: '' },
      pushoverUserKey: ''
    },
    interests: []
  };
}

export function emptyRules(locale: string): RuleFile {
  return { locale, rules: [] };
}

export function normalizeKit(raw: KitDocument | null | undefined): KitDocument {
  const base = emptyKit();
  if (!raw) {
    return base;
  }
  return {
    version: raw.version ?? 1,
    preferences: {
      channels: {
        email: raw.preferences?.channels?.email ?? true,
        slack: raw.preferences?.channels?.slack ?? false,
        pushover: raw.preferences?.channels?.pushover ?? false
      },
      email: raw.preferences?.email ?? '',
      slack: {
        mode: raw.preferences?.slack?.mode ?? 'chatbot',
        userId: raw.preferences?.slack?.userId ?? ''
      },
      pushoverUserKey: raw.preferences?.pushoverUserKey ?? ''
    },
    interests: Array.isArray(raw.interests) ? raw.interests.map((item) => ({ ...item })) : []
  };
}

export function normalizeRules(raw: Record<string, unknown> | null | undefined, locale: string): RuleFile {
  const rulesRaw = raw?.['rules'];
  const rules: Rule[] = Array.isArray(rulesRaw)
    ? rulesRaw
        .filter((item): item is Record<string, unknown> => !!item && typeof item === 'object')
        .map((item) => ({
          when: String(item['when'] ?? ''),
          level: String(item['level'] ?? 'medium')
        }))
    : [];
  return {
    locale: String(raw?.['locale'] ?? locale),
    rules
  };
}

export function rulesToPayload(file: RuleFile): Record<string, unknown> {
  return {
    locale: file.locale,
    rules: file.rules.map((rule) => ({ when: rule.when, level: rule.level }))
  };
}
