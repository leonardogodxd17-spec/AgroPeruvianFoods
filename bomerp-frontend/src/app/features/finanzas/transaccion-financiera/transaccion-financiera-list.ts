import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CategoriaFinancieraService } from '../categoria-financiera/categoria-financiera-service';
import { CategoriaFinanciera } from '../categoria-financiera/categoria-financiera.model';
import { TransaccionFinancieraService } from './transaccion-financiera-service';
import { TransaccionFinanciera } from './transaccion-financiera.model';

@Component({
  selector: 'app-transaccion-financiera-list',
  imports: [RouterLink, CurrencyPipe, DatePipe],
  templateUrl: './transaccion-financiera-list.html',
})
export class TransaccionFinancieraList implements OnInit {
  private readonly transaccionService = inject(TransaccionFinancieraService);
  private readonly categoriaService = inject(CategoriaFinancieraService);

  protected readonly transacciones = signal<TransaccionFinanciera[]>([]);
  protected readonly categorias = signal<CategoriaFinanciera[]>([]);
  protected readonly categoriaFiltro = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly loading = signal(false);

  ngOnInit(): void {
    this.categoriaService.listar().subscribe({
      next: (data) => this.categorias.set(data),
      error: () => this.error.set('No se pudo cargar la lista de categorías financieras.'),
    });
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.error.set(null);
    this.transaccionService.listar(this.categoriaFiltro() ?? undefined).subscribe({
      next: (data) => this.transacciones.set(data),
      error: () => {
        this.error.set('No se pudo cargar la lista de transacciones financieras.');
        this.loading.set(false);
      },
      complete: () => this.loading.set(false),
    });
  }

  filtrar(categoriaId: number): void {
    this.categoriaFiltro.set(categoriaId || null);
    this.cargar();
  }

  eliminar(id: number): void {
    if (!confirm(`¿Está seguro de eliminar la transacción financiera ${id}?`)) {
      return;
    }

    this.transaccionService.eliminar(id).subscribe({
      next: () => this.cargar(),
      error: (err: HttpErrorResponse) => {
        if (err.status === 404) {
          this.error.set('La transacción ya no existe. Se recargó la lista.');
          this.cargar();
        } else {
          this.error.set('No se pudo eliminar la transacción.');
        }
      },
    });
  }
}
