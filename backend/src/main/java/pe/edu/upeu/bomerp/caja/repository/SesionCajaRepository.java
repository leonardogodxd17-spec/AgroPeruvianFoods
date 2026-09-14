package pe.edu.upeu.bomerp.caja.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.caja.entity.EstadoSesionCaja;
import pe.edu.upeu.bomerp.caja.entity.SesionCaja;

import java.util.Optional;

public interface SesionCajaRepository extends JpaRepository<SesionCaja, Long> {

    @EntityGraph(attributePaths = "movimientos")
    Optional<SesionCaja> findFirstByEstadoOrderByFechaAperturaDesc(EstadoSesionCaja estado);

    @Override
    @EntityGraph(attributePaths = "movimientos")
    Optional<SesionCaja> findById(Long id);
}
