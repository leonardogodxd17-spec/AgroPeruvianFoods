# ERP Agro Peruvian Foods

**Proyecto Integrador Modular (ADS | BD2 | LP2)**  
*Equipo: Los Hijos del Backend - Sección G2G3*

---

## 📌 Descripción del Proyecto
Plataforma empresarial para la gestión integral de la elaboración y comercialización de derivados de cacao y aceite de coco prensado en frío. Unifica ventas en mostrador/ferias, arqueo estricto de caja, abastecimiento y trazabilidad por lotes con criterio **FEFO** (*First Expired, First Out*).

---

## 🏛️ Arquitectura Modular (Spring Modulith)
Construido como un monolito modular sobre **Java 21 LTS** y **Spring Boot 4.0.7**, con persistencia en **Oracle Database Free 23ai**:

| Módulo | Responsable | Tipo | Transacción Cabecera - Detalle |
| :--- | :--- | :---: | :--- |
| **`caja`** | Brandon Andree Ccalla Colquehuanca | Transaccional | `SesionCaja` $\to$ `MovimientoCaja` |
| **`ventas`** | Zaggy Leonardo Morales Auquipata | Transaccional | `Venta` $\to$ `DetalleVenta` |
| **`produccion`** | Isai Armuto Uñapilco | Transaccional | `OrdenProduccion` $\to$ `DetalleConsumoInsumo` (+ `LoteProducto`) |
| **`compras`** | Elisban Huaylla Nestor | Transaccional | `Compra` $\to$ `DetalleCompra` (+ `Proveedor`) |
| **`catalogo`** | Común | Maestro | `Producto`, `Categoria` |

---

## 🚀 Puesta en Marcha

### Prerrequisitos
- **Java 21 LTS** (Eclipse Adoptium Temurin 21 recomendado)
- **Docker Desktop** (para el contenedor de Oracle Database)

### 1. Levantar Base de Datos Oracle
```powershell
cd backend
docker compose -f compose-dev.yml up -d
```

### 2. Ejecutar el Backend
```powershell
.\mvnw.cmd spring-boot:run
```

### 3. Verificar Arquitectura Modular y Pruebas
```powershell
.\mvnw.cmd test -Dtest=ModularityTests
```

### 4. Documentación Interactiva de la API
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **Health Check:** `http://localhost:8080/actuator/health`
