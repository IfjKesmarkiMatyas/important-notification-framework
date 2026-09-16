import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthApiService } from '../../api/auth-api.service';
import { apiError } from '../../core/auth/auth.interceptor';
import { AuthService } from '../../core/auth/auth.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ToastService } from '../../core/toast/toast.service';
import { Logo } from '../../shared/brand/logo';
import { ThemeToggle } from '../../shared/brand/theme-toggle';

@Component({
  selector: 'app-login',
  imports: [FormsModule, Logo, ThemeToggle],
  templateUrl: './login.page.html',
  styleUrl: './login.page.css'
})
export class LoginPage {
  readonly i18n = inject(I18nService);
  private readonly api = inject(AuthApiService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  email = '';
  password = '';
  busy = signal(false);

  submit(): void {
    this.busy.set(true);
    this.api.login(this.email, this.password).subscribe({
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
