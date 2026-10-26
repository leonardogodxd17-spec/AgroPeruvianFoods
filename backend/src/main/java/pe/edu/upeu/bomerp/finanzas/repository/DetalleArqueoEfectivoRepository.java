package pe.edu.upeu.bomerp.finanzas.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.bomerp.finanzas.entity.DetalleArqueoEfectivo;
import pe.edu.upeu.bomerp.finanzas.entity.DetalleArqueoEfectivoId;

import java.util.List;

@Repository
public interface DetalleArqueoEfectivoRepository extends JpaRepository<DetalleArqueoEfectivo, DetalleArqueoEfectivoId> {

    List<DetalleArqueoEfectivo> findByIdSesionId(Long sesionId);

    List<DetalleArqueoEfectivo> findBySesionCajaId(Long sesionId);
}
