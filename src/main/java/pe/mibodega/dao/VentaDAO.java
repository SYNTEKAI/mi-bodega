package pe.mibodega.dao;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Venta;
import pe.mibodega.model.VentaDetalle;

@Repository
public class VentaDAO extends BaseDAO {

    private static final String SELECT = "SELECT v.*, c.nombre AS cliente_nombre, c.documento AS cliente_documento, "
            + "c.tipo_doc AS cliente_tipo_doc, s.nombre AS sede_nombre, COALESCE(u.nombre, ui.nombre) AS vendedor_nombre "
            + "FROM ventas v JOIN sedes s ON s.id_sede = v.id_sede LEFT JOIN clientes c ON c.id_cliente = v.id_cliente "
            + "LEFT JOIN usuarios u ON u.id_usuario = v.id_usuario "
            + "LEFT JOIN usuarios_internos ui ON ui.id_usuario_interno = v.id_usuario_interno ";

    public int insertar(Venta v) {
        return insertarConId("INSERT INTO ventas (id_empresa, id_sede, id_cliente, id_usuario, id_usuario_interno, numero_comprobante, "
                + "fecha_hora, metodo_pago, total, monto_recibido, vuelto) VALUES (?,?,?,?,?,?,NOW(),?,?,?,?)",
                v.getIdEmpresa(), v.getIdSede(), v.getIdCliente(), v.getIdUsuario(), v.getIdUsuarioInterno(),
                v.getNumeroComprobante(), v.getMetodoPago(), v.getTotal(), v.getMontoRecibido(), v.getVuelto());
    }

    public int insertarDetalle(VentaDetalle d) {
        return insertarConId("INSERT INTO venta_detalles (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES (?,?,?,?,?)",
                d.getIdVenta(), d.getIdProducto(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
    }

    public void insertarDetalleLote(int idDetalle, int idLote, int cantidad) {
        jdbc.update("INSERT INTO venta_detalle_lotes (id_detalle, id_lote, cantidad) VALUES (?,?,?)", idDetalle, idLote, cantidad);
    }

    public Venta buscar(int idVenta, int idEmpresa) {
        Venta v = uno(Venta.class, SELECT + "WHERE v.id_venta = ? AND v.id_empresa = ?", idVenta, idEmpresa);
        if (v != null) {
            v.setDetalles(detalles(idVenta));
        }
        return v;
    }

    public List<VentaDetalle> detalles(int idVenta) {
        return lista(VentaDetalle.class, "SELECT d.*, p.nombre AS producto_nombre, "
                + "(SELECT GROUP_CONCAT(CONCAT('L-', dl.id_lote) SEPARATOR ', ') FROM venta_detalle_lotes dl WHERE dl.id_detalle = d.id_detalle) AS lotes "
                + "FROM venta_detalles d JOIN productos p ON p.id_producto = d.id_producto WHERE d.id_venta = ? ORDER BY d.id_detalle", idVenta);
    }

    /** Lotes de los que salio cada linea (para devolver el stock al anular). */
    public List<Map<String, Object>> lotesDeVenta(int idVenta) {
        return jdbc.queryForList("SELECT dl.id_lote, dl.cantidad, d.id_producto, l.costo_unitario FROM venta_detalle_lotes dl "
                + "JOIN venta_detalles d ON d.id_detalle = dl.id_detalle JOIN lotes l ON l.id_lote = dl.id_lote WHERE d.id_venta = ?", idVenta);
    }

    public void anular(int idVenta, String motivo) {
        jdbc.update("UPDATE ventas SET estado = 'ANULADA', motivo_anulacion = ? WHERE id_venta = ?", motivo, idVenta);
    }

    public List<Venta> ventasDelDia(int idEmpresa, int idSede) {
        return lista(Venta.class, SELECT + "WHERE v.id_empresa = ? AND v.id_sede = ? AND DATE(v.fecha_hora) = CURDATE() "
                + "ORDER BY v.fecha_hora DESC, v.id_venta DESC", idEmpresa, idSede);
    }

    /** Totales del dia por medio de pago (resumen de caja). */
    public List<Map<String, Object>> cajaDelDia(int idEmpresa, int idSede) {
        return jdbc.queryForList("SELECT metodo_pago, COUNT(*) AS cantidad, SUM(total) AS total FROM ventas "
                + "WHERE id_empresa = ? AND id_sede = ? AND DATE(fecha_hora) = CURDATE() AND estado = 'EMITIDA' "
                + "GROUP BY metodo_pago ORDER BY metodo_pago", idEmpresa, idSede);
    }

    public Venta ultimaVenta(int idEmpresa) {
        Venta v = uno(Venta.class, SELECT + "WHERE v.id_empresa = ? ORDER BY v.id_venta DESC LIMIT 1", idEmpresa);
        if (v != null) {
            v.setDetalles(detalles(v.getIdVenta()));
        }
        return v;
    }
}
