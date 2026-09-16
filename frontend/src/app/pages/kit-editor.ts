import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { KitDocument, Rule, RuleFile, emptyRules } from '../core/models';
import { Strings } from '../core/i18n';

export type WizardStep = 1 | 2 | 3;
export type ChannelKey = 'email' | 'slack' | 'pushover';
export type InterestType = 'breaking' | 'market' | 'disaster';

@Component({
  selector: 'app-kit-editor',
  imports: [FormsModule],
  templateUrl: './kit-editor.html',
  styleUrl: './kit-editor.css'
})
export class KitEditor {
  @Input({ required: true }) kit!: KitDocument;
  @Input({ required: true }) t!: Strings;
  @Input() rulesHu: RuleFile = emptyRules('hu');
  @Input() rulesEn: RuleFile = emptyRules('en');
  @Output() save = new EventEmitter<void>();

  step: WizardStep = 1;
  topicDraft: Record<number, string> = {};
  readonly whenOptions = [
    'disaster.earthquake.minMagnitude',
    'market.movePercent',
    'breaking'
  ];
  readonly levels = ['critical', 'high', 'medium', 'low'] as const;

  toggleChannel(channel: ChannelKey): void {
    this.kit.preferences.channels[channel] = !this.kit.preferences.channels[channel];
  }

  addInterest(type: InterestType = 'breaking'): void {
    if (type === 'disaster') {
      this.kit.interests.push({ type: 'disaster', kind: 'earthquake', minMagnitude: 6 });
    } else if (type === 'market') {
      this.kit.interests.push({ type: 'market', instrument: 'bitcoin', movePercent: 5 });
    } else {
      this.kit.interests.push({ type: 'breaking', topics: ['belfold'] });
    }
  }

  removeInterest(index: number): void {
    this.kit.interests.splice(index, 1);
    delete this.topicDraft[index];
  }

  topicsText(index: number): string {
    return (this.kit.interests[index]?.topics ?? []).join(', ');
  }

  setTopics(index: number, value: string): void {
    this.kit.interests[index].topics = value
      .split(',')
      .map((part) => part.trim())
      .filter(Boolean);
  }

  addTopic(index: number): void {
    const value = (this.topicDraft[index] ?? '').trim();
    if (!value) {
      return;
    }
    const current = this.kit.interests[index];
    const topics = current.topics ?? [];
    if (!topics.includes(value)) {
      current.topics = [...topics, value];
    }
    this.topicDraft[index] = '';
  }

  removeTopic(index: number, topic: string): void {
    const current = this.kit.interests[index];
    current.topics = (current.topics ?? []).filter((item) => item !== topic);
  }

  addRule(file: RuleFile): void {
    file.rules.push({ when: 'breaking', level: 'medium' });
  }

  removeRule(file: RuleFile, index: number): void {
    file.rules.splice(index, 1);
  }

  go(step: WizardStep): void {
    this.step = step;
  }

  next(): void {
    if (this.step < 3) {
      this.step = (this.step + 1) as WizardStep;
    }
  }

  back(): void {
    if (this.step > 1) {
      this.step = (this.step - 1) as WizardStep;
    }
  }

  whenLabel(when: string): string {
    switch (when) {
      case 'disaster.earthquake.minMagnitude':
        return this.t.whenEarthquake;
      case 'market.movePercent':
        return this.t.whenMarket;
      case 'breaking':
        return this.t.whenBreaking;
      default:
        return when;
    }
  }

  levelLabel(level: Rule['level']): string {
    switch (level) {
      case 'critical':
        return this.t.levelCritical;
      case 'high':
        return this.t.levelHigh;
      case 'medium':
        return this.t.levelMedium;
      case 'low':
        return this.t.levelLow;
      default:
        return String(level);
    }
  }
}
