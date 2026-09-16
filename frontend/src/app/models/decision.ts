export type DecisionEngineMode = 'native' | 'ai' | string;
export type DecisionOutcome = 'fire' | 'no' | 'error' | string;
export type AlertLevel = 'critical' | 'high' | 'medium' | 'low' | string;

export interface DecisionSwitchView {
  mode: DecisionEngineMode;
  openaiConfigured: boolean;
}

export interface DecisionResult {
  id: string;
  userId: string;
  email: string;
  eventId: string | null;
  sourceId: string;
  externalId: string;
  outcome: DecisionOutcome;
  level: AlertLevel | null;
  reason: string | null;
  engine: DecisionEngineMode;
  channels: string[];
  deliveryJobIds: string[];
  errorMessage: string | null;
}

export interface GoldenScore {
  engine?: string;
  n: number;
  tp: number;
  fp: number;
  fn: number;
  tn: number;
  precision: number | null;
  recall: number | null;
  f1: number | null;
  fireLevelChecked?: number;
  fireLevelOk?: number;
  byFamily?: GoldenSlice[];
  byUser?: GoldenSlice[];
  fires?: GoldenFireCase[];
  mismatches: GoldenMismatch[];
}

export interface GoldenSlice {
  name: string;
  n: number;
  tp: number;
  fp: number;
  fn: number;
  tn: number;
  f1: number | null;
}

export interface GoldenFireCase {
  caseId: string;
  user: string;
  family: string;
  level: string | null;
  channels: string[];
  reason: string | null;
}

export interface GoldenMismatch {
  caseId: string;
  user: string;
  golden: string;
  engine: string;
  type: string;
}
