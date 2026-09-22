# Sustentación y Evidencia Técnica: Evaluación Unidad 1 (LP2) - BOM ERP

**Módulo:** Finanzas (`pe.edu.upeu.bomerp.finanzas`)  
**Operación de Dominio Principal:** Rendición de Caja Chica (Cabecera: `RendicionCajaChica`, Detalle: `DetalleRendicionCajaChica`)  
**Regla de Negocio Real:** `RN-FIN-01` (Control de Solvencia y Liquidez de Caja Chica)  

---

## 1. Ficha Técnica del Módulo y Arquitectura

* **Tipo de Arquitectura:** Monolito Modular con Spring Boot 4.0 / Spring 7.0 y Spring Modulith.
* **Control de Fronteras:** Comunicación entre módulos (`finanzas` hacia `catalogo`) desacoplada mediante `@NamedInterface` y verificada con `ModularityTests` de ArchUnit.
* **Trazabilidad de Peticiones:** Filtro de correlación `CorrelationIdFilter` que inyecta `X-Correlation-Id` en MDC para logs estructurados y trazabilidad distribuida.
* **Manejo Centralizado de Excepciones:** `GlobalExceptionHandler` con `@RestControllerAdvice` retornando respuestas estructuradas (`status`, `error`, `message`, `timestamp`, `reglaNegocio`).

---

## 2. Contrato de API REST Versionado (`/api/v1/finanzas`)

| Método | Endpoint | Descripción | Parámetros Clave | Código Éxito |
|---|---|---|---|---|
| `POST` | `/api/v1/finanzas/cajas-chicas` | Creación de Caja Chica | `codigo`, `responsable`, `limiteMaximo`, `saldoInicial` | `201 Created` |
| `GET` | `/api/v1/finanzas/cajas-chicas/{id}` | Obtener Caja Chica por ID | `id` | `200 OK` |
| `POST` | `/api/v1/finanzas/rendiciones` | Registrar Rendición (Cabecera-Detalle Atómica) | Body: `RendicionCajaChicaRequest` | `201 Created` |
| `GET` | `/api/v1/finanzas/rendiciones/{id}` | Obtener Rendición Completa | `id` | `200 OK` |
| `GET` | `/api/v1/finanzas/rendiciones/paginado` | Listado Paginado con Ordenamiento | `page`, `size`, `sort` | `200 OK` |
| `GET` | `/api/v1/finanzas/rendiciones/filtros` | Búsqueda con Filtros Combinados y Orden | `cajaChicaId`, `estado`, `responsable`, `fechaInicio`, `fechaFin`, `montoMin`, `montoMax`, `sort` | `200 OK` |
| `GET` | `/api/v1/finanzas/rendiciones/resumen` | Proyección Ligera de Resumen (Constructor Expression) | `cajaChicaId` | `200 OK` |
| `GET` | `/api/v1/finanzas/rendiciones/reporte-agregado` | Agregaciones Matemáticas (SUM, AVG, MAX, MIN, COUNT) | `cajaChicaId` | `200 OK` |

---

## 3. Evidencias de los 4 Bloques de la Rúbrica

### Bloque 1: Modelo Cabecera-Detalle y Cálculos
* **Entidades:**
  - Cabecera: `RendicionCajaChica` (`codigo`, `responsable`, `fecha`, `montoTotal`, `estado`, `@ManyToOne CajaChica`, `@OneToMany List<DetalleRendicionCajaChica>`).
  - Detalle: `DetalleRendicionCajaChica` (`concepto`, `cantidad`, `precioUnitario`, `subtotal`, `productoId`).
* **Lógica de Negocio de Dominio:**
  - El método de dominio `rendicion.calcularTotal()` itera cada detalle calculando `subtotal = cantidad * precioUnitario` y sumando el `montoTotal` en la cabecera antes de persistir.

### Bloque 2: Regla de Negocio Real (`RN-FIN-01`)
* **Identificación:** `RN-FIN-01 (Solvencia de Caja Chica / Control de Liquidez)`.
* **Regla:** Ninguna rendición puede registrarse si su monto total acumulado supera el saldo disponible actual de la Caja Chica a la que rinde.
* **Implementación:**
  ```java
  if (rendicion.getMontoTotal().compareTo(cajaChica.getSaldoDisponible()) > 0) {
      throw new SolvenciaInsuficienteException(
          "RN-FIN-01: El monto total de la rendición (S/ " + rendicion.getMontoTotal() +
          ") supera la solvencia/saldo disponible de Caja Chica (S/ " + cajaChica.getSaldoDisponible() + ")"
      );
  }
  ```
* **Manejo de Excepción:** Retorna código `400 Bad Request` con payload descriptivo identificando la regla violada.

### Bloque 3: Transacción Atómica (Caso Éxito y Rollback)
* **Caso Éxito:**
  - *Estado Inicial:* Caja Chica `CC-001` con Saldo Disponible `S/ 1000.00`.
  - *Operación:* Rendición por `S/ 300.00` (2 ítems).
  - *Estado Final:* Caja Chica `CC-001` con Saldo `S/ 700.00`, 1 registro nuevo en `RENDICIONES_CAJA_CHICA` y 2 registros en `DETALLES_RENDICION_CAJA_CHICA`.
* **Caso Rollback:**
  - *Estado Inicial:* Caja Chica `CC-001` con Saldo Disponible `S/ 1000.00`.
  - *Operación:* Rendición por `S/ 1500.00` (Supera saldo).
  - *Comportamiento:* Se lanza `SolvenciaInsuficienteException`. Spring `@Transactional` ejecuta rollback total.
  - *Estado Final:* Saldo Disponible permanece intacto en `S/ 1000.00` y 0 registros insertados en cabecera o detalle.

### Bloque 4: Límites de Módulo y ModularityTests
* **Límites:** El módulo `finanzas` necesita validar que los productos comprados existan en el módulo `catalogo`.
* **Solución:** Se accede a `ProductoService` ubicado en `pe.edu.upeu.bomerp.catalogo.producto.service`, el cual está exportado con `@NamedInterface` en su `package-info.java`.
* **Prueba de Verificación:** `ModularityTests` ejecuta ArchUnit sobre la estructura modular asegurando cero acoplamientos prohibidos.

---

## 4. Consultas Empresariales, Proyecciones, Reportes y CORS

### 4.1. Filtros Combinados y Ordenamiento Dinámico
* **JPQL con Parámetros Opcionales:**
  `SELECT r FROM RendicionCajaChica r WHERE (:cajaChicaId IS NULL OR r.cajaChica.id = :cajaChicaId) AND (:estado IS NULL OR r.estado = :estado) ...`
* **Ejemplos de Consumo:**
  1. `GET /api/v1/finanzas/rendiciones/filtros?responsable=Carlos&estado=APROBADO&sort=montoTotal,desc`
  2. `GET /api/v1/finanzas/rendiciones/filtros?cajaChicaId=1&montoMin=150.00&montoMax=350.00&sort=montoTotal,asc`
  3. `GET /api/v1/finanzas/rendiciones/filtros?fechaInicio=2026-09-01&fechaFin=2026-09-30&estado=APROBADO&sort=responsable,asc`

### 4.2. Proyección de Resumen (`SELECT new ...`)
* Consulta que construye directamente `RendicionResumenDto` sin sobrecargar memoria con entidades JPA ni relaciones perezosas (`LAZY`):
  ```java
  @Query("SELECT new pe.edu.upeu.bomerp.finanzas.dto.RendicionResumenDto(" +
         "r.id, r.codigo, r.responsable, r.fecha, r.montoTotal, r.estado, r.cajaChica.codigo, SIZE(r.detalles)) " +
         "FROM RendicionCajaChica r WHERE (:cajaChicaId IS NULL OR r.cajaChica.id = :cajaChicaId)")
  List<RendicionResumenDto> obtenerResumenProyectado(Long cajaChicaId);
  ```

### 4.3. Reporte Agregado (0 en vez de null)
* Uso de `COALESCE` para prevenir retornos `null` en bases de datos relacionales cuando la consulta no tiene registros:
  ```java
  @Query("SELECT new pe.edu.upeu.bomerp.finanzas.dto.ReporteRendicionesAgregadoDto(" +
         "COALESCE(SUM(r.montoTotal), 0), " +
         "COALESCE(AVG(r.montoTotal), 0.0), " +
         "COALESCE(MAX(r.montoTotal), 0), " +
         "COALESCE(MIN(r.montoTotal), 0), " +
         "COUNT(r)) " +
         "FROM RendicionCajaChica r WHERE (:cajaChicaId IS NULL OR r.cajaChica.id = :cajaChicaId)")
  ReporteRendicionesAgregadoDto obtenerReporteAgregado(Long cajaChicaId);
  ```
* **Caso con datos:** Devuelve `totalMonto: 600.00`, `promedioMonto: 300.0`, `cantidadRendiciones: 2`.
* **Caso sin datos (Caja vacía):** Devuelve `totalMonto: 0.00`, `promedioMonto: 0.0`, `cantidadRendiciones: 0` (**NO null**).

### 4.4. Configuración CORS y Demostración en Navegador Real
* En [WebConfig.java](file:///e:/repoprofe/bomerp-backend/src/main/java/pe/edu/upeu/bomerp/config/WebConfig.java) se habilita `allowedOriginPatterns`, `allowCredentials(true)` y métodos `GET, POST, PUT, DELETE, OPTIONS, PATCH`.
* **Prueba en Consola del Navegador (`F12`):**
  ```javascript
  // Llamada exitosa desde origen permitido:
  fetch("http://localhost:8080/api/v1/finanzas/rendiciones/paginado", { credentials: "include" })
    .then(r => r.json())
    .then(data => console.log("CORS OK:", data));
  ```

---

## 5. Respuestas a las 5 Preguntas Teóricas Clave de Sustentación

### 1. ¿Por qué usamos DTOs en lugar de exponer las entidades JPA directamente en los controladores?
> **Respuesta:**  
> Por tres razones fundamentales:
> 1. **Seguridad y Encapsulamiento:** Evitamos sobreexposición de campos sensibles o vulnerabilidades de asignación masiva (*Mass Assignment*).
> 2. **Prevención de Ciclos Infinitos:** En relaciones bidireccionales (`@ManyToOne` / `@OneToMany`), Jackson entra en recursión infinita al serializar a JSON. Los DTOs planos resuelven esto desacoplando la estructura del API del modelo relacional.
> 3. **Evolución del Contrato y Rendimiento:** La base de datos puede evolucionar o cambiar de esquema sin romper el contrato del cliente REST, permitiendo además optimizaciones como proyecciones ligeras de constructor.

### 2. ¿Cómo garantiza `@Transactional` la atomicidad en la operación Cabecera-Detalle?
> **Respuesta:**  
> `@Transactional` delimita un contexto de persistencia y una transacción de base de datos a nivel de hilo. Todas las operaciones DML ejecutadas dentro del método (descuento de saldo en Caja Chica, inserción en `RENDICIONES_CAJA_CHICA` e inserción en `DETALLES_RENDICION_CAJA_CHICA`) se ejecutan bajo la misma conexión. Si se lanza cualquier `RuntimeException` (como `SolvenciaInsuficienteException`), Spring intercepta la excepción mediante AOP y emite una instrucción `ROLLBACK` al gestor de base de datos, garantizando que o se persisten todos los cambios o no se persiste ninguno (*Principio ACID: Todo o Nada*).

### 3. ¿Por qué es fundamental que un reporte financiero retorne `0` o `0.00` y nunca `null`?
> **Respuesta:**  
> En el dominio financiero y contable, la ausencia de transacciones significa un balance neutro o cero (`S/ 0.00`), no un valor indeterminado o desconocido (`null`). Técnicamente, devolver `null` obliga a los clientes frontend y servicios downstream a implementar validaciones defensivas constantes para evitar `NullPointerException` al formatear montos monetarios o graficar reportes. El uso de `COALESCE` en JPQL asegura consistencia semántica y robustez en la API.

### 4. ¿Qué es CORS y en qué capa del sistema se ejecuta la restricción?
> **Respuesta:**  
> CORS (*Cross-Origin Resource Sharing*) es un mecanismo de seguridad implementado por los **navegadores web** (no por el servidor ni por clientes como Postman o cURL) para flexibilizar de forma segura la política del mismo origen (*Same-Origin Policy*). Cuando un frontend alojado en un origen (ej. `localhost:5173`) intenta consumir un backend en otro origen (ej. `localhost:8080`), el navegador intercepta la llamada y emite un pre-vuelo `OPTIONS`. Solo si el backend responde con la cabecera `Access-Control-Allow-Origin` correspondiente, el navegador permite que JavaScript lea la respuesta.

### 5. ¿Cómo ayuda Spring Modulith a mantener la arquitectura en un monolito modular?
> **Respuesta:**  
> Spring Modulith organiza el monolito en módulos de dominio bien delimitados por paquetes de primer nivel. Impone reglas arquitecturales de encapsulamiento: las clases internas de un módulo son invisibles para otros módulos, salvo que se expongan explícitamente mediante interfaces marcadas con `@NamedInterface`. A través de pruebas automáticas con ArchUnit (`ModularityTests`), se verifica en tiempo de compilación y pruebas continuas que no existan dependencias cíclicas ni accesos indebidos entre módulos, manteniendo la arquitectura limpia como si fueran microservicios pero con la simplicidad de un único despliegue.
