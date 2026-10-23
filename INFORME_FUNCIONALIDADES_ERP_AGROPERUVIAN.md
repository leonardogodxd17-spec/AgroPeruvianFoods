# Informe Técnico y Funcional del ERP Agro Peruvian Foods S.A.C.
**Proyecto Integrador Modular (ADS | BD2 | LP2)**  
*Equipo: Los Hijos del Backend - Sección G2G3*

---

## 1. Visión General del Sistema
El **ERP Agro Peruvian Foods S.A.C.** es una plataforma empresarial integral desarrollada bajo los estándares de arquitectura de software moderna y buenas prácticas de ingeniería de datos. Gestiona la cadena de valor completa de una empresa agroindustrial dedicada a la producción y comercialización de derivados de **cacao criollo chuncho** y **aceite de coco virgen prensado en frío**.

### Stack Tecnológico
* **Backend:** Java 21 LTS, Spring Boot 4.0.7, Spring Modulith, Spring Data JPA, Hibernate ORM, Bean Validation (Jakarta).
* **Persistencia:** Oracle Database Free 23ai / H2 In-Memory Database (con secuencias automáticas e integridad referencial).
* **Seguridad e Identidad:** Keycloak 26.8.0 (OAuth 2.0 / OpenID Connect), Spring Security (OAuth2 Resource Server), JWT RSA-256.
* **Frontend SPA:** Angular (v19/22), TypeScript, Signals reactivos, Zoneless Change Detection, Web Crypto API nativa, CSS Vanilla y tokens de diseño corporativo.
* **Trazabilidad:** Interceptor HTTP con inyección automática de cabeceras `X-Trace-ID` y `Authorization: Bearer`.

---

## 2. Mapa Integral de Módulos y Funcionalidades

```
                             ┌───────────────────────────────────────┐
                             │       ERP AGRO PERUVIAN FOODS         │
                             └───────────────────┬───────────────────┘
                                                 │
         ┌───────────────────┬───────────────────┼───────────────────┬───────────────────┐
         │                   │                   │                   │                   │
         ▼                   ▼                   ▼                   ▼                   ▼
 ┌───────────────┐   ┌───────────────┐   ┌───────────────┐   ┌───────────────┐   ┌───────────────┐
 │   FINANZAS    │   │  VENTAS POS   │   │  PRODUCCIÓN   │   │    COMPRAS    │   │   CATÁLOGO    │
 │    Y CAJA     │   │ (TRANSACT.)   │   │   BOM/FEFO    │   │  PROVEEDORES  │   │  (MAESTROS)   │
 └───────────────┘   └───────────────┘   └───────────────┘   └───────────────┘   └───────────────┘
```

---

### Módulo 1: Finanzas & Gestión de Caja (`caja`)
* **Responsable:** Brandon Andree Ccalla Colquehuanca
* **Tipo:** Transaccional (Cabecera: `SesionCaja` $\to$ Detalle: `MovimientoCaja`)

#### Funcionalidades Principales:
1. **Apertura de Turno de Caja:**
   - Registro de sesión de caja asignada al cajero/vendedor activo.
   - Declaración de monto de apertura (fondo inicial en efectivo) y timestamp de inicio.
2. **Registro de Movimientos Transaccionales:**
   - **Ingresos:** Cobro de ventas mostrador en efectivo, billeteras digitales (Yape/Plin) o transferencias bancarias con referencia de ticket.
   - **Egresos:** Salidas de caja chica autorizadas para compra de insumos menores, suministros de empaque y limpieza con número de recibo.
3. **Arqueo y Cierre de Caja:**
   - **Cálculo Automático de Saldo Teórico:**  
     $$\text{Saldo Teórico} = \text{Monto Apertura} + \sum \text{Ingresos} - \sum \text{Egresos}$$
   - Registro de saldo real físico contado por el cajero.
   - Determinación automática de diferencias de arqueo: **Cuadrada** ($\Delta = 0$), **Faltante** ($\Delta < 0$) o **Sobrante** ($\Delta > 0$).
4. **Historial y Reporte de Tesorería:**
   - Consulta de la sesión actualmente activa.
   - Reporte consolidado de turnos de caja y auditoría financiera.

---

### Módulo 2: Ventas POS Transaccionales (`ventas`)
* **Responsable:** Zaggy Leonardo Morales Auquipata
* **Tipo:** Transaccional (Cabecera: `Venta` $\to$ Detalle: `DetalleVenta`)

#### Funcionalidades Principales:
1. **Formulario de Venta Dinámico (FormArray):**
   - Agregado, modificación y eliminación fluida de múltiples líneas de detalle de venta en tiempo real.
   - Selector reactivo con autocompletado en vivo de productos del catálogo.
2. **Trazabilidad por Lotes con Criterio FEFO (*First Expired, First Out*):**
   - Asignación inteligente del lote de producción disponible más próximo a vencer para garantizar la frescura de los superfoods y derivados.
3. **Cálculos Transaccionales Atómicos:**
   - Cálculo automático por línea: $\text{Subtotal} = \text{Cantidad} \times \text{Precio Unitario}$.
   - Desglose contable peruano: Subtotal neto, IGV (18%) y Total general en Soles (S/).
4. **Generación de Ticket y Afectación de Inventario:**
   - Asignación automática de número de ticket único correlativo (ej. `TKT-20261021-001`).
   - Descuento inmediato y atómico de stock tanto en el catálogo general como en el lote asignado.
5. **Aislamiento Multi-Tenant por Vendedor (RN5 / RN8):**
   - El backend extrae automáticamente el `sub` (UUID) del vendedor autenticado desde el JWT de Keycloak.
   - **RN8:** Al ingresar al reporte de ventas, un `VENDEDOR` solo puede visualizar y consultar sus propias ventas. Las ventas ajenas son inaccesibles.
6. **Anulación Segura y Reversión de Stock (RN4):**
   - Acción restringida exclusivamente a usuarios con rol `SUPERVISOR` o `ADMIN`.
   - Al anular una venta registrada, el sistema revierte el estado a `ANULADA` y repone automáticamente las unidades al inventario. Los vendedores tienen el botón oculto con la etiqueta *Solo Supervisor*.
7. **Reporte Agregado:**
   - Métricas en tiempo real: número total de ventas, volumen total facturado, ticket promedio y filtros por rango de fechas y estado (`REGISTRADA` / `ANULADA`).

---

### Módulo 3: Producción & Recetas BOM (`produccion`)
* **Responsable:** Isaí Armuto Uñapilco
* **Tipo:** Transaccional (Cabecera: `OrdenProduccion` $\to$ Detalle: `DetalleConsumoInsumo` + `LoteProducto`)

#### Funcionalidades Principales:
1. **Órdenes de Trabajo con Lista de Materiales (BOM):**
   - Planificación de lotes de transformación (ej. extracción de aceite de coco virgen prensado en frío, conchado de chocolate bitter al 70%, tostado y descascarillado de nibs).
   - Definición de insumos y materiales requeridos (granos de cacao, cocos frescos, frascos de vidrio, cajas).
2. **Ciclo de Estados de Producción:**
   - Flujo controlado: `PLANIFICADA` $\longrightarrow$ `EN_PROCESO` $\longrightarrow$ `COMPLETADA` (o `CANCELADA`).
3. **Liquidación de Insumos y Consumo Real:**
   - Registro de mermas, cantidad requerida vs cantidad consumida por cada insumo del proceso.
4. **Generación de Lotes FEFO:**
   - Al completar la orden, el sistema da de alta el producto terminado y genera un lote con:
     - Código de lote (ej. `LOT-COC-20261011-01`).
     - Stock disponible inicial.
     - Fecha de producción y fecha de caducidad.
5. **API de Despacho FEFO:**
   - Endpoint `/api/v1/produccion/lotes/producto/{id}/fefo` que ordena los lotes de menor a mayor fecha de expiración para su uso inmediato en el módulo de ventas.

---

### Módulo 4: Compras & Insumos (`compras`)
* **Responsable:** Elisban Huaylla Nestor
* **Tipo:** Transaccional (Cabecera: `Compra` $\to$ Detalle: `DetalleCompra` + `Proveedor`)

#### Funcionalidades Principales:
1. **Directorio Maestro de Proveedores:**
   - Gestión de proveedores agrícolas (cooperativas cacaoteras de Quillabamba, productores de coco de Sullana, proveedores de empaques del Callao).
   - Validación de RUC (11 dígitos), razón social, dirección, teléfono y correo electrónico.
2. **Registro de Compras de Materias Primas:**
   - Registro de comprobante fiscal (Factura/Guía de remisión).
   - Detalle de insumos comprados con cantidad, precio unitario y cálculo de IGV.
3. **Control de Cuentas por Pagar (Pasivos):**
   - Seguimiento del estado financiero de cada compra: `REGISTRADA` (con saldo pendiente) o `PAGADA`.
   - Control de saldo pendiente de amortización.
4. **Amortización y Liquidación de Pagos:**
   - Registro de abonos parciales o liquidación total del saldo de una factura.
5. **Anulación de Compras:**
   - Cancelación de comprobantes y anulación de compromisos pendientes de pago.

---

### Módulo Transversal: Catálogo de Productos y Categorías (`catalogo`)
* **Tipo:** Catálogo Maestro Dependiente (S07 / S08)

#### Funcionalidades Principales:
1. **Categorías Maestras (S07):**
   - Granos Andinos, Superfoods y Frutas, Insumos Agrícolas, Materiales de Empaque, Derivados de Cacao, Aceites y Derivados de Coco.
2. **Productos Agroindustriales (S08) - Catálogo Oficial Agro Peruvian Foods:**
   - **Aceite de Coco Virgen (Prensado en Frío):** Balde 4L (S/ 200.00), Balde 20L (S/ 730.00), Frasco 1L (S/ 65.00), Frasco 460ml (S/ 35.00), Frasco 250ml (S/ 20.00), Frasco 120ml (S/ 12.00), Frasco 30ml (S/ 5.00), y presentaciones mayoristas en bandejas de 24 y 36 unidades.
   - **Derivados de Coco:** Coco Rallado Doypack Hebra Larga 500g (S/ 16.00) y 250g (S/ 8.00).
   - **Derivados de Cacao:** Cacao Grano Entero Sin Tostar Aroma Intenso 500g (S/ 14.00) y 1kg (S/ 28.00).
   - **Columna de Vista de Producto (Foto/Miniatura):** Visualización de la fotografía real de cada producto al lado de su identificador ID, con efecto hover de micro-animación y zoom (`scale(1.18)`).
   - **Modal Interactivo de Vista Ampliada:** Diálogo modal con backdrop difuminado (`backdrop-filter: blur(4px)`) para inspeccionar la imagen a alta resolución, precio de venta, categoría y stock en almacén al hacer clic en cualquier miniatura.
   - Filtro interactivo de productos por categoría tanto en cliente como en servidor.
   - Indicadores visuales de stock crítico (alertas cuando el stock es menor o igual a 10 unidades).
3. **Control de Accesos por Rol (RBAC):**
   - **Lectura:** Disponible para cualquier usuario autenticado (`VENDEDOR`, `SUPERVISOR`, `ADMIN`).
   - **Escritura (Crear, Editar, Eliminar):** Exclusivo para el rol `ADMIN`. Para otros roles, la interfaz oculta los botones de acción y muestra la etiqueta protectora `Lectura`.

---

### Módulo de Seguridad e Identidad Corporativa (S10b / S11)
* **Arquitectura:** Keycloak 26.8.0 + OAuth2 Resource Server + Angular PKCE Client

#### Funcionalidades Principales:
1. **Flujo de Autenticación Authorization Code + PKCE (RFC 7636 y RFC 10017):**
   - Autenticación 100% delegada en Keycloak (`http://localhost:38080/realms/bomerp`).
   - Ninguna contraseña pasa por el frontend de la aplicación.
   - Generación de `code_verifier`, `code_challenge` (S256) y validación de `state` anti-CSRF mediante Web Crypto API.
   - Eliminación de *Direct Access Grants* en el cliente de producción para prevenir riesgos de credenciales expuestas (*RFC 9700*).
2. **Almacenamiento de Tokens Seguro (RFC 10017):**
   - Token almacenado en memoria mediante Signals reactivos (`sesion = signal(...)`).
   - Espejo en `sessionStorage` para mantener la sesión ante recargas de página (`F5`) dentro de la pestaña.
   - **Prohibido `localStorage`:** Evita persistencia trans-pestaña o posterior al cierre de navegador, reduciendo drásticamente la exposición a ataques XSS.
3. **Interceptor HTTP con Auto-Renovación Silenciosa:**
   - Inyección automática del token `Authorization: Bearer <token>` en cada solicitud HTTP saliente.
   - Detección de error `401 Unauthorized` (token expirado tras 5 minutos): ejecuta llamada atómica a Keycloak con `refresh_token`, actualiza la sesión y reintenta la petición original **una única vez**.
   - Detección de error `403 Forbidden` (permiso insuficiente): el error se propaga inmediatamente sin reintentar innecesariamente.
4. **Guards Funcionales de Angular:**
   - `authGuard`: Redirección automática al login de Keycloak si no existe sesión activa, preservando la URL de retorno solicitada.
   - `rolGuard(...roles)`: Valida si el usuario posee los roles requeridos para una ruta de escritura. En caso negativo, redirige a la pantalla `/sin-permiso`.
5. **Menú Dinámico e Interfaz Adaptativa:**
   - Topbar con display de nombre de usuario, avatar, lista de roles y botón de cierre de sesión real (*RP-Initiated Logout*).
   - Sidebar que solo despliega las opciones para las cuales el usuario cuenta con permisos autorizados.

---

## 3. Matriz de Roles y Permisos por Pantalla

| Módulo / Funcionalidad | Ruta Frontend | `VENDEDOR` | `SUPERVISOR` | `ADMIN` |
| :--- | :--- | :---: | :---: | :---: |
| **Dashboard General** | `/` | Lectura | Lectura | Lectura |
| **Sesión y Movimientos de Caja** | `/caja` | Oculto | **Acceso Total** | **Acceso Total** |
| **Reporte de Ventas** | `/ventas/reporte` | Solo sus ventas | **Ventas Globales** | **Ventas Globales** |
| **Anular Venta Registrada** | `/ventas/reporte` | ⛔ Bloqueado | **Permitido** | **Permitido** |
| **Nueva Venta (FormArray)** | `/ventas/nueva` | **Permitido** | **Permitido** | Redirige `/sin-permiso` |
| **Órdenes de Producción** | `/produccion` | Oculto | **Acceso Total** | **Acceso Total** |
| **Nueva Orden BOM** | `/produccion/nueva` | Oculto | Oculto | **Permitido** |
| **Consultar Categorías/Productos** | `/catalogo/...` | Lectura | Lectura | Lectura |
| **Crear/Editar/Eliminar Catálogo** | `/catalogo/...` | ⛔ Bloqueado | ⛔ Bloqueado | **Permitido** |
| **Compras e Insumos** | `/compras` | Oculto | **Acceso Total** | **Acceso Total** |
| **Registrar Proveedor** | `/compras/proveedores/nuevo` | Oculto | Oculto | **Permitido** |

---

## 4. Guía de Ejecución Rápida

### Credenciales de Prueba (Keycloak Realm: `bomerp`)
*Contraseña universal:* **`Bomerp2026!`**

- **Vendedor 1:** `vendedor1` / `Bomerp2026!` (Rol: `VENDEDOR`)
- **Vendedor 2:** `vendedor2` / `Bomerp2026!` (Rol: `VENDEDOR`)
- **Supervisor:** `supervisor` / `Bomerp2026!` (Rol: `SUPERVISOR`)
- **Administrador:** `admin` / `Bomerp2026!` (Rol: `ADMIN`)

### URLs de Servicios
- **Frontend SPA:** [http://localhost:4200](http://localhost:4200)
- **Backend Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Keycloak Console:** [http://localhost:38080](http://localhost:38080) (`admin` / `admin123`)
