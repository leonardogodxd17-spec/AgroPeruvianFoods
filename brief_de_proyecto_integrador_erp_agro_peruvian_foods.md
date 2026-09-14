# HITO S2 ALINEAMIENTO DE PROYECTO INTEGRADOR (ADS | BD2 | LP2)
## BRIEF DE PROYECTO: ERP Agro Peruvian Foods
*Ficha corta oficial para declarar el rumbo del proyecto desde S2 a S16.*

---

## 1. DATOS DEL EQUIPO

| Campo | Detalle |
| :--- | :--- |
| **Nombre del equipo:** | Los Hijos del Backend |
| **Sección:** | G2G3 |
| **Repositorio (URL):** | https://github.com/B-randon36/TAREALP2-AGROPERUVIANFOODS.git |
| **Topics configurados:** | Sí |

### Integrantes y cursos matriculados:
| Integrante | ADS | BD2 | LP2 |
| :--- | :---: | :---: | :---: |
| Ccalla Colquehuanca Brandon Andree | Sí | Sí | Sí |
| Morales Auquipata Zaggy Leonardo | Sí | Sí | Sí |
| Armuto Uñapilco Isai | Sí | Sí | Sí |
| Huaylla Nestor Elisban | Sí | Sí | Sí |

---

## 2. DOMINIO DEL PROYECTO

| Campo | Descripción |
| :--- | :--- |
| **Nombre del proyecto:** | ERP Agro Peruvian Foods |
| **Problema o necesidad que resuelve (2-4 líneas):** | Reemplaza el control manual en hojas de Excel y notas de venta físicas por una plataforma integral que unifica las ventas en tienda física, ferias y despachos por delivery, garantizando el arqueo de turnos de caja, conciliación de pagos (efectivo/billeteras digitales) y trazabilidad por lotes con fechas de vencimiento (criterio FEFO). |
| **Dominio de negocio (breve):** | Elaboración y comercialización de derivados de cacao y aceite de coco prensado en frío. |
| **Usuarios/actores principales:** | **Administradora / Cajera:** Realiza apertura/cierre de turnos de caja, administra cobranzas, gestiona despachos y supervisa el abastecimiento.<br>**Cliente:** Actor indirecto que compra presencialmente o vía delivery y genera transacciones de cobro. |
| **¿Continúa proyecto anterior?:** | Es un dominio nuevo. |

---

## 3. MÓDULOS DE NEGOCIO Y ALCANCE FULL-STACK ESPERADO

Conforme a las recomendaciones del docente (*"Quitar reporte métrica"*, *"Modula venta separarlo"* y *"Cada uno un solo módulo"*), la arquitectura se rediseña a **un módulo de negocio por integrante**, asegurando que cada miembro lidere un componente transaccional con persistencia cabecera-detalle:

| Integrante | Módulo Asignado | Tipo | Enfoque de Negocio |
| :--- | :--- | :---: | :--- |
| **Ccalla Colquehuanca Brandon Andree** | Gestión Financiera de Caja y Movimientos | Transaccional | Turnos de caja, arqueos, ingresos, egresos y conciliación. |
| **Morales Auquipata Zaggy Leonardo** | Ventas y Tickets POS | Transaccional | Transacciones en tienda física/ferias, cobros y emisión de tickets. |
| **Armuto Uñapilco Isai** | Producción y Envasado por Lotes (BOM) | Transaccional | Órdenes de trabajo, costeo BOM y control de lotes producidos. |
| **Huaylla Nestor Elisban** | Compras de Insumos y Pagos a Proveedores | Transaccional | Abastecimiento, ingreso de materia prima y pagos a proveedores. |

---

## 4. FICHAS DETALLADAS DE MÓDULOS

### Módulo 1: Gestión Financiera de Caja y Movimientos
- **Responsable:** Ccalla Colquehuanca Brandon Andree
- **Tipo:** Transaccional
- **Descripción breve:** Administra los turnos y sesiones de caja, registrando y liquidando de manera transaccional cada movimiento de ingreso (por ventas en mostrador y cobranzas de delivery) y egreso (gastos menores y caja chica), garantizando un arqueo estricto contra el saldo físico reportado.
- **Entidad Cabecera-Detalle:** `SesionCaja` (cabecera) / `MovimientoCaja` (detalle)
- **Lista inicial de requisitos:**
  1. El sistema debe registrar la apertura de turno de caja con saldo inicial y estado `'ABIERTA'`.
  2. El sistema debe registrar movimientos transaccionales de ingreso y egreso detallando monto, concepto, método de pago (Efectivo, Yape/Plin, Transferencia bancaria) y referencia de la operación.
  3. El sistema debe realizar el arqueo y cierre formal de sesión de caja, calculando el saldo teórico total contra el monto físico ingresado para asentar sobrantes o faltantes.

---

### Módulo 2: Ventas y Tickets POS
- **Responsable:** Morales Auquipata Zaggy Leonardo
- **Tipo:** Transaccional
- **Descripción breve:** Procesa las transacciones comerciales directas en tienda física, ferias y venta rápida en mostrador, calculando subtotales, impuestos, aplicando métodos de pago y realizando la salida de inventario por lote antes de generar el comprobante.
- **Entidad Cabecera-Detalle:** `Venta` (cabecera) / `DetalleVenta` (detalle)
- **Lista inicial de requisitos:**
  1. El sistema debe registrar ventas directas aplicando múltiples métodos de pago (Efectivo, Yape/Plin, Transferencia).
  2. El sistema debe solicitar la salida atómica de stock de productos terminados afectando el lote correspondiente según el criterio FEFO (*First Expired, First Out*).
  3. El sistema debe formatear y emitir comprobantes/tickets de venta optimizados para impresión térmica (ESC/POS).

---

### Módulo 3: Producción y Envasado por Lotes (BOM)
- **Responsable:** Armuto Uñapilco Isai
- **Tipo:** Transaccional
- **Descripción breve:** Gestiona el ciclo productivo en planta mediante órdenes de trabajo para derivados de cacao y aceite de coco prensado en frío, aplicando la lista de materiales (BOM), deduciendo materia prima e insumos de empaque, y generando los lotes terminados con fecha de vencimiento.
- **Entidad Cabecera-Detalle:** `OrdenProduccion` (cabecera) / `DetalleConsumoInsumo` (detalle)
- **Lista inicial de requisitos:**
  1. El sistema debe registrar órdenes de producción vinculadas a la receta BOM (*Bill of Materials*) vigente para el producto a fabricar.
  2. El sistema debe generar y codificar el lote resultante, registrando su fecha de producción y fecha de vencimiento calculada.
  3. El sistema debe descontar transaccionalmente del stock los insumos consumidos (materia prima y material de empaque) y registrar el ingreso del producto terminado al almacén.

---

### Módulo 4: Compras de Insumos y Pagos a Proveedores
- **Responsable:** Huaylla Nestor Elisban
- **Tipo:** Transaccional
- **Descripción breve:** Registra y da seguimiento a las adquisiciones de insumos de empaque (botellas de vidrio, tapas, precintos, etiquetas) y materias primas (cacao, coco), gestionando la recepción en almacén, actualización de costos de adquisición y el estado de cuentas por pagar.
- **Entidad Cabecera-Detalle:** `Compra` (cabecera) / `DetalleCompra` (detalle)
- **Lista inicial de requisitos:**
  1. El sistema debe registrar órdenes de compra detallando cantidades, precios unitarios pactados y datos del proveedor.
  2. El sistema debe dar ingreso formal al stock de insumos tras la recepción en almacén y actualizar los costos promedio de inventario.
  3. El sistema debe registrar amortizaciones o pagos totales a proveedores, manteniendo el control de saldos pendientes por liquidar.

---

## 5. ALCANCE GENERAL DEL PROYECTO

### Qué SÍ cubre este proyecto en conjunto:
- Gestión integral de sesiones y arqueo de caja con control de ingresos y egresos por método de pago.
- Punto de Venta (POS) transaccional con emisión de tickets para atención en mostrador y ferias.
- Trazabilidad y control de inventario por lotes con fechas de envasado y caducidad (criterio FEFO).
- Estructuración de recetas de fabricación (BOM) y ejecución de órdenes de producción en planta.
- Registro transaccional de compras a proveedores con costeo de insumos y amortización de pagos.

### Qué NO cubre - Fuera de alcance explícito:
- Módulo analítico de reportería avanzada, cubos OLAP o dashboards de métricas ejecutivas (retirado por indicación docente).
- Conexión activa y firma digital en tiempo real con SUNAT/PSE (queda desacoplada como extensión futura).
- Pasarela de pagos web de autoservicio o tienda virtual B2C e-commerce para clientes finales.
- Contabilidad general formal (asientos contables de partida doble, libros electrónicos PLE, balances contables auditados).

---

> **Nota para S3:** Arquitectura física y lógica inicial (ADS), definición de recursos y contratos RESTful (LP2), y diseño de modelos relacionales/objetos Oracle (BD2).

---

## 6. APROBACIÓN

| Cargo | Nombre del Docente | Firma / Conformidad |
| :--- | :--- | :--- |
| **Docente ADS:** | ____________________________________ | [ &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ] |
| **Docente BD2:** | ____________________________________ | [ &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ] |
| **Docente LP2:** | ____________________________________ | [ &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ] |

**Fecha de entrega ajustada:** 25/08/2026