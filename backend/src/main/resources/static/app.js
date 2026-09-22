/**
 * CONSOLA API VENTAS Y TICKETS POS - AGRO PERUVIAN FOODS
 * Módulo de Negocio Transaccional: Morales Auquipata Zaggy Leonardo
 * Entidad Cabecera-Detalle: Venta -> DetalleVenta
 * Integración directa con Spring Boot REST API & Swagger UI
 */

const AppState = {
  cart: [],
  catalog: [],
  lastVentas: [],
  defaultCatalog: [
    { id: 1, nombre: "Quinua Blanca Real Orgánica 1kg", precio: 14.50, stock: 850 },
    { id: 2, nombre: "Quinua Roja Seleccionada 1kg", precio: 16.00, stock: 420 },
    { id: 3, nombre: "Palta Hass Calibre 16 Exportación", precio: 8.50, stock: 1200 },
    { id: 4, nombre: "Arándano Fresco Biloxi Clamshell 125g", precio: 6.20, stock: 900 },
    { id: 5, nombre: "Harina de Lúcuma Pura 250g", precio: 18.00, stock: 310 },
    { id: 6, nombre: "Mango Kent Fresco Caja 4kg", precio: 28.00, stock: 150 },
    { id: 7, nombre: "Cacao Nativo Chuncho en Grano 1kg", precio: 24.00, stock: 500 },
    { id: 8, nombre: "Caja de Cartón Corrugado 10kg Export", precio: 4.20, stock: 2500 }
  ]
};

// Utilidad para obtener la URL base
function getBaseUrl() {
  const inputVal = document.getElementById("apiUrl").value.trim();
  return inputVal.replace(/\/+$/, "");
}

// Utilidad para renderizar respuesta en el Visor JSON
function showApiResponse(method, path, status, timeMs, data) {
  const statusEl = document.getElementById("resStatus");
  const methodEl = document.getElementById("resMethod");
  const pathEl = document.getElementById("resPath");
  const timeEl = document.getElementById("resTime");
  const boxEl = document.getElementById("responseBox");

  methodEl.textContent = method;
  pathEl.textContent = path;
  timeEl.textContent = `${timeMs} ms`;

  statusEl.className = "http-tag status-tag";
  if (status >= 200 && status < 300) {
    statusEl.classList.add("status-200");
    statusEl.textContent = `HTTP ${status} OK`;
  } else if (status >= 400 && status < 409) {
    statusEl.classList.add("status-400");
    statusEl.textContent = `HTTP ${status} BAD REQUEST`;
  } else if (status === 409) {
    statusEl.classList.add("status-409");
    statusEl.textContent = `HTTP ${status} CONFLICT (ROLLBACK)`;
  } else {
    statusEl.classList.add("status-500");
    statusEl.textContent = `HTTP ${status} ERROR`;
  }

  boxEl.textContent = JSON.stringify(data, null, 2);
}

// Actualizar indicador de estado de la API
function updateApiStatus(online, message) {
  const dot = document.querySelector(".status-dot");
  const text = document.getElementById("statusText");

  if (online) {
    dot.className = "status-dot online";
    text.textContent = message || "Conectado a Spring Boot";
  } else {
    dot.className = "status-dot offline";
    text.textContent = message || "Desconectado";
  }
}

// ==========================================================================
// 1. GESTIÓN DEL CATÁLOGO DE PRODUCTOS (GET /api/v1/productos)
// ==========================================================================
async function fetchCatalog() {
  const baseUrl = getBaseUrl();
  const select = document.getElementById("productoSelect");
  select.innerHTML = '<option value="">Cargando productos...</option>';

  try {
    const res = await fetch(`${baseUrl}/productos`);
    if (res.ok) {
      AppState.catalog = await res.json();
      updateApiStatus(true, "API Online (Oracle/H2)");
    } else {
      throw new Error(`HTTP ${res.status}`);
    }
  } catch (err) {
    console.warn("No se pudo conectar a /api/v1/productos. Usando catálogo semilla.", err);
    AppState.catalog = AppState.defaultCatalog;
    updateApiStatus(false, "Modo local / Esperando backend");
  }

  // Llenar select
  select.innerHTML = "";
  AppState.catalog.forEach((prod) => {
    const opt = document.createElement("option");
    opt.value = prod.id;
    opt.dataset.nombre = prod.nombre;
    opt.dataset.precio = prod.precio;
    opt.dataset.stock = prod.stock != null ? prod.stock : 100;
    opt.textContent = `[ID: ${prod.id}] ${prod.nombre} - S/ ${Number(prod.precio).toFixed(2)} (Stock: ${opt.dataset.stock})`;
    select.appendChild(opt);
  });
}

// ==========================================================================
// 2. CARRITO / DETALLE DE VENTA LOCAL (EN MEMORIA ANTES DE POST)
// ==========================================================================
function renderCart() {
  const tbody = document.getElementById("cartTableBody");
  tbody.innerHTML = "";

  if (AppState.cart.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" class="empty-state" style="padding: 1rem !important;">Sin productos en la venta. Añada al menos un ítem.</td></tr>';
    document.getElementById("lblSubtotal").textContent = "S/ 0.00";
    document.getElementById("lblIgv").textContent = "S/ 0.00";
    document.getElementById("lblTotal").textContent = "S/ 0.00";
    return;
  }

  let total = 0;
  AppState.cart.forEach((item, index) => {
    total += item.subtotal;
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${item.nombre}</td>
      <td class="num">S/ ${item.precio.toFixed(2)}</td>
      <td class="num">${item.cantidad}</td>
      <td class="num"><strong>S/ ${item.subtotal.toFixed(2)}</strong></td>
      <td class="center">
        <button type="button" class="del-item-btn" data-index="${index}" title="Quitar producto">&times;</button>
      </td>
    `;
    tbody.appendChild(tr);
  });

  const igv = total * 0.18;
  const subtotal = total - igv;

  document.getElementById("lblSubtotal").textContent = `S/ ${subtotal.toFixed(2)}`;
  document.getElementById("lblIgv").textContent = `S/ ${igv.toFixed(2)}`;
  document.getElementById("lblTotal").textContent = `S/ ${total.toFixed(2)}`;
}

function addItemToCart() {
  const select = document.getElementById("productoSelect");
  if (!select.value) return;

  const opt = select.selectedOptions[0];
  const prodId = Number(opt.value);
  const nombre = opt.dataset.nombre;
  const precio = Number(opt.dataset.precio);
  const cantInput = document.getElementById("cantidad");
  const cantidad = parseInt(cantInput.value, 10);

  if (isNaN(cantidad) || cantidad <= 0) {
    alert("La cantidad debe ser mayor a 0");
    return;
  }

  // Verificar si ya existe en el carrito para sumar
  const exist = AppState.cart.find((it) => it.productoId === prodId);
  if (exist) {
    exist.cantidad += cantidad;
    exist.subtotal = exist.cantidad * exist.precio;
  } else {
    AppState.cart.push({
      productoId: prodId,
      nombre,
      precio,
      cantidad,
      subtotal: cantidad * precio
    });
  }

  renderCart();
}

// ==========================================================================
// 3. REGISTRAR VENTA (POST /api/v1/ventas)
// ==========================================================================
async function submitVenta(overridePayload = null) {
  const baseUrl = getBaseUrl();
  const cliente = document.getElementById("cliente").value.trim();
  const metodoPago = document.getElementById("metodoPago").value;

  let payload = overridePayload;
  if (!payload) {
    payload = {
      clienteNombre: cliente || "Cliente General",
      metodoPago: metodoPago,
      detalles: AppState.cart.map((it) => ({
        productoId: it.productoId,
        cantidad: it.cantidad
      }))
    };
  }

  const startTime = performance.now();
  try {
    const res = await fetch(`${baseUrl}/ventas`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    const elapsed = Math.round(performance.now() - startTime);
    const data = await res.json();

    showApiResponse("POST", "/api/v1/ventas", res.status, elapsed, data);

    if (res.ok) {
      AppState.cart = [];
      renderCart();
      fetchVentas();     // Refrescar tabla
      fetchCatalog();    // Refrescar stock actualizado de catálogo
    }
  } catch (err) {
    const elapsed = Math.round(performance.now() - startTime);
    showApiResponse("POST", "/api/v1/ventas", 500, elapsed, {
      error: "Error de conexión con el backend",
      detalle: err.message
    });
  }
}

// ==========================================================================
// 4. CONSULTAR VENTAS CON FILTROS (GET /api/v1/ventas)
// ==========================================================================
async function fetchVentas() {
  const baseUrl = getBaseUrl();
  const estado = document.getElementById("filtroEstado").value;
  const ordenarPor = document.getElementById("ordenarPor").value;
  const direccion = document.getElementById("direccion").value;

  const params = new URLSearchParams();
  if (estado && estado !== "TODAS") params.append("estado", estado);
  if (ordenarPor) params.append("ordenarPor", ordenarPor);
  if (direccion) params.append("direccion", direccion);

  const url = `${baseUrl}/ventas${params.toString() ? "?" + params.toString() : ""}`;
  const startTime = performance.now();

  try {
    const res = await fetch(url);
    const elapsed = Math.round(performance.now() - startTime);
    const data = await res.json();

    showApiResponse("GET", `/api/v1/ventas${params.toString() ? "?" + params.toString() : ""}`, res.status, elapsed, data);

    if (res.ok) {
      AppState.lastVentas = Array.isArray(data) ? data : [];
      renderVentasTable(AppState.lastVentas);
      updateApiStatus(true, "API Online (Oracle/H2)");
    }
  } catch (err) {
    const elapsed = Math.round(performance.now() - startTime);
    showApiResponse("GET", "/api/v1/ventas", 500, elapsed, {
      error: "No se pudo consultar /api/v1/ventas",
      detalle: err.message
    });
    updateApiStatus(false, "Sin conexión al backend");
  }
}

// ==========================================================================
// 5. REPORTE / RESUMEN AGREGADO (GET /api/v1/ventas/resumen)
// ==========================================================================
async function fetchResumen() {
  const baseUrl = getBaseUrl();
  const estado = document.getElementById("filtroEstado").value;
  const params = new URLSearchParams();
  if (estado && estado !== "TODAS") params.append("estado", estado);

  const url = `${baseUrl}/ventas/resumen${params.toString() ? "?" + params.toString() : ""}`;
  const startTime = performance.now();

  try {
    const res = await fetch(url);
    const elapsed = Math.round(performance.now() - startTime);
    const data = await res.json();

    showApiResponse("GET", `/api/v1/ventas/resumen`, res.status, elapsed, data);

    if (res.ok && data.agregado) {
      document.getElementById("kpiContainer").style.display = "grid";
      document.getElementById("kpiCount").textContent = data.agregado.cantidadVentas || 0;
      document.getElementById("kpiTotal").textContent = `S/ ${Number(data.agregado.montoTotal || 0).toFixed(2)}`;
      document.getElementById("kpiAvg").textContent = `S/ ${Number(data.agregado.ticketPromedio || 0).toFixed(2)}`;
    }
  } catch (err) {
    const elapsed = Math.round(performance.now() - startTime);
    showApiResponse("GET", "/api/v1/ventas/resumen", 500, elapsed, {
      error: "Error al obtener reporte",
      detalle: err.message
    });
  }
}

// ==========================================================================
// 6. ANULAR VENTA (PUT /api/v1/ventas/{id}/anular)
// ==========================================================================
async function anularVenta(id) {
  if (!confirm(`¿Está seguro de anular la venta con ID ${id}?\nSe repondrá el stock de todos sus productos en la base de datos.`)) {
    return;
  }

  const baseUrl = getBaseUrl();
  const startTime = performance.now();

  try {
    const res = await fetch(`${baseUrl}/ventas/${id}/anular`, {
      method: "PUT"
    });

    const elapsed = Math.round(performance.now() - startTime);
    const data = await res.json();

    showApiResponse("PUT", `/api/v1/ventas/${id}/anular`, res.status, elapsed, data);

    if (res.ok) {
      fetchVentas();
      fetchCatalog();
    }
  } catch (err) {
    const elapsed = Math.round(performance.now() - startTime);
    showApiResponse("PUT", `/api/v1/ventas/${id}/anular`, 500, elapsed, {
      error: `Error al anular venta ${id}`,
      detalle: err.message
    });
  }
}

// ==========================================================================
// 7. RENDERIZADO DE TABLA DE VENTAS PERSISTIDAS
// ==========================================================================
function renderVentasTable(ventas) {
  const tbody = document.getElementById("ventasBody");
  const countBadge = document.getElementById("ventasCountBadge");
  tbody.innerHTML = "";

  countBadge.textContent = `${ventas.length} registro(s)`;

  if (ventas.length === 0) {
    tbody.innerHTML = '<tr><td colspan="11" class="empty-state">No hay ventas registradas con el criterio seleccionado.</td></tr>';
    return;
  }

  ventas.forEach((v) => {
    const tr = document.createElement("tr");

    // Formatear detalles
    const detallesHtml = (v.detalles || []).map(
      (d) => `&bull; ${d.nombreProducto || `Producto ${d.productoId}`} &times; <strong>${d.cantidad}</strong> (S/ ${Number(d.precioUnitario).toFixed(2)})`
    ).join("<br>");

    const fechaFmt = v.fecha ? v.fecha.replace("T", " ").substring(0, 19) : "--";

    tr.innerHTML = `
      <td><strong>${v.id}</strong></td>
      <td><code>${v.numeroTicket || "TKT-" + v.id}</code></td>
      <td><small>${fechaFmt}</small></td>
      <td>${v.clienteNombre || "Cliente General"}</td>
      <td><span class="badge-pago">${v.metodoPago || "EFECTIVO"}</span></td>
      <td><small>${detallesHtml || "Sin detalles"}</small></td>
      <td class="num">S/ ${Number(v.subtotal || 0).toFixed(2)}</td>
      <td class="num">S/ ${Number(v.igv || 0).toFixed(2)}</td>
      <td class="num"><strong>S/ ${Number(v.total || 0).toFixed(2)}</strong></td>
      <td><span class="badge-status ${v.estado}">${v.estado}</span></td>
      <td class="center">
        <div class="actions-cell">
          <button type="button" class="tbl-btn btn-ticket" data-action="ticket" data-id="${v.id}" title="Ver Ticket POS">
            Ticket
          </button>
          <button type="button" class="tbl-btn btn-anular" data-action="anular" data-id="${v.id}" ${v.estado === "ANULADA" ? "disabled" : ""} title="${v.estado === "ANULADA" ? "Ya anulada" : "Anular venta y reponer stock"}">
            Anular
          </button>
        </div>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

// ==========================================================================
// 8. MODAL DE TICKET TÉRMICO POS (80mm)
// ==========================================================================
function openTicketModal(ventaId) {
  const venta = AppState.lastVentas.find((v) => v.id === Number(ventaId));
  if (!venta) return;

  document.getElementById("tktNumero").textContent = venta.numeroTicket || `TKT-${venta.id}`;
  document.getElementById("tktFecha").textContent = venta.fecha ? venta.fecha.replace("T", " ").substring(0, 19) : new Date().toLocaleString();
  document.getElementById("tktCliente").textContent = venta.clienteNombre || "Cliente General";
  document.getElementById("tktPago").textContent = venta.metodoPago || "EFECTIVO";
  document.getElementById("tktEstado").textContent = venta.estado || "REGISTRADA";

  const itemsBody = document.getElementById("tktItemsBody");
  itemsBody.innerHTML = "";

  (venta.detalles || []).forEach((d) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${d.cantidad} &times; ${d.nombreProducto || `Producto ${d.productoId}`}</td>
      <td class="num">${Number(d.precioUnitario).toFixed(2)}</td>
      <td class="num"><strong>${Number(d.subtotal).toFixed(2)}</strong></td>
    `;
    itemsBody.appendChild(tr);
  });

  document.getElementById("tktSubtotal").textContent = `S/ ${Number(venta.subtotal || 0).toFixed(2)}`;
  document.getElementById("tktIgv").textContent = `S/ ${Number(venta.igv || 0).toFixed(2)}`;
  document.getElementById("tktTotal").textContent = `S/ ${Number(venta.total || 0).toFixed(2)}`;

  document.getElementById("ticketModal").classList.add("active");
}

function closeTicketModal() {
  document.getElementById("ticketModal").classList.remove("active");
}

// ==========================================================================
// 9. EVENT LISTENERS E INICIALIZACIÓN
// ==========================================================================
document.addEventListener("DOMContentLoaded", () => {
  // 1. Cargar catálogo inicial
  fetchCatalog();

  // 2. Consultar ventas persistidas
  fetchVentas();

  // Form submit
  document.getElementById("ventaForm").addEventListener("submit", (e) => {
    e.preventDefault();
    if (AppState.cart.length === 0) {
      // Si no hay items en el carrito, añadir el que está seleccionado actualmente
      addItemToCart();
    }
    submitVenta();
  });

  // Añadir item al detalle
  document.getElementById("btnAddItem").addEventListener("click", addItemToCart);

  // Botón quitar item de la tabla del carrito
  document.getElementById("cartTableBody").addEventListener("click", (e) => {
    const btn = e.target.closest(".del-item-btn");
    if (btn) {
      const idx = Number(btn.dataset.index);
      AppState.cart.splice(idx, 1);
      renderCart();
    }
  });

  // Botones de consulta y filtros
  document.getElementById("getVentasBtn").addEventListener("click", fetchVentas);
  document.getElementById("getResumenBtn").addEventListener("click", fetchResumen);
  document.getElementById("refreshTableBtn").addEventListener("click", fetchVentas);
  document.getElementById("btnRefreshCatalog").addEventListener("click", fetchCatalog);
  document.getElementById("btnTestConn").addEventListener("click", () => {
    fetchCatalog();
    fetchVentas();
  });

  document.getElementById("clearConsoleBtn").addEventListener("click", () => {
    document.getElementById("responseBox").textContent = '{\n  "mensaje": "Visor limpio"\n}';
    document.getElementById("resStatus").className = "http-tag status-tag status-idle";
    document.getElementById("resStatus").textContent = "STATUS";
  });

  // Acciones en la tabla de ventas (Ticket / Anular)
  document.getElementById("ventasBody").addEventListener("click", (e) => {
    const btn = e.target.closest("button[data-action]");
    if (!btn) return;
    const action = btn.dataset.action;
    const id = btn.dataset.id;

    if (action === "ticket") {
      openTicketModal(id);
    } else if (action === "anular") {
      anularVenta(id);
    }
  });

  // Modal ticket
  document.getElementById("btnCloseTicketModal").addEventListener("click", closeTicketModal);
  document.getElementById("btnCancelTicketModal").addEventListener("click", closeTicketModal);
  document.getElementById("btnPrintTicket").addEventListener("click", () => {
    window.print();
  });

  // ==========================================================================
  // CASOS DE PRUEBA RÁPIDOS DE RÚBRICA S06 LP2 (ZAGGY MORALES)
  // ==========================================================================

  // Caso 1: Venta exitosa de 2 unidades
  document.getElementById("testCaseSuccess").addEventListener("click", () => {
    document.getElementById("cliente").value = "María Quispe";
    document.getElementById("metodoPago").value = "EFECTIVO";
    const firstProd = AppState.catalog[0] || AppState.defaultCatalog[0];
    AppState.cart = [
      {
        productoId: firstProd.id,
        nombre: firstProd.nombre,
        precio: Number(firstProd.precio),
        cantidad: 2,
        subtotal: 2 * Number(firstProd.precio)
      }
    ];
    renderCart();
  });

  // Caso 2: Stock insuficiente (999,999 uds) -> Prueba de Rollback Atómico 409
  document.getElementById("testCaseRollback").addEventListener("click", () => {
    const firstProd = AppState.catalog[0] || AppState.defaultCatalog[0];
    submitVenta({
      clienteNombre: "Cliente Prueba Rollback",
      metodoPago: "EFECTIVO",
      detalles: [
        {
          productoId: firstProd.id,
          cantidad: 999999
        }
      ]
    });
  });

  // Caso 3: Cantidad 0 -> 400 Bad Request por Jakarta Bean Validation
  document.getElementById("testCaseZero").addEventListener("click", () => {
    const firstProd = AppState.catalog[0] || AppState.defaultCatalog[0];
    submitVenta({
      clienteNombre: "Cliente Cantidad Cero",
      metodoPago: "EFECTIVO",
      detalles: [
        {
          productoId: firstProd.id,
          cantidad: 0
        }
      ]
    });
  });

  // Caso 4: Venta sin detalles -> 400 Bad Request
  document.getElementById("testCaseEmpty").addEventListener("click", () => {
    submitVenta({
      clienteNombre: "Cliente Venta Vacía",
      metodoPago: "EFECTIVO",
      detalles: []
    });
  });
});


// ========================================================
// MULTI-MODULE CONTROLLER LOGIC (BOMERP)
// ========================================================

// 1. Tab Navigation
document.addEventListener("DOMContentLoaded", () => {
  const tabs = document.querySelectorAll(".mod-tab");
  tabs.forEach(tab => {
    tab.addEventListener("click", () => {
      const targetId = tab.getAttribute("data-tab");
      document.querySelectorAll(".mod-tab").forEach(t => t.classList.remove("active"));
      document.querySelectorAll(".tab-pane").forEach(p => p.classList.remove("active"));
      tab.classList.add("active");
      const pane = document.getElementById(targetId);
      if (pane) pane.classList.add("active");

      // Auto-load data for target module
      if (targetId === "tab-compras") {
        cargarProveedores();
        cargarCompras();
        cargarResumenCompras();
      } else if (targetId === "tab-produccion") {
        cargarProductosProduccion();
        cargarOrdenes();
        cargarResumenProduccion();
      } else if (targetId === "tab-caja") {
        cargarEstadoCaja();
        cargarHistorialCaja();
        cargarResumenCaja();
      }
    });
  });

  initComprasHandlers();
  initProduccionHandlers();
  initCajaHandlers();
});

// --------------------------------------------------------
// MODULO COMPRAS (Elishan Huaylla)
// --------------------------------------------------------
async function cargarProveedores() {
  try {
    const res = await fetch(`${getBaseUrl()}/compras/proveedores`);
    if (!res.ok) return;
    const proveedores = await res.json();
    const sel = document.getElementById("cProveedor");
    if (sel) {
      sel.innerHTML = proveedores.map(p => `<option value="${p.id}">${p.razonSocial} (RUC: ${p.ruc})</option>`).join("");
    }
  } catch (e) {
    console.error("Error cargando proveedores:", e);
  }
}

async function cargarCompras() {
  const t0 = performance.now();
  try {
    const res = await fetch(`${getBaseUrl()}/compras`);
    const data = await res.json();
    const timeMs = Math.round(performance.now() - t0);
    renderComprasTable(Array.isArray(data) ? data : []);
  } catch (e) {
    console.error("Error cargando compras:", e);
  }
}

async function cargarResumenCompras() {
  const t0 = performance.now();
  try {
    const res = await fetch(`${getBaseUrl()}/compras/resumen`);
    if (!res.ok) return;
    const data = await res.json();
    const timeMs = Math.round(performance.now() - t0);

    const cCount = document.getElementById("cKpiCount");
    const cComprado = document.getElementById("cKpiComprado");
    const cPagado = document.getElementById("cKpiPagado");
    const cSaldo = document.getElementById("cKpiSaldo");
    const cBox = document.getElementById("cResponseBox");

    if (cCount) cCount.textContent = data.totalCompras || 0;
    if (cComprado) cComprado.textContent = `S/ ${(data.montoTotalComprado || 0).toFixed(2)}`;
    if (cPagado) cPagado.textContent = `S/ ${(data.montoTotalPagado || 0).toFixed(2)}`;
    if (cSaldo) cSaldo.textContent = `S/ ${(data.saldoPendienteTotal || 0).toFixed(2)}`;
    if (cBox) cBox.textContent = JSON.stringify(data, null, 2);
  } catch (e) {
    console.error("Error cargando resumen compras:", e);
  }
}

function renderComprasTable(compras) {
  const tbody = document.getElementById("comprasTbody");
  if (!tbody) return;
  if (!compras || compras.length === 0) {
    tbody.innerHTML = '<tr class="empty-row"><td colspan="10">No hay compras registradas</td></tr>';
    return;
  }

  tbody.innerHTML = compras.map(c => {
    const estadoClass = c.estado === 'PAGADA' ? 'status-registrada' : (c.estado === 'ANULADA' ? 'status-anulada' : 'status-pendiente');
    return `<tr>
      <td>#${c.id}</td>
      <td><strong>${c.numeroComprobante}</strong></td>
      <td>${c.proveedor ? c.proveedor.razonSocial : '--'}</td>
      <td>${c.fechaEmision || '--'}</td>
      <td>S/ ${(c.subtotal || 0).toFixed(2)}</td>
      <td>S/ ${(c.igv || 0).toFixed(2)}</td>
      <td><strong>S/ ${(c.total || 0).toFixed(2)}</strong></td>
      <td><span style="color: ${c.saldoPendiente > 0 ? '#dc2626' : '#059669'}; font-weight: 700;">S/ ${(c.saldoPendiente || 0).toFixed(2)}</span></td>
      <td><span class="badge ${estadoClass}">${c.estado}</span></td>
      <td>
        ${c.estado !== 'ANULADA' ? `<button class="secondary-btn sm-btn" onclick="anularCompra(${c.id})">Anular</button>` : '<span style="color:#64748b; font-size:11px;">Anulada</span>'}
      </td>
    </tr>`;
  }).join("");
}

async function anularCompra(id) {
  if (!confirm(`¿Confirmas la anulación de la compra #${id}?`)) return;
  try {
    const res = await fetch(`${getBaseUrl()}/compras/${id}/anular`, { method: "POST" });
    if (res.ok) {
      showToast(`Compra #${id} anulada exitosamente`, "success");
      cargarCompras();
      cargarResumenCompras();
    } else {
      const err = await res.json();
      showToast(err.message || "Error anulando compra", "error");
    }
  } catch (e) {
    showToast("Error de conexión al anular compra", "error");
  }
}

function initComprasHandlers() {
  const compraForm = document.getElementById("compraForm");
  if (compraForm) {
    compraForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const payload = {
        numeroComprobante: document.getElementById("cComprobante").value.trim(),
        proveedorId: parseInt(document.getElementById("cProveedor").value),
        fechaEmision: new Date().toISOString().split("T")[0],
        detalles: [
          {
            insumoId: 1,
            nombreInsumo: document.getElementById("cInsumoNombre").value.trim(),
            cantidad: parseFloat(document.getElementById("cCantidad").value),
            precioUnitario: parseFloat(document.getElementById("cPrecio").value)
          }
        ]
      };

      try {
        const res = await fetch(`${getBaseUrl()}/compras`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (res.ok) {
          showToast(`Compra registrada: ${data.numeroComprobante}`, "success");
          compraForm.reset();
          cargarCompras();
          cargarResumenCompras();
        } else {
          showToast(data.message || "Error al registrar compra", "error");
        }
      } catch (err) {
        showToast("Error de conexión con la API de Compras", "error");
      }
    });
  }

  const provForm = document.getElementById("proveedorForm");
  if (provForm) {
    provForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const payload = {
        ruc: document.getElementById("provRuc").value.trim(),
        razonSocial: document.getElementById("provRazon").value.trim(),
        telefono: "966123456",
        email: "contacto@proveedor.pe",
        direccion: "Lima, Perú"
      };

      try {
        const res = await fetch(`${getBaseUrl()}/compras/proveedores`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (res.ok) {
          showToast(`Proveedor registrado: ${data.razonSocial}`, "success");
          provForm.reset();
          cargarProveedores();
        } else {
          showToast(data.message || "Error al registrar proveedor", "error");
        }
      } catch (err) {
        showToast("Error al conectar con la API", "error");
      }
    });
  }

  const amortizarForm = document.getElementById("amortizarForm");
  if (amortizarForm) {
    amortizarForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const id = document.getElementById("amIdCompra").value.trim();
      const monto = parseFloat(document.getElementById("amMonto").value);

      try {
        const res = await fetch(`${getBaseUrl()}/compras/${id}/amortizar`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ montoPago: monto })
        });
        const data = await res.json();
        if (res.ok) {
          showToast(`Abono realizado. Nuevo saldo: S/ ${(data.saldoPendiente || 0).toFixed(2)}`, "success");
          amortizarForm.reset();
          cargarCompras();
          cargarResumenCompras();
        } else {
          showToast(data.message || "Error al amortizar compra", "error");
        }
      } catch (err) {
        showToast("Error de conexión al amortizar", "error");
      }
    });
  }

  const btnRefreshC = document.getElementById("btnRefreshCompras");
  if (btnRefreshC) {
    btnRefreshC.addEventListener("click", () => {
      cargarCompras();
      cargarResumenCompras();
      showToast("Compras actualizadas", "success");
    });
  }
}

// --------------------------------------------------------
// MODULO PRODUCCIÓN (Isaí Armuto)
// --------------------------------------------------------
async function cargarProductosProduccion() {
  try {
    const res = await fetch(`${getBaseUrl()}/productos`);
    if (!res.ok) return;
    const productos = await res.json();
    const sel = document.getElementById("pProducto");
    if (sel) {
      sel.innerHTML = productos.map(p => `<option value="${p.id}" data-name="${p.nombre}">${p.nombre} (Stock actual: ${p.stock})</option>`).join("");
    }
  } catch (e) {
    console.error("Error cargando productos para producción:", e);
  }
}

async function cargarOrdenes() {
  try {
    const res = await fetch(`${getBaseUrl()}/produccion/ordenes`);
    const data = await res.json();
    renderOrdenesTable(Array.isArray(data) ? data : []);
  } catch (e) {
    console.error("Error cargando órdenes:", e);
  }
}

async function cargarResumenProduccion() {
  try {
    const res = await fetch(`${getBaseUrl()}/produccion/resumen`);
    if (!res.ok) return;
    const data = await res.json();

    const pCount = document.getElementById("pKpiCount");
    const pProg = document.getElementById("pKpiProgramado");
    const pProd = document.getElementById("pKpiProducido");
    const pEfic = document.getElementById("pKpiEficiencia");
    const pBox = document.getElementById("pResponseBox");

    if (pCount) pCount.textContent = data.totalOrdenes || 0;
    if (pProg) pProg.textContent = `${data.totalCantidadProgramada || 0} Und`;
    if (pProd) pProd.textContent = `${data.totalCantidadProducida || 0} Und`;
    if (pEfic) pEfic.textContent = `${(data.porcentajeEficiencia || 0).toFixed(1)}%`;
    if (pBox) pBox.textContent = JSON.stringify(data, null, 2);
  } catch (e) {
    console.error("Error cargando resumen producción:", e);
  }
}

function renderOrdenesTable(ordenes) {
  const tbody = document.getElementById("ordenesTbody");
  if (!tbody) return;
  if (!ordenes || ordenes.length === 0) {
    tbody.innerHTML = '<tr class="empty-row"><td colspan="8">No hay órdenes registradas</td></tr>';
    return;
  }

  tbody.innerHTML = ordenes.map(o => {
    const estadoClass = o.estado === 'COMPLETADA' ? 'status-registrada' : (o.estado === 'CANCELADA' ? 'status-anulada' : 'status-pendiente');
    return `<tr>
      <td>#${o.id}</td>
      <td><strong>${o.codigoOrden}</strong></td>
      <td>${o.nombreProducto}</td>
      <td>${o.cantidadProgramada} Und</td>
      <td>${o.cantidadProducida != null ? o.cantidadProducida + ' Und' : '--'}</td>
      <td>${o.fechaInicio ? o.fechaInicio.split("T")[0] : '--'}</td>
      <td><span class="badge ${estadoClass}">${o.estado}</span></td>
      <td>
        ${o.estado === 'PLANIFICADA' ? `<button class="secondary-btn sm-btn" onclick="iniciarOrden(${o.id})">Iniciar</button> ` : ''}
        ${o.estado === 'EN_PROCESO' ? `<button class="primary-btn sm-btn" onclick="prepararCompletar(${o.id}, ${o.cantidadProgramada})">Completar</button> ` : ''}
        ${(o.estado === 'PLANIFICADA' || o.estado === 'EN_PROCESO') ? `<button class="outline-btn sm-btn" onclick="cancelarOrden(${o.id})">Cancelar</button>` : ''}
      </td>
    </tr>`;
  }).join("");
}

async function iniciarOrden(id) {
  try {
    const res = await fetch(`${getBaseUrl()}/produccion/ordenes/${id}/iniciar`, { method: "PUT" });
    if (res.ok) {
      showToast(`Orden #${id} iniciada en planta`, "success");
      cargarOrdenes();
      cargarResumenProduccion();
    } else {
      const err = await res.json();
      showToast(err.message || "Error al iniciar orden", "error");
    }
  } catch (e) {
    showToast("Error de conexión al iniciar orden", "error");
  }
}

function prepararCompletar(id, cantidad) {
  const idInput = document.getElementById("cmpIdOrden");
  const cantInput = document.getElementById("cmpCantidad");
  const venceInput = document.getElementById("cmpVence");
  if (idInput) idInput.value = id;
  if (cantInput) cantInput.value = cantidad;
  if (venceInput) {
    const d = new Date();
    d.setFullYear(d.getFullYear() + 1);
    venceInput.value = d.toISOString().split("T")[0];
  }
  showToast(`Datos de orden #${id} cargados en formulario de cierre`, "info");
}

async function cancelarOrden(id) {
  if (!confirm(`¿Confirmas la cancelación de la orden #${id}?`)) return;
  try {
    const res = await fetch(`${getBaseUrl()}/produccion/ordenes/${id}/cancelar`, { method: "POST" });
    if (res.ok) {
      showToast(`Orden #${id} cancelada`, "success");
      cargarOrdenes();
      cargarResumenProduccion();
    } else {
      const err = await res.json();
      showToast(err.message || "Error al cancelar orden", "error");
    }
  } catch (e) {
    showToast("Error de conexión al cancelar orden", "error");
  }
}

function initProduccionHandlers() {
  const ordenForm = document.getElementById("ordenForm");
  if (ordenForm) {
    ordenForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const sel = document.getElementById("pProducto");
      const prodId = parseInt(sel.value);
      const prodNombre = sel.options[sel.selectedIndex].getAttribute("data-name") || "Producto Agroindustrial";
      const cant = parseInt(document.getElementById("pCantidad").value);

      const payload = {
        productoId: prodId,
        nombreProducto: prodNombre,
        cantidadProgramada: cant,
        observaciones: document.getElementById("pObs").value.trim(),
        insumos: [
          {
            insumoId: 9,
            nombreInsumo: "Bolsa Bilaminada al Vacío 1kg",
            cantidadRequerida: cant,
            cantidadConsumida: cant,
            unidadMedida: "UNIDADES"
          }
        ]
      };

      try {
        const res = await fetch(`${getBaseUrl()}/produccion/ordenes`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (res.ok) {
          showToast(`Orden creada: ${data.codigoOrden}`, "success");
          ordenForm.reset();
          cargarOrdenes();
          cargarResumenProduccion();
        } else {
          showToast(data.message || "Error al crear orden", "error");
        }
      } catch (err) {
        showToast("Error de conexión con Producción", "error");
      }
    });
  }

  const completarForm = document.getElementById("completarOrdenForm");
  if (completarForm) {
    completarForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const id = document.getElementById("cmpIdOrden").value.trim();
      const payload = {
        cantidadProducida: parseInt(document.getElementById("cmpCantidad").value),
        fechaVencimiento: document.getElementById("cmpVence").value
      };

      try {
        const res = await fetch(`${getBaseUrl()}/produccion/ordenes/${id}/completar`, {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (res.ok) {
          showToast(`Orden completada! Stock incrementado y Lote FEFO emitido.`, "success");
          completarForm.reset();
          cargarOrdenes();
          cargarResumenProduccion();
        } else {
          showToast(data.message || "Error al completar orden", "error");
        }
      } catch (err) {
        showToast("Error de conexión al completar orden", "error");
      }
    });
  }

  const btnRefreshO = document.getElementById("btnRefreshOrdenes");
  if (btnRefreshO) {
    btnRefreshO.addEventListener("click", () => {
      cargarOrdenes();
      cargarResumenProduccion();
      showToast("Órdenes actualizadas", "success");
    });
  }
}

// --------------------------------------------------------
// MODULO FINANZAS & CAJA (Brandon Ccalla)
// --------------------------------------------------------
async function cargarEstadoCaja() {
  try {
    const res = await fetch(`${getBaseUrl()}/caja/activa`);
    const estadoEl = document.getElementById("fKpiEstado");
    const saldoEl = document.getElementById("fKpiSaldo");
    const crId = document.getElementById("crIdSesion");

    if (res.ok) {
      const sesion = await res.json();
      if (estadoEl) {
        estadoEl.textContent = `ABIERTA (ID: #${sesion.id})`;
        estadoEl.parentElement.className = "kpi-card highlight-green";
      }
      if (saldoEl) saldoEl.textContent = `S/ ${(sesion.saldoTeorico || 0).toFixed(2)}`;
      if (crId) crId.value = sesion.id;
    } else {
      if (estadoEl) {
        estadoEl.textContent = "CERRADA";
        estadoEl.parentElement.className = "kpi-card highlight-danger";
      }
      if (saldoEl) saldoEl.textContent = "S/ 0.00";
      if (crId) crId.value = "";
    }
  } catch (e) {
    console.error("Error verificando caja activa:", e);
  }
}

async function cargarHistorialCaja() {
  try {
    const res = await fetch(`${getBaseUrl()}/caja/historial`);
    const data = await res.json();
    renderCajaTable(Array.isArray(data) ? data : []);
  } catch (e) {
    console.error("Error cargando historial caja:", e);
  }
}

async function cargarResumenCaja() {
  try {
    const res = await fetch(`${getBaseUrl()}/caja/resumen`);
    if (!res.ok) return;
    const data = await res.json();

    const fIng = document.getElementById("fKpiIngresos");
    const fEgr = document.getElementById("fKpiEgresos");
    const fBox = document.getElementById("fResponseBox");

    if (fIng) fIng.textContent = `S/ ${(data.totalIngresos || 0).toFixed(2)}`;
    if (fEgr) fEgr.textContent = `S/ ${(data.totalEgresos || 0).toFixed(2)}`;
    if (fBox) fBox.textContent = JSON.stringify(data, null, 2);
  } catch (e) {
    console.error("Error cargando resumen caja:", e);
  }
}

function renderCajaTable(sesiones) {
  const tbody = document.getElementById("cajaTbody");
  if (!tbody) return;
  if (!sesiones || sesiones.length === 0) {
    tbody.innerHTML = '<tr class="empty-row"><td colspan="9">No hay sesiones de caja registradas</td></tr>';
    return;
  }

  tbody.innerHTML = sesiones.map(s => {
    const estadoClass = s.estado === 'ABIERTA' ? 'status-registrada' : 'status-anulada';
    return `<tr>
      <td>#${s.id}</td>
      <td><strong>${s.cajeroNombre}</strong></td>
      <td>${s.fechaApertura ? s.fechaApertura.replace("T", " ").substring(0, 16) : '--'}</td>
      <td>${s.fechaCierre ? s.fechaCierre.replace("T", " ").substring(0, 16) : '<span style="color:#059669; font-weight:700;">En curso</span>'}</td>
      <td>S/ ${(s.montoApertura || 0).toFixed(2)}</td>
      <td>S/ ${(s.saldoTeorico || 0).toFixed(2)}</td>
      <td>${s.saldoReal != null ? 'S/ ' + s.saldoReal.toFixed(2) : '--'}</td>
      <td>${s.diferencia != null ? `<span style="color:${s.diferencia < 0 ? '#dc2626' : '#059669'};">S/ ${s.diferencia.toFixed(2)}</span>` : '--'}</td>
      <td><span class="badge ${estadoClass}">${s.estado}</span></td>
    </tr>`;
  }).join("");
}

function initCajaHandlers() {
  const movForm = document.getElementById("movimientoForm");
  if (movForm) {
    movForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const payload = {
        tipo: document.getElementById("mTipo").value,
        monto: parseFloat(document.getElementById("mMonto").value),
        concepto: document.getElementById("mConcepto").value.trim(),
        metodoPago: document.getElementById("mMetodo").value,
        referencia: document.getElementById("mRef").value.trim()
      };

      try {
        const res = await fetch(`${getBaseUrl()}/caja/movimientos`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (res.ok) {
          showToast(`Movimiento registrado: S/ ${data.monto.toFixed(2)} (${data.tipo})`, "success");
          movForm.reset();
          cargarEstadoCaja();
          cargarHistorialCaja();
          cargarResumenCaja();
        } else {
          showToast(data.message || "Error al registrar movimiento", "error");
        }
      } catch (err) {
        showToast("Error de conexión con Caja", "error");
      }
    });
  }

  const apForm = document.getElementById("aperturaForm");
  if (apForm) {
    apForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const payload = {
        cajeroNombre: document.getElementById("apCajero").value.trim(),
        montoApertura: parseFloat(document.getElementById("apMonto").value)
      };

      try {
        const res = await fetch(`${getBaseUrl()}/caja/apertura`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (res.ok) {
          showToast(`Turno abierto con éxito (ID: #${data.id})`, "success");
          apForm.reset();
          cargarEstadoCaja();
          cargarHistorialCaja();
          cargarResumenCaja();
        } else {
          showToast(data.message || "Error al abrir turno", "error");
        }
      } catch (err) {
        showToast("Error de conexión al abrir turno", "error");
      }
    });
  }

  const crForm = document.getElementById("cierreForm");
  if (crForm) {
    crForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const id = document.getElementById("crIdSesion").value.trim();
      if (!id) {
        showToast("No hay ninguna sesión abierta para cerrar", "error");
        return;
      }
      const payload = {
        saldoReal: parseFloat(document.getElementById("crSaldoReal").value),
        observaciones: document.getElementById("crObs").value.trim()
      };

      try {
        const res = await fetch(`${getBaseUrl()}/caja/${id}/cierre`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(payload)
        });
        const data = await res.json();
        if (res.ok) {
          showToast(`Caja cerrada. Diferencia asentada: S/ ${(data.diferencia || 0).toFixed(2)}`, "success");
          crForm.reset();
          cargarEstadoCaja();
          cargarHistorialCaja();
          cargarResumenCaja();
        } else {
          showToast(data.message || "Error al cerrar caja", "error");
        }
      } catch (err) {
        showToast("Error de conexión al cerrar caja", "error");
      }
    });
  }

  const btnRefreshCaja = document.getElementById("btnRefreshCaja");
  if (btnRefreshCaja) {
    btnRefreshCaja.addEventListener("click", () => {
      cargarEstadoCaja();
      cargarHistorialCaja();
      cargarResumenCaja();
      showToast("Historial de caja actualizado", "success");
    });
  }
}
