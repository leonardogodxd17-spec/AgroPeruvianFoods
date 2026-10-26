import { Component, OnInit, OnDestroy, AfterViewInit, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { DeliveryService } from './delivery-service';
import { PedidoDelivery, EstadoDelivery } from './delivery.model';

interface CoordenadaMarker {
  lat: number;
  lng: number;
}

const COORD_PLANTA: [number, number] = [-12.0970, -77.0270]; // Planta AgroPeruvian - San Isidro

const DISTRITOS_LIMA: Record<string, [number, number]> = {
  'san isidro': [-12.0950, -77.0370],
  'miraflores': [-12.1190, -77.0300],
  'surco': [-12.1320, -76.9950],
  'santiago de surco': [-12.1320, -76.9950],
  'san borja': [-12.0930, -77.0010],
  'la molina': [-12.0830, -76.9450],
  'barranco': [-12.1480, -77.0210],
  'san miguel': [-12.0790, -77.0910],
  'magdalena': [-12.0910, -77.0700],
  'jesus maria': [-12.0750, -77.0470],
  'lince': [-12.0840, -77.0350],
  'pueblo libre': [-12.0740, -77.0650],
  'lima': [-12.0520, -77.0340],
  'ate': [-12.0280, -76.9180],
  'callao': [-12.0560, -77.1180],
};

@Component({
  selector: 'app-delivery-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './delivery-list.html',
  styleUrls: ['./delivery-list.css'],
})
export class DeliveryList implements OnInit, AfterViewInit, OnDestroy {
  private readonly deliveryService = inject(DeliveryService);

  @ViewChild('mapaElemento') mapaElemento?: ElementRef<HTMLDivElement>;

  readonly pedidos = signal<PedidoDelivery[]>([]);
  readonly cargando = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly exito = signal<string | null>(null);

  // Selector de vista: 'mapa' o 'tarjetas' (por defecto 'mapa' como en la imagen)
  readonly vistaActual = signal<'tarjetas' | 'mapa'>('mapa');
  readonly tipoCapa = signal<'mapa' | 'satelite'>('mapa');
  readonly isFullscreen = signal<boolean>(false);

  // Instancia Leaflet
  private map: L.Map | null = null;
  private layerMapa: L.TileLayer | null = null;
  private layerSatelite: L.TileLayer | null = null;
  private markersLayer: L.LayerGroup | null = null;
  private routesLayer: L.LayerGroup | null = null;

  // Modal para nuevo delivery
  mostrarModalNuevo = false;
  nuevoClienteNombre = '';
  nuevoClienteTelefono = '';
  nuevaDireccion = '';
  nuevaReferencia = '';
  nuevoCostoEnvio = 10;
  nuevoMontoTotal = 120;
  nuevoRepartidor = 'Carlos Mendoza (Moto 01)';
  nuevasObservaciones = '';

  // Pedido seleccionado para ver detalles en panel flotante del mapa
  pedidoDetalleMapa = signal<PedidoDelivery | null>(null);

  ngOnInit(): void {
    this.cargarPedidos();
  }

  ngAfterViewInit(): void {
    if (this.vistaActual() === 'mapa') {
      setTimeout(() => this.inicializarMapa(), 100);
    }
  }

  ngOnDestroy(): void {
    this.destruirMapa();
  }

  cambiarVista(vista: 'tarjetas' | 'mapa'): void {
    this.vistaActual.set(vista);
    if (vista === 'mapa') {
      setTimeout(() => {
        if (!this.map) {
          this.inicializarMapa();
        } else {
          this.map.invalidateSize();
          this.actualizarMarcadores();
        }
      }, 100);
    }
  }

  cambiarCapa(capa: 'mapa' | 'satelite'): void {
    this.tipoCapa.set(capa);
    if (!this.map || !this.layerMapa || !this.layerSatelite) return;

    if (capa === 'satelite') {
      this.map.removeLayer(this.layerMapa);
      this.layerSatelite.addTo(this.map);
    } else {
      this.map.removeLayer(this.layerSatelite);
      this.layerMapa.addTo(this.map);
    }
  }

  toggleFullscreen(): void {
    const contenedor = document.getElementById('mapaContainer');
    if (!contenedor) return;

    if (!document.fullscreenElement) {
      contenedor.requestFullscreen().then(() => {
        this.isFullscreen.set(true);
        setTimeout(() => this.map?.invalidateSize(), 150);
      }).catch(() => {});
    } else {
      document.exitFullscreen().then(() => {
        this.isFullscreen.set(false);
        setTimeout(() => this.map?.invalidateSize(), 150);
      }).catch(() => {});
    }
  }

  zoomIn(): void {
    this.map?.zoomIn();
  }

  zoomOut(): void {
    this.map?.zoomOut();
  }

  private inicializarMapa(): void {
    if (this.map) return;
    const elem = document.getElementById('mapaRutas');
    if (!elem) return;

    // Inicializar mapa centrado en Lima (San Isidro / Miraflores)
    this.map = L.map('mapaRutas', {
      center: [-12.1050, -77.0250],
      zoom: 13,
      zoomControl: false, // Desactivamos por defecto para usar los controles idénticos a Google Maps
      attributionControl: false,
    });

    // Capa Estilo Mapa de Calles (Esri World Street Map - 100% libre sin marcas de agua ni API key)
    this.layerMapa = L.tileLayer(
      'https://server.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer/tile/{z}/{y}/{x}',
      { maxZoom: 19 }
    );

    // Capa Satélite Esri
    this.layerSatelite = L.tileLayer(
      'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}',
      { maxZoom: 19 }
    );

    this.layerMapa.addTo(this.map);

    this.markersLayer = L.layerGroup().addTo(this.map);
    this.routesLayer = L.layerGroup().addTo(this.map);

    this.actualizarMarcadores();
  }

  private destruirMapa(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  private actualizarMarcadores(): void {
    if (!this.map || !this.markersLayer || !this.routesLayer) return;

    this.markersLayer.clearLayers();
    this.routesLayer.clearLayers();

    // 1. Marcador Planta Central AgroPeruvian (Verde)
    const iconoPlanta = L.divIcon({
      className: 'custom-leaflet-marker',
      html: `
        <div class="map-marker-pin pin-planta">
          <svg class="marker-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
          </svg>
        </div>
      `,
      iconSize: [38, 38],
      iconAnchor: [19, 19],
    });

    const markerPlanta = L.marker(COORD_PLANTA, { icon: iconoPlanta })
      .bindPopup(`
        <div class="map-popup-card">
          <div class="popup-badge badge-planta">ORIGEN PRINCIPAL</div>
          <h4 class="popup-title">Planta San Isidro</h4>
          <p class="popup-subtitle">AgroPeruvian Foods S.A.C.</p>
          <div class="popup-meta">
            <span>Dirección: Av. Canaval y Moreyra 450, San Isidro</span>
          </div>
        </div>
      `);
    this.markersLayer.addLayer(markerPlanta);

    // 2. Marcadores para los pedidos
    const lista = this.pedidos();
    const coordenadasDestinos: [number, number][] = [];

    lista.forEach((p, index) => {
      const coord = this.obtenerCoordenada(p, index);
      coordenadasDestinos.push(coord);

      let claseColor = 'pin-registrado'; // naranja para PENDIENTE
      let svgContenido = `
        <svg class="marker-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"/>
        </svg>
      `;

      if (p.estadoDelivery === 'EN_RUTA') {
        claseColor = 'pin-enruta'; // violeta / azul
        svgContenido = `
          <svg class="marker-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13 16V6a1 1 0 00-1-1H4a1 1 0 00-1 1v10a1 1 0 001 1h1m8-1a1 1 0 01-1 1H9m4-1V8a1 1 0 011-1h2.586a1 1 0 01.707.293l3.414 3.414a1 1 0 01.293.707V16a1 1 0 01-1 1h-1m-6-1a2 2 0 104 0m-4 0a2 2 0 114 0m-8 0a2 2 0 104 0m-4 0a2 2 0 114 0"/>
          </svg>
        `;

        // Trazar línea de ruta conectando planta con pedido en ruta
        const polyline = L.polyline([COORD_PLANTA, coord], {
          color: '#6366f1',
          weight: 3.5,
          opacity: 0.8,
          dashArray: '6, 8',
        });
        this.routesLayer?.addLayer(polyline);

      } else if (p.estadoDelivery === 'ENTREGADO') {
        claseColor = 'pin-entregado'; // verde
        svgContenido = `
          <svg class="marker-svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
            <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
          </svg>
        `;
      }

      const iconoPedido = L.divIcon({
        className: 'custom-leaflet-marker',
        html: `
          <div class="map-marker-pin ${claseColor}">
            ${svgContenido}
          </div>
        `,
        iconSize: [36, 36],
        iconAnchor: [18, 18],
      });

      const marker = L.marker(coord, { icon: iconoPedido });

      marker.on('click', () => {
        this.pedidoDetalleMapa.set(p);
      });

      marker.bindPopup(`
        <div class="map-popup-card">
          <div class="popup-header-row">
            <span class="popup-code">${p.codigoPedido}</span>
            <span class="popup-status-badge ${claseColor}">
              ${p.estadoDelivery === 'PENDIENTE' ? 'Registrado' : (p.estadoDelivery === 'EN_RUTA' ? 'En Ruta' : 'Entregado')}
            </span>
          </div>
          <h4 class="popup-title">${p.clienteNombre}</h4>
          <p class="popup-meta"><strong>Dirección:</strong> ${p.direccionEntrega}</p>
          <p class="popup-meta"><strong>Teléfono:</strong> ${p.clienteTelefono || 'No registrado'}</p>
          <p class="popup-meta"><strong>Total:</strong> S/ ${(p.montoTotal || 0).toFixed(2)}</p>
          ${p.repartidorNombre ? `<p class="popup-meta"><strong>Repartidor:</strong> ${p.repartidorNombre}</p>` : ''}
        </div>
      `);

      this.markersLayer?.addLayer(marker);
    });
  }

  private obtenerCoordenada(p: PedidoDelivery, index: number): [number, number] {
    const dir = (p.direccionEntrega || '').toLowerCase();
    for (const [distrito, coords] of Object.entries(DISTRITOS_LIMA)) {
      if (dir.includes(distrito)) {
        // Leve desplazamiento para no encimar marcadores si caen en el mismo distrito
        return [coords[0] + (index * 0.0025), coords[1] + (index * 0.0025)];
      }
    }
    // Si no coincide, ubicar en radio central de Lima según índice
    const offsets: [number, number][] = [
      [-12.1190, -77.0300], // Miraflores
      [-12.0950, -77.0370], // San Isidro
      [-12.1320, -76.9950], // Surco
      [-12.0930, -77.0010], // San Borja
      [-12.0830, -76.9450], // La Molina
    ];
    return offsets[index % offsets.length];
  }

  cargarPedidos(): void {
    this.cargando.set(true);
    this.deliveryService.listar().subscribe({
      next: (data) => {
        this.pedidos.set(data);
        this.cargando.set(false);
        this.actualizarMarcadores();
      },
      error: (err) => {
        this.error.set('Error al cargar pedidos de delivery: ' + (err.error?.message || err.message));
        this.cargando.set(false);
      },
    });
  }

  pedidosPorEstado(estado: EstadoDelivery): PedidoDelivery[] {
    return this.pedidos().filter((p) => p.estadoDelivery === estado);
  }

  marcarEnRuta(p: PedidoDelivery): void {
    this.deliveryService.actualizarEstado(p.id, {
      estado: 'EN_RUTA',
      observaciones: 'Repartidor salió a ruta de entrega',
    }).subscribe({
      next: () => {
        this.exito.set(`Pedido ${p.codigoPedido} ahora está EN RUTA.`);
        this.cargarPedidos();
        this.pedidoDetalleMapa.set(null);
        setTimeout(() => this.exito.set(null), 4000);
      },
      error: (err) => alert('Error: ' + err.message),
    });
  }

  marcarEntregado(p: PedidoDelivery): void {
    this.deliveryService.actualizarEstado(p.id, {
      estado: 'ENTREGADO',
      observaciones: 'Entregado en destino conforme con firma o foto',
    }).subscribe({
      next: () => {
        this.exito.set(`Pedido ${p.codigoPedido} marcado como ENTREGADO.`);
        this.cargarPedidos();
        this.pedidoDetalleMapa.set(null);
        setTimeout(() => this.exito.set(null), 4000);
      },
      error: (err) => alert('Error: ' + err.message),
    });
  }

  abrirModalNuevo(): void {
    this.mostrarModalNuevo = true;
    this.nuevoClienteNombre = '';
    this.nuevoClienteTelefono = '';
    this.nuevaDireccion = '';
    this.nuevaReferencia = '';
    this.nuevoCostoEnvio = 10;
    this.nuevoMontoTotal = 150;
    this.nuevoRepartidor = 'Carlos Mendoza (Moto 01)';
    this.nuevasObservaciones = '';
  }

  cerrarModalNuevo(): void {
    this.mostrarModalNuevo = false;
  }

  guardarNuevoDelivery(): void {
    if (!this.nuevoClienteNombre.trim() || !this.nuevaDireccion.trim()) {
      alert('Por favor complete el nombre del cliente y la dirección.');
      return;
    }

    this.deliveryService.crearPedido({
      clienteNombre: this.nuevoClienteNombre,
      clienteTelefono: this.nuevoClienteTelefono,
      direccionEntrega: this.nuevaDireccion,
      referencia: this.nuevaReferencia,
      costoEnvio: +this.nuevoCostoEnvio,
      montoTotal: +this.nuevoMontoTotal,
      repartidorNombre: this.nuevoRepartidor,
      observaciones: this.nuevasObservaciones,
    }).subscribe({
      next: () => {
        this.cerrarModalNuevo();
        this.cargarPedidos();
        this.exito.set('Pedido de delivery registrado correctamente.');
        setTimeout(() => this.exito.set(null), 4000);
      },
      error: (err) => alert('Error: ' + (err.error?.message || err.message)),
    });
  }
}
