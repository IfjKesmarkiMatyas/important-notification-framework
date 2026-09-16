import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DeliveryApiService } from '../../api/delivery-api.service';
import { apiError } from '../../core/auth/auth.interceptor';
import { I18nService } from '../../core/i18n/i18n.service';
import { ToastService } from '../../core/toast/toast.service';
import { Channel, DeliveryJobView } from '../../models';

@Component({
  selector: 'app-delivery',
  imports: [FormsModule],
  templateUrl: './delivery.page.html',
  styleUrl: './delivery.page.css'
})
export class DeliveryPage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(DeliveryApiService);
  private readonly toast = inject(ToastService);

  jobs = signal<DeliveryJobView[]>([]);
  channel: Channel = 'email';
  recipient = '';

  constructor() {
    this.reload();
  }

  reload(): void {
    this.api.jobs().subscribe({
      next: (jobs) => this.jobs.set(jobs),
      error: (err) => this.toast.show(apiError(err))
    });
  }

  test(): void {
    this.api.test(this.channel, this.recipient).subscribe({
      next: () => {
        this.toast.show(this.i18n.t().queued);
        setTimeout(() => this.reload(), 800);
      },
      error: (err) => this.toast.show(apiError(err))
    });
  }
}
