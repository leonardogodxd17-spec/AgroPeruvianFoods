import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CategoriaService } from './categoria.service';
import { Categoria } from './categoria.model';
import { AuthService } from '../../../core/auth/auth-service';

@Component({
  selector: 'app-categoria-list',
  imports: [RouterLink],
  templateUrl: './categoria-list.html',
  styleUrl: './categoria-list.css',
})
export class CategoriaList implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly categoriaService = inject(CategoriaService);

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly error = signal<string | null>(null);
  protected readonly successMsg = signal<string | null>(null);
  protected readonly loading = signal(false);

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.loading.set(true);
    this.error.set(null);

    this.categoriaService.listar().subscribe({
      next: (data) => {
        this.categorias.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de categorías desde el backend.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  eliminar(categoria: Categoria): void {
    if (!confirm(`¿Eliminar la categoría "${categoria.nombre}"?`)) {
      return;
    }

    this.loading.set(true);
    this.categoriaService.eliminar(categoria.id).subscribe({
      next: () => {
        this.successMsg.set(`Categoría "${categoria.nombre}" eliminada correctamente.`);
        this.cargarDatos();
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'No se pudo eliminar la categoría.');
        this.loading.set(false);
      },
    });
  }
}
