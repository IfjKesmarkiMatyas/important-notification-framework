import { Component, inject } from '@angular/core';
import { I18nService } from '../core/i18n.service';

@Component({
  selector: 'app-tower',
  template: `
    <div class="kicker">{{ i18n.t().tower }}</div>
    <h1>{{ i18n.t().tower }}</h1>
    <p class="mute">{{ i18n.t().towerEmpty }}</p>
  `
})
export class TowerPage {
  readonly i18n = inject(I18nService);
}
