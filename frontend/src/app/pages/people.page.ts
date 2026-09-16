import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { apiError } from '../core/auth.interceptor';
import { I18nService } from '../core/i18n.service';
import { UserView } from '../core/models';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'app-people',
  imports: [FormsModule, RouterLink],
  templateUrl: './people.page.html',
  styleUrl: './people.page.css'
})
export class PeoplePage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);

  users = signal<UserView[]>([]);
  loaded = signal(false);
  email = '';
  locale = 'hu';

  constructor() {
    this.reload();
  }

  reload(): void {
    this.api.listUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loaded.set(true);
      },
      error: (err) => this.toast.show(apiError(err))
    });
  }

  invite(): void {
    this.api.invite(this.email, this.locale).subscribe({
      next: () => {
        this.email = '';
        this.toast.show(this.i18n.t().invited);
        this.reload();
      },
      error: (err) => this.toast.show(apiError(err))
    });
  }

  resend(user: UserView): void {
    this.api.resend(user.id, this.locale).subscribe({
      next: () => {
        this.toast.show(this.i18n.t().invited);
        this.reload();
      },
      error: (err) => this.toast.show(apiError(err))
    });
  }

  revoke(user: UserView): void {
    this.api.revoke(user.id).subscribe({
      next: () => this.reload(),
      error: (err) => this.toast.show(apiError(err))
    });
  }

  deactivate(user: UserView): void {
    this.api.deactivate(user.id).subscribe({
      next: () => this.reload(),
      error: (err) => this.toast.show(apiError(err))
    });
  }
}
