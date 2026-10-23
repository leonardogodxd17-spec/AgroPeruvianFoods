import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CompraService } from './compra.service';
import { Proveedor } from './compra.model';

@Component({
  selector: 'app-proveedor-list',
  imports: [CommonModule, RouterLink],
  templateUrl: './proveedor-list.html',
  styleUrl: './proveedor-list.css',
})
export class ProveedorList implements OnInit {
  private readonly compraService = inject(CompraService);

  protected readonly proveedores = signal<Proveedor[]>([]);
  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.loading.set(true);
    this.error.set(null);

    this.compraService.listarProveedores().subscribe({
      next: (data) => {
        this.proveedores.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de proveedores.');
        this.loading.set(false);
      },
    });
  }
}
