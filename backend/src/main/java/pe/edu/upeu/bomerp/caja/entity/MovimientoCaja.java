package pe.edu.upeu.bomerp.caja.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "MOVIMIENTOS_CAJA", schema = "BOM_CAJA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SESION_CAJA", nullable = false)
    private SesionCaja sesionCaja;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 20)
    private TipoMovimientoCaja tipo;

    @Column(name = "MONTO", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "CONCEPTO", nullable = false, length = 200)
    private String concepto;

    @Enumerated(EnumType.STRING)
    @Column(name = "METODO_PAGO", nullable = false, length = 30)
    private MetodoPagoCaja metodoPago;

    @Column(name = "REFERENCIA", length = 100)
    private String referencia;

    @Column(name = "FECHA", nullable = false)
    private LocalDateTime fecha;
}
