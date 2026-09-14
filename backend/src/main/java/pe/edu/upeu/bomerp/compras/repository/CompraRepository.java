package pe.edu.upeu.bomerp.compras.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.compras.entity.Compra;

import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    @Override
    @EntityGraph(attributePaths = {"proveedor", "detalles"})
    List<Compra> findAll();

    @Override
    @EntityGraph(attributePaths = {"proveedor", "detalles"})
    Optional<Compra> findById(Long id);
}
