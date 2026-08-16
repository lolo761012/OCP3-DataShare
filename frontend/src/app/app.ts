import { Component, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-root',
  imports: [],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  backendStatus = signal('vérification en cours...');

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.http.get<{ status: string }>('/actuator/health').subscribe({
      next: (res) => this.backendStatus.set(res.status),
      error: () => this.backendStatus.set('backend injoignable')
    });
  }
}