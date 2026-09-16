export type UserRole = 'USER' | 'ADMIN';
export type UserStatus = 'INVITED' | 'ACTIVE' | 'INACTIVE' | 'REVOKED';
export type Channel = 'email' | 'slack' | 'pushover';

export interface SessionUser {
  token: string;
  id: string;
  email: string;
  displayName: string | null;
  role: UserRole;
  status: UserStatus;
}

export interface UserView {
  id: string;
  email: string;
  displayName: string | null;
  role: UserRole;
  status: UserStatus;
  createdAt: string;
  lastInviteStatus: string | null;
  lastInviteError: string | null;
  lastInviteAt: string | null;
}

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

export interface DeliveryJobView {
  id: string;
  purpose: string;
  channel: string;
  recipient: string;
  status: string;
  errorMessage: string | null;
  attemptCount: number;
  createdAt: string;
  sentAt: string | null;
}

export interface DefaultsView {
  kit: KitDocument;
  rulesHu: Record<string, unknown>;
  rulesEn: Record<string, unknown>;
  updatedAt: string;
}

export type ScrapeHealth = 'idle' | 'ok' | 'empty' | 'silent' | 'error' | string;
export type EventFamily = 'breaking' | 'market' | 'disaster' | string;

export interface ScrapeSourceView {
  sourceId: string;
  family: EventFamily;
  locale: string;
  url: string;
  health: ScrapeHealth;
  lastStatus: string | null;
  lastOkAt: string | null;
  lastError: string | null;
  lastNormalized: number;
}

export interface ScrapeEventView {
  id: string;
  family: EventFamily;
  sourceId: string;
  externalId: string;
  occurredAt: string;
  ingestedAt: string;
  locale: string;
  headline: string;
  summary: string | null;
  canonicalUrl: string | null;
  payload: Record<string, unknown>;
}

export interface ScrapeEventDetail extends ScrapeEventView {
  rawIntakeId: string | null;
  rawExcerpt: string;
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
