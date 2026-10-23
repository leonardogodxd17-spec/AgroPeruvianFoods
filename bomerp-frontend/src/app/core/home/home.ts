import { Component, inject, signal, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../services/api.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  private readonly api = inject(ApiService);
  private readonly http = inject(HttpClient);

  protected readonly apiOnline = signal(false);
  protected readonly checkingStatus = signal(true);

  ngOnInit(): void {
    // Probar conectividad con el backend
    this.http.get(this.api.buildUrl('/api/v1/categorias')).subscribe({
      next: () => {
        this.apiOnline.set(true);
        this.checkingStatus.set(false);
      },
      error: () => {
        // Puede fallar si aún no está levantado, pero no bloquea la interfaz
        this.apiOnline.set(false);
        this.checkingStatus.set(false);
      },
    });
  }
}
