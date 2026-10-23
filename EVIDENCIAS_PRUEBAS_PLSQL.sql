-- =============================================================================
-- UNIVERSIDAD PERUANA UNIÓN - FACULTAD DE INGENIERÍA Y ARQUITECTURA
-- ESCUELA PROFESIONAL DE INGENIERÍA DE SISTEMAS
-- CURSO: BASE DE DATOS II (BD2)
-- ARCHIVO: EVIDENCIAS_PRUEBAS_PLSQL.sql
-- BATERÍA COMPLETA DE PRUEBAS CON DATOS IMPLEMENTADOS
-- (Incluye estado ANTES, EJECUCIÓN y estado DESPUÉS para tomar capturas)
-- =============================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED;
SET LINESIZE 250;
SET PAGESIZE 100;
SET FEEDBACK ON;

PROMPT =============================================================================;
PROMPT  INICIANDO BATERÍA DE PRUEBAS DE OBJETOS PL/SQL (EVIDENCIAS PARA INFORME)    ;
PROMPT =============================================================================;


-- =============================================================================
-- PRUEBA 1: STORED PROCEDURE 1 (SP_COMPLETAR_ORDEN_PRODUCCION)
-- =============================================================================
PROMPT ;
PROMPT >>> [CASO 1] PRUEBA DE SP_COMPLETAR_ORDEN_PRODUCCION;
PROMPT 1.1. Estado ANTES de ejecutar el procedimiento (Orden #2):;

SELECT id_orden, codigo_orden, estado, cantidad_a_producir, fecha_inicio, fecha_fin 
FROM ORDENES_PRODUCCION 
WHERE id_orden = 2;

SELECT COUNT(*) AS lotes_existentes_orden_2 
FROM LOTES_PRODUCTO 
WHERE id_orden = 2;

PROMPT 1.2. Ejecutando SP_COMPLETAR_ORDEN_PRODUCCION para fabricar 780 unidades...;
DECLARE
    v_lote_generado VARCHAR2(50);
BEGIN
    SP_COMPLETAR_ORDEN_PRODUCCION(
        p_id_orden             => 2,
        p_cantidad_producida   => 780,
        p_fecha_vencimiento    => DATE '2026-11-30',
        p_codigo_lote_generado => v_lote_generado
    );
    DBMS_OUTPUT.PUT_LINE('--> RESULTADO EXITOSO: Lote FEFO generado: ' || v_lote_generado);
END;
/

PROMPT 1.3. Estado DESPUÉS: Comprobando orden en estado COMPLETADA y nuevo lote creado:;
SELECT id_orden, codigo_orden, estado, cantidad_a_producir, fecha_fin 
FROM ORDENES_PRODUCCION 
WHERE id_orden = 2;

SELECT id_lote, id_orden, codigo_lote, stock_disponible, fecha_produccion, fecha_vencimiento 
FROM LOTES_PRODUCTO 
WHERE id_orden = 2;


-- =============================================================================
-- PRUEBA 2: STORED PROCEDURE 2 (SP_CERRAR_SESION_CAJA_ARQUEO)
-- =============================================================================
PROMPT ;
PROMPT >>> [CASO 2] PRUEBA DE SP_CERRAR_SESION_CAJA_ARQUEO;
PROMPT 2.1. Estado ANTES de cerrar la caja (Sesión #1 y sus movimientos):;

SELECT id_sesion, codigo_sesion, saldo_inicial, estado, fecha_apertura 
FROM SESIONES_CAJA 
WHERE id_sesion = 1;

SELECT tipo, metodo_pago, monto, concepto 
FROM MOVIMIENTOS_CAJA 
WHERE id_sesion = 1;

PROMPT 2.2. Ejecutando SP_CERRAR_SESION_CAJA_ARQUEO con conteo físico de S/ 395.40...;
DECLARE
    v_diferencia NUMBER;
BEGIN
    SP_CERRAR_SESION_CAJA_ARQUEO(
        p_id_sesion          => 1,
        p_saldo_real_contado => 395.40, -- Saldo esperado = 200 + 250.40 - 50.00 = 400.40 (Faltante: -5.00)
        p_observaciones      => 'Arqueo de turno cerrado conforme con descuadre menor de S/ -5.00',
        p_diferencia_out     => v_diferencia
    );
    DBMS_OUTPUT.PUT_LINE('--> RESULTADO EXITOSO: Diferencia de Arqueo calculada = S/ ' || v_diferencia);
END;
/

PROMPT 2.3. Estado DESPUÉS: Comprobando sesión CERRADA y descuadre de arqueo registrado:;
SELECT id_sesion, estado, total_ingresos_efectivo, total_egresos_efectivo, 
       saldo_esperado_efectivo, saldo_real_efectivo, diferencia_arqueo, fecha_cierre, observaciones 
FROM SESIONES_CAJA 
WHERE id_sesion = 1;


-- =============================================================================
-- PRUEBA 3: FUNCIÓN 1 (FN_CALCULAR_COSTO_RECETA_BOM)
-- =============================================================================
PROMPT ;
PROMPT >>> [CASO 3] PRUEBA DE FN_CALCULAR_COSTO_RECETA_BOM;
PROMPT 3.1. Detalle de insumos y cantidades de la Receta #1 (Quinua Blanca 1kg):;

SELECT d.id_detalle_receta, i.nombre AS insumo, d.cantidad_requerida, 
       d.costo_unitario, (d.cantidad_requerida * d.costo_unitario) AS costo_parcial
FROM DETALLES_RECETA_BOM d
JOIN INSUMOS i ON d.id_insumo = i.id_insumo
WHERE d.id_receta = 1;

PROMPT 3.2. Ejecutando FN_CALCULAR_COSTO_RECETA_BOM en consulta SQL:;
SELECT r.id_receta, 
       r.nombre_receta,
       r.costo_estimado_unitario AS costo_en_tabla,
       FN_CALCULAR_COSTO_RECETA_BOM(r.id_receta) AS costo_calculado_por_funcion
FROM RECETAS_BOM r;


-- =============================================================================
-- PRUEBA 4: FUNCIÓN 2 (FN_OBTENER_LOTE_FEFO_PRIORITARIO)
-- =============================================================================
PROMPT ;
PROMPT >>> [CASO 4] PRUEBA DE FN_OBTENER_LOTE_FEFO_PRIORITARIO;
PROMPT 4.1. Lotes vigentes en inventario para Quinua Blanca (Producto #1):;

SELECT id_lote, id_producto, codigo_lote, stock_disponible, fecha_vencimiento,
       ROUND(fecha_vencimiento - SYSDATE) AS dias_para_vencer
FROM LOTES_PRODUCTO
WHERE id_producto = 1
ORDER BY fecha_vencimiento ASC;

PROMPT 4.2. Ejecutando la función FEFO para solicitar 15 unidades del producto #1:;
SELECT p.id_producto, 
       p.nombre AS producto,
       FN_OBTENER_LOTE_FEFO_PRIORITARIO(p.id_producto, 15) AS id_lote_fefo_sugerido
FROM PRODUCTOS p
WHERE p.id_producto IN (1, 4);


-- =============================================================================
-- PRUEBA 5: TRIGGER 1 (TRG_DESCONTAR_STOCK_LOTE_VENTA)
-- =============================================================================
PROMPT ;
PROMPT >>> [CASO 5] PRUEBA DE TRG_DESCONTAR_STOCK_LOTE_VENTA;
PROMPT 5.1. Stock disponible del Lote #1 ANTES de la venta:;

SELECT id_lote, codigo_lote, stock_disponible 
FROM LOTES_PRODUCTO 
WHERE id_lote = 1;

PROMPT 5.2. Registrando una venta de 8 unidades del Lote #1...;
INSERT INTO DETALLES_VENTA (id_venta, id_producto, id_lote, cantidad, precio_unitario, subtotal)
VALUES (2, 1, 1, 8, 14.50, 116.00);

PROMPT 5.3. Stock disponible del Lote #1 DESPUÉS de la venta (Disparado por Trigger):;
SELECT id_lote, codigo_lote, stock_disponible AS stock_descontado_por_trigger 
FROM LOTES_PRODUCTO 
WHERE id_lote = 1;

PROMPT 5.4. Prueba de Bloqueo por Stock Insuficiente (Intento de sobreventa de 9999 unidades):;
BEGIN
    INSERT INTO DETALLES_VENTA (id_venta, id_producto, id_lote, cantidad, precio_unitario, subtotal)
    VALUES (2, 1, 1, 9999, 14.50, 144985.50);
    DBMS_OUTPUT.PUT_LINE('ERROR: El trigger debió bloquear la venta.');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('--> BLOQUEO CORRECTO DEL TRIGGER: ' || SQLERRM);
END;
/


-- =============================================================================
-- PRUEBA 6: TRIGGER 2 (TRG_CALCULAR_SUBTOTAL_COMPRA)
-- =============================================================================
PROMPT ;
PROMPT >>> [CASO 6] PRUEBA DE TRG_CALCULAR_SUBTOTAL_COMPRA;
PROMPT 6.1. Insertando ítem de compra (150 unidades a S/ 4.20) enviando subtotal = 0...;

INSERT INTO DETALLES_COMPRA (id_compra, id_insumo, cantidad, costo_unitario, subtotal)
VALUES (1, 4, 150.00, 4.2000, 0.00);

PROMPT 6.2. Comprobando que el Trigger calculó automáticamente el subtotal (150 * 4.20 = 630.00):;
SELECT id_detalle_compra, id_compra, cantidad, costo_unitario, 
       subtotal AS subtotal_calculado_automaticamente 
FROM DETALLES_COMPRA
WHERE id_compra = 1 AND id_insumo = 4;

COMMIT;

PROMPT =============================================================================;
PROMPT  BATERÍA DE PRUEBAS COMPLETADA EXITOSAMENTE (TODAS LAS REGLAS FUNCIONAN)    ;
PROMPT =============================================================================;
