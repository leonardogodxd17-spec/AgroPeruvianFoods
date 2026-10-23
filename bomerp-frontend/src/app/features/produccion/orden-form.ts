import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProduccionService } from './produccion.service';
import { OrdenProduccionRequest } from './produccion.model';

@Component({
  selector: 'app-orden-form',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './orden-form.html',
  styleUrl: './orden-form.css',
})
export class OrdenForm {
  private readonly fb = inject(FormBuilder);
  private readonly produccionService = inject(ProduccionService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  protected readonly productosDisponibles = [
    { id: 1, nombre: 'Quinua Blanca Real Orgánica 1kg' },
    { id: 2, nombre: 'Quinua Roja Seleccionada 1kg' },
    { id: 3, nombre: 'Palta Hass Calibre 16 Exportación' },
    { id: 4, nombre: 'Arándano Fresco Biloxi Clamshell 125g' },
    { id: 5, nombre: 'Harina de Lúcuma Pura 250g' },
    { id: 7, nombre: 'Cacao Nativo Chuncho en Grano 1kg' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    productoId: [1, [Validators.required]],
    cantidadProgramada: [350, [Validators.required, Validators.min(10)]],
    observaciones: ['Lote de producción y envasado al vacío bajo norma HACCP'],
  });

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    const val = this.form.getRawValue();
    const prod = this.productosDisponibles.find((p) => p.id === Number(val.productoId));

    const request: OrdenProduccionRequest = {
      productoId: Number(val.productoId),
      nombreProducto: prod ? prod.nombre : 'Producto Agroindustrial',
      cantidadProgramada: Number(val.cantidadProgramada),
      observaciones: val.observaciones,
      insumos: [
        {
          insumoId: 9,
          nombreInsumo: 'Bolsa Bilaminada al Vacío 1kg',
          cantidadRequerida: Number(val.cantidadProgramada),
          cantidadConsumida: Number(val.cantidadProgramada),
          unidadMedida: 'UNIDADES',
        },
        {
          insumoId: 8,
          nombreInsumo: 'Caja de Cartón Corrugado 10kg Export',
          cantidadRequerida: Math.ceil(Number(val.cantidadProgramada) / 10),
          cantidadConsumida: Math.ceil(Number(val.cantidadProgramada) / 10),
          unidadMedida: 'UNIDADES',
        },
      ],
    };

    this.produccionService.crearOrden(request).subscribe({
      next: () => {
        this.router.navigate(['/produccion']);
      },
      error: (err) => {
        this.error.set(err?.error?.message || 'Error al planificar la orden de producción.');
        this.loading.set(false);
      },
    });
  }
}
