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
  n: number;
  tp: number;
  fp: number;
  fn: number;
  tn: number;
  precision: number | null;
  recall: number | null;
  f1: number | null;
  mismatches: GoldenMismatch[];
}

export interface GoldenMismatch {
  caseId: string;
  user: string;
  golden: string;
  engine: string;
  type: string;
}
