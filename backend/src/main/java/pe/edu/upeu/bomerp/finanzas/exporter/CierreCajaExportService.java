package pe.edu.upeu.bomerp.finanzas.exporter;

import org.springframework.stereotype.Service;
import pe.edu.upeu.bomerp.finanzas.dto.CierreCajaReporteDto;

import java.util.List;

@Service
public class CierreCajaExportService {

    private final List<CierreCajaExporter> exporters;

    public CierreCajaExportService(List<CierreCajaExporter> exporters) {
        this.exporters = exporters;
    }

    public CierreCajaExporter resolverExportador(FormatoReporte formato) {
        return exporters.stream()
                .filter(exp -> exp.supports(formato))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe exportador configurado para el formato: " + formato));
    }

    public byte[] exportar(FormatoReporte formato, CierreCajaReporteDto datos) {
        return resolverExportador(formato).exportarCierreCaja(datos);
    }
}
