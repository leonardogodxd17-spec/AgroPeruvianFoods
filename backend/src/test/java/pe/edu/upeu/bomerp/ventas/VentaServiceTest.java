package pe.edu.upeu.bomerp.ventas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.bomerp.catalogo.categoria.dto.CategoriaResumen;
import pe.edu.upeu.bomerp.catalogo.producto.dto.ProductoResponse;
import pe.edu.upeu.bomerp.catalogo.producto.service.ProductoService;
import pe.edu.upeu.bomerp.exception.BusinessConflictException;
import pe.edu.upeu.bomerp.ventas.dto.DetalleVentaRequest;
import pe.edu.upeu.bomerp.ventas.dto.VentaRequest;
import pe.edu.upeu.bomerp.ventas.dto.VentaResponse;
import pe.edu.upeu.bomerp.ventas.entity.EstadoVenta;
import pe.edu.upeu.bomerp.ventas.entity.MetodoPagoVenta;
import pe.edu.upeu.bomerp.ventas.entity.Venta;
import pe.edu.upeu.bomerp.ventas.repository.VentaRepository;
import pe.edu.upeu.bomerp.ventas.service.VentaServiceImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private ProductoResponse mockProducto;

    @BeforeEach
    void setUp() {
        mockProducto = ProductoResponse.builder()
                .id(1L)
                .nombre("Quinua Blanca Real Orgánica 1kg")
                .precio(new BigDecimal("10.00"))
                .stock(100)
                .categoria(new CategoriaResumen(1L, "Granos Andinos"))
                .build();
    }

    @Test
    @DisplayName("Debe registrar venta cabecera-detalle calculando subtotal, IGV (18%) y total")
    void crear_ventaValida_debeCalcularTotalesYDescontarStock() {
        DetalleVentaRequest item = new DetalleVentaRequest();
        item.setProductoId(1L);
        item.setCantidad(2);

        VentaRequest request = new VentaRequest();
        request.setClienteNombre("Supermercados Wong");
        request.setMetodoPago(MetodoPagoVenta.EFECTIVO);
        request.setDetalles(List.of(item));

        when(productoService.obtener(1L)).thenReturn(mockProducto);
        doNothing().when(productoService).descontarStock(1L, 2);

        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> {
            Venta v = invocation.getArgument(0);
            v.setId(100L);
            return v;
        });

        VentaResponse response = ventaService.crear(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Supermercados Wong", response.getClienteNombre());
        assertEquals(EstadoVenta.REGISTRADA, response.getEstado());
        assertEquals(MetodoPagoVenta.EFECTIVO, response.getMetodoPago());
        
        assertEquals(new BigDecimal("20.00"), response.getTotal());
        assertEquals(new BigDecimal("3.60"), response.getIgv());
        assertEquals(new BigDecimal("16.40"), response.getSubtotal());

        verify(productoService, times(1)).descontarStock(1L, 2);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("Debe fallar y abortar la venta si el stock es insuficiente (rollback)")
    void crear_stockInsuficiente_debeLanzarExcepcion() {
        DetalleVentaRequest item = new DetalleVentaRequest();
        item.setProductoId(1L);
        item.setCantidad(500);

        VentaRequest request = new VentaRequest();
        request.setClienteNombre("Cliente Mayorista");
        request.setMetodoPago(MetodoPagoVenta.TRANSFERENCIA);
        request.setDetalles(List.of(item));

        when(productoService.obtener(1L)).thenReturn(mockProducto);
        doThrow(new BusinessConflictException("Stock insuficiente"))
                .when(productoService).descontarStock(1L, 500);

        assertThrows(BusinessConflictException.class, () -> ventaService.crear(request));
        verify(ventaRepository, never()).save(any(Venta.class));
    }
}
