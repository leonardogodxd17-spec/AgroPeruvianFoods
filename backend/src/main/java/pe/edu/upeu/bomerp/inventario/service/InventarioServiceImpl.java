package pe.edu.upeu.bomerp.inventario.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.upeu.bomerp.catalogo.producto.entity.Producto;
import pe.edu.upeu.bomerp.catalogo.producto.repository.ProductoRepository;
import pe.edu.upeu.bomerp.inventario.dto.DescuentoFefoRequest;
import pe.edu.upeu.bomerp.inventario.dto.DescuentoFefoResponse;
import pe.edu.upeu.bomerp.inventario.dto.LoteCreateDto;
import pe.edu.upeu.bomerp.inventario.dto.LoteDescontadoItem;
import pe.edu.upeu.bomerp.inventario.dto.LoteDto;
import pe.edu.upeu.bomerp.inventario.dto.MovimientoKardexDto;
import pe.edu.upeu.bomerp.inventario.entity.EstadoLote;
import pe.edu.upeu.bomerp.inventario.entity.Lote;
import pe.edu.upeu.bomerp.inventario.entity.MovimientoKardex;
import pe.edu.upeu.bomerp.inventario.entity.TipoMovimientoKardex;
import pe.edu.upeu.bomerp.inventario.repository.LoteRepository;
import pe.edu.upeu.bomerp.inventario.repository.MovimientoKardexRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final LoteRepository loteRepository;
    private final MovimientoKardexRepository kardexRepository;
    private final ProductoRepository productoRepository;

    @Override
    public LoteDto registrarLote(LoteCreateDto dto) {
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con ID: " + dto.getProductoId()));

        if (loteRepository.findByCodigoLote(dto.getCodigoLote()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un lote registrado con el código: " + dto.getCodigoLote());
        }

        if (dto.getFechaVencimiento().isBefore(dto.getFechaFabricacion())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de vencimiento no puede ser anterior a la fecha de fabricación");
        }

        EstadoLote estado = calcularEstadoLote(dto.getFechaVencimiento(), dto.getStockInicial());

        Lote lote = Lote.builder()
                .codigoLote(dto.getCodigoLote().trim().toUpperCase())
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .fechaFabricacion(dto.getFechaFabricacion())
                .fechaVencimiento(dto.getFechaVencimiento())
                .stockInicial(dto.getStockInicial())
                .stockActual(dto.getStockInicial())
                .costoUnitario(dto.getCostoUnitario())
                .estado(estado)
                .observaciones(dto.getObservaciones())
                .build();

        Lote guardado = loteRepository.save(lote);

        // Registro en Kardex (ENTRADA por ingreso de lote a almacén)
        MovimientoKardex kardex = MovimientoKardex.builder()
                .tipo(TipoMovimientoKardex.ENTRADA)
                .lote(guardado)
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .cantidad(dto.getStockInicial())
                .saldoStockResultante(dto.getStockInicial())
                .motivo("INGRESO_LOTE_PRODUCCION")
                .documentoReferencia("LOTE-" + guardado.getCodigoLote())
                .fechaHora(LocalDateTime.now())
                .build();
        kardexRepository.save(kardex);

        // Actualizar stock consolidado del producto
        int nuevoStock = (producto.getStock() != null ? producto.getStock() : 0) + dto.getStockInicial().intValue();
        producto.setStock(nuevoStock);
        productoRepository.save(producto);

        log.info("Lote {} creado exitosamente para el producto '{}'. Stock inicial: {}",
                guardado.getCodigoLote(), producto.getNombre(), guardado.getStockInicial());

        return mapToDto(guardado);
    }

    @Override
    public DescuentoFefoResponse descontarStockFEFO(DescuentoFefoRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Producto no encontrado con ID: " + request.getProductoId()));

        BigDecimal cantidadSolicitada = request.getCantidad();
        if (cantidadSolicitada.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad a descontar debe ser mayor a 0");
        }

        // Consultar lotes disponibles ordenados por FEFO (First Expired, First Out)
        List<Lote> lotesFefo = loteRepository.findLotesParaDespachoFEFO(
                producto.getId(),
                List.of(EstadoLote.DISPONIBLE, EstadoLote.PROXIMO_A_VENCER)
        );

        BigDecimal stockDisponibleTotal = lotesFefo.stream()
                .map(Lote::getStockActual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (stockDisponibleTotal.compareTo(cantidadSolicitada) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Stock insuficiente bajo regla FEFO para '%s'. Solicitado: %s, Disponible en lotes vigentes: %s",
                            producto.getNombre(), cantidadSolicitada, stockDisponibleTotal));
        }

        BigDecimal pendiente = cantidadSolicitada;
        List<LoteDescontadoItem> lotesAfectados = new ArrayList<>();

        for (Lote lote : lotesFefo) {
            if (pendiente.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal disponibleEnLote = lote.getStockActual();
            BigDecimal aDescontar;
            boolean loteQuedaAgotado;

            if (disponibleEnLote.compareTo(pendiente) <= 0) {
                // Se agota completamente este lote
                aDescontar = disponibleEnLote;
                lote.setStockActual(BigDecimal.ZERO);
                lote.setEstado(EstadoLote.AGOTADO);
                loteQuedaAgotado = true;
                pendiente = pendiente.subtract(aDescontar);
            } else {
                // El lote cubre el restante y le queda saldo
                aDescontar = pendiente;
                BigDecimal nuevoStock = disponibleEnLote.subtract(aDescontar);
                lote.setStockActual(nuevoStock);
                loteQuedaAgotado = false;
                pendiente = BigDecimal.ZERO;
            }

            loteRepository.save(lote);

            // Registro en Kardex para este lote
            MovimientoKardex kardex = MovimientoKardex.builder()
                    .tipo(TipoMovimientoKardex.SALIDA)
                    .lote(lote)
                    .productoId(producto.getId())
                    .productoNombre(producto.getNombre())
                    .cantidad(aDescontar)
                    .saldoStockResultante(lote.getStockActual())
                    .motivo(request.getMotivo() != null ? request.getMotivo() : "DESPACHO_FEFO")
                    .documentoReferencia(request.getDocumentoReferencia())
                    .fechaHora(LocalDateTime.now())
                    .build();
            kardexRepository.save(kardex);

            lotesAfectados.add(LoteDescontadoItem.builder()
                    .loteId(lote.getId())
                    .codigoLote(lote.getCodigoLote())
                    .fechaVencimiento(lote.getFechaVencimiento())
                    .cantidadDescontada(aDescontar)
                    .stockRestanteLote(lote.getStockActual())
                    .loteAgotado(loteQuedaAgotado)
                    .build());
        }

        // Actualizar stock agregado en entidad Producto
        int stockRestanteCatalogo = Math.max(0, (producto.getStock() != null ? producto.getStock() : 0) - cantidadSolicitada.intValue());
        producto.setStock(stockRestanteCatalogo);
        productoRepository.save(producto);

        BigDecimal stockTotalRestanteLotes = stockDisponibleTotal.subtract(cantidadSolicitada);

        log.info("Despacho FEFO completado para producto '{}'. Cantidad: {}. Lotes impactados: {}",
                producto.getNombre(), cantidadSolicitada, lotesAfectados.size());

        return DescuentoFefoResponse.builder()
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .cantidadTotalSolicitada(cantidadSolicitada)
                .cantidadTotalDescontada(cantidadSolicitada)
                .stockTotalRestante(stockTotalRestanteLotes)
                .lotesAfectados(lotesAfectados)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteDto> listarLotes(Long productoId) {
        List<Lote> list = (productoId != null)
                ? loteRepository.findByProductoIdOrderByFechaVencimientoAsc(productoId)
                : loteRepository.findAllByOrderByFechaVencimientoAsc();

        return list.stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoteDto> listarLotesProximosAVencer(int diasUmbral) {
        LocalDate limite = LocalDate.now().plusDays(diasUmbral);
        return loteRepository.findAllByOrderByFechaVencimientoAsc().stream()
                .filter(l -> l.getStockActual().compareTo(BigDecimal.ZERO) > 0)
                .filter(l -> !l.getFechaVencimiento().isAfter(limite))
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoKardexDto> listarKardex(Long productoId) {
        List<MovimientoKardex> list = (productoId != null)
                ? kardexRepository.findByProductoIdOrderByFechaHoraDesc(productoId)
                : kardexRepository.findTop100ByOrderByFechaHoraDesc();

        return list.stream().map(k -> MovimientoKardexDto.builder()
                .id(k.getId())
                .tipo(k.getTipo())
                .loteId(k.getLote() != null ? k.getLote().getId() : null)
                .codigoLote(k.getLote() != null ? k.getLote().getCodigoLote() : "SIN_LOTE")
                .productoId(k.getProductoId())
                .productoNombre(k.getProductoNombre())
                .cantidad(k.getCantidad())
                .saldoStockResultante(k.getSaldoStockResultante())
                .motivo(k.getMotivo())
                .documentoReferencia(k.getDocumentoReferencia())
                .fechaHora(k.getFechaHora())
                .build()
        ).toList();
    }

    @Override
    public void evaluarEstadosVencimiento() {
        List<Lote> lotes = loteRepository.findAll();
        for (Lote l : lotes) {
            EstadoLote nuevoEstado = calcularEstadoLote(l.getFechaVencimiento(), l.getStockActual());
            if (l.getEstado() != nuevoEstado) {
                l.setEstado(nuevoEstado);
                loteRepository.save(l);
            }
        }
    }

    private EstadoLote calcularEstadoLote(LocalDate fechaVencimiento, BigDecimal stockActual) {
        if (stockActual == null || stockActual.compareTo(BigDecimal.ZERO) <= 0) {
            return EstadoLote.AGOTADO;
        }
        LocalDate hoy = LocalDate.now();
        if (fechaVencimiento.isBefore(hoy)) {
            return EstadoLote.VENCIDO;
        }
        if (fechaVencimiento.isBefore(hoy.plusDays(30))) {
            return EstadoLote.PROXIMO_A_VENCER;
        }
        return EstadoLote.DISPONIBLE;
    }

    private LoteDto mapToDto(Lote l) {
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), l.getFechaVencimiento());
        return LoteDto.builder()
                .id(l.getId())
                .codigoLote(l.getCodigoLote())
                .productoId(l.getProductoId())
                .productoNombre(l.getProductoNombre())
                .fechaFabricacion(l.getFechaFabricacion())
                .fechaVencimiento(l.getFechaVencimiento())
                .stockInicial(l.getStockInicial())
                .stockActual(l.getStockActual())
                .costoUnitario(l.getCostoUnitario())
                .estado(l.getEstado())
                .observaciones(l.getObservaciones())
                .diasParaVencer(dias)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public pe.edu.upeu.bomerp.inventario.dto.ReporteSemaforoInventarioResponse obtenerReporteSemaforoInventario() {
        List<Lote> todos = loteRepository.findAllByOrderByFechaVencimientoAsc();
        LocalDate hoy = LocalDate.now();

        BigDecimal valorizacionTotal = BigDecimal.ZERO;
        long criticos = 0;
        long preventivos = 0;
        long optimos = 0;
        long vencidos = 0;

        List<pe.edu.upeu.bomerp.inventario.dto.LoteSemaforoDto> dtos = new ArrayList<>();

        for (Lote l : todos) {
            BigDecimal stock = l.getStockActual() != null ? l.getStockActual() : BigDecimal.ZERO;
            BigDecimal costo = l.getCostoUnitario() != null ? l.getCostoUnitario() : BigDecimal.ZERO;
            BigDecimal valLote = stock.multiply(costo);
            valorizacionTotal = valorizacionTotal.add(valLote);

            long dias = ChronoUnit.DAYS.between(hoy, l.getFechaVencimiento());
            String semaforo;
            if (dias < 0) {
                semaforo = "VENCIDO";
                vencidos++;
            } else if (dias <= 30) {
                semaforo = "CRITICO";
                criticos++;
            } else if (dias <= 60) {
                semaforo = "PREVENTIVO";
                preventivos++;
            } else {
                semaforo = "OPTIMO";
                optimos++;
            }

            dtos.add(pe.edu.upeu.bomerp.inventario.dto.LoteSemaforoDto.builder()
                    .loteId(l.getId())
                    .codigoLote(l.getCodigoLote())
                    .productoId(l.getProductoId())
                    .productoNombre(l.getProductoNombre())
                    .fechaFabricacion(l.getFechaFabricacion())
                    .fechaVencimiento(l.getFechaVencimiento())
                    .diasRestantes(dias)
                    .estadoSemaforo(semaforo)
                    .stockActual(stock)
                    .costoUnitario(costo)
                    .valorizacionTotal(valLote)
                    .build());
        }

        return pe.edu.upeu.bomerp.inventario.dto.ReporteSemaforoInventarioResponse.builder()
                .valorizacionTotalInventario(valorizacionTotal)
                .totalLotes((long) dtos.size())
                .lotesCriticos(criticos)
                .lotesPreventivos(preventivos)
                .lotesOptimos(optimos)
                .lotesVencidos(vencidos)
                .lotes(dtos)
                .build();
    }
}
