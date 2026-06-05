package com.tambo.sistematambo.reporte;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
 
/**
 * Controlador REST para la descarga de reportes en formato Excel.
 * Endpoints disponibles bajo /api/reportes
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
 
    private static final Logger logger = LoggerFactory.getLogger(ReporteController.class);
    private final ReporteExcelService reporteExcelService;
 
    public ReporteController(ReporteExcelService reporteExcelService) {
        this.reporteExcelService = reporteExcelService;
    }
 
    /** GET /api/reportes/usuarios/excel */
    @GetMapping("/usuarios/excel")
    public ResponseEntity<byte[]> descargarReporteUsuarios() throws IOException {
        logger.info("Solicitud de descarga: reporte de usuarios");
        byte[] archivo = reporteExcelService.generarReporteUsuarios();
        return buildResponse(archivo, "reporte_usuarios");
    }
 
    /** GET /api/reportes/productos/excel */
    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> descargarReporteProductos() throws IOException {
        logger.info("Solicitud de descarga: reporte de productos");
        byte[] archivo = reporteExcelService.generarReporteProductos();
        return buildResponse(archivo, "reporte_productos");
    }
 
    /** GET /api/reportes/perfiles/excel */
    @GetMapping("/perfiles/excel")
    public ResponseEntity<byte[]> descargarReportePerfiles() throws IOException {
        logger.info("Solicitud de descarga: reporte de perfiles");
        byte[] archivo = reporteExcelService.generarReportePerfiles();
        return buildResponse(archivo, "reporte_perfiles");
    }
 
    private ResponseEntity<byte[]> buildResponse(byte[] archivo, String nombre) {
        String nombreArchivo = nombre + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + nombreArchivo)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(archivo);
    }
}