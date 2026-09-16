export type Channel = 'email' | 'slack' | 'pushover';

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

export interface TestDeliveryResponse {
  jobId: string;
}
