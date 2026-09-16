import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthApiService } from '../../api/auth-api.service';
import { apiError } from '../../core/auth/auth.interceptor';
import { AuthService } from '../../core/auth/auth.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ToastService } from '../../core/toast/toast.service';
import { Logo } from '../../shared/brand/logo';
import { ThemeToggle } from '../../shared/brand/theme-toggle';

@Component({
  selector: 'app-invite',
  imports: [FormsModule, Logo, ThemeToggle],
  templateUrl: './invite.page.html',
  styleUrl: './invite.page.css'
})
export class InvitePage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(AuthApiService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  email = signal<string | null>(null);
  invalid = signal(false);
  displayName = '';
  password = '';
  busy = signal(false);
  private token = '';

  constructor() {
    this.token = this.route.snapshot.paramMap.get('token') ?? '';
    this.api.peekInvite(this.token).subscribe({
      next: (peek) => this.email.set(peek.email),
      error: () => this.invalid.set(true)
    });
  }

  submit(): void {
    this.busy.set(true);
    this.api.acceptInvite(this.token, this.displayName, this.password).subscribe({
      next: (session) => {
        this.auth.setSession(session);
        void this.router.navigateByUrl('/app/kit');
      },
      error: (err) => {
        this.busy.set(false);
        this.toast.show(apiError(err));
      }
    });
  }
}
