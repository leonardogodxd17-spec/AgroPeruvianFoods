import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InventarioService } from './inventario-service';
import {
  DescuentoFefoRequest,
  DescuentoFefoResponse,
  EstadoLote,
  Lote,
  LoteCreateDto,
} from './inventario.model';

@Component({
  selector: 'app-lote-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, DatePipe, DecimalPipe],
  templateUrl: './lote-list.html',
  styleUrls: ['./lote-list.css'],
})
export class LoteList implements OnInit {
  private readonly inventarioService = inject(InventarioService);

  // Estados
  protected readonly lotes = signal<Lote[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly exito = signal<string | null>(null);

  // Filtros
  protected readonly filtroTexto = signal('');
  protected readonly filtroEstado = signal<string>('');

  // Simulador / Ejecutor de Despacho FEFO
  protected readonly mostrarModalFefo = signal(false);
  protected readonly simularProductoId = signal<number>(4); // Aceite de Coco 500ml por defecto
  protected readonly simularCantidad = signal<number>(15);
  protected readonly fefoResultado = signal<DescuentoFefoResponse | null>(null);
  protected readonly loadingFefo = signal(false);

  // Modal Nuevo Lote
  protected readonly mostrarModalNuevoLote = signal(false);
  protected readonly nuevoCodigo = signal('');
  protected readonly nuevoProductoId = signal<number>(4);
  protected readonly nuevaFechaFab = signal(new Date().toISOString().slice(0, 10));
  protected readonly nuevaFechaVenc = signal(
    new Date(Date.now() + 180 * 24 * 3600 * 1000).toISOString().slice(0, 10)
  );
  protected readonly nuevoStock = signal<number>(20);
  protected readonly nuevoCosto = signal<number>(32.5);
  protected readonly nuevasObservaciones = signal('');
  protected readonly loadingCrearLote = signal(false);

  // Métricas computadas
  protected readonly lotesFiltrados = computed(() => {
    let list = this.lotes();
    const txt = this.filtroTexto().toLowerCase().trim();
    const est = this.filtroEstado();

    if (txt) {
      list = list.filter(
        (l) =>
          l.codigoLote.toLowerCase().includes(txt) ||
          l.productoNombre.toLowerCase().includes(txt)
      );
    }
    if (est) {
      list = list.filter((l) => l.estado === est);
    }
    return list;
  });

  protected readonly totalLotesVigentes = computed(
    () => this.lotes().filter((l) => l.estado === 'DISPONIBLE').length
  );

  protected readonly totalProximosVencer = computed(
    () => this.lotes().filter((l) => l.estado === 'PROXIMO_A_VENCER').length
  );

  protected readonly totalVencidos = computed(
    () => this.lotes().filter((l) => l.estado === 'VENCIDO').length
  );

  ngOnInit(): void {
    this.cargarLotes();
  }

  cargarLotes(): void {
    this.loading.set(true);
    this.error.set(null);
    this.inventarioService.listarLotes().subscribe({
      next: (data) => {
        this.lotes.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudieron cargar los lotes de inventario.');
        this.loading.set(false);
      },
    });
  }

  abrirModalFefo(): void {
    this.fefoResultado.set(null);
    this.mostrarModalFefo.set(true);
  }

  cerrarModalFefo(): void {
    this.mostrarModalFefo.set(false);
    this.fefoResultado.set(null);
  }

  ejecutarDespachoFefo(): void {
    const prodId = Number(this.simularProductoId());
    const cant = Number(this.simularCantidad());
    if (!prodId || cant <= 0) return;

    this.loadingFefo.set(true);
    this.error.set(null);

    const req: DescuentoFefoRequest = {
      productoId: prodId,
      cantidad: cant,
      motivo: 'DEMOSTRACION_REGLA_FEFO',
      documentoReferencia: 'SIM-FEFO-' + Date.now().toString().slice(-4),
    };

    this.inventarioService.despachoFefo(req).subscribe({
      next: (res) => {
        this.fefoResultado.set(res);
        this.loadingFefo.set(false);
        this.cargarLotes(); // Recargar estados actualizados de lotes
      },
      error: (err) => {
        this.error.set(
          err?.error?.message || 'Error al ejecutar despacho FEFO: stock insuficiente o lote vencido.'
        );
        this.loadingFefo.set(false);
      },
    });
  }

  abrirModalNuevoLote(): void {
    const randomSuffix = Math.floor(100 + Math.random() * 900);
    this.nuevoCodigo.set(`LOT-PROD-2026-${randomSuffix}`);
    this.mostrarModalNuevoLote.set(true);
  }

  cerrarModalNuevoLote(): void {
    this.mostrarModalNuevoLote.set(false);
  }

  guardarNuevoLote(): void {
    if (!this.nuevoCodigo() || this.nuevoStock() <= 0 || this.nuevoCosto() <= 0) return;

    this.loadingCrearLote.set(true);
    const dto: LoteCreateDto = {
      codigoLote: this.nuevoCodigo(),
      productoId: Number(this.nuevoProductoId()),
      fechaFabricacion: this.nuevaFechaFab(),
      fechaVencimiento: this.nuevaFechaVenc(),
      stockInicial: Number(this.nuevoStock()),
      costoUnitario: Number(this.nuevoCosto()),
      observaciones: this.nuevasObservaciones(),
    };

    this.inventarioService.registrarLote(dto).subscribe({
      next: (creado) => {
        this.exito.set(`Lote ${creado.codigoLote} registrado exitosamente.`);
        this.loadingCrearLote.set(false);
        this.cerrarModalNuevoLote();
        this.cargarLotes();
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Error al crear el lote.');
        this.loadingCrearLote.set(false);
      },
    });
  }
}
