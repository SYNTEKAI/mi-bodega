package pe.mibodega.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.mibodega.dao.AuditoriaDAO;
import pe.mibodega.dao.KardexDAO;
import pe.mibodega.dao.RecepcionDAO;
import pe.mibodega.dao.ReporteDAO;
import pe.mibodega.model.MovimientoKardex;
import pe.mibodega.model.Recepcion;
import pe.mibodega.model.RecepcionModificacion;
import pe.mibodega.model.RegistroAuditoria;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.ExcelUtil;
import pe.mibodega.util.ExcelUtil.Columna;

/**
 * Consultas de kardex, reportes de ventas, guias y auditoria, con su exportacion a Excel.
 */
@Service
public class ReporteService {

    @Autowired
    private KardexDAO kardexDAO;
    @Autowired
    private ReporteDAO reporteDAO;
    @Autowired
    private RecepcionDAO recepcionDAO;
    @Autowired
    private AuditoriaDAO auditoriaDAO;
    @Autowired
    private AuditoriaService auditoria;

    // ---------------- Kardex ----------------

    /** Si el usuario no es administrador solo ve el kardex de su sede. */
    public List<MovimientoKardex> kardex(UsuarioSesion s, LocalDate desde, LocalDate hasta, Integer idProducto, Integer idCategoria,
            Integer idSede, String tipo, Integer idProveedor) {
        Integer sede = s.isAdminEmpresa() || s.isInterno() ? idSede : s.getIdSedeUsuario();
        return kardexDAO.buscar(s.getIdEmpresa(), desde, hasta, idProducto, idCategoria, sede, tipo, idProveedor);
    }

    public byte[] kardexExcel(UsuarioSesion s, List<MovimientoKardex> lista, List<String[]> filtros) {
        s.exigir("kardex", "exportar");
        List<Columna> cols = List.of(new Columna("Fecha / hora", "FECHA", 17, false), new Columna("Tipo", "TEXTO", 10, false),
                new Columna("Documento", "TEXTO", 34, false), new Columna("Producto", "TEXTO", 26, false),
                new Columna("Categoría", "TEXTO", 13, false), new Columna("Lote", "TEXTO", 8, false),
                new Columna("Entrada", "ENTERO", 10, true), new Columna("Salida", "ENTERO", 10, true),
                new Columna("Saldo", "ENTERO", 10, false), new Columna("Costo unit.", "MONEDA", 12, false),
                new Columna("Costo total", "MONEDA", 13, true), new Columna("Usuario", "TEXTO", 18, false),
                new Columna("Sede", "TEXTO", 16, false), new Columna("Proveedor", "TEXTO", 26, false));
        List<Object[]> filas = new ArrayList<>();
        for (MovimientoKardex m : lista) {
            filas.add(new Object[]{m.getFechaHora(), m.getTipo(), m.getDocumento(), m.getProductoNombre(), m.getCategoriaNombre(),
                "L-" + m.getIdLote(), m.getEntrada(), m.getSalida(), m.getSaldo(), m.getCostoUnitario(), m.getCostoTotal(),
                m.getUsuarioNombre(), m.getSedeNombre(), m.getProveedorNombre()});
        }
        auditoria.registrar(s, "Kardex", "EXPORTÓ KARDEX", "Exportó el kardex a Excel (" + lista.size() + " movimientos)");
        return ExcelUtil.exportar("Kardex de inventario", empresa(s), s.getNombre(), filtros, cols, filas);
    }

    // ---------------- Reporte de ventas ----------------

    public List<Map<String, Object>> lineasDeVenta(UsuarioSesion s, LocalDate desde, LocalDate hasta, Integer idSede,
            Integer idVendedor, Integer idCategoria, Integer idProducto, String pago) {
        return reporteDAO.lineasDeVenta(s.getIdEmpresa(), desde, hasta, idSede, idVendedor, idCategoria, idProducto, pago);
    }

    public List<Map<String, Object>> gananciaPorProducto(UsuarioSesion s, LocalDate desde, LocalDate hasta, Integer idSede) {
        return reporteDAO.gananciaPorProducto(s.getIdEmpresa(), desde, hasta, idSede);
    }

    public byte[] ventasExcel(UsuarioSesion s, List<Map<String, Object>> lineas, List<String[]> filtros) {
        s.exigir("reportes", "exportar");
        List<Columna> cols = List.of(new Columna("Fecha", "FECHA", 17, false), new Columna("Comprobante", "TEXTO", 14, false),
                new Columna("Producto", "TEXTO", 28, false), new Columna("Categoría", "TEXTO", 13, false),
                new Columna("Vendedor", "TEXTO", 16, false), new Columna("Sede", "TEXTO", 16, false),
                new Columna("Cliente", "TEXTO", 24, false), new Columna("Pago", "TEXTO", 13, false),
                new Columna("Cantidad", "ENTERO", 10, true), new Columna("Precio", "MONEDA", 11, false),
                new Columna("Total", "MONEDA", 13, true));
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> l : lineas) {
            filas.add(new Object[]{l.get("fecha_hora"), l.get("numero_comprobante"), l.get("producto"), l.get("categoria"),
                l.get("vendedor"), l.get("sede"), l.get("cliente"), l.get("metodo_pago"), l.get("cantidad"),
                l.get("precio_unitario"), l.get("subtotal")});
        }
        auditoria.registrar(s, "Reportes", "EXPORTÓ REPORTE", "Exportó el reporte de ventas a Excel (" + lineas.size() + " líneas)");
        return ExcelUtil.exportar("Reporte de ventas", empresa(s), s.getNombre(), filtros, cols, filas);
    }

    public byte[] gananciaExcel(UsuarioSesion s, List<Map<String, Object>> filasDatos, List<String[]> filtros) {
        s.exigir("reportes", "exportar");
        List<Columna> cols = List.of(new Columna("Producto", "TEXTO", 28, false), new Columna("Categoría", "TEXTO", 14, false),
                new Columna("Unidades", "ENTERO", 10, true), new Columna("Venta", "MONEDA", 13, true),
                new Columna("Costo", "MONEDA", 13, true), new Columna("Ganancia", "MONEDA", 13, true));
        List<Object[]> filas = new ArrayList<>();
        for (Map<String, Object> f : filasDatos) {
            filas.add(new Object[]{f.get("producto"), f.get("categoria"), f.get("unidades"), f.get("venta"), f.get("costo"), f.get("ganancia")});
        }
        auditoria.registrar(s, "Reportes", "EXPORTÓ REPORTE", "Exportó la ganancia por producto a Excel");
        return ExcelUtil.exportar("Ganancia por producto", empresa(s), s.getNombre(), filtros, cols, filas);
    }

    // ---------------- Guias recibidas ----------------

    public List<Recepcion> guias(UsuarioSesion s, LocalDate desde, LocalDate hasta, Integer idProveedor, Integer idProducto,
            String numero, String estado) {
        return recepcionDAO.buscarConFiltros(s.getIdEmpresa(), desde, hasta, idProveedor, idProducto, numero, estado);
    }

    public List<RecepcionModificacion> modificaciones(int idRecepcion) {
        return recepcionDAO.modificaciones(idRecepcion);
    }

    public List<RegistroAuditoria> historialRecepcion(UsuarioSesion s, String numero) {
        return auditoriaDAO.porDocumento(s.getIdEmpresa(), "Recepción", numero);
    }

    public byte[] guiasExcel(UsuarioSesion s, List<Recepcion> guias, List<String[]> filtros) {
        s.exigir("recepcion", "exportar");
        List<Columna> cols = List.of(new Columna("N° interno", "TEXTO", 12, false), new Columna("Guía proveedor", "TEXTO", 14, false),
                new Columna("Fecha", "FECHA", 17, false), new Columna("Proveedor", "TEXTO", 28, false),
                new Columna("Producto", "TEXTO", 26, false), new Columna("Empaque", "TEXTO", 10, false),
                new Columna("Unid./empaque", "ENTERO", 12, false), new Columna("Empaques", "ENTERO", 11, true),
                new Columna("Total unidades", "ENTERO", 12, true), new Columna("Costo unit.", "MONEDA", 12, false),
                new Columna("Importe", "MONEDA", 13, true), new Columna("Estado", "TEXTO", 13, false),
                new Columna("Registró", "TEXTO", 16, false));
        List<Object[]> filas = new ArrayList<>();
        for (Recepcion r : guias) {
            for (Map<String, Object> l : recepcionDAO.lineasParaExcel(r.getIdRecepcion())) {
                BigDecimal costo = (BigDecimal) l.get("costo_unitario");
                int unidades = ((Number) l.get("total_unidades")).intValue();
                filas.add(new Object[]{r.getNumero(), r.getGuiaProveedor(), r.getFechaHora(), r.getProveedorNombre(), l.get("nombre"),
                    l.get("empaque"), l.get("contenido"), l.get("cantidad_empaques"), unidades, costo,
                    costo.multiply(BigDecimal.valueOf(unidades)), r.getEstado(), r.getUsuarioNombre()});
            }
        }
        auditoria.registrar(s, "Recepción", "EXPORTÓ GUÍAS", "Exportó " + guias.size() + " recepción(es) a Excel");
        return ExcelUtil.exportar("Recepciones de mercadería", empresa(s), s.getNombre(), filtros, cols, filas);
    }

    // ---------------- Auditoria ----------------

    public List<RegistroAuditoria> auditoria(Integer empresa, Integer idSede, String usuario, String modulo, String accion,
            LocalDate desde, LocalDate hasta, String texto, int pagina) {
        return auditoriaDAO.buscar(empresa, idSede, usuario, modulo, accion, desde, hasta, texto, pagina);
    }

    public int contarAuditoria(Integer empresa, Integer idSede, String usuario, String modulo, String accion,
            LocalDate desde, LocalDate hasta, String texto) {
        return auditoriaDAO.contar(empresa, idSede, usuario, modulo, accion, desde, hasta, texto);
    }

    public List<Map<String, Object>> usuariosAuditoria(Integer empresa) {
        return auditoriaDAO.usuarios(empresa);
    }

    public List<String> modulosAuditoria(Integer empresa) {
        return auditoriaDAO.modulos(empresa);
    }

    public List<String> accionesAuditoria(Integer empresa) {
        return auditoriaDAO.acciones(empresa);
    }

    private String empresa(UsuarioSesion s) {
        return s.getEmpresaNombre() + " - RUC " + s.getEmpresaRuc() + " - " + new pe.mibodega.util.Formato().fechaHora(LocalDateTime.now());
    }
}
