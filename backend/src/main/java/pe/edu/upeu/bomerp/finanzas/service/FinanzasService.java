package pe.edu.upeu.bomerp.finanzas.service;

import pe.edu.upeu.bomerp.finanzas.dto.CategoriaFinancieraRequest;
import pe.edu.upeu.bomerp.finanzas.dto.CategoriaFinancieraResponse;
import pe.edu.upeu.bomerp.finanzas.dto.FlujoCajaResumenResponse;
import pe.edu.upeu.bomerp.finanzas.dto.TransaccionFinancieraRequest;
import pe.edu.upeu.bomerp.finanzas.dto.TransaccionFinancieraResponse;
import pe.edu.upeu.bomerp.finanzas.entity.EstadoFinanciero;
import pe.edu.upeu.bomerp.finanzas.entity.TipoFinanciero;

import java.util.List;

public interface FinanzasService {
    // Categorías Financieras (Entidad de Clasificación)
    List<CategoriaFinancieraResponse> listarCategorias(TipoFinanciero tipo);
    CategoriaFinancieraResponse obtenerCategoria(Long id);
    CategoriaFinancieraResponse crearCategoria(CategoriaFinancieraRequest request);
    CategoriaFinancieraResponse actualizarCategoria(Long id, CategoriaFinancieraRequest request);
    void eliminarCategoria(Long id);

    // Transacciones Financieras (Entidad Principal con Navegación Controlada)
    List<TransaccionFinancieraResponse> listarTransacciones(TipoFinanciero tipo, EstadoFinanciero estado, Long categoriaFinancieraId);
    List<TransaccionFinancieraResponse> listarTransaccionesPorCategoria(Long categoriaFinancieraId);
    TransaccionFinancieraResponse obtenerTransaccion(Long id);
    TransaccionFinancieraResponse crearTransaccion(TransaccionFinancieraRequest request);
    TransaccionFinancieraResponse actualizarTransaccion(Long id, TransaccionFinancieraRequest request);
    TransaccionFinancieraResponse cambiarEstadoTransaccion(Long id, EstadoFinanciero nuevoEstado);
    void eliminarTransaccion(Long id);
    FlujoCajaResumenResponse obtenerResumenFlujoCaja();
}
