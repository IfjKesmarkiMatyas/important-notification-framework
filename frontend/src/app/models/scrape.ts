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
