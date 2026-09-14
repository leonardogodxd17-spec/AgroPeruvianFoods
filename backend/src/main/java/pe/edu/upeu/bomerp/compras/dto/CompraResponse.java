package pe.edu.upeu.bomerp.compras.dto;

import lombok.*;
import pe.edu.upeu.bomerp.compras.entity.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponse {
    private Long id;
    private String numeroComprobante;
    private ProveedorResponse proveedor;
    private LocalDate fechaEmision;
    private EstadoCompra estado;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private BigDecimal saldoPendiente;
    private List<DetalleCompraResponse> detalles;
}
