import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Logo } from '../brand/logo';
import { ThemeToggle } from '../brand/theme-toggle';
import { AuthService } from '../core/auth.service';
import { I18nService } from '../core/i18n.service';

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
