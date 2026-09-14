package pe.edu.upeu.bomerp.caja.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.caja.entity.MovimientoCaja;

import java.util.List;

public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {
    List<MovimientoCaja> findBySesionCajaId(Long sesionCajaId);
}
