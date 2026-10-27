package pe.mibodega.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Recepcion;
import pe.mibodega.model.RecepcionDetalle;
import pe.mibodega.model.RecepcionModificacion;

@Repository
public class RecepcionDAO extends BaseDAO {

    private static final String SELECT = "SELECT r.*, pr.razon_social AS proveedor_nombre, pr.documento AS proveedor_documento, "
            + "s.nombre AS sede_nombre, COALESCE(u.nombre, ui.nombre) AS usuario_nombre, "
            + "(SELECT COUNT(DISTINCT d.id_producto) FROM recepcion_detalles d WHERE d.id_recepcion = r.id_recepcion) AS productos, "
            + "(SELECT COALESCE(SUM(d.total_unidades), 0) FROM recepcion_detalles d WHERE d.id_recepcion = r.id_recepcion) AS unidades, "
            + "(SELECT MAX(m.fecha_hora) FROM recepcion_modificaciones m WHERE m.id_recepcion = r.id_recepcion) AS ultima_modificacion, "
            + "(SELECT m.usuario_nombre FROM recepcion_modificaciones m WHERE m.id_recepcion = r.id_recepcion "
            + " ORDER BY m.id_modificacion DESC LIMIT 1) AS modifico_nombre "
            + "FROM recepciones r JOIN proveedores pr ON pr.id_proveedor = r.id_proveedor JOIN sedes s ON s.id_sede = r.id_sede "
            + "LEFT JOIN usuarios u ON u.id_usuario = r.id_usuario LEFT JOIN usuarios_internos ui ON ui.id_usuario_interno = r.id_usuario_interno ";

    public List<Recepcion> buscarConFiltros(int idEmpresa, LocalDate desde, LocalDate hasta, Integer idProveedor,
            Integer idProducto, String numero, String estado) {
        StringBuilder sql = new StringBuilder(SELECT + "WHERE r.id_empresa = ? ");
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        if (desde != null) {
            sql.append("AND DATE(r.fecha_hora) >= ? ");
            params.add(desde);
        }
        if (hasta != null) {
            sql.append("AND DATE(r.fecha_hora) <= ? ");
            params.add(hasta);
        }
        if (idProveedor != null) {
            sql.append("AND r.id_proveedor = ? ");
            params.add(idProveedor);
        }
        if (idProducto != null) {
            sql.append("AND EXISTS (SELECT 1 FROM recepcion_detalles d WHERE d.id_recepcion = r.id_recepcion AND d.id_producto = ?) ");
            params.add(idProducto);
        }
        if (StringUtils.isNotBlank(numero)) {
            sql.append("AND (r.numero LIKE ? OR r.guia_proveedor LIKE ?) ");
            params.add("%" + numero.trim() + "%");
            params.add("%" + numero.trim() + "%");
        }
        if (StringUtils.isNotBlank(estado)) {
            sql.append("AND r.estado = ? ");
            params.add(estado);
        }
        sql.append("ORDER BY r.numero DESC");
        return lista(Recepcion.class, sql.toString(), params.toArray());
    }

    public Recepcion buscar(int idRecepcion, int idEmpresa) {
        Recepcion r = uno(Recepcion.class, SELECT + "WHERE r.id_recepcion = ? AND r.id_empresa = ?", idRecepcion, idEmpresa);
        if (r != null) {
            r.setDetalles(detalles(idRecepcion));
        }
        return r;
    }

    /** Lineas con datos del producto y cuantas unidades ya salieron del lote. */
    public List<RecepcionDetalle> detalles(int idRecepcion) {
        return lista(RecepcionDetalle.class, "SELECT d.*, p.nombre AS producto_nombre, p.marca, p.presentacion, p.unidad, "
                + "COALESCE(l.cantidad_ingresada - l.cantidad_actual, 0) AS consumido FROM recepcion_detalles d "
                + "JOIN productos p ON p.id_producto = d.id_producto LEFT JOIN lotes l ON l.id_lote = d.id_lote "
                + "WHERE d.id_recepcion = ? ORDER BY d.id_detalle", idRecepcion);
    }

    public boolean existeGuia(int idEmpresa, int idProveedor, String guia, Integer excluirId) {
        return contar("SELECT COUNT(*) FROM recepciones WHERE id_empresa = ? AND id_proveedor = ? AND guia_proveedor = ? "
                + "AND estado <> 'ANULADA' AND id_recepcion <> ?", idEmpresa, idProveedor, guia, excluirId == null ? 0 : excluirId) > 0;
    }

    public int insertar(Recepcion r) {
        return insertarConId("INSERT INTO recepciones (id_empresa, id_sede, id_proveedor, numero, guia_proveedor, fecha_hora, estado, "
                + "observaciones, id_usuario, id_usuario_interno) VALUES (?,?,?,?,?,NOW(),?,?,?,?)",
                r.getIdEmpresa(), r.getIdSede(), r.getIdProveedor(), r.getNumero(), r.getGuiaProveedor(), r.getEstado(),
                r.getObservaciones(), r.getIdUsuario(), r.getIdUsuarioInterno());
    }

    public void actualizarCabecera(Recepcion r) {
        jdbc.update("UPDATE recepciones SET id_proveedor = ?, guia_proveedor = ?, observaciones = ? WHERE id_recepcion = ?",
                r.getIdProveedor(), r.getGuiaProveedor(), r.getObservaciones(), r.getIdRecepcion());
    }

    /** Al confirmar, la fecha real de recepcion la pone el sistema. */
    public void confirmar(int idRecepcion) {
        jdbc.update("UPDATE recepciones SET estado = 'CONFIRMADA', fecha_hora = NOW() WHERE id_recepcion = ?", idRecepcion);
    }

    public void cambiarEstado(int idRecepcion, String estado) {
        jdbc.update("UPDATE recepciones SET estado = ? WHERE id_recepcion = ?", estado, idRecepcion);
    }

    public void eliminarDetalles(int idRecepcion) {
        jdbc.update("DELETE FROM recepcion_detalles WHERE id_recepcion = ?", idRecepcion);
    }

    public void insertarDetalle(int idRecepcion, RecepcionDetalle d) {
        jdbc.update("INSERT INTO recepcion_detalles (id_recepcion, id_producto, empaque, contenido, cantidad_empaques, total_unidades, "
                + "costo_unitario, fecha_vencimiento, id_lote) VALUES (?,?,?,?,?,?,?,?,?)",
                idRecepcion, d.getIdProducto(), d.getEmpaque(), d.getContenido(), d.getCantidadEmpaques(), d.getTotalUnidades(),
                d.getCostoUnitario(), d.getFechaVencimiento(), d.getIdLote());
    }

    public void insertarModificacion(RecepcionModificacion m) {
        jdbc.update("INSERT INTO recepcion_modificaciones (id_recepcion, fecha_hora, usuario_nombre, rol_nombre, motivo, cambios) "
                + "VALUES (?, NOW(), ?, ?, ?, ?)", m.getIdRecepcion(), m.getUsuarioNombre(), m.getRolNombre(), m.getMotivo(), m.getCambios());
    }

    public List<RecepcionModificacion> modificaciones(int idRecepcion) {
        return lista(RecepcionModificacion.class, "SELECT * FROM recepcion_modificaciones WHERE id_recepcion = ? ORDER BY id_modificacion",
                idRecepcion);
    }

    /** Filas para el Excel de guias (una fila por linea recibida). */
    public List<Map<String, Object>> lineasParaExcel(int idRecepcion) {
        return jdbc.queryForList("SELECT p.nombre, p.marca, p.presentacion, d.empaque, d.contenido, d.cantidad_empaques, "
                + "d.total_unidades, d.costo_unitario FROM recepcion_detalles d JOIN productos p ON p.id_producto = d.id_producto "
                + "WHERE d.id_recepcion = ? ORDER BY d.id_detalle", idRecepcion);
    }
}
