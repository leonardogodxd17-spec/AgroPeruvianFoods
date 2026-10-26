import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-sin-permiso',
  imports: [RouterLink],
  template: `
    <div style="max-width: 600px; margin: 4rem auto; text-align: center; padding: 2.5rem; background: #ffffff; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.08); border-top: 5px solid #d32f2f;">
      <div style="margin-bottom: 1rem;"><span class="material-symbols-outlined" style="font-size: 4rem; color: #d32f2f;">gpp_bad</span></div>
      <h2 style="color: #c62828; margin-bottom: 0.5rem; font-size: 1.8rem;">Acceso Denegado (403)</h2>
      <p style="color: #555; font-size: 1.1rem; line-height: 1.6; margin: 1.5rem 0;">No tienes los permisos ni el rol requerido en Keycloak para acceder a esta pantalla o funcionalidad.</p>
      <a routerLink="/" style="display: inline-block; background: #00796b; color: #ffffff; padding: 0.75rem 1.5rem; border-radius: 6px; text-decoration: none; font-weight: 600; transition: background 0.2s;">Volver al Panel Principal</a>
    </div>
  `,
})
export class SinPermiso {}
