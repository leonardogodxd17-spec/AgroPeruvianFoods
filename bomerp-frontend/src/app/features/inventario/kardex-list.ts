import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InventarioService } from './inventario-service';
import { MovimientoKardex } from './inventario.model';

@Component({
  selector: 'app-kardex-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePipe, DecimalPipe],
  templateUrl: './kardex-list.html',
  styleUrls: ['./kardex-list.css'],
})
export class KardexList implements OnInit {
  private readonly inventarioService = inject(InventarioService);

  protected readonly movimientos = signal<MovimientoKardex[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly filtroTipo = signal<string>('');
  protected readonly filtroTexto = signal<string>('');

  protected readonly movimientosFiltrados = computed(() => {
    let list = this.movimientos();
    const tipo = this.filtroTipo();
    const txt = this.filtroTexto().toLowerCase().trim();

    if (tipo) {
      list = list.filter((m) => m.tipo === tipo);
    }
    if (txt) {
      list = list.filter(
        (m) =>
          m.productoNombre.toLowerCase().includes(txt) ||
          (m.codigoLote && m.codigoLote.toLowerCase().includes(txt)) ||
          (m.documentoReferencia && m.documentoReferencia.toLowerCase().includes(txt)) ||
          m.motivo.toLowerCase().includes(txt)
      );
    }
    return list;
  });

  ngOnInit(): void {
    this.cargarKardex();
  }

  cargarKardex(): void {
    this.loading.set(true);
    this.error.set(null);
    this.inventarioService.listarKardex().subscribe({
      next: (data) => {
        this.movimientos.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el historial de Kardex de almacén.');
        this.loading.set(false);
      },
    });
  }
}
