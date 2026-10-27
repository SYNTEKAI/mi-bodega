package pe.mibodega.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Producto;

@Repository
public class ProductoDAO extends BaseDAO implements GenericDAO<Producto> {

    // El stock se calcula sumando los lotes de la sede
    private static final String SELECT = "SELECT p.*, c.nombre AS categoria_nombre, "
            + "COALESCE((SELECT SUM(l.cantidad_actual) FROM lotes l WHERE l.id_producto = p.id_producto AND l.id_sede = ?), 0) AS stock "
            + "FROM productos p JOIN categorias c ON c.id_categoria = p.id_categoria ";

    @Override
    public List<Producto> listar(int idEmpresa) {
        return lista(Producto.class, SELECT + "WHERE p.id_empresa = ? ORDER BY p.nombre", 0, idEmpresa);
    }

    /** Lista con filtros. El stock mostrado es el de la sede indicada. */
    public List<Producto> buscarConFiltros(int idEmpresa, int idSede, String texto, Integer idCategoria,
            String marca, String estado) {
        StringBuilder sql = new StringBuilder(SELECT + "WHERE p.id_empresa = ? ");
        List<Object> params = new ArrayList<>();
        params.add(idSede);
        params.add(idEmpresa);
        if (StringUtils.isNotBlank(texto)) {
            sql.append("AND (p.nombre LIKE ? OR p.codigo_barras LIKE ?) ");
            params.add("%" + texto.trim() + "%");
            params.add("%" + texto.trim() + "%");
        }
        if (idCategoria != null) {
            sql.append("AND p.id_categoria = ? ");
            params.add(idCategoria);
        }
        if (StringUtils.isNotBlank(marca)) {
            sql.append("AND p.marca = ? ");
            params.add(marca);
        }
        if ("activo".equals(estado)) {
            sql.append("AND p.activo = TRUE ");
        } else if ("inactivo".equals(estado)) {
            sql.append("AND p.activo = FALSE ");
        }
        sql.append("ORDER BY p.nombre");
        return lista(Producto.class, sql.toString(), params.toArray());
    }

    /** Productos activos con su stock, para el punto de venta. */
    public List<Producto> listarParaVenta(int idEmpresa, int idSede) {
        return lista(Producto.class, SELECT + "WHERE p.id_empresa = ? AND p.activo = TRUE ORDER BY p.nombre", idSede, idEmpresa);
    }

    @Override
    public Producto buscar(int idProducto, int idEmpresa) {
        return uno(Producto.class, SELECT + "WHERE p.id_producto = ? AND p.id_empresa = ?", 0, idProducto, idEmpresa);
    }

    public Producto buscarEnSede(int idProducto, int idEmpresa, int idSede) {
        return uno(Producto.class, SELECT + "WHERE p.id_producto = ? AND p.id_empresa = ?", idSede, idProducto, idEmpresa);
    }

    public boolean existeCodigo(int idEmpresa, String codigo, Integer excluirId) {
        return contar("SELECT COUNT(*) FROM productos WHERE id_empresa = ? AND codigo_barras = ? AND id_producto <> ?",
                idEmpresa, codigo, excluirId == null ? 0 : excluirId) > 0;
    }

    public List<String> marcas(int idEmpresa) {
        return jdbc.queryForList("SELECT DISTINCT marca FROM productos WHERE id_empresa = ? AND marca IS NOT NULL AND marca <> '' ORDER BY marca",
                String.class, idEmpresa);
    }

    /** Stock del producto en cada sede activa. */
    public List<Map<String, Object>> stockPorSede(int idProducto, int idEmpresa) {
        return jdbc.queryForList("SELECT s.nombre, COALESCE(SUM(l.cantidad_actual), 0) AS stock FROM sedes s "
                + "LEFT JOIN lotes l ON l.id_sede = s.id_sede AND l.id_producto = ? "
                + "WHERE s.id_empresa = ? AND s.activo = TRUE GROUP BY s.id_sede, s.nombre ORDER BY s.codigo", idProducto, idEmpresa);
    }

    @Override
    public int insertar(Producto p) {
        return insertarConId("INSERT INTO productos (id_empresa, id_categoria, codigo_barras, nombre, marca, descripcion, presentacion, "
                + "unidad, empaque, unidades_por_empaque, precio_compra, precio_venta, stock_minimo) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                p.getIdEmpresa(), p.getIdCategoria(), p.getCodigoBarras(), p.getNombre(), p.getMarca(), p.getDescripcion(),
                p.getPresentacion(), p.getUnidad(), p.getEmpaque(), p.getUnidadesPorEmpaque(), p.getPrecioCompra(),
                p.getPrecioVenta(), p.getStockMinimo());
    }

    @Override
    public void actualizar(Producto p) {
        jdbc.update("UPDATE productos SET id_categoria=?, codigo_barras=?, nombre=?, marca=?, descripcion=?, presentacion=?, unidad=?, "
                + "empaque=?, unidades_por_empaque=?, precio_compra=?, precio_venta=?, stock_minimo=? WHERE id_producto=? AND id_empresa=?",
                p.getIdCategoria(), p.getCodigoBarras(), p.getNombre(), p.getMarca(), p.getDescripcion(), p.getPresentacion(),
                p.getUnidad(), p.getEmpaque(), p.getUnidadesPorEmpaque(), p.getPrecioCompra(), p.getPrecioVenta(),
                p.getStockMinimo(), p.getIdProducto(), p.getIdEmpresa());
    }

    public void cambiarActivo(int idProducto, boolean activo) {
        jdbc.update("UPDATE productos SET activo = ? WHERE id_producto = ?", activo, idProducto);
    }

    /** Lotes, ventas y recepciones del producto. */
    public int contarRelacionados(int idProducto) {
        return contar("SELECT (SELECT COUNT(*) FROM lotes WHERE id_producto = ?) + (SELECT COUNT(*) FROM venta_detalles WHERE id_producto = ?) "
                + "+ (SELECT COUNT(*) FROM recepcion_detalles WHERE id_producto = ?)", idProducto, idProducto, idProducto);
    }

    public void eliminar(int idProducto) {
        jdbc.update("DELETE FROM productos WHERE id_producto = ?", idProducto);
    }
}
