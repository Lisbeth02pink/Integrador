package com.tambo.sistematambo.reporte;
 
import com.tambo.sistematambo.model.Modulo;
import com.tambo.sistematambo.model.Perfil;
import com.tambo.sistematambo.model.Producto;
import com.tambo.sistematambo.model.User;
import com.tambo.sistematambo.repository.ModuloRepository;
import com.tambo.sistematambo.repository.PerfilRepository;
import com.tambo.sistematambo.repository.ProductoRepository;
import com.tambo.sistematambo.repository.UserRepository;
 
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;
 
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
 
/**
 * Servicio para generación de reportes en formato Excel (.xlsx)
 * usando la librería Apache POI.
 */
@Service
public class ReporteExcelService {
 
    private static final Logger logger = LoggerFactory.getLogger(ReporteExcelService.class);
 
    private final UserRepository userRepository;
    private final PerfilRepository perfilRepository;
    private final ModuloRepository moduloRepository;
    private final ProductoRepository productoRepository;
 
    public ReporteExcelService(UserRepository userRepository,
                               PerfilRepository perfilRepository,
                               ModuloRepository moduloRepository,
                               ProductoRepository productoRepository) {
        this.userRepository = userRepository;
        this.perfilRepository = perfilRepository;
        this.moduloRepository = moduloRepository;
        this.productoRepository = productoRepository;
    }
 
    /**
     * Genera un Excel con el listado completo de usuarios del sistema.
     */
    public byte[] generarReporteUsuarios() throws IOException {
        logger.info("Generando reporte Excel de usuarios");
        List<User> usuarios = userRepository.findAll();
 
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Usuarios");
            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloFila = crearEstiloFila(workbook);
            CellStyle estiloFilaAlterna = crearEstiloFilaAlterna(workbook);
 
            Row filaTitulo = sheet.createRow(0);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("REPORTE DE USUARIOS - SISTEMA TAMBO");
            celdaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
 
            Row filaFecha = sheet.createRow(1);
            filaFecha.createCell(0).setCellValue("Generado: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            sheet.createRow(2);
 
            String[] encabezados = {"ID", "Nombre", "Usuario", "Correo protegido", "Estado", "Perfil"};
            Row filaEncabezado = sheet.createRow(3);
            for (int i = 0; i < encabezados.length; i++) {
                Cell celda = filaEncabezado.createCell(i);
                celda.setCellValue(encabezados[i]);
                celda.setCellStyle(estiloEncabezado);
            }
 
            int numFila = 4;
            for (User u : usuarios) {
                Row fila = sheet.createRow(numFila);
                CellStyle estilo = (numFila % 2 == 0) ? estiloFilaAlterna : estiloFila;
                setCelda(fila, 0, u.getId().toString(), estilo);
                setCelda(fila, 1, u.getNombre(), estilo);
                setCelda(fila, 2, u.getUsuario(), estilo);
                setCelda(fila, 3, maskCorreo(u.getCorreo()), estilo);
                setCelda(fila, 4, u.getEstado() == 1 ? "Activo" : "Inactivo", estilo);
                setCelda(fila, 5, u.getPerfil() != null ? u.getPerfil().getNombre() : "Sin perfil", estilo);
                numFila++;
            }
 
            for (int i = 0; i < encabezados.length; i++) sheet.autoSizeColumn(i);
            logger.info("Reporte de usuarios generado con {} registros", usuarios.size());
            return toBytes(workbook);
        }
    }
 
    /**
     * Genera un Excel con el listado de productos del sistema.
     */
    public byte[] generarReporteProductos() throws IOException {
        logger.info("Generando reporte Excel de productos");
        List<Producto> productos = productoRepository.findAll();
 
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos");
            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloFila = crearEstiloFila(workbook);
            CellStyle estiloFilaAlterna = crearEstiloFilaAlterna(workbook);
 
            Row filaTitulo = sheet.createRow(0);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("REPORTE DE PRODUCTOS - SISTEMA TAMBO");
            celdaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
 
            Row filaFecha = sheet.createRow(1);
            filaFecha.createCell(0).setCellValue("Generado: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            sheet.createRow(2);
 
            String[] encabezados = {"ID", "Nombre", "SKU", "Precio", "Stock", "Categoría"};
            Row filaEncabezado = sheet.createRow(3);
            for (int i = 0; i < encabezados.length; i++) {
                Cell celda = filaEncabezado.createCell(i);
                celda.setCellValue(encabezados[i]);
                celda.setCellStyle(estiloEncabezado);
            }
 
            int numFila = 4;
            for (Producto p : productos) {
                Row fila = sheet.createRow(numFila);
                CellStyle estilo = (numFila % 2 == 0) ? estiloFilaAlterna : estiloFila;
                setCelda(fila, 0, p.getId().toString(), estilo);
                setCelda(fila, 1, p.getNombre(), estilo);
                setCelda(fila, 2, p.getSku() != null ? p.getSku() : "", estilo);
                setCelda(fila, 3, p.getPrecioVenta() != null ? p.getPrecioVenta().toString() : "0", estilo);
                setCelda(fila, 4, p.getStock() != null ? p.getStock().toString() : "0", estilo);
                setCelda(fila, 5, p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría", estilo);
                numFila++;
            }
 
            for (int i = 0; i < encabezados.length; i++) sheet.autoSizeColumn(i);
            logger.info("Reporte de productos generado con {} registros", productos.size());
            return toBytes(workbook);
        }
    }
 
    /**
     * Genera un Excel con el listado de perfiles y sus módulos asignados.
     */
    public byte[] generarReportePerfiles() throws IOException {
        logger.info("Generando reporte Excel de perfiles");
        List<Perfil> perfiles = perfilRepository.findAll();
 
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Perfiles");
            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloFila = crearEstiloFila(workbook);
            CellStyle estiloFilaAlterna = crearEstiloFilaAlterna(workbook);
 
            Row filaTitulo = sheet.createRow(0);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("REPORTE DE PERFILES - SISTEMA TAMBO");
            celdaTitulo.setCellStyle(estiloTitulo);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 4));
 
            Row filaFecha = sheet.createRow(1);
            filaFecha.createCell(0).setCellValue("Generado: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            sheet.createRow(2);
 
            String[] encabezados = {"ID", "Nombre", "Descripción", "Estado", "Módulos Asignados"};
            Row filaEncabezado = sheet.createRow(3);
            for (int i = 0; i < encabezados.length; i++) {
                Cell celda = filaEncabezado.createCell(i);
                celda.setCellValue(encabezados[i]);
                celda.setCellStyle(estiloEncabezado);
            }
 
            int numFila = 4;
            for (Perfil p : perfiles) {
                Row fila = sheet.createRow(numFila);
                CellStyle estilo = (numFila % 2 == 0) ? estiloFilaAlterna : estiloFila;
                String modulos = p.getModulos().stream()
                        .map(Modulo::getNombre)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Sin módulos");
                setCelda(fila, 0, p.getId().toString(), estilo);
                setCelda(fila, 1, p.getNombre(), estilo);
                setCelda(fila, 2, p.getDescripcion() != null ? p.getDescripcion() : "", estilo);
                setCelda(fila, 3, p.isEstado() ? "Activo" : "Inactivo", estilo);
                setCelda(fila, 4, modulos, estilo);
                numFila++;
            }
 
            for (int i = 0; i < encabezados.length; i++) sheet.autoSizeColumn(i);
            logger.info("Reporte de perfiles generado con {} registros", perfiles.size());
            return toBytes(workbook);
        }
    }
 
    // ── Helpers ──────────────────────────────────
 
    private byte[] toBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return out.toByteArray();
    }
 
    private void setCelda(Row fila, int col, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(col);
        celda.setCellValue(valor != null ? valor : "");
        celda.setCellStyle(estilo);
    }

    private String maskCorreo(String correo) {
        if (StringUtils.isBlank(correo) || !StringUtils.contains(correo, "@")) {
            return "";
        }

        String usuario = StringUtils.substringBefore(correo, "@");
        String dominio = StringUtils.substringAfter(correo, "@");
        String prefijoVisible = StringUtils.left(usuario, Math.min(2, usuario.length()));
        return prefijoVisible + "***@" + dominio;
    }
 
    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 14);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }
 
    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.RED.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        return estilo;
    }
 
    private CellStyle crearEstiloFila(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }
 
    private CellStyle crearEstiloFilaAlterna(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        estilo.setFillForegroundColor(IndexedColors.ROSE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }
}
