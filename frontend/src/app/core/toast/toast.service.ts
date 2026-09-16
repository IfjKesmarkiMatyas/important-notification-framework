import { Injectable, inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class ToastService {
  private readonly snack = inject(MatSnackBar);

  show(message: string): void {
    this.snack.open(message, undefined, {
      duration: 3200,
      panelClass: 'notif-snack',
      horizontalPosition: 'center',
      verticalPosition: 'bottom'
    });
  }
}
