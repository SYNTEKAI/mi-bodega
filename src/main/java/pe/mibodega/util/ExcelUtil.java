package pe.mibodega.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Exporta una tabla a Excel con Apache POI:
 * titulo, empresa, filtros usados, encabezados, filas y total.
 */
public class ExcelUtil {

    /** Columna del reporte. tipo: TEXTO, ENTERO, MONEDA, FECHA */
    public static class Columna {
        String titulo;
        String tipo;
        int ancho;
        boolean sumar;

        public Columna(String titulo, String tipo, int ancho, boolean sumar) {
            this.titulo = titulo;
            this.tipo = tipo;
            this.ancho = ancho;
            this.sumar = sumar;
        }
    }

    private ExcelUtil() {
    }

    public static byte[] exportar(String titulo, String empresa, String generadoPor, List<String[]> filtros,
            List<Columna> columnas, List<Object[]> filas) {
        try (XSSFWorkbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Reporte");

            // Estilos
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 15);
            CellStyle estTitulo = libro.createCellStyle();
            estTitulo.setFont(fuenteTitulo);

            Font fuenteNegrita = libro.createFont();
            fuenteNegrita.setBold(true);
            CellStyle estEtiqueta = libro.createCellStyle();
            estEtiqueta.setFont(fuenteNegrita);

            Font fuenteBlanca = libro.createFont();
            fuenteBlanca.setBold(true);
            fuenteBlanca.setColor(IndexedColors.WHITE.getIndex());
            CellStyle estEncabezado = libro.createCellStyle();
            estEncabezado.setFont(fuenteBlanca);
            estEncabezado.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            estEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle estMoneda = libro.createCellStyle();
            estMoneda.setDataFormat(libro.createDataFormat().getFormat("\"S/ \"#,##0.00"));
            CellStyle estEntero = libro.createCellStyle();
            estEntero.setDataFormat(libro.createDataFormat().getFormat("#,##0"));
            CellStyle estFecha = libro.createCellStyle();
            estFecha.setDataFormat(libro.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

            CellStyle estTotal = libro.createCellStyle();
            estTotal.setFont(fuenteNegrita);
            estTotal.setBorderTop(BorderStyle.MEDIUM);
            CellStyle estTotalMoneda = libro.createCellStyle();
            estTotalMoneda.cloneStyleFrom(estTotal);
            estTotalMoneda.setDataFormat(estMoneda.getDataFormat());

            // Cabecera del reporte
            int r = 0;
            celda(hoja.createRow(r++), 0, titulo, estTitulo);
            celda(hoja.createRow(r++), 0, empresa, null);
            celda(hoja.createRow(r++), 0, "Generado el " + new Formato().fechaHora(LocalDateTime.now()) + " por " + generadoPor, null);
            for (String[] f : filtros) {
                Row fila = hoja.createRow(r++);
                celda(fila, 0, f[0] + ":", estEtiqueta);
                celda(fila, 1, f[1] == null || f[1].isBlank() ? "Todos" : f[1], null);
            }
            r++;

            // Encabezados
            int filaEncabezado = r;
            Row enc = hoja.createRow(r++);
            for (int c = 0; c < columnas.size(); c++) {
                celda(enc, c, columnas.get(c).titulo, estEncabezado);
                hoja.setColumnWidth(c, columnas.get(c).ancho * 256);
            }

            // Datos
            double[] sumas = new double[columnas.size()];
            for (Object[] datos : filas) {
                Row fila = hoja.createRow(r++);
                for (int c = 0; c < columnas.size(); c++) {
                    Columna col = columnas.get(c);
                    Object valor = datos[c];
                    Cell cell = fila.createCell(c);
                    if (valor == null) {
                        cell.setCellValue("");
                    } else if (valor instanceof Number n) {
                        cell.setCellValue(n.doubleValue());
                        cell.setCellStyle("MONEDA".equals(col.tipo) ? estMoneda : estEntero);
                        if (col.sumar) {
                            sumas[c] += n.doubleValue();
                        }
                    } else if (valor instanceof LocalDateTime fecha) {
                        cell.setCellValue(fecha);
                        cell.setCellStyle(estFecha);
                    } else {
                        cell.setCellValue(valor.toString());
                    }
                }
            }

            // Total
            Row total = hoja.createRow(r);
            celda(total, 0, "TOTAL (" + filas.size() + " registros)", estTotal);
            for (int c = 1; c < columnas.size(); c++) {
                Cell cell = total.createCell(c);
                if (columnas.get(c).sumar) {
                    cell.setCellValue(sumas[c]);
                    cell.setCellStyle("MONEDA".equals(columnas.get(c).tipo) ? estTotalMoneda : estTotal);
                } else {
                    cell.setCellStyle(estTotal);
                }
            }

            hoja.setAutoFilter(new CellRangeAddress(filaEncabezado, Math.max(r - 1, filaEncabezado + 1), 0, columnas.size() - 1));
            hoja.createFreezePane(0, filaEncabezado + 1);

            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new NegocioException("No se pudo generar el Excel: " + e.getMessage());
        }
    }

    private static void celda(Row fila, int col, String texto, CellStyle estilo) {
        Cell c = fila.createCell(col);
        c.setCellValue(texto);
        if (estilo != null) {
            c.setCellStyle(estilo);
        }
    }

    public static BigDecimal cero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
