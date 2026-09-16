import { Strings } from '../../core/i18n/strings';
import { GoldenScore, ScrapeEventView, ScrapeHealth } from '../../models';

export function healthLabel(health: ScrapeHealth, t: Strings): string {
  switch (health) {
    case 'ok':
      return t.healthOk;
    case 'empty':
      return t.healthEmpty;
    case 'silent':
      return t.healthSilent;
    case 'error':
      return t.healthError;
    default:
      return t.healthIdle;
  }
}

export function eventMeta(event: ScrapeEventView, t: Strings): string {
  const payload = event.payload ?? {};
  if (event.family === 'disaster') {
    return `${t.magnitude} ${payload['magnitude'] ?? '—'} · ${payload['place'] ?? ''}`.trim();
  }
  if (event.family === 'market') {
    return `${payload['instrument'] ?? ''} · ${payload['movePercent'] ?? 0}%`.trim();
  }
  const topics = Array.isArray(payload['topics']) ? payload['topics'].join(', ') : '';
  return topics;
}

export function formatWhen(iso: string | null | undefined, locale: string): string {
  if (!iso) {
    return '—';
  }
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return iso;
  }
  return date.toLocaleString(locale === 'en' ? 'en-GB' : 'hu-HU', {
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

export function formatF1(score: GoldenScore | null): string {
  if (!score || score.f1 == null) {
    return '—';
  }
  return score.f1.toFixed(2);
}

export function familyLine(score: GoldenScore): string {
  return (score.byFamily ?? [])
    .map((slice) => `${slice.name} ${slice.f1 == null ? '—' : slice.f1.toFixed(2)}`)
    .join(' · ');
}

export function outcomeLabel(outcome: string, t: Strings): string {
  if (outcome === 'fire') {
    return t.fire;
  }
  if (outcome === 'error') {
    return t.healthError;
  }
  return t.no;
}
