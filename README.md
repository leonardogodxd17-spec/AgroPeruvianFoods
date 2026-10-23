# ERP Agro Peruvian Foods

**Proyecto Integrador Modular (ADS | BD2 | LP2)**  
*Equipo: Los Hijos del Backend - Sección G2G3*

---

## 📌 Descripción del Proyecto
Plataforma empresarial para la gestión integral de la elaboración y comercialización de derivados de cacao y aceite de coco prensado en frío. Unifica ventas en mostrador/ferias, arqueo estricto de caja, abastecimiento y trazabilidad por lotes con criterio **FEFO** (*First Expired, First Out*).

---

## 🏛️ Arquitectura Modular (Spring Modulith)
Construido como un monolito modular sobre **Java 21 LTS** y **Spring Boot 4.0.7**, con persistencia en **Oracle Database Free 23ai / H2 In-Memory**:

| Módulo | Responsable | Tipo | Transacción Cabecera - Detalle |
| :--- | :--- | :---: | :--- |
| **`caja`** | Brandon Andree Ccalla Colquehuanca | Transaccional | `SesionCaja` $\to$ `MovimientoCaja` |
| **`ventas`** | Zaggy Leonardo Morales Auquipata | Transaccional | `Venta` $\to$ `DetalleVenta` (RN5 / RN8 con Keycloak) |
| **`produccion`** | Isai Armuto Uñapilco | Transaccional | `OrdenProduccion` $\to$ `DetalleConsumoInsumo` (+ `LoteProducto`) |
| **`compras`** | Elisban Huaylla Nestor | Transaccional | `Compra` $\to$ `DetalleCompra` (+ `Proveedor`) |
| **`catalogo`** | Común | Maestro | `Producto`, `Categoria` |

---

## 🔐 Seguridad e Identidad con Keycloak (S10b)
El backend opera como un **OAuth2 Resource Server** conectado al proveedor de identidad **Keycloak 26.8.0**:
- **Keycloak Host:** `http://localhost:38080`
- **Realm:** `bomerp` (Discovery: `http://localhost:38080/realms/bomerp/.well-known/openid-configuration`)
- **Cliente:** `bomerp-frontend` (*Public Client* para SPA Angular y pruebas directas)
- **Aislamiento por Vendedor (RN5/RN8)**: `vendedorId` extraído del claim `sub` (UUID) del token firmado por Keycloak. Acceso a ventas ajenas responde `403 Forbidden`.
- **Roles de Realm**: `VENDEDOR`, `SUPERVISOR`, `ADMIN` (claims en `realm_access.roles`).

### Usuarios Semilla en Keycloak (Contraseña: `Bomerp2026!`)
- `vendedor1` (`vendedor1@bomerp.com`) $\to$ Rol `VENDEDOR`
- `vendedor2` (`vendedor2@bomerp.com`) $\to$ Rol `VENDEDOR`
- `supervisor` (`supervisor@bomerp.com`) $\to$ Rol `SUPERVISOR`
- `admin` (`admin@bomerp.com`) $\to$ Rol `ADMIN`

---

## 🛡️ Seguridad Frontend con Keycloak (S11)
La SPA Angular (`bomerp-frontend`) implementa el estándar **OAuth 2.0 / OpenID Connect** mediante el flujo **Authorization Code con PKCE**:
- **Flujo Authorization Code + PKCE (RFC 7636 y RFC 10017):**
  - Autenticación delegada en Keycloak (sin formularios de contraseña en el código de la SPA).
  - Parámetros `code_verifier`, `code_challenge` (S256) y `state` anti-CSRF generados con Web Crypto API.
  - Intercambio de código por tokens en `/auth/callback`.
- **Almacenamiento Seguro del Token (RFC 10017):**
  - Sesión activa en un `signal` privado en memoria con sincronización espejo en `sessionStorage` para resistir `F5` sin recurrir a `localStorage`.
- **Protección de Rutas (`CanActivateFn`):**
  - `authGuard`: Redirección automática a Keycloak login si no hay sesión activa.
  - `rolGuard(...roles)`: Control RBAC funcional que redirige a `/sin-permiso` ante intentos no autorizados.
- **Interceptor HTTP (`authInterceptor`):**
  - Adjunta `Authorization: Bearer <token>` a cada petición hacia el backend.
  - Auto-renovación silenciosa con `refresh_token` ante respuestas `401 Unauthorized`.
  - Propagación directa de errores `403 Forbidden` sin bucles de reintento.
- **Interfaz y Menú Dinámico:**
  - Sidebar condicionado por roles (ej. "Nueva Venta" exclusivo para `VENDEDOR`/`SUPERVISOR`, "Caja" y "Compras" para `SUPERVISOR`/`ADMIN`).
  - Botón "Anular Venta" protegido para `SUPERVISOR`/`ADMIN` (oculto para `VENDEDOR` como *Solo Supervisor*).
  - Cierre de sesión real (RP-Initiated Logout).

---

## 🚀 Puesta en Marcha

### Prerrequisitos
- **Java 21 LTS** (Eclipse Adoptium Temurin 21)
- **Node.js 20+** y Angular CLI
- **Docker Desktop** (para Keycloak y Oracle Database)

### 1. Levantar Keycloak y Base de Datos (Docker)
```powershell
cd backend
docker compose -f compose-dev.yml up -d
```
- **Keycloak Admin Console:** `http://localhost:38080` (`admin` / `admin123`)

### 2. Levantar el Backend (Spring Boot 4.0.7)
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
- **Backend API:** `http://localhost:8080`
- **Swagger UI con Bearer JWT:** `http://localhost:8080/swagger-ui.html`

### 3. Levantar la SPA Frontend (Angular 19)
```powershell
cd bomerp-frontend
npm start
```
- **SPA:** `http://localhost:4200`

### 4. Verificar Suite de Pruebas
```powershell
cd backend
.\mvnw.cmd test
```
*(31 pruebas ejecutadas con 100% de éxito: Spring Modulith, autorización RBAC/ABAC, cálculo de totales, rollback y transacciones).*
