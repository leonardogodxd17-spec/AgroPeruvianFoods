package pe.edu.upeu.bomerp.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.bomerp.finanzas.entity.AbonoCuentaPorCobrar;

import java.util.List;

@Repository
public interface AbonoCuentaPorCobrarRepository extends JpaRepository<AbonoCuentaPorCobrar, Long> {
    List<AbonoCuentaPorCobrar> findByCuentaPorCobrarIdOrderByFechaDesc(Long cuentaPorCobrarId);
}
