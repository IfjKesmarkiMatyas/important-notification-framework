import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { I18nService } from '../core/i18n/i18n.service';
import { Logo } from '../shared/brand/logo';
import { ThemeToggle } from '../shared/brand/theme-toggle';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, Logo, ThemeToggle],
  templateUrl: './shell.html',
  styleUrl: './shell.css'
})
export class Shell {
  readonly auth = inject(AuthService);
  readonly i18n = inject(I18nService);
  private readonly router = inject(Router);

  logout(): void {
    this.auth.clear();
    void this.router.navigateByUrl('/login');
  }
}
