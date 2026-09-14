package pe.edu.upeu.bomerp.catalogo.producto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.bomerp.catalogo.categoria.entity.Categoria;
import pe.edu.upeu.bomerp.catalogo.categoria.repository.CategoriaRepository;
import pe.edu.upeu.bomerp.catalogo.producto.dto.ProductoRequest;
import pe.edu.upeu.bomerp.catalogo.producto.dto.ProductoResponse;
import pe.edu.upeu.bomerp.catalogo.producto.entity.Producto;
import pe.edu.upeu.bomerp.catalogo.producto.repository.ProductoRepository;
import pe.edu.upeu.bomerp.catalogo.producto.service.ProductoServiceImpl;
import pe.edu.upeu.bomerp.exception.BusinessConflictException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Categoria categoriaGranos;
    private Producto productoQuinua;

    @BeforeEach
    void setUp() {
        categoriaGranos = Categoria.builder()
                .id(1L)
                .nombre("Granos Andinos")
                .descripcion("Granos originarios de alto valor nutricional")
                .build();

        productoQuinua = Producto.builder()
                .id(10L)
                .nombre("Quinua Blanca Real 1kg")
                .precio(new BigDecimal("14.50"))
                .stock(100)
                .categoria(categoriaGranos)
                .build();
    }

    @Test
    @DisplayName("Debe crear un producto correctamente si la categoría existe")
    void crear_conCategoriaValida_debeRetornarProductoResponse() {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Quinua Roja 1kg");
        request.setPrecio(new BigDecimal("16.00"));
        request.setStock(50);
        request.setCategoriaId(1L);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaGranos));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setId(11L);
            return p;
        });

        ProductoResponse response = productoService.crear(request);

        assertNotNull(response);
        assertEquals("Quinua Roja 1kg", response.getNombre());
        assertEquals(new BigDecimal("16.00"), response.getPrecio());
        assertEquals(50, response.getStock());
        assertEquals(1L, response.getCategoria().id());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si la categoría asignada no existe")
    void crear_conCategoriaInexistente_debeLanzarExcepcion() {
        ProductoRequest request = new ProductoRequest();
        request.setNombre("Producto Sin Categoria");
        request.setPrecio(new BigDecimal("10.00"));
        request.setStock(20);
        request.setCategoriaId(99L);

        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productoService.crear(request));
        verify(productoRepository, never()).save(any(Producto.class));
    }

    @Test
    @DisplayName("Debe descontar stock atómicamente cuando el inventario es suficiente")
    void descontarStock_conStockSuficiente_debeActualizarStock() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoQuinua));

        productoService.descontarStock(10L, 30);

        assertEquals(70, productoQuinua.getStock());
        verify(productoRepository, times(1)).save(productoQuinua);
    }

    @Test
    @DisplayName("Debe lanzar BusinessConflictException si el stock disponible es insuficiente")
    void descontarStock_conStockInsuficiente_debeLanzarBusinessConflictException() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoQuinua));

        assertThrows(BusinessConflictException.class, () -> productoService.descontarStock(10L, 150));
        assertEquals(100, productoQuinua.getStock());
        verify(productoRepository, never()).save(productoQuinua);
    }

    @Test
    @DisplayName("Debe aumentar stock correctamente al recibir producción o compra")
    void aumentarStock_debeIncrementarCantidad() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoQuinua));

        productoService.aumentarStock(10L, 50);

        assertEquals(150, productoQuinua.getStock());
        verify(productoRepository, times(1)).save(productoQuinua);
    }
}
