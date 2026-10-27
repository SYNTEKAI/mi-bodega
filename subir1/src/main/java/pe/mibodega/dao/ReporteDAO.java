package pe.mibodega.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

/**
 * Consultas de resumen para los reportes y el dashboard.
 * Solo se cuentan las ventas EMITIDAS (no las anuladas).
 */
@Repository
public class ReporteDAO extends BaseDAO {

    // ---------------- Reporte de ventas ----------------

    public List<Map<String, Object>> lineasDeVenta(int idEmpresa, LocalDate desde, LocalDate hasta, Integer idSede, Integer idVendedor,
            Integer idCategoria, Integer idProducto, String metodoPago) {
        StringBuilder sql = new StringBuilder("SELECT v.fecha_hora, v.numero_comprobante, p.nombre AS producto, c.nombre AS categoria, "
                + "COALESCE(u.nombre, ui.nombre) AS vendedor, s.nombre AS sede, COALESCE(cl.nombre, 'Cliente varios') AS cliente, "
                + "v.metodo_pago, d.cantidad, d.precio_unitario, d.subtotal "
                + "FROM venta_detalles d JOIN ventas v ON v.id_venta = d.id_venta JOIN productos p ON p.id_producto = d.id_producto "
                + "JOIN categorias c ON c.id_categoria = p.id_categoria JOIN sedes s ON s.id_sede = v.id_sede "
                + "LEFT JOIN usuarios u ON u.id_usuario = v.id_usuario LEFT JOIN usuarios_internos ui ON ui.id_usuario_interno = v.id_usuario_interno "
                + "LEFT JOIN clientes cl ON cl.id_cliente = v.id_cliente WHERE v.id_empresa = ? AND v.estado = 'EMITIDA' ");
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        filtrosVenta(sql, params, desde, hasta, idSede);
        if (idVendedor != null) {
            sql.append("AND v.id_usuario = ? ");
            params.add(idVendedor);
        }
        if (idCategoria != null) {
            sql.append("AND p.id_categoria = ? ");
            params.add(idCategoria);
        }
        if (idProducto != null) {
            sql.append("AND p.id_producto = ? ");
            params.add(idProducto);
        }
        if (StringUtils.isNotBlank(metodoPago)) {
            sql.append("AND v.metodo_pago = ? ");
            params.add(metodoPago);
        }
        sql.append("ORDER BY v.fecha_hora DESC, d.id_detalle");
        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    /** Unidades, venta, costo y ganancia por producto (RF-24). */
    public List<Map<String, Object>> gananciaPorProducto(int idEmpresa, LocalDate desde, LocalDate hasta, Integer idSede) {
        StringBuilder sql = new StringBuilder("SELECT p.nombre AS producto, c.nombre AS categoria, SUM(d.cantidad) AS unidades, "
                + "SUM(d.subtotal) AS venta, SUM(d.cantidad * p.precio_compra) AS costo, "
                + "SUM(d.subtotal) - SUM(d.cantidad * p.precio_compra) AS ganancia "
                + "FROM venta_detalles d JOIN ventas v ON v.id_venta = d.id_venta JOIN productos p ON p.id_producto = d.id_producto "
                + "JOIN categorias c ON c.id_categoria = p.id_categoria WHERE v.id_empresa = ? AND v.estado = 'EMITIDA' ");
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        filtrosVenta(sql, params, desde, hasta, idSede);
        sql.append("GROUP BY p.id_producto, p.nombre, c.nombre ORDER BY unidades DESC");
        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    private void filtrosVenta(StringBuilder sql, List<Object> params, LocalDate desde, LocalDate hasta, Integer idSede) {
        if (desde != null) {
            sql.append("AND DATE(v.fecha_hora) >= ? ");
            params.add(desde);
        }
        if (hasta != null) {
            sql.append("AND DATE(v.fecha_hora) <= ? ");
            params.add(hasta);
        }
        if (idSede != null) {
            sql.append("AND v.id_sede = ? ");
            params.add(idSede);
        }
    }

    // ---------------- Dashboard de la empresa ----------------

    /** Cantidad y total de ventas entre dos fechas (hasta exclusivo). */
    public Map<String, Object> totalVentas(int idEmpresa, LocalDate desde, LocalDate hasta) {
        return jdbc.queryForMap("SELECT COUNT(*) AS cantidad, COALESCE(SUM(total), 0) AS total FROM ventas "
                + "WHERE id_empresa = ? AND estado = 'EMITIDA' AND fecha_hora >= ? AND fecha_hora < ?", idEmpresa, desde, hasta);
    }

    public int unidadesVendidas(int idEmpresa, LocalDate desde, LocalDate hasta) {
        return contar("SELECT COALESCE(SUM(d.cantidad), 0) FROM venta_detalles d JOIN ventas v ON v.id_venta = d.id_venta "
                + "WHERE v.id_empresa = ? AND v.estado = 'EMITIDA' AND v.fecha_hora >= ? AND v.fecha_hora < ?", idEmpresa, desde, hasta);
    }

    public List<Map<String, Object>> ventasPorDia(int idEmpresa, LocalDate desde, Integer idSede) {
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        params.add(desde);
        String sede = "";
        if (idSede != null) {
            sede = "AND id_sede = ? ";
            params.add(idSede);
        }
        return jdbc.queryForList("SELECT DATE_FORMAT(t.dia, '%d/%m') AS dia, t.total FROM (SELECT DATE(fecha_hora) AS dia, SUM(total) AS total "
                + "FROM ventas WHERE id_empresa = ? AND estado = 'EMITIDA' AND fecha_hora >= ? " + sede
                + "GROUP BY DATE(fecha_hora)) t ORDER BY t.dia", params.toArray());
    }

    public List<Map<String, Object>> ventasPorSede(int idEmpresa, LocalDate desde) {
        return jdbc.queryForList("SELECT s.nombre AS etiqueta, SUM(v.total) AS valor FROM ventas v JOIN sedes s ON s.id_sede = v.id_sede "
                + "WHERE v.id_empresa = ? AND v.estado = 'EMITIDA' AND v.fecha_hora >= ? GROUP BY s.id_sede, s.nombre ORDER BY valor DESC",
                idEmpresa, desde);
    }

    public List<Map<String, Object>> ventasPorCategoria(int idEmpresa, LocalDate desde, Integer idSede) {
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        params.add(desde);
        String sede = "";
        if (idSede != null) {
            sede = "AND v.id_sede = ? ";
            params.add(idSede);
        }
        return jdbc.queryForList("SELECT c.nombre AS etiqueta, SUM(d.subtotal) AS valor FROM venta_detalles d "
                + "JOIN ventas v ON v.id_venta = d.id_venta JOIN productos p ON p.id_producto = d.id_producto "
                + "JOIN categorias c ON c.id_categoria = p.id_categoria WHERE v.id_empresa = ? AND v.estado = 'EMITIDA' "
                + "AND v.fecha_hora >= ? " + sede + "GROUP BY c.id_categoria, c.nombre ORDER BY valor DESC", params.toArray());
    }

    public List<Map<String, Object>> topProductos(int idEmpresa, LocalDate desde, Integer idSede) {
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        params.add(desde);
        String sede = "";
        if (idSede != null) {
            sede = "AND v.id_sede = ? ";
            params.add(idSede);
        }
        return jdbc.queryForList("SELECT p.nombre AS etiqueta, SUM(d.cantidad) AS valor FROM venta_detalles d "
                + "JOIN ventas v ON v.id_venta = d.id_venta JOIN productos p ON p.id_producto = d.id_producto "
                + "WHERE v.id_empresa = ? AND v.estado = 'EMITIDA' AND v.fecha_hora >= ? " + sede
                + "GROUP BY p.id_producto, p.nombre ORDER BY valor DESC LIMIT 5", params.toArray());
    }

    public List<Map<String, Object>> ventasPorMetodo(int idEmpresa, LocalDate desde, Integer idSede) {
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        params.add(desde);
        String sede = "";
        if (idSede != null) {
            sede = "AND id_sede = ? ";
            params.add(idSede);
        }
        return jdbc.queryForList("SELECT metodo_pago AS etiqueta, SUM(total) AS valor FROM ventas WHERE id_empresa = ? "
                + "AND estado = 'EMITIDA' AND fecha_hora >= ? " + sede + "GROUP BY metodo_pago ORDER BY valor DESC", params.toArray());
    }

    public List<Map<String, Object>> ventasPorVendedor(int idEmpresa, LocalDate desde, Integer idSede) {
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        params.add(desde);
        String sede = "";
        if (idSede != null) {
            sede = "AND v.id_sede = ? ";
            params.add(idSede);
        }
        return jdbc.queryForList("SELECT COALESCE(u.nombre, ui.nombre) AS etiqueta, SUM(v.total) AS valor FROM ventas v "
                + "LEFT JOIN usuarios u ON u.id_usuario = v.id_usuario LEFT JOIN usuarios_internos ui ON ui.id_usuario_interno = v.id_usuario_interno "
                + "WHERE v.id_empresa = ? AND v.estado = 'EMITIDA' AND v.fecha_hora >= ? " + sede
                + "GROUP BY etiqueta ORDER BY valor DESC", params.toArray());
    }

    /** Stock total de cada producto activo (para el estado del inventario). */
    public List<Map<String, Object>> stockProductos(int idEmpresa, Integer idSede) {
        List<Object> params = new ArrayList<>();
        String sede = "";
        if (idSede != null) {
            sede = "AND l.id_sede = ? ";
            params.add(idSede);
        }
        params.add(idEmpresa);
        return jdbc.queryForList("SELECT p.id_producto, p.stock_minimo, COALESCE(SUM(l.cantidad_actual), 0) AS stock FROM productos p "
                + "LEFT JOIN lotes l ON l.id_producto = p.id_producto " + sede
                + "WHERE p.id_empresa = ? AND p.activo = TRUE GROUP BY p.id_producto, p.stock_minimo", params.toArray());
    }

    // ---------------- Dashboard global (plataforma) ----------------

    public List<Map<String, Object>> ventasPlataformaPorDia(LocalDate desde) {
        return jdbc.queryForList("SELECT DATE_FORMAT(t.dia, '%d/%m') AS dia, t.total FROM (SELECT DATE(fecha_hora) AS dia, COUNT(*) AS total "
                + "FROM ventas WHERE fecha_hora >= ? GROUP BY DATE(fecha_hora)) t ORDER BY t.dia", desde);
    }

    public List<Map<String, Object>> actividadPorEmpresa(LocalDate desde) {
        return jdbc.queryForList("SELECT e.id_empresa, COALESCE(e.nombre_comercial, e.razon_social) AS empresa, e.estado, "
                + "(SELECT COUNT(*) FROM ventas v WHERE v.id_empresa = e.id_empresa AND v.fecha_hora >= ?) AS ventas, "
                + "(SELECT COUNT(*) FROM auditoria a WHERE a.id_empresa = e.id_empresa) AS acciones, "
                + "(SELECT COUNT(*) FROM usuarios u WHERE u.id_empresa = e.id_empresa) AS usuarios "
                + "FROM empresas e ORDER BY ventas DESC", desde);
    }

    public Map<String, Object> totalesPlataforma() {
        return jdbc.queryForMap("SELECT "
                + "(SELECT COUNT(*) FROM empresas WHERE estado = 'ACTIVA') AS empresas_activas, "
                + "(SELECT COUNT(*) FROM empresas WHERE estado = 'EN PRUEBA') AS empresas_prueba, "
                + "(SELECT COUNT(*) FROM empresas WHERE estado IN ('SUSPENDIDA','INACTIVA')) AS empresas_inactivas, "
                + "(SELECT COUNT(*) FROM sedes) AS sedes, (SELECT COUNT(*) FROM sedes WHERE activo = TRUE) AS sedes_activas, "
                + "(SELECT COUNT(*) FROM usuarios) AS usuarios, (SELECT COUNT(*) FROM usuarios WHERE estado = 'ACTIVO') AS usuarios_activos, "
                + "(SELECT COUNT(*) FROM usuarios_internos) AS internos, "
                + "(SELECT COUNT(*) FROM ventas WHERE DATE(fecha_hora) = CURDATE()) AS ventas_hoy, "
                + "(SELECT COUNT(DISTINCT usuario_tipo, id_usuario) FROM auditoria WHERE accion = 'LOGIN' AND DATE(fecha_hora) = CURDATE()) AS conectados_hoy");
    }
}
