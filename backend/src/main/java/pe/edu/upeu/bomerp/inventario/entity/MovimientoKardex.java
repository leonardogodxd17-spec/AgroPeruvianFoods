package pe.edu.upeu.bomerp.inventario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_MOVIMIENTOS_KARDEX")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoKardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 20)
    private TipoMovimientoKardex tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LOTE_ID")
    private Lote lote;

    @Column(name = "PRODUCTO_ID", nullable = false)
    private Long productoId;

    @Column(name = "PRODUCTO_NOMBRE", nullable = false, length = 150)
    private String productoNombre;

    @Column(name = "CANTIDAD", nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "SALDO_STOCK_RESULTANTE", nullable = false, precision = 12, scale = 2)
    private BigDecimal saldoStockResultante;

    @Column(name = "MOTIVO", nullable = false, length = 100)
    private String motivo;

    @Column(name = "DOCUMENTO_REFERENCIA", length = 100)
    private String documentoReferencia;

    @Column(name = "FECHA_HORA", nullable = false)
    private LocalDateTime fechaHora;
}
