package pe.mibodega.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.mibodega.dao.CategoriaDAO;
import pe.mibodega.dao.ProductoDAO;
import pe.mibodega.model.Categoria;
import pe.mibodega.model.EstadoStock;
import pe.mibodega.model.Producto;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.Validar;

/**
 * Catalogo de productos, categorias e importacion desde Excel.
 */
@Service
public class ProductoService {

    public static final List<String> UNIDADES = List.of("unidad", "bolsa", "botella", "lata", "paquete", "caja", "kg");
    public static final List<String> EMPAQUES = List.of("Unidad", "Paquete", "Six pack", "Caja", "Fardo", "Saco", "Bandeja");
    private static final String[] COLUMNAS_PLANTILLA = {"codigo_barras", "nombre", "categoria", "marca", "presentacion",
        "unidad", "precio_compra", "precio_venta", "stock_minimo"};

    @Autowired
    private ProductoDAO productoDAO;
    @Autowired
    private CategoriaDAO categoriaDAO;
    @Autowired
    private AuditoriaService auditoria;

    /** Resultado de la importacion desde Excel. */
    public static class ResultadoImportacion {
        private int importados;
        private final List<String> errores = new ArrayList<>();

        public int getImportados() {
            return importados;
        }

        public List<String> getErrores() {
            return errores;
        }
    }

    public List<Producto> buscar(UsuarioSesion s, String texto, Integer idCategoria, String marca, String estado, String inventario) {
        List<Producto> lista = productoDAO.buscarConFiltros(s.getIdEmpresa(), s.getSedeActual(), texto, idCategoria, marca, estado);
        if (StringUtils.isBlank(inventario)) {
            return lista;
        }
        List<Producto> filtrada = new ArrayList<>();
        for (Producto p : lista) {
            EstadoStock e = p.getEstadoStock();
            boolean critico = "CRITICO".equals(inventario) && e != EstadoStock.DISPONIBLE;
            if (critico || e.name().equals(inventario)) {
                filtrada.add(p);
            }
        }
        return filtrada;
    }

    public Producto buscar(UsuarioSesion s, int id) {
        Producto p = productoDAO.buscarEnSede(id, s.getIdEmpresa(), s.getSedeActual());
        if (p == null) {
            throw new NegocioException("El producto no existe.");
        }
        return p;
    }

    public List<Producto> paraVenta(UsuarioSesion s) {
        return productoDAO.listarParaVenta(s.getIdEmpresa(), s.getSedeActual());
    }

    public List<String> marcas(UsuarioSesion s) {
        return productoDAO.marcas(s.getIdEmpresa());
    }

    public List<Map<String, Object>> stockPorSede(UsuarioSesion s, int idProducto) {
        return productoDAO.stockPorSede(idProducto, s.getIdEmpresa());
    }

    public void guardar(UsuarioSesion s, Producto p) {
        s.exigir("productos", p.getIdProducto() == null ? "crear" : "editar");
        validar(s, p);
        if (p.getIdProducto() == null) {
            productoDAO.insertar(p);
            auditoria.registrar(s, "Productos", "CREAR PRODUCTO", "Registró el producto " + p.getNombre());
        } else {
            buscar(s, p.getIdProducto());
            productoDAO.actualizar(p);
            auditoria.registrar(s, "Productos", "EDITAR PRODUCTO", "Editó el producto " + p.getNombre());
        }
    }

    private void validar(UsuarioSesion s, Producto p) {
        Validar.requerido(p.getNombre(), "nombre");
        Validar.requerido(p.getCodigoBarras(), "código de barras");
        if (p.getPrecioVenta() == null || p.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El precio de venta debe ser mayor a 0.");
        }
        if (p.getPrecioCompra() == null) {
            p.setPrecioCompra(BigDecimal.ZERO);
        }
        if (p.getPrecioCompra().compareTo(BigDecimal.ZERO) < 0 || p.getStockMinimo() < 0) {
            throw new NegocioException("El precio de compra y el stock mínimo no pueden ser negativos.");
        }
        if (p.getIdCategoria() == null || categoriaDAO.buscar(p.getIdCategoria(), s.getIdEmpresa()) == null) {
            throw new NegocioException("Elige una categoría.");
        }
        p.setCodigoBarras(p.getCodigoBarras().trim());
        if (productoDAO.existeCodigo(s.getIdEmpresa(), p.getCodigoBarras(), p.getIdProducto())) {
            throw new NegocioException("Ya existe un producto con el código " + p.getCodigoBarras() + ".");
        }
        p.setIdEmpresa(s.getIdEmpresa());
        p.setUnidad(StringUtils.defaultIfBlank(p.getUnidad(), "unidad"));
        p.setEmpaque(StringUtils.defaultIfBlank(p.getEmpaque(), "Unidad"));
        if ("Unidad".equals(p.getEmpaque()) || p.getUnidadesPorEmpaque() < 1) {
            p.setUnidadesPorEmpaque(1);
        }
    }

    public void cambiarActivo(UsuarioSesion s, int id) {
        s.exigir("productos", "editar");
        Producto p = buscar(s, id);
        productoDAO.cambiarActivo(id, !p.isActivo());
        auditoria.registrar(s, "Productos", "CAMBIAR ESTADO", "Producto " + p.getNombre() + " pasó a "
                + (p.isActivo() ? "INACTIVO" : "ACTIVO"));
    }

    /** Si tiene lotes o ventas no se borra: se desactiva. */
    public String eliminar(UsuarioSesion s, int id) {
        s.exigir("productos", "eliminar");
        Producto p = buscar(s, id);
        int relacionados = productoDAO.contarRelacionados(id);
        if (relacionados > 0) {
            productoDAO.cambiarActivo(id, false);
            auditoria.registrar(s, "Productos", "DESACTIVAR", "Desactivó el producto " + p.getNombre()
                    + " (eliminación bloqueada: " + relacionados + " registros)");
            return "El producto tiene " + relacionados + " registros (lotes o ventas): no se eliminó, se desactivó.";
        }
        productoDAO.eliminar(id);
        auditoria.registrar(s, "Productos", "ELIMINAR PRODUCTO", "Eliminó el producto " + p.getNombre());
        return "Producto eliminado.";
    }

    // ---------------- Categorias ----------------

    public List<Categoria> categorias(UsuarioSesion s) {
        return categoriaDAO.listar(s.getIdEmpresa());
    }

    public List<Categoria> categoriasActivas(UsuarioSesion s) {
        return categoriaDAO.listarActivas(s.getIdEmpresa());
    }

    public void guardarCategoria(UsuarioSesion s, Integer id, String nombre) {
        s.exigir("productos", "categorias");
        Validar.requerido(nombre, "nombre");
        nombre = nombre.trim();
        if (categoriaDAO.existeNombre(s.getIdEmpresa(), nombre, id)) {
            throw new NegocioException("La categoría " + nombre + " ya existe.");
        }
        Categoria c = new Categoria();
        c.setIdEmpresa(s.getIdEmpresa());
        c.setNombre(nombre);
        if (id == null) {
            categoriaDAO.insertar(c);
            auditoria.registrar(s, "Productos", "CREÓ CATEGORÍA", "Creó la categoría " + nombre);
        } else {
            Categoria anterior = categoriaDAO.buscar(id, s.getIdEmpresa());
            if (anterior == null) {
                throw new NegocioException("La categoría no existe.");
            }
            c.setIdCategoria(id);
            categoriaDAO.actualizar(c);
            auditoria.registrar(s, "Productos", "EDITÓ CATEGORÍA", "Renombró la categoría " + anterior.getNombre() + " a " + nombre);
        }
    }

    public void cambiarActivoCategoria(UsuarioSesion s, int id) {
        s.exigir("productos", "categorias");
        Categoria c = categoriaDAO.buscar(id, s.getIdEmpresa());
        if (c == null) {
            throw new NegocioException("La categoría no existe.");
        }
        categoriaDAO.cambiarActivo(id, !c.isActivo());
        auditoria.registrar(s, "Productos", "EDITÓ CATEGORÍA", "Categoría " + c.getNombre() + " pasó a "
                + (c.isActivo() ? "INACTIVA" : "ACTIVA"));
    }

    public void eliminarCategoria(UsuarioSesion s, int id) {
        s.exigir("productos", "categorias");
        Categoria c = categoriaDAO.buscar(id, s.getIdEmpresa());
        if (c == null) {
            throw new NegocioException("La categoría no existe.");
        }
        if (c.getProductos() > 0) {
            throw new NegocioException("La categoría " + c.getNombre() + " tiene " + c.getProductos()
                    + " producto(s). Muévelos a otra categoría o desactívala.");
        }
        categoriaDAO.eliminar(id);
        auditoria.registrar(s, "Productos", "ELIMINÓ CATEGORÍA", "Eliminó la categoría " + c.getNombre());
    }

    // ---------------- Importacion desde Excel (Apache POI) ----------------

    /** Plantilla vacia con las columnas que se deben llenar. */
    public byte[] plantillaExcel() {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Sheet hoja = libro.createSheet("Productos");
            Row enc = hoja.createRow(0);
            for (int i = 0; i < COLUMNAS_PLANTILLA.length; i++) {
                enc.createCell(i).setCellValue(COLUMNAS_PLANTILLA[i]);
                hoja.setColumnWidth(i, 18 * 256);
            }
            Row ejemplo = hoja.createRow(1);
            String[] datos = {"7751234567890", "Galleta Soda Field", "Snacks", "Field", "paquete 6 un.", "paquete", "1.20", "1.80", "10"};
            for (int i = 0; i < datos.length; i++) {
                ejemplo.createCell(i).setCellValue(datos[i]);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new NegocioException("No se pudo generar la plantilla.");
        }
    }

    /**
     * Lee el Excel del cliente: carga las filas validas y devuelve los errores de las demas.
     * Si la categoria no existe la crea.
     */
    @Transactional
    public ResultadoImportacion importarExcel(UsuarioSesion s, InputStream archivo) {
        s.exigir("productos", "crear");
        ResultadoImportacion resultado = new ResultadoImportacion();
        DataFormatter formato = new DataFormatter();
        try (Workbook libro = WorkbookFactory.create(archivo)) {
            Sheet hoja = libro.getSheetAt(0);
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila == null || StringUtils.isBlank(texto(formato, fila.getCell(1)))) {
                    continue;
                }
                try {
                    Producto p = new Producto();
                    p.setCodigoBarras(texto(formato, fila.getCell(0)));
                    p.setNombre(texto(formato, fila.getCell(1)));
                    p.setIdCategoria(categoriaPorNombre(s, texto(formato, fila.getCell(2))));
                    p.setMarca(texto(formato, fila.getCell(3)));
                    p.setPresentacion(texto(formato, fila.getCell(4)));
                    String unidad = texto(formato, fila.getCell(5)).toLowerCase();
                    p.setUnidad(UNIDADES.contains(unidad) ? unidad : "unidad");
                    p.setEmpaque("Unidad");
                    p.setUnidadesPorEmpaque(1);
                    p.setPrecioCompra(numero(formato, fila.getCell(6)));
                    p.setPrecioVenta(numero(formato, fila.getCell(7)));
                    p.setStockMinimo(numero(formato, fila.getCell(8)).intValue());
                    validar(s, p);
                    productoDAO.insertar(p);
                    resultado.importados++;
                } catch (NegocioException e) {
                    resultado.errores.add("Fila " + (i + 1) + ": " + e.getMessage());
                } catch (NumberFormatException e) {
                    resultado.errores.add("Fila " + (i + 1) + ": precio o stock mínimo no es un número.");
                }
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof NegocioException ne) {
                throw ne;
            }
            throw new NegocioException("El archivo no es un Excel válido (.xlsx).");
        }
        auditoria.registrar(s, "Productos", "IMPORTÓ PRODUCTOS", "Importó " + resultado.importados + " productos desde Excel ("
                + resultado.errores.size() + " filas con error)");
        return resultado;
    }

    private int categoriaPorNombre(UsuarioSesion s, String nombre) {
        if (StringUtils.isBlank(nombre)) {
            throw new NegocioException("falta la categoría");
        }
        Categoria c = categoriaDAO.buscarPorNombre(s.getIdEmpresa(), nombre.trim());
        if (c != null) {
            return c.getIdCategoria();
        }
        Categoria nueva = new Categoria();
        nueva.setIdEmpresa(s.getIdEmpresa());
        nueva.setNombre(nombre.trim());
        return categoriaDAO.insertar(nueva);
    }

    private String texto(DataFormatter formato, Cell celda) {
        return celda == null ? "" : formato.formatCellValue(celda).trim();
    }

    private BigDecimal numero(DataFormatter formato, Cell celda) {
        if (celda == null) {
            return BigDecimal.ZERO;
        }
        if (celda.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(celda.getNumericCellValue());
        }
        String valor = formato.formatCellValue(celda).trim().replace(",", ".");
        return valor.isEmpty() ? BigDecimal.ZERO : new BigDecimal(valor);
    }
}
