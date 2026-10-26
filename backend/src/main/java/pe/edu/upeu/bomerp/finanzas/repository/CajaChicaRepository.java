package pe.edu.upeu.bomerp.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.bomerp.finanzas.entity.CajaChica;

import java.util.Optional;

@Repository
public interface CajaChicaRepository extends JpaRepository<CajaChica, Long> {
    Optional<CajaChica> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
