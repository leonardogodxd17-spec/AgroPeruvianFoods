import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CompraService } from './compra.service';
import { Compra, CompraReporte } from './compra.model';

@Component({
  selector: 'app-compra-list',
  imports: [CommonModule, RouterLink, ReactiveFormsModule, CurrencyPipe],
  templateUrl: './compra-list.html',
  styleUrl: './compra-list.css',
})
export class CompraList implements OnInit {
  private readonly compraService = inject(CompraService);
  private readonly fb = inject(FormBuilder);

  protected readonly compras = signal<Compra[]>([]);
  protected readonly resumen = signal<CompraReporte | null>(null);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly successMsg = signal<string | null>(null);

  protected readonly compraSeleccionada = signal<Compra | null>(null);
  protected readonly modalAmortizar = signal(false);
  protected readonly modalDetalle = signal(false);

  protected readonly formAmortizar = this.fb.nonNullable.group({
    montoAmortizado: [500, [Validators.required, Validators.min(1)]],
    metodoPago: ['TRANSFERENCIA', [Validators.required]],
    referencia: ['OP-BANCARIA-8891'],
  });

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.loading.set(true);
    this.error.set(null);

    this.compraService.listarCompras().subscribe({
      next: (data) => {
        this.compras.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el historial de compras.');
        this.loading.set(false);
      },
    });

    this.compraService.reporte().subscribe({
      next: (rep) => this.resumen.set(rep),
      error: () => {},
    });
  }

  abrirAmortizar(compra: Compra): void {
    this.compraSeleccionada.set(compra);
    this.formAmortizar.patchValue({
      montoAmortizado: compra.saldoPendiente,
    });
    this.modalAmortizar.set(true);
  }

  confirmarAmortizar(): void {
    const c = this.compraSeleccionada();
    if (!c || this.formAmortizar.invalid) return;

    this.loading.set(true);
    const val = this.formAmortizar.getRawValue();

    this.compraService.amortizarPago(c.id, val).subscribe({
      next: () => {
        this.successMsg.set(`Pago amortizado para la compra ${c.numeroComprobante}.`);
        this.modalAmortizar.set(false);
        this.cargarDatos();
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Error al amortizar el pago.');
        this.loading.set(false);
      },
    });
  }

  verDetalle(compra: Compra): void {
    this.compraService.obtenerCompra(compra.id).subscribe({
      next: (det) => {
        this.compraSeleccionada.set(det);
        this.modalDetalle.set(true);
      },
      error: () => {
        this.compraSeleccionada.set(compra);
        this.modalDetalle.set(true);
      },
    });
  }
}
