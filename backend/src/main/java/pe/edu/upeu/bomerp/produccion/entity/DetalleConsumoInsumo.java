package pe.edu.upeu.bomerp.produccion.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "DETALLES_CONSUMO_INSUMO", schema = "BOM_PRODUCCION")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleConsumoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_ORDEN_PRODUCCION", nullable = false)
    private OrdenProduccion ordenProduccion;

    @Column(name = "ID_INSUMO", nullable = false)
    private Long insumoId;

    @Column(name = "NOMBRE_INSUMO", nullable = false, length = 120)
    private String nombreInsumo;

    @Column(name = "CANTIDAD_REQUERIDA", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadRequerida;

    @Column(name = "CANTIDAD_CONSUMIDA", nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidadConsumida;

    @Column(name = "UNIDAD_MEDIDA", nullable = false, length = 20)
    private String unidadMedida;
}
