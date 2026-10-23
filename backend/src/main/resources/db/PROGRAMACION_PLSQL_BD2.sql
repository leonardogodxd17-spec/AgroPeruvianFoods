-- =============================================================================
-- UNIVERSIDAD PERUANA UNIÓN - FACULTAD DE INGENIERÍA Y ARQUITECTURA
-- ESCUELA PROFESIONAL DE INGENIERÍA DE SISTEMAS
-- CURSO: BASE DE DATOS II (BD2) - CICLO 2026-II
-- PROYECTO ERP: AGRO PERUVIAN FOODS S.A.C.
-- ARCHIVO: PROGRAMACION_PLSQL_BD2.sql
-- OBJETOS PL/SQL: 2 PROCEDIMIENTOS | 2 FUNCIONES | 2 TRIGGERS + CASOS DE PRUEBA
-- =============================================================================
-- INSTRUCCIONES:
-- 1. Abra este archivo en Oracle SQL Developer.
-- 2. Conéctese a su esquema de trabajo (donde ejecutó TABLAS_SQLDEVELOPER_BOMERP.sql).
-- 3. Ejecute con F5 o bloque por bloque (Ctrl + Enter) para ver los resultados.
-- =============================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED;
SET LINESIZE 250;
SET PAGESIZE 100;
SET FEEDBACK ON;

PROMPT =============================================================================;
PROMPT  INICIANDO COMPILACIÓN DE OBJETOS PL/SQL (PROCEDURES, FUNCIONES, TRIGGERS)   ;
PROMPT =============================================================================;


-- =============================================================================
-- PARTE 1: STORED PROCEDURES (2 PROCEDIMIENTOS ALMACENADOS)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- PROCEDIMIENTO 1: SP_COMPLETAR_ORDEN_PRODUCCION
-- Responsable: Isai Armuto (Módulo de Producción Industrial)
-- Propósito: Culmina una orden de manufactura, actualiza su estado a 'COMPLETADA',
--            registra la fecha de fin y crea automáticamente un nuevo Lote FEFO
--            en LOTES_PRODUCTO con su fecha de vencimiento y stock disponible.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE SP_COMPLETAR_ORDEN_PRODUCCION (
    p_id_orden              IN  NUMBER,
    p_cantidad_producida    IN  NUMBER,
    p_fecha_vencimiento     IN  DATE,
    p_codigo_lote_generado  OUT VARCHAR2
) AS
    v_estado_actual      VARCHAR2(25);
    v_id_producto        NUMBER;
    v_nombre_producto    VARCHAR2(120);
    v_codigo_lote_nuevo  VARCHAR2(50);
BEGIN
    -- 1. Verificar existencia y estado actual de la orden de producción
    SELECT op.estado, r.id_producto, p.nombre
    INTO v_estado_actual, v_id_producto, v_nombre_producto
    FROM ORDENES_PRODUCCION op
    JOIN RECETAS_BOM r ON op.id_receta = r.id_receta
    JOIN PRODUCTOS p ON r.id_producto = p.id_producto
    WHERE op.id_orden = p_id_orden
    FOR UPDATE OF op.estado;

    -- Validar que la orden no esté ya finalizada o cancelada
    IF v_estado_actual = 'COMPLETADA' THEN
        RAISE_APPLICATION_ERROR(-20010, 'La orden de producción #' || p_id_orden || ' ya fue completada previamente.');
    ELSIF v_estado_actual = 'CANCELADA' THEN
        RAISE_APPLICATION_ERROR(-20011, 'No se puede culminar una orden que ha sido cancelada.');
    END IF;

    -- Validar fecha de vencimiento
    IF p_fecha_vencimiento <= TRUNC(SYSDATE) THEN
        RAISE_APPLICATION_ERROR(-20012, 'La fecha de vencimiento debe ser posterior a la fecha actual.');
    END IF;

    -- 2. Generar código único para el Lote FEFO
    v_codigo_lote_nuevo := 'LOT-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-OP' || LPAD(p_id_orden, 3, '0');

    -- 3. Actualizar la orden de producción
    UPDATE ORDENES_PRODUCCION
    SET estado = 'COMPLETADA',
        fecha_fin = SYSDATE,
        cantidad_a_producir = p_cantidad_producida
    WHERE id_orden = p_id_orden;

    -- 4. Inyectar el nuevo Lote FEFO en el inventario disponible
    INSERT INTO LOTES_PRODUCTO (
        id_producto,
        id_orden,
        codigo_lote,
        fecha_produccion,
        fecha_envasado,
        fecha_vencimiento,
        stock_disponible
    ) VALUES (
        v_id_producto,
        p_id_orden,
        v_codigo_lote_nuevo,
        TRUNC(SYSDATE),
        TRUNC(SYSDATE),
        p_fecha_vencimiento,
        p_cantidad_producida
    );

    -- 5. Asignar parámetro de salida
    p_codigo_lote_generado := v_codigo_lote_nuevo;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('>>> [SP_COMPLETAR_ORDEN_PRODUCCION] Orden #' || p_id_orden || 
                         ' completada exitosamente. Lote creado: ' || v_codigo_lote_nuevo);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20013, 'No existe la orden de producción con ID: ' || p_id_orden);
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END SP_COMPLETAR_ORDEN_PRODUCCION;
/


-- -----------------------------------------------------------------------------
-- PROCEDIMIENTO 2: SP_CERRAR_SESION_CAJA_ARQUEO
-- Responsable: Brandon Ccalla (Módulo de Finanzas y Tesorería)
-- Propósito: Cierra formalmente un turno POS, calcula los ingresos/egresos de efectivo,
--            determina el saldo esperado y calcula el descuadre de arqueo (sobrante/faltante).
-- -----------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE SP_CERRAR_SESION_CAJA_ARQUEO (
    p_id_sesion          IN  NUMBER,
    p_saldo_real_contado IN  NUMBER,
    p_observaciones      IN  VARCHAR2,
    p_diferencia_out     OUT NUMBER
) AS
    v_estado_actual       VARCHAR2(20);
    v_saldo_inicial       NUMBER(12,2);
    v_total_ingresos      NUMBER(12,2) := 0;
    v_total_egresos       NUMBER(12,2) := 0;
    v_saldo_esperado      NUMBER(12,2);
    v_diferencia          NUMBER(12,2);
BEGIN
    -- 1. Obtener la sesión y validar que esté abierta
    SELECT estado, saldo_inicial
    INTO v_estado_actual, v_saldo_inicial
    FROM SESIONES_CAJA
    WHERE id_sesion = p_id_sesion
    FOR UPDATE;

    IF v_estado_actual <> 'ABIERTA' THEN
        RAISE_APPLICATION_ERROR(-20020, 'La sesión de caja #' || p_id_sesion || ' no está abierta (Estado: ' || v_estado_actual || ').');
    END IF;

    -- 2. Consolidar ingresos y egresos de efectivo de los movimientos registrados
    SELECT 
        NVL(SUM(CASE WHEN tipo = 'INGRESO' AND metodo_pago = 'EFECTIVO' THEN monto ELSE 0 END), 0),
        NVL(SUM(CASE WHEN tipo = 'EGRESO'  AND metodo_pago = 'EFECTIVO' THEN monto ELSE 0 END), 0)
    INTO v_total_ingresos, v_total_egresos
    FROM MOVIMIENTOS_CAJA
    WHERE id_sesion = p_id_sesion;

    -- 3. Calcular saldo esperado y diferencia de arqueo
    v_saldo_esperado := v_saldo_inicial + v_total_ingresos - v_total_egresos;
    v_diferencia     := p_saldo_real_contado - v_saldo_esperado;

    -- 4. Actualizar sesión con cierre y arqueo
    UPDATE SESIONES_CAJA
    SET total_ingresos_efectivo = v_total_ingresos,
        total_egresos_efectivo  = v_total_egresos,
        saldo_esperado_efectivo = v_saldo_esperado,
        saldo_real_efectivo     = p_saldo_real_contado,
        diferencia_arqueo       = v_diferencia,
        estado                  = 'CERRADA',
        fecha_cierre            = CURRENT_TIMESTAMP,
        observaciones           = p_observaciones
    WHERE id_sesion = p_id_sesion;

    p_diferencia_out := v_diferencia;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('>>> [SP_CERRAR_SESION_CAJA_ARQUEO] Sesión #' || p_id_sesion || 
                         ' cerrada. Saldo Esperado: S/ ' || v_saldo_esperado || 
                         ' | Saldo Real: S/ ' || p_saldo_real_contado || 
                         ' | Diferencia: S/ ' || v_diferencia);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        ROLLBACK;
        RAISE_APPLICATION_ERROR(-20021, 'No existe la sesión de caja con ID: ' || p_id_sesion);
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END SP_CERRAR_SESION_CAJA_ARQUEO;
/


-- =============================================================================
-- PARTE 2: FUNCIONES DEFINIDAS POR EL USUARIO (2 FUNCIONES CON RETURN)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- FUNCIÓN 1: FN_CALCULAR_COSTO_RECETA_BOM
-- Responsable: Isai Armuto (Módulo de Producción)
-- Propósito: Calcula dinámicamente el costo unitario de manufactura de una receta
--            sumando el costo parcial de cada insumo (cantidad_requerida * costo_unitario).
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_CALCULAR_COSTO_RECETA_BOM (
    p_id_receta IN NUMBER
) RETURN NUMBER AS
    v_costo_total NUMBER(12,4) := 0;
    v_existe      NUMBER := 0;
BEGIN
    -- Validar existencia de la receta
    SELECT COUNT(*) INTO v_existe FROM RECETAS_BOM WHERE id_receta = p_id_receta;
    IF v_existe = 0 THEN
        RETURN 0;
    END IF;

    -- Calcular sumatoria de insumos requeridos
    SELECT NVL(SUM(cantidad_requerida * costo_unitario), 0)
    INTO v_costo_total
    FROM DETALLES_RECETA_BOM
    WHERE id_receta = p_id_receta;

    RETURN v_costo_total;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END FN_CALCULAR_COSTO_RECETA_BOM;
/


-- -----------------------------------------------------------------------------
-- FUNCIÓN 2: FN_OBTENER_LOTE_FEFO_PRIORITARIO
-- Responsable: Zaggy Morales (Módulo de Ventas)
-- Propósito: Implementa el algoritmo FEFO (First Expired, First Out). Dado un producto
--            y la cantidad requerida, retorna el ID_LOTE que vence primero y que
--            cuenta con stock suficiente no caducado.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_OBTENER_LOTE_FEFO_PRIORITARIO (
    p_id_producto        IN NUMBER,
    p_cantidad_solicitada IN NUMBER
) RETURN NUMBER AS
    v_id_lote_prioritario NUMBER := NULL;
BEGIN
    SELECT id_lote
    INTO v_id_lote_prioritario
    FROM (
        SELECT id_lote
        FROM LOTES_PRODUCTO
        WHERE id_producto = p_id_producto
          AND stock_disponible >= p_cantidad_solicitada
          AND fecha_vencimiento > TRUNC(SYSDATE)
        ORDER BY fecha_vencimiento ASC, id_lote ASC
    )
    WHERE ROWNUM = 1;

    RETURN v_id_lote_prioritario;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RETURN NULL; -- No hay ningún lote con suficiente stock no vencido
    WHEN OTHERS THEN
        RETURN NULL;
END FN_OBTENER_LOTE_FEFO_PRIORITARIO;
/


-- =============================================================================
-- PARTE 3: TRIGGERS DE BASE DE DATOS (2 DISPARADORES)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TRIGGER 1: TRG_DESCONTAR_STOCK_LOTE_VENTA
-- Responsable: Zaggy Morales (Módulo de Ventas)
-- Evento: AFTER INSERT ON DETALLES_VENTA (Nivel Fila)
-- Propósito: Cada vez que se vende un producto con asignación de lote FEFO,
--            descuenta automáticamente el stock del lote físico en LOTES_PRODUCTO.
--            Si el lote no tiene stock suficiente, aborta la transacción con error.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_DESCONTAR_STOCK_LOTE_VENTA
AFTER INSERT ON DETALLES_VENTA
FOR EACH ROW
DECLARE
    v_stock_actual NUMBER;
BEGIN
    -- Solo aplica si la venta está amarrada a un lote físico
    IF :NEW.id_lote IS NOT NULL THEN
        -- Verificar stock actual del lote
        SELECT stock_disponible
        INTO v_stock_actual
        FROM LOTES_PRODUCTO
        WHERE id_lote = :NEW.id_lote
        FOR UPDATE;

        IF v_stock_actual < :NEW.cantidad THEN
            RAISE_APPLICATION_ERROR(-20030, 
                'Regla FEFO violada: Stock insuficiente en el Lote #' || :NEW.id_lote || 
                '. Disponible: ' || v_stock_actual || ', solicitado: ' || :NEW.cantidad);
        END IF;

        -- Descontar stock del lote
        UPDATE LOTES_PRODUCTO
        SET stock_disponible = stock_disponible - :NEW.cantidad
        WHERE id_lote = :NEW.id_lote;
    END IF;
END TRG_DESCONTAR_STOCK_LOTE_VENTA;
/


-- -----------------------------------------------------------------------------
-- TRIGGER 2: TRG_CALCULAR_SUBTOTAL_COMPRA
-- Responsable: Nestor Huaylla (Módulo de Compras)
-- Evento: BEFORE INSERT OR UPDATE ON DETALLES_COMPRA (Nivel Fila)
-- Propósito: Garantiza consistencia matemática en las facturas de proveedores:
--            calcula de forma automática :NEW.subtotal := cantidad * costo_unitario
--            evitando errores de digitación o redondeo desde la aplicación cliente.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_CALCULAR_SUBTOTAL_COMPRA
BEFORE INSERT OR UPDATE ON DETALLES_COMPRA
FOR EACH ROW
BEGIN
    :NEW.subtotal := ROUND(:NEW.cantidad * :NEW.costo_unitario, 2);
END TRG_CALCULAR_SUBTOTAL_COMPRA;
/

PROMPT =============================================================================;
PROMPT  OBJETOS PL/SQL COMPILADOS CON ÉXITO. INICIANDO BATERÍA DE PRUEBAS...         ;
PROMPT =============================================================================;


-- =============================================================================
-- PARTE 4: BATERÍA DE PRUEBAS Y DEMOSTRACIÓN DE RESULTADOS
-- =============================================================================

PROMPT >>> PRUEBA 1: Ejecutando SP_COMPLETAR_ORDEN_PRODUCCION (Orden #2)...;
DECLARE
    v_lote_generado VARCHAR2(50);
BEGIN
    SP_COMPLETAR_ORDEN_PRODUCCION(
        p_id_orden             => 2,
        p_cantidad_producida   => 780,
        p_fecha_vencimiento    => DATE '2026-11-30',
        p_codigo_lote_generado => v_lote_generado
    );
    DBMS_OUTPUT.PUT_LINE('Resultado de Prueba SP 1: Código de Lote = ' || v_lote_generado);
END;
/

-- Comprobar estado de la orden y el lote recién insertado
SELECT id_orden, codigo_orden, cantidad_a_producir, estado, fecha_fin 
FROM ORDENES_PRODUCCION WHERE id_orden = 2;

SELECT id_lote, id_orden, codigo_lote, fecha_vencimiento, stock_disponible 
FROM LOTES_PRODUCTO WHERE id_orden = 2;


PROMPT >>> PRUEBA 2: Ejecutando SP_CERRAR_SESION_CAJA_ARQUEO (Sesión #1)...;
DECLARE
    v_diferencia NUMBER;
BEGIN
    SP_CERRAR_SESION_CAJA_ARQUEO(
        p_id_sesion          => 1,
        p_saldo_real_contado => 395.00, -- Conteo físico de caja (da faltante de S/ -5.00)
        p_observaciones      => 'Cierre de turno tarde con arqueo conforme (-5 soles ajuste de sencillo)',
        p_diferencia_out     => v_diferencia
    );
    DBMS_OUTPUT.PUT_LINE('Resultado de Prueba SP 2: Diferencia Arqueo = S/ ' || v_diferencia);
END;
/

-- Comprobar cierre de la sesión
SELECT id_sesion, codigo_sesion, saldo_inicial, total_ingresos_efectivo, 
       total_egresos_efectivo, saldo_esperado_efectivo, saldo_real_efectivo, 
       diferencia_arqueo, estado, fecha_cierre
FROM SESIONES_CAJA WHERE id_sesion = 1;


PROMPT >>> PRUEBA 3: Consultando FUNCIÓN 1 (FN_CALCULAR_COSTO_RECETA_BOM)...;
SELECT r.id_receta, r.nombre_receta,
       r.costo_estimado_unitario AS costo_registrado,
       FN_CALCULAR_COSTO_RECETA_BOM(r.id_receta) AS costo_calculado_formula
FROM RECETAS_BOM r;


PROMPT >>> PRUEBA 4: Consultando FUNCIÓN 2 (FN_OBTENER_LOTE_FEFO_PRIORITARIO)...;
-- Busca el lote de Quinua Blanca (id_producto = 1) para despachar 15 unidades
SELECT p.id_producto, p.nombre AS producto,
       FN_OBTENER_LOTE_FEFO_PRIORITARIO(p.id_producto, 15) AS id_lote_fefo_sugerido
FROM PRODUCTOS p
WHERE p.id_producto IN (1, 4);


PROMPT >>> PRUEBA 5: Demostrando TRIGGER 1 (TRG_DESCONTAR_STOCK_LOTE_VENTA)...;
-- Consultamos el stock actual del lote 1 antes de vender
SELECT id_lote, codigo_lote, stock_disponible FROM LOTES_PRODUCTO WHERE id_lote = 1;

-- Insertamos un detalle de venta descontando 8 unidades del lote 1
INSERT INTO DETALLES_VENTA (id_venta, id_producto, id_lote, cantidad, precio_unitario, subtotal)
VALUES (2, 1, 1, 8, 14.50, 116.00);

-- Verificamos que el stock_disponible del lote 1 bajó automáticamente
SELECT id_lote, codigo_lote, stock_disponible AS stock_tras_disparo_trigger 
FROM LOTES_PRODUCTO WHERE id_lote = 1;


PROMPT >>> PRUEBA 6: Demostrando TRIGGER 2 (TRG_CALCULAR_SUBTOTAL_COMPRA)...;
-- Insertamos detalle de compra sin especificar subtotal (o con valor 0)
INSERT INTO DETALLES_COMPRA (id_compra, id_insumo, cantidad, costo_unitario, subtotal)
VALUES (1, 4, 150.00, 4.2000, 0.00);

-- Comprobamos que el trigger calculó automáticamente 150 * 4.20 = 630.00
SELECT id_detalle_compra, id_compra, cantidad, costo_unitario, subtotal AS subtotal_calculado_por_trigger
FROM DETALLES_COMPRA
WHERE id_compra = 1 AND id_insumo = 4;

COMMIT;

PROMPT =============================================================================;
PROMPT  TODOS LOS PROCEDIMIENTOS, FUNCIONES Y TRIGGERS OPERATIVOS AL 100%           ;
PROMPT =============================================================================;
