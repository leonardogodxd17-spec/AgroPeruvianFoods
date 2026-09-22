package pe.edu.upeu.bomerp.produccion.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.bomerp.produccion.entity.EstadoOrdenProduccion;
import pe.edu.upeu.bomerp.produccion.entity.OrdenProduccion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccion, Long> {

    @Override
    @EntityGraph(attributePaths = "insumosConsumidos")
    List<OrdenProduccion> findAll();

    @Override
    @EntityGraph(attributePaths = "insumosConsumidos")
    Optional<OrdenProduccion> findById(Long id);

    @EntityGraph(attributePaths = "insumosConsumidos")
    @Query("SELECT o FROM OrdenProduccion o WHERE (:estado IS NULL OR o.estado = :estado) " +
           "AND (CAST(:desde AS timestamp) IS NULL OR o.fechaInicio >= :desde) " +
           "AND (CAST(:hasta AS timestamp) IS NULL OR o.fechaInicio <= :hasta) " +
           "ORDER BY o.fechaInicio DESC, o.id DESC")
    List<OrdenProduccion> buscar(@Param("estado") EstadoOrdenProduccion estado,
                                 @Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta);
}
