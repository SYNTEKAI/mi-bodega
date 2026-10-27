package pe.mibodega.dao;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Proveedor;

@Repository
public class ProveedorDAO extends BaseDAO implements GenericDAO<Proveedor> {

    private static final String SELECT = "SELECT p.*, (SELECT COUNT(*) FROM recepciones r WHERE r.id_proveedor = p.id_proveedor) AS guias "
            + "FROM proveedores p ";

    @Override
    public List<Proveedor> listar(int idEmpresa) {
        return lista(Proveedor.class, SELECT + "WHERE p.id_empresa = ? ORDER BY p.razon_social", idEmpresa);
    }

    public List<Proveedor> listarActivos(int idEmpresa) {
        return lista(Proveedor.class, SELECT + "WHERE p.id_empresa = ? AND p.activo = TRUE ORDER BY p.razon_social", idEmpresa);
    }

    public List<Proveedor> buscarConFiltros(int idEmpresa, String texto, String estado) {
        StringBuilder sql = new StringBuilder(SELECT + "WHERE p.id_empresa = ? ");
        List<Object> params = new ArrayList<>();
        params.add(idEmpresa);
        if (StringUtils.isNotBlank(texto)) {
            sql.append("AND CONCAT_WS(' ', p.documento, p.razon_social, p.nombre_comercial, p.contacto) LIKE ? ");
            params.add("%" + texto.trim() + "%");
        }
        if ("activo".equals(estado)) {
            sql.append("AND p.activo = TRUE ");
        } else if ("inactivo".equals(estado)) {
            sql.append("AND p.activo = FALSE ");
        }
        sql.append("ORDER BY p.razon_social");
        return lista(Proveedor.class, sql.toString(), params.toArray());
    }

    @Override
    public Proveedor buscar(int id, int idEmpresa) {
        return uno(Proveedor.class, SELECT + "WHERE p.id_proveedor = ? AND p.id_empresa = ?", id, idEmpresa);
    }

    public boolean existeDocumento(int idEmpresa, String documento, Integer excluirId) {
        return contar("SELECT COUNT(*) FROM proveedores WHERE id_empresa = ? AND documento = ? AND id_proveedor <> ?",
                idEmpresa, documento, excluirId == null ? 0 : excluirId) > 0;
    }

    @Override
    public int insertar(Proveedor p) {
        return insertarConId("INSERT INTO proveedores (id_empresa, tipo_doc, documento, razon_social, nombre_comercial, contacto, telefono, "
                + "correo, direccion, observaciones) VALUES (?,?,?,?,?,?,?,?,?,?)",
                p.getIdEmpresa(), p.getTipoDoc(), p.getDocumento(), p.getRazonSocial(), p.getNombreComercial(), p.getContacto(),
                p.getTelefono(), p.getCorreo(), p.getDireccion(), p.getObservaciones());
    }

    @Override
    public void actualizar(Proveedor p) {
        jdbc.update("UPDATE proveedores SET tipo_doc=?, documento=?, razon_social=?, nombre_comercial=?, contacto=?, telefono=?, correo=?, "
                + "direccion=?, observaciones=? WHERE id_proveedor=? AND id_empresa=?",
                p.getTipoDoc(), p.getDocumento(), p.getRazonSocial(), p.getNombreComercial(), p.getContacto(), p.getTelefono(),
                p.getCorreo(), p.getDireccion(), p.getObservaciones(), p.getIdProveedor(), p.getIdEmpresa());
    }

    public void cambiarActivo(int id, boolean activo) {
        jdbc.update("UPDATE proveedores SET activo = ? WHERE id_proveedor = ?", activo, id);
    }

    public void eliminar(int id) {
        jdbc.update("DELETE FROM proveedores WHERE id_proveedor = ?", id);
    }
}
