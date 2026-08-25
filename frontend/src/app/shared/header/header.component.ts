import { Component, inject, Input } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/service/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {

  private authService = inject(AuthService);
  private router = inject(Router);

isAuthenticated(): boolean {
  return this.authService.isAuthenticated();
}

logout(): void {
  this.authService.logout();
  this.router.navigate(['/login']);
}

  @Input() actionLabel = 'Se connecter';
  @Input() actionRoute = '/login';
  @Input() showAddFiles = false;
}