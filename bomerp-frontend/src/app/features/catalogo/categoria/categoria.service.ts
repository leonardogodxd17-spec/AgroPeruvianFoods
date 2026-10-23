import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { Categoria, CategoriaRequest } from './categoria.model';

@Injectable({ providedIn: 'root' })
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly api = inject(ApiService);

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.api.buildUrl('/api/v1/categorias'));
  }

  obtener(id: number): Observable<Categoria> {
    return this.http.get<Categoria>(this.api.buildUrl(`/api/v1/categorias/${id}`));
  }

  crear(categoria: CategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(this.api.buildUrl('/api/v1/categorias'), categoria);
  }

  actualizar(id: number, categoria: CategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(this.api.buildUrl(`/api/v1/categorias/${id}`), categoria);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(this.api.buildUrl(`/api/v1/categorias/${id}`));
  }
}
