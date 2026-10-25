# GUION CRONOMETRADO DE SUSTENTACIÓN: UNIDAD II (LP2)
## Segundo Corte Integrado · ERP Agro Peruvian Foods S.A.C.
### Equipo: Los Hijos del Backend | Estudiante Principal: Zaggy Leonardo Morales Auquipata

Este guion está optimizado para cumplir con los tiempos exactos de la sesión S12 (Tabla 3 de la guía docente):
- **Bloque Grupal (Alcance, Arquitectura y Asignación):** 6 a 8 minutos.
- **Demo Técnica en Vivo Individual:** 3 minutos exactos.
- **Ronda de Preguntas del Docente:** 3 minutos por integrante.

---

## ⏱️ BLOQUE 1: PRESENTACIÓN GRUPAL (0:00 - 6:00 min)

### Minuto 0:00 - 2:00 | Alcance del Negocio (Diapositivas 1 y 2)
> **Qué decir:**
> "Buenos días profesor y jurado evaluador. Somos el equipo **Los Hijos del Backend** y hoy presentamos la sustentación del Segundo Corte Integrado de nuestro proyecto: **ERP Agro Peruvian Foods S.A.C.**
> 
> Nuestra empresa se dedica a la elaboración y comercialización de derivados de cacao fino de aroma y aceite de coco virgen prensado en frío en Tarapoto y Lima. Anteriormente, las ventas en mostrador y ferias se gestionaban de manera manual con talonarios físicos y hojas de cálculo, lo que provocaba pérdidas de stock, cobros no registrados y demoras de hasta 15 minutos en el arqueo diario.
> 
> En este segundo corte, presentamos nuestra solución: una **SPA empresarial modular y segura en Angular 19**, conectada a un único backend **Spring Boot 4.0 / Java 21**, autenticada centralizadamente mediante **Keycloak v26 con estándar PKCE**, y persistida sobre **Oracle Database 23c**."

### Minuto 2:00 - 4:00 | Arquitectura de Integración (Diapositiva 3)
> **Qué decir:**
> "A nivel arquitectónico, implementamos una arquitectura en cuatro capas desacopladas:
> 1. **Capa Cliente (SPA Angular):** Diseñada bajo el estándar de Angular con Standalone Components, organizada en `core/` (donde vive la identidad, guards e interceptores), `shared/` y `features/` para cada módulo del ERP.
> 2. **Capa de Identidad (Keycloak v26):** Actúa como nuestro proveedor de identidad externo bajo OAuth 2.0 y OpenID Connect. La SPA realiza el flujo Authorization Code con **PKCE (S256)**, garantizando que ninguna credencial del usuario pase por el código del frontend.
> 3. **Capa Backend (Spring Boot 4 / Java 21):** Configurado como OAuth2 Resource Server. Valida la firma criptográfica de los tokens mediante las claves públicas expuestas por Keycloak, mapeando los roles del claim `realm_access.roles` a la seguridad de Spring Security.
> 4. **Capa de Datos y Servicios Externos:** Persistencia transaccional con Hibernate ORM hacia Oracle Database 23c, e integración HTTP con la API de **APIsPERU** para la consulta de DNI en tiempo real con RENIEC."

### Minuto 4:00 - 6:00 | Asignación de Módulos Transaccionales (Diapositiva 4)
> **Qué decir:**
> "Cada integrante del equipo lidera un módulo transaccional con persistencia cabecera-detalle:
> - **Zaggy Morales:** Módulo de Ventas POS y Emisión de Tickets Térmicos (`Venta` / `DetalleVenta`), integrando consulta DNI con RENIEC y control atómico de stock.
> - **Brandon Ccalla:** Módulo de Gestión de Caja y Arqueo (`SesionCaja` / `MovimientoCaja`), controlando aperturas, ingresos y cierres de turno.
> - **Isai Armuto:** Módulo de Producción y Envasado BOM (`OrdenProduccion` / `DetalleOrden`), gestionando la receta de materiales y costos.
> - **Elisban Huaylla:** Módulo de Compras de Insumos (`OrdenCompra` / `DetalleCompra`), registrando el abastecimiento y cuentas por pagar.
> 
> A continuación, doy paso a la demostración técnica en vivo de mi módulo asignado."

---

## ⏱️ BLOQUE 2: DEMO TÉCNICA EN VIVO DE ZAGGY MORALES (3 MINUTOS)

### Minuto 0:00 - 0:45 | Inicio de Sesión Keycloak (PKCE) y Persistencia F5
1. **Acción en pantalla:** Abrir el navegador en `http://localhost:4200`. Al intentar navegar al dashboard o hacer clic en "Iniciar Sesión", la SPA redirige a la pantalla de login de Keycloak (`http://localhost:38080/realms/bomerp/...`).
2. **Qué decir:**
   > "Observen que la SPA no posee ningún formulario propio de usuario y contraseña. El `authGuard` detecta la ausencia de sesión y nos redirige al servidor de Keycloak mediante el flujo Authorization Code con PKCE. En la consola pueden observar la generación del `code_verifier` y `code_challenge` S256."
3. **Acción en pantalla:** Ingresar con usuario `vendedor` y contraseña `vendedor123`.
4. **Acción en pantalla:** Una vez en el Dashboard, abrir las DevTools (`F12` $\rightarrow$ pestaña `Application` $\rightarrow$ `Storage`).
5. **Qué decir:**
   > "Una vez autenticados, el token de acceso vive en la memoria de la aplicación mediante Signals de Angular. Como pueden ver en DevTools, `localStorage` está completamente limpio por seguridad contra ataques XSS; únicamente mantenemos una copia en `sessionStorage` para no perder la sesión si el usuario presiona la tecla `F5`. Observen: presiono `F5`, la página se recarga y la sesión continúa intacta."

### Minuto 0:45 - 1:45 | Formulario Cabecera-Detalle, DNI APIsPERU y Cálculos en Vivo
1. **Acción en pantalla:** En el sidebar, hacer clic en **Ventas $\rightarrow$ Nueva Venta (FormArray)** (`/ventas/nueva`).
2. **Acción en pantalla:** En el campo DNI, ingresar `72491563` y presionar el botón azul **"🔍 Buscar RENIEC"**.
3. **Qué decir:**
   > "Nuestro módulo de ventas se conecta en línea con la API de APIsPERU. Al ingresar el DNI y hacer clic en buscar, el backend consulta al padrón nacional de RENIEC y autocompleta de inmediato los nombres y apellidos del cliente en la cabecera de la venta."
4. **Acción en pantalla:** En la sección de detalle, seleccionar *Aceite de Coco Virgen 250ml*, cantidad `2`. Luego hacer clic en **"+ Agregar Producto"**, seleccionar *Pasta Pura de Cacao 100% 200g*, cantidad `3`.
5. **Qué decir:**
   > "Este formulario implementa un `FormArray` reactivo. Conforme agregamos o modificamos líneas, el sistema recalcula en tiempo real el subtotal, la base imponible y el IGV del 18%, validando que no se pueda registrar una venta con cero líneas."

### Minuto 1:45 - 2:30 | Registro Transaccional y Modal de Ticket Térmico POS
1. **Acción en pantalla:** Seleccionar método de pago `YAPE` y hacer clic en **"Confirmar y Generar Venta"**.
2. **Qué decir:**
   > "Al enviar la venta, el backend ejecuta la transacción atómica: valida existencias, descuenta el stock en el inventario, persiste cabecera y detalle en Oracle, y genera el número de ticket correlativo. De manera automática, la SPA abre el modal interactivo con el ticket térmico de 80mm ESC/POS con código de barras, listo para imprimir en la ticketera del mostrador."
3. **Acción en pantalla:** Mostrar el ticket en pantalla y hacer clic en **"🖨️ Imprimir Ticket"** para mostrar la previsualización nativa de impresión de Windows / Chrome.

### Minuto 2:30 - 3:00 | Caso de Error Real (Rollback por Stock Insuficiente)
1. **Acción en pantalla:** Cerrar el modal del ticket y agregar una nueva venta. Seleccionar un producto y en cantidad ingresar `9999` (superando el stock disponible).
2. **Acción en pantalla:** Hacer clic en "Confirmar y Generar Venta".
3. **Qué decir:**
   > "Ahora provocamos deliberadamente una violación a la regla de negocio `RN-VEN-01`. Al intentar vender 9999 unidades, el backend rechaza la transacción con código `400 Bad Request` y nos retorna el mensaje estructurado de dominio: *'Stock insuficiente para el producto'*. Gracias a la anotación `@Transactional`, la base de datos ejecuta un Rollback inmediato, garantizando que no existan registros huérfanos ni alteraciones indebidas en el stock."

---

## ⏱️ BLOQUE 3: PREGUNTAS TÉCNICAS DEL JURADO (RESPUESTAS MEMORIZADAS)

### Pregunta 1: "¿Por qué el `vendedorId` se extrae del claim `sub` del token y no del cuerpo JSON del request?"
> **Respuesta exacta:**
> *"Profesor, por principio de no repudio e integridad de la seguridad. Si el `vendedorId` se enviara en el JSON del cliente, un usuario malintencionado o un cliente alterado podría modificar ese valor y adjudicarle ventas, cobros o comisiones a otro vendedor. Al extraerlo del claim `sub` del JWT verificado por Spring Security, aseguramos que el ID proviene directamente de la firma criptográfica generada por Keycloak al momento del login."*

### Pregunta 2: "¿Por qué usamos PKCE en la SPA en lugar de almacenar un `client_secret`?"
> **Respuesta exacta:**
> *"Una Single Page Application (SPA) que corre en el navegador web es considerada un 'Cliente Público' según el estándar OAuth 2.0. Dado que cualquier usuario puede inspeccionar el código JavaScript compilado con las herramientas de desarrollador, no existe forma de mantener un `client_secret` en secreto. PKCE (Proof Key for Code Exchange) resuelve esto generando un secreto temporal y aleatorio (`code_verifier`) por cada intento de login, y enviando su resumen criptográfico SHA-256 (`code_challenge`), impidiendo que un tercero intercepte el código de autorización."*

### Pregunta 3: "¿Cómo diferencia su interceptor HTTP entre un error 401 y un error 403?"
> **Respuesta exacta:**
> *"El error `401 Unauthorized` representa una falla de autenticación, comúnmente originada porque el token de acceso expiró (configurado a 5 minutos). En ese caso, nuestro `authInterceptor` captura el 401 y realiza una sola llamada asíncrona de refresco usando el `refresh_token`; si tiene éxito, reintenta la petición original de forma transparente.
> En cambio, el error `403 Forbidden` significa que el usuario sí está debidamente autenticado pero no posee el rol o privilegio suficiente para esa operación (por ejemplo, un Vendedor intentando anular una venta). En el 403 está prohibido reintentar porque generaría un bucle infinito innecesario; el interceptor lo deja pasar y la SPA redirige a la pantalla `/sin-permiso`."*

### Pregunta 4: "¿Por qué el layout general (sidebar y navbar) tiene su propio `router-outlet`?"
> **Respuesta exacta:**
> *"Para garantizar la persistencia visual del shell empresarial y optimizar el rendimiento. Al tener el layout en una ruta padre con su propio `router-outlet`, Angular solo destruye y monta los componentes hijos que cambian (`features`), manteniendo en memoria la barra de navegación, el menú lateral y el estado reactivo del usuario logueado sin necesidad de recargar la página completa."*
