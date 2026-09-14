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
