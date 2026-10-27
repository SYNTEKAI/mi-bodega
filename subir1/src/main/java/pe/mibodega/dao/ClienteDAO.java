package pe.mibodega.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Cliente;
import pe.mibodega.model.Venta;

@Repository
public class ClienteDAO extends BaseDAO implements GenericDAO<Cliente> {

    // Resumen de compras de cada cliente (solo ventas emitidas)
    private static final String SELECT = "SELECT c.*, COUNT(v.id_venta) AS compras, COALESCE(SUM(v.total), 0) AS total_comprado, "
            + "MAX(v.fecha_hora) AS ultima_compra FROM clientes c "
            + "LEFT JOIN ventas v ON v.id_cliente = c.id_cliente AND v.estado = 'EMITIDA' ";

    @Override
    public List<Cliente> listar(int idEmpresa) {
        return buscarConFiltros(idEmpresa, null, null);
    }

    public List<Cliente> buscarConFiltros(int idEmpresa, String texto, String tipo) {
        StringBuilder sql = new StringBuilder(SELECT + "WHERE c.id_empresa = ? ");
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        if (StringUtils.isNotBlank(texto)) {
            sql.append("AND CONCAT(c.documento, ' ', c.nombre) LIKE ? ");
            params.add("%" + texto.trim() + "%");
        }
        if (StringUtils.isNotBlank(tipo)) {
            sql.append("AND c.tipo_doc = ? ");
            params.add(tipo);
        }
        sql.append("GROUP BY c.id_cliente ORDER BY total_comprado DESC");
        return lista(Cliente.class, sql.toString(), params.toArray());
    }

    @Override
    public Cliente buscar(int id, int idEmpresa) {
        return uno(Cliente.class, SELECT + "WHERE c.id_cliente = ? AND c.id_empresa = ? GROUP BY c.id_cliente", id, idEmpresa);
    }

    public Cliente buscarPorDocumento(int idEmpresa, String documento) {
        return uno(Cliente.class, SELECT + "WHERE c.id_empresa = ? AND c.documento = ? GROUP BY c.id_cliente", idEmpresa, documento);
    }

    @Override
    public int insertar(Cliente c) {
        return insertarConId("INSERT INTO clientes (id_empresa, tipo_doc, documento, nombre, telefono, correo, direccion, fecha_registro) "
                + "VALUES (?,?,?,?,?,?,?,CURDATE())",
                c.getIdEmpresa(), c.getTipoDoc(), c.getDocumento(), c.getNombre(), c.getTelefono(), c.getCorreo(), c.getDireccion());
    }

    @Override
    public void actualizar(Cliente c) {
        jdbc.update("UPDATE clientes SET tipo_doc=?, documento=?, nombre=?, telefono=?, correo=?, direccion=? WHERE id_cliente=? AND id_empresa=?",
                c.getTipoDoc(), c.getDocumento(), c.getNombre(), c.getTelefono(), c.getCorreo(), c.getDireccion(),
                c.getIdCliente(), c.getIdEmpresa());
    }

    public boolean existeDocumento(int idEmpresa, String documento, Integer excluirId) {
        return contar("SELECT COUNT(*) FROM clientes WHERE id_empresa = ? AND documento = ? AND id_cliente <> ?",
                idEmpresa, documento, excluirId == null ? 0 : excluirId) > 0;
    }

    public int contarVentas(int idCliente) {
        return contar("SELECT COUNT(*) FROM ventas WHERE id_cliente = ?", idCliente);
    }

    public void eliminar(int id) {
        jdbc.update("DELETE FROM clientes WHERE id_cliente = ?", id);
    }

    /** Los 5 productos que mas compra el cliente. */
    public List<Map<String, Object>> productosFrecuentes(int idCliente) {
        return jdbc.queryForList("SELECT p.nombre, SUM(d.cantidad) AS unidades FROM venta_detalles d "
                + "JOIN ventas v ON v.id_venta = d.id_venta JOIN productos p ON p.id_producto = d.id_producto "
                + "WHERE v.id_cliente = ? AND v.estado = 'EMITIDA' GROUP BY p.id_producto, p.nombre ORDER BY unidades DESC LIMIT 5", idCliente);
    }

    public List<Venta> historial(int idCliente) {
        return lista(Venta.class, "SELECT v.*, s.nombre AS sede_nombre FROM ventas v JOIN sedes s ON s.id_sede = v.id_sede "
                + "WHERE v.id_cliente = ? ORDER BY v.fecha_hora DESC", idCliente);
    }
}
