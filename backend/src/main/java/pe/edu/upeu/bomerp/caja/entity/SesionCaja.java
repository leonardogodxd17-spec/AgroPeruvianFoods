package pe.edu.upeu.bomerp.caja.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SESIONES_CAJA", schema = "BOM_CAJA")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SesionCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CAJERO_NOMBRE", nullable = false, length = 100)
    private String cajeroNombre;

    @Column(name = "FECHA_APERTURA", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "FECHA_CIERRE")
    private LocalDateTime fechaCierre;

    @Column(name = "MONTO_APERTURA", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoApertura;

    @Column(name = "MONTO_CIERRE", precision = 12, scale = 2)
    private BigDecimal montoCierre;

    @Column(name = "SALDO_TEORICO", precision = 12, scale = 2)
    private BigDecimal saldoTeorico;

    @Column(name = "SALDO_REAL", precision = 12, scale = 2)
    private BigDecimal saldoReal;

    @Column(name = "DIFERENCIA", precision = 12, scale = 2)
    private BigDecimal diferencia;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO", nullable = false, length = 20)
    private EstadoSesionCaja estado;

    @Column(name = "OBSERVACIONES", length = 300)
    private String observaciones;

    @Builder.Default
    @OneToMany(mappedBy = "sesionCaja", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimientoCaja> movimientos = new ArrayList<>();
}
