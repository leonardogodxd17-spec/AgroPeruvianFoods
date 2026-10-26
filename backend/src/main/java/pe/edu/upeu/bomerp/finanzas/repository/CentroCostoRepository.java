package pe.edu.upeu.bomerp.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.bomerp.finanzas.entity.CentroCosto;

import java.util.List;
import java.util.Optional;

@Repository
public interface CentroCostoRepository extends JpaRepository<CentroCosto, Long> {

    List<CentroCosto> findByActivoTrueOrderByCodigoCcAsc();

    Optional<CentroCosto> findByCodigoCc(String codigoCc);

    boolean existsByCodigoCc(String codigoCc);
}
