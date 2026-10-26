import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TercerosService } from './terceros-service';
import { Tercero, TipoTercero } from './terceros.model';

@Component({
  selector: 'app-terceros-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './terceros-list.html',
  styleUrls: ['./terceros-list.css'],
})
export class TercerosList implements OnInit {
  private readonly tercerosService = inject(TercerosService);

  readonly terceros = signal<Tercero[]>([]);
  readonly cargando = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly filtroTipo = signal<string>('TODOS');
  readonly terminoBusqueda = signal<string>('');

  ngOnInit(): void {
    this.cargarTerceros();
  }

  cargarTerceros(): void {
    this.cargando.set(true);
    const tipo = this.filtroTipo() === 'TODOS' ? undefined : (this.filtroTipo() as TipoTercero);
    this.tercerosService.listar(tipo).subscribe({
      next: (data) => {
        this.terceros.set(data);
        this.cargando.set(false);
      },
      error: (err) => {
        this.error.set('Error al cargar directorio de terceros: ' + (err.error?.message || err.message));
        this.cargando.set(false);
      },
    });
  }

  cambiarFiltro(tipo: string): void {
    this.filtroTipo.set(tipo);
    this.cargarTerceros();
  }

  get tercerosFiltrados(): Tercero[] {
    const q = this.terminoBusqueda().toLowerCase().trim();
    if (!q) return this.terceros();
    return this.terceros().filter(
      (t) =>
        t.razonSocial.toLowerCase().includes(q) ||
        t.numeroDocumento.toLowerCase().includes(q) ||
        (t.direccion && t.direccion.toLowerCase().includes(q))
    );
  }
}
