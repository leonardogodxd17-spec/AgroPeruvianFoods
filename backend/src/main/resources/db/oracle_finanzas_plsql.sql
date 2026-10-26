-- ============================================================================
-- PROYECTO BOMERP (Agro Peruvian Foods) - MÓDULO DE FINANZAS
-- SCRIPT DDL Y ESTRUCTURA DE RELACIONES MUCHOS A MUCHOS (M:N) CON CLAVE COMPUESTA
-- ============================================================================
-- 1. Relación M:N N° 1 (Caja Diaria / Turnos POS):
--    SESIONES_CAJA <--(M:N)--> DENOMINACIONES_MONEDA vía DETALLES_ARQUEO_EFECTIVO
-- 2. Relación M:N N° 2 (Fondos Fijos / Gastos Menores):
--    CAJAS_CHICAS <--(M:N)--> CENTROS_COSTO vía ASIGNACIONES_CAJA_CENTRO_COSTO
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. TABLA CATÁLOGO: DENOMINACIONES_MONEDA (Billetes y Monedas del Perú)
-- ----------------------------------------------------------------------------
CREATE TABLE DENOMINACIONES_MONEDA (
    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    VALOR_FACIAL NUMBER(8, 2) NOT NULL,
    TIPO VARCHAR2(15) NOT NULL,
    ORDEN NUMBER(4) NOT NULL,
    ACTIVO NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT CK_DENOM_VALOR_POSITIVO CHECK (VALOR_FACIAL > 0),
    CONSTRAINT CK_DENOM_TIPO CHECK (TIPO IN ('BILLETE', 'MONEDA')),
    CONSTRAINT CK_DENOM_ACTIVO CHECK (ACTIVO IN (0, 1))
);

COMMENT ON TABLE DENOMINACIONES_MONEDA IS 'Catálogo maestro de denominaciones monetarias oficiales (billetes y monedas) para arqueo físico';
COMMENT ON COLUMN DENOMINACIONES_MONEDA.VALOR_FACIAL IS 'Valor nominal de la moneda o billete (S/ 200, 100, 50, etc.)';
COMMENT ON COLUMN DENOMINACIONES_MONEDA.TIPO IS 'Clasificación física: BILLETE o MONEDA';
COMMENT ON COLUMN DENOMINACIONES_MONEDA.ORDEN IS 'Orden jerárquico de visualización en interfaz de caja';

-- ----------------------------------------------------------------------------
-- 2. TABLA INTERMEDIA M:N N° 1: DETALLES_ARQUEO_EFECTIVO (Clave Compuesta)
-- ----------------------------------------------------------------------------
CREATE TABLE DETALLES_ARQUEO_EFECTIVO (
    SESION_ID NUMBER NOT NULL,
    DENOMINACION_ID NUMBER NOT NULL,
    CANTIDAD NUMBER(8) NOT NULL,
    SUBTOTAL NUMBER(12, 2) NOT NULL,
    CONSTRAINT PK_DETALLES_ARQUEO_EFECTIVO PRIMARY KEY (SESION_ID, DENOMINACION_ID),
    CONSTRAINT FK_ARQUEO_SESION FOREIGN KEY (SESION_ID) 
        REFERENCES SESIONES_CAJA (ID) ON DELETE CASCADE,
    CONSTRAINT FK_ARQUEO_DENOMINACION FOREIGN KEY (DENOMINACION_ID) 
        REFERENCES DENOMINACIONES_MONEDA (ID),
    CONSTRAINT CK_ARQUEO_CANTIDAD CHECK (CANTIDAD >= 0),
    CONSTRAINT CK_ARQUEO_SUBTOTAL CHECK (SUBTOTAL >= 0)
);

COMMENT ON TABLE DETALLES_ARQUEO_EFECTIVO IS 'Tabla intermedia M:N entre SesionCaja y DenominacionMoneda que almacena el conteo físico de gaveta al cierre de turno';
COMMENT ON COLUMN DETALLES_ARQUEO_EFECTIVO.SESION_ID IS 'Identificador de la sesión de caja sellada';
COMMENT ON COLUMN DETALLES_ARQUEO_EFECTIVO.DENOMINACION_ID IS 'Identificador de la denominación contada';
COMMENT ON COLUMN DETALLES_ARQUEO_EFECTIVO.CANTIDAD IS 'Unidades físicas contadas de esta denominación';
COMMENT ON COLUMN DETALLES_ARQUEO_EFECTIVO.SUBTOTAL IS 'Monto total en soles aportado por esta denominación (cantidad * valorFacial)';

-- ----------------------------------------------------------------------------
-- 3. TABLA CATÁLOGO: CENTROS_COSTO (Líneas de Producción y Áreas de Imputación)
-- ----------------------------------------------------------------------------
CREATE TABLE CENTROS_COSTO (
    ID NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    CODIGO_CC VARCHAR2(30) NOT NULL UNIQUE,
    NOMBRE_AREA VARCHAR2(100) NOT NULL,
    ACTIVO NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT CK_CC_ACTIVO CHECK (ACTIVO IN (0, 1))
);

COMMENT ON TABLE CENTROS_COSTO IS 'Catálogo de Centros de Costo para asignación presupuestal y trazabilidad analítica de gastos';
COMMENT ON COLUMN CENTROS_COSTO.CODIGO_CC IS 'Código mnemotécnico único (ej. CC-COCO, CC-CACAO, CC-LOGISTICA)';
COMMENT ON COLUMN CENTROS_COSTO.NOMBRE_AREA IS 'Nombre descriptivo del área o línea productiva beneficiaria';

-- ----------------------------------------------------------------------------
-- 4. TABLA INTERMEDIA M:N N° 2: ASIGNACIONES_CAJA_CENTRO_COSTO (Clave Compuesta)
-- ----------------------------------------------------------------------------
CREATE TABLE ASIGNACIONES_CAJA_CENTRO_COSTO (
    CAJA_CHICA_ID NUMBER NOT NULL,
    CENTRO_COSTO_ID NUMBER NOT NULL,
    PRESUPUESTO_ASIGNADO NUMBER(12, 2) NOT NULL,
    SALDO_DISPONIBLE NUMBER(12, 2) NOT NULL,
    FECHA_ASIGNACION DATE DEFAULT SYSDATE NOT NULL,
    ACTIVO NUMBER(1) DEFAULT 1 NOT NULL,
    CONSTRAINT PK_ASIGN_CAJA_CC PRIMARY KEY (CAJA_CHICA_ID, CENTRO_COSTO_ID),
    CONSTRAINT FK_ASIGN_CAJA_CHICA FOREIGN KEY (CAJA_CHICA_ID) 
        REFERENCES CAJAS_CHICAS (ID) ON DELETE CASCADE,
    CONSTRAINT FK_ASIGN_CENTRO_COSTO FOREIGN KEY (CENTRO_COSTO_ID) 
        REFERENCES CENTROS_COSTO (ID),
    CONSTRAINT CK_ASIGN_PRESUPUESTO CHECK (PRESUPUESTO_ASIGNADO >= 0),
    CONSTRAINT CK_ASIGN_SALDO CHECK (SALDO_DISPONIBLE >= 0),
    CONSTRAINT CK_ASIGN_SALDO_LE_PRESUP CHECK (SALDO_DISPONIBLE <= PRESUPUESTO_ASIGNADO),
    CONSTRAINT CK_ASIGN_ACTIVO CHECK (ACTIVO IN (0, 1))
);

COMMENT ON TABLE ASIGNACIONES_CAJA_CENTRO_COSTO IS 'Tabla intermedia M:N entre CajaChica y CentroCosto para techos presupuestales y control de liquidez';
COMMENT ON COLUMN ASIGNACIONES_CAJA_CENTRO_COSTO.CAJA_CHICA_ID IS 'Identificador del fondo de Caja Chica asignador';
COMMENT ON COLUMN ASIGNACIONES_CAJA_CENTRO_COSTO.CENTRO_COSTO_ID IS 'Centro de Costo al que se le autoriza el presupuesto';
COMMENT ON COLUMN ASIGNACIONES_CAJA_CENTRO_COSTO.PRESUPUESTO_ASIGNADO IS 'Límite presupuestal asignado a esta línea';
COMMENT ON COLUMN ASIGNACIONES_CAJA_CENTRO_COSTO.SALDO_DISPONIBLE IS 'Remanente en soles disponible para rendiciones (disminuye con cada comprobante)';

-- ----------------------------------------------------------------------------
-- 5. RELACIÓN CON DETALLES DE RENDICIÓN (IMPUTACIÓN DE GASTO A CENTRO DE COSTO)
-- ----------------------------------------------------------------------------
-- ALTER TABLE DETALLES_RENDICION_CAJA_CHICA ADD ID_CENTRO_COSTO NUMBER;
-- ALTER TABLE DETALLES_RENDICION_CAJA_CHICA ADD CONSTRAINT FK_DETALLE_REND_CC
--     FOREIGN KEY (ID_CENTRO_COSTO) REFERENCES CENTROS_COSTO (ID);
