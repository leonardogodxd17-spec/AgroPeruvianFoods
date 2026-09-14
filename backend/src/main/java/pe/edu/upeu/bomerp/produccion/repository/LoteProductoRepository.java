package pe.edu.upeu.bomerp.produccion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.produccion.entity.LoteProducto;

import java.util.List;

public interface LoteProductoRepository extends JpaRepository<LoteProducto, Long> {

    // Consulta FEFO (First Expired, First Out): recupera lotes disponibles ordenados por fecha de vencimiento más próxima
    List<LoteProducto> findByProductoIdAndStockDisponibleGreaterThanOrderByFechaVencimientoAsc(Long productoId, Integer stockMinimo);

    List<LoteProducto> findByProductoId(Long productoId);
}
