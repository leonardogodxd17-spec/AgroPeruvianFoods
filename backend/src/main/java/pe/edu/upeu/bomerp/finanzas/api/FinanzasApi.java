package pe.edu.upeu.bomerp.finanzas.api;

import pe.edu.upeu.bomerp.finanzas.entity.MetodoPago;

import java.math.BigDecimal;

/**
 * Puerto público de Spring Modulith para el módulo de Finanzas.
 * Permite a otros módulos del ERP (ej: Ventas y Despacho) registrar cobros y consultar el
 * estado del turno de caja sin acoplarse directamente a repositorios ni entidades JPA internas.
 */
public interface FinanzasApi {

    /**
     * Registra un cobro transaccional originado en una venta sobre la sesión activa del usuario.
     *
     * @param usuarioId        Identificador del cajero/usuario
     * @param monto            Importe monetario cobrado
     * @param metodoPago       Método de pago (EFECTIVO, YAPE, PLIN, TRANSFERENCIA, TARJETA)
     * @param referenciaVenta  Código de la venta (ej: "VTA-00123")
     * @param categoriaId      ID opcional de categoría financiera (si es null se usa categoría por defecto de ventas)
     * @return Identificador del movimiento de caja registrado
     */
    Long registrarCobroVenta(Long usuarioId, BigDecimal monto, MetodoPago metodoPago, String referenciaVenta, Long categoriaId);

    /**
     * Verifica si el usuario dispone de un turno de caja en estado ABIERTO.
     */
    boolean tieneSesionAbierta(Long usuarioId);

    /**
     * Retorna el ID de la sesión activa del usuario, o lanza excepción si no existe.
     */
    Long obtenerIdSesionActiva(Long usuarioId);
}
