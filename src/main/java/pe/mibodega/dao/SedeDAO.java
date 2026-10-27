package pe.mibodega.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Sede;

@Repository
public class SedeDAO extends BaseDAO implements GenericDAO<Sede> {

    @Override
    public List<Sede> listar(int idEmpresa) {
        return lista(Sede.class, "SELECT * FROM sedes WHERE id_empresa = ? ORDER BY codigo", idEmpresa);
    }

    public List<Sede> listarActivas(int idEmpresa) {
        return lista(Sede.class, "SELECT * FROM sedes WHERE id_empresa = ? AND activo = TRUE ORDER BY codigo", idEmpresa);
    }

    @Override
    public Sede buscar(int idSede, int idEmpresa) {
        return uno(Sede.class, "SELECT * FROM sedes WHERE id_sede = ? AND id_empresa = ?", idSede, idEmpresa);
    }

    @Override
    public int insertar(Sede s) {
        return insertarConId("INSERT INTO sedes (id_empresa, codigo, nombre, direccion, distrito, provincia, responsable, telefono, fecha_creacion) "
                + "VALUES (?,?,?,?,?,?,?,?,CURDATE())",
                s.getIdEmpresa(), s.getCodigo(), s.getNombre(), s.getDireccion(), s.getDistrito(),
                s.getProvincia(), s.getResponsable(), s.getTelefono());
    }

    @Override
    public void actualizar(Sede s) {
        jdbc.update("UPDATE sedes SET codigo=?, nombre=?, direccion=?, distrito=?, provincia=?, responsable=?, telefono=? "
                + "WHERE id_sede=? AND id_empresa=?",
                s.getCodigo(), s.getNombre(), s.getDireccion(), s.getDistrito(), s.getProvincia(),
                s.getResponsable(), s.getTelefono(), s.getIdSede(), s.getIdEmpresa());
    }

    public boolean existeCodigo(int idEmpresa, String codigo, Integer excluirId) {
        return contar("SELECT COUNT(*) FROM sedes WHERE id_empresa = ? AND codigo = ? AND id_sede <> ?",
                idEmpresa, codigo, excluirId == null ? 0 : excluirId) > 0;
    }

    public void cambiarActivo(int idSede, boolean activo) {
        jdbc.update("UPDATE sedes SET activo = ? WHERE id_sede = ?", activo, idSede);
    }

    /** Usuarios, ventas, lotes y recepciones que usan la sede. */
    public int contarRelacionados(int idSede) {
        return contar("SELECT (SELECT COUNT(*) FROM usuarios WHERE id_sede = ?) + (SELECT COUNT(*) FROM ventas WHERE id_sede = ?) "
                + "+ (SELECT COUNT(*) FROM lotes WHERE id_sede = ?) + (SELECT COUNT(*) FROM recepciones WHERE id_sede = ?)",
                idSede, idSede, idSede, idSede);
    }

    public void eliminar(int idSede) {
        jdbc.update("DELETE FROM sedes WHERE id_sede = ?", idSede);
    }

    public int contarVentas(int idSede) {
        return contar("SELECT COUNT(*) FROM ventas WHERE id_sede = ?", idSede);
    }

    public int contarUsuarios(int idSede) {
        return contar("SELECT COUNT(*) FROM usuarios WHERE id_sede = ?", idSede);
    }
}
