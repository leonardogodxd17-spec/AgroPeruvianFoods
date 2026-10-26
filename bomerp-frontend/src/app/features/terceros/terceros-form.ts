import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TercerosService } from './terceros-service';
import { SunatApiService, ConsultaDocumentoResponse } from '../../core/services/sunat-api.service';

@Component({
  selector: 'app-terceros-form',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './terceros-form.html',
  styleUrls: ['./terceros-form.css'],
})
export class TercerosForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly tercerosService = inject(TercerosService);
  private readonly sunatService = inject(SunatApiService);

  readonly guardando = signal<boolean>(false);
  readonly consultando = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly resultadoSunat = signal<ConsultaDocumentoResponse | null>(null);

  form: FormGroup = this.fb.group({
    tipoDocumento: ['RUC', [Validators.required]],
    numeroDocumento: ['', [Validators.required, Validators.maxLength(30)]],
    razonSocial: ['', [Validators.required, Validators.maxLength(150)]],
    tipoTercero: ['PROVEEDOR', [Validators.required]],
    telefono: [''],
    direccion: [''],
    correo: ['', [Validators.email]],
  });

  ngOnInit(): void {}

  consultarDocumento(): void {
    const tipo = this.form.get('tipoDocumento')?.value;
    const numero = this.form.get('numeroDocumento')?.value?.trim();

    if (!numero) {
      this.error.set('Ingrese el número de RUC o DNI antes de consultar.');
      return;
    }

    this.consultando.set(true);
    this.error.set(null);
    this.resultadoSunat.set(null);

    this.sunatService.consultarDocumento(tipo, numero).subscribe({
      next: (res) => {
        this.consultando.set(false);
        if (res && res.exito) {
          this.resultadoSunat.set(res);
          this.form.patchValue({
            razonSocial: res.razonSocial,
            direccion: res.direccion || this.form.get('direccion')?.value,
          });
        } else {
          this.error.set(res?.mensaje || 'No se encontró información en SUNAT/RENIEC para este documento.');
        }
      },
      error: () => {
        this.consultando.set(false);
        this.error.set('No se pudo conectar con el servicio de SUNAT/RENIEC. Ingrese los datos manualmente.');
      },
    });
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    this.error.set(null);

    this.tercerosService.crear(this.form.value).subscribe({
      next: () => {
        this.guardando.set(false);
        this.router.navigate(['/terceros']);
      },
      error: (err) => {
        this.error.set(err.error?.message || err.message || 'Error al registrar tercero');
        this.guardando.set(false);
      },
    });
  }
}
