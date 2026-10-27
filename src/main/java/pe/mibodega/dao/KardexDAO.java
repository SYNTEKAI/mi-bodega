package pe.mibodega.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.MovimientoKardex;
import pe.mibodega.model.UsuarioSesion;

@Repository
public class KardexDAO extends BaseDAO {

    /** Registra un movimiento. tipo: ENTRADA, SALIDA o AJUSTE (en ajuste la cantidad lleva signo). */
    public void registrar(int idEmpresa, int idSede, int idProducto, int idLote, String tipo, int cantidad, BigDecimal costo,
            String documento, Integer idVenta, Integer idRecepcion, Integer idProveedor, UsuarioSesion usuario) {
        jdbc.update("INSERT INTO kardex (id_empresa, id_sede, id_producto, id_lote, tipo, cantidad, costo_unitario, documento, "
                + "id_venta, id_recepcion, id_proveedor, id_usuario, id_usuario_interno, fecha_hora) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())",
                idEmpresa, idSede, idProducto, idLote, tipo, cantidad, costo, documento, idVenta, idRecepcion, idProveedor,
                usuario.getIdUsuarioEmpresa(), usuario.getIdUsuarioInterno());
    }

    /**
     * Movimientos con el saldo acumulado por producto y sede.
     * El saldo se calcula sobre todos los movimientos (funcion de ventana) y despues se filtra,
     * asi sigue siendo correcto aunque la lista este filtrada.
     */
    public List<MovimientoKardex> buscar(int idEmpresa, LocalDate desde, LocalDate hasta, Integer idProducto, Integer idCategoria,
            Integer idSede, String tipo, Integer idProveedor) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ("
                + "SELECT k.id_movimiento, k.id_producto, k.id_sede, k.id_lote, k.fecha_hora, k.tipo, k.cantidad, k.documento, "
                + "k.costo_unitario, k.id_proveedor, p.id_categoria, p.nombre AS producto_nombre, c.nombre AS categoria_nombre, "
                + "COALESCE(p.presentacion, p.unidad) AS presentacion, s.nombre AS sede_nombre, "
                + "COALESCE(u.nombre, ui.nombre, '-') AS usuario_nombre, pr.razon_social AS proveedor_nombre, "
                + "SUM(CASE WHEN k.tipo = 'SALIDA' THEN -k.cantidad ELSE k.cantidad END) "
                + "  OVER (PARTITION BY k.id_producto, k.id_sede ORDER BY k.fecha_hora, k.id_movimiento) AS saldo "
                + "FROM kardex k JOIN productos p ON p.id_producto = k.id_producto JOIN categorias c ON c.id_categoria = p.id_categoria "
                + "JOIN sedes s ON s.id_sede = k.id_sede LEFT JOIN usuarios u ON u.id_usuario = k.id_usuario "
                + "LEFT JOIN usuarios_internos ui ON ui.id_usuario_interno = k.id_usuario_interno "
                + "LEFT JOIN proveedores pr ON pr.id_proveedor = k.id_proveedor WHERE k.id_empresa = ?) t WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        if (desde != null) {
            sql.append("AND DATE(t.fecha_hora) >= ? ");
            params.add(desde);
        }
        if (hasta != null) {
            sql.append("AND DATE(t.fecha_hora) <= ? ");
            params.add(hasta);
        }
        if (idProducto != null) {
            sql.append("AND t.id_producto = ? ");
            params.add(idProducto);
        }
        if (idCategoria != null) {
            sql.append("AND t.id_categoria = ? ");
            params.add(idCategoria);
        }
        if (idSede != null) {
            sql.append("AND t.id_sede = ? ");
            params.add(idSede);
        }
        if (tipo != null && !tipo.isBlank()) {
            sql.append("AND t.tipo = ? ");
            params.add(tipo);
        }
        if (idProveedor != null) {
            sql.append("AND t.id_proveedor = ? ");
            params.add(idProveedor);
        }
        sql.append("ORDER BY t.fecha_hora, t.id_movimiento");
        return lista(MovimientoKardex.class, sql.toString(), params.toArray());
    }
}
