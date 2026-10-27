package pe.mibodega.dao;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Empresa;

@Repository
public class EmpresaDAO extends BaseDAO {

    private static final String SELECT = "SELECT e.id_empresa, e.ruc, e.razon_social, e.nombre_comercial, e.direccion, e.rubro, "
            + "e.telefono, e.correo, e.plan, e.estado, e.logo_tipo, e.logo IS NOT NULL AS tiene_logo, e.fecha_registro, "
            + "(SELECT COUNT(*) FROM sedes s WHERE s.id_empresa = e.id_empresa) AS sedes, "
            + "(SELECT COUNT(*) FROM usuarios u WHERE u.id_empresa = e.id_empresa) AS usuarios "
            + "FROM empresas e ";

    public List<Empresa> listar() {
        return lista(Empresa.class, SELECT + "ORDER BY e.id_empresa");
    }

    public Empresa buscar(int idEmpresa) {
        return uno(Empresa.class, SELECT + "WHERE e.id_empresa = ?", idEmpresa);
    }

    public boolean existeRuc(String ruc, Integer excluirId) {
        return contar("SELECT COUNT(*) FROM empresas WHERE ruc = ? AND id_empresa <> ?",
                ruc, excluirId == null ? 0 : excluirId) > 0;
    }

    public int insertar(Empresa e) {
        return insertarConId("INSERT INTO empresas (ruc, razon_social, nombre_comercial, direccion, rubro, telefono, correo, plan, estado, fecha_registro) "
                + "VALUES (?,?,?,?,?,?,?,?,'EN PRUEBA',CURDATE())",
                e.getRuc(), e.getRazonSocial(), e.getNombreComercial(), e.getDireccion(), e.getRubro(),
                e.getTelefono(), e.getCorreo(), e.getPlan());
    }

    public void actualizar(Empresa e) {
        jdbc.update("UPDATE empresas SET ruc=?, razon_social=?, nombre_comercial=?, direccion=?, rubro=?, telefono=?, correo=?, plan=? WHERE id_empresa=?",
                e.getRuc(), e.getRazonSocial(), e.getNombreComercial(), e.getDireccion(), e.getRubro(),
                e.getTelefono(), e.getCorreo(), e.getPlan(), e.getIdEmpresa());
    }

    public void cambiarEstado(int idEmpresa, String estado) {
        jdbc.update("UPDATE empresas SET estado = ? WHERE id_empresa = ?", estado, idEmpresa);
    }

    public void guardarLogo(int idEmpresa, byte[] logo, String tipo) {
        jdbc.update("UPDATE empresas SET logo = ?, logo_tipo = ? WHERE id_empresa = ?", logo, tipo, idEmpresa);
    }

    public void quitarLogo(int idEmpresa) {
        jdbc.update("UPDATE empresas SET logo = NULL, logo_tipo = NULL WHERE id_empresa = ?", idEmpresa);
    }

    public Map<String, Object> obtenerLogo(int idEmpresa) {
        List<Map<String, Object>> filas = jdbc.queryForList(
                "SELECT logo, logo_tipo FROM empresas WHERE id_empresa = ? AND logo IS NOT NULL", idEmpresa);
        return filas.isEmpty() ? null : filas.get(0);
    }
}
