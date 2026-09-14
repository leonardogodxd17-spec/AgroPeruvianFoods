package pe.edu.upeu.bomerp.produccion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.produccion.entity.DetalleConsumoInsumo;

import java.util.List;

public interface DetalleConsumoInsumoRepository extends JpaRepository<DetalleConsumoInsumo, Long> {
    List<DetalleConsumoInsumo> findByOrdenProduccionId(Long ordenId);
}
