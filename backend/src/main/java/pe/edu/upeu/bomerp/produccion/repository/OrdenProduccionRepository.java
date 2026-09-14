package pe.edu.upeu.bomerp.produccion.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.produccion.entity.OrdenProduccion;

import java.util.List;
import java.util.Optional;

public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccion, Long> {

    @Override
    @EntityGraph(attributePaths = "insumosConsumidos")
    List<OrdenProduccion> findAll();

    @Override
    @EntityGraph(attributePaths = "insumosConsumidos")
    Optional<OrdenProduccion> findById(Long id);
}
