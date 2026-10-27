package pe.mibodega.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Rol;
import pe.mibodega.model.RolInterno;

@Repository
public class RolDAO extends BaseDAO {

    private static final String SELECT = "SELECT r.*, "
            + "(SELECT COUNT(*) FROM usuario_roles ur WHERE ur.id_rol = r.id_rol) AS usuarios, "
            + "(SELECT COUNT(*) FROM rol_permisos rp WHERE rp.id_rol = r.id_rol) AS total_permisos FROM roles r ";

    public List<Rol> listar(int idEmpresa) {
        return lista(Rol.class, SELECT + "WHERE r.id_empresa = ? ORDER BY r.id_rol", idEmpresa);
    }

    public List<Rol> listarActivos(int idEmpresa) {
        return lista(Rol.class, SELECT + "WHERE r.id_empresa = ? AND r.activo = TRUE ORDER BY r.id_rol", idEmpresa);
    }

    public Rol buscar(int idRol, int idEmpresa) {
        return uno(Rol.class, SELECT + "WHERE r.id_rol = ? AND r.id_empresa = ?", idRol, idEmpresa);
    }

    public List<Rol> rolesDeUsuario(int idUsuario) {
        return lista(Rol.class, "SELECT r.* FROM roles r JOIN usuario_roles ur ON ur.id_rol = r.id_rol "
                + "WHERE ur.id_usuario = ? ORDER BY r.id_rol", idUsuario);
    }

    public boolean existeNombre(int idEmpresa, String nombre, Integer excluirId) {
        return contar("SELECT COUNT(*) FROM roles WHERE id_empresa = ? AND nombre = ? AND id_rol <> ?",
                idEmpresa, nombre, excluirId == null ? 0 : excluirId) > 0;
    }

    public int insertar(int idEmpresa, String nombre) {
        return insertarConId("INSERT INTO roles (id_empresa, nombre) VALUES (?, ?)", idEmpresa, nombre);
    }

    public void cambiarNombre(int idRol, String nombre) {
        jdbc.update("UPDATE roles SET nombre = ? WHERE id_rol = ?", nombre, idRol);
    }

    public void cambiarActivo(int idRol, boolean activo) {
        jdbc.update("UPDATE roles SET activo = ? WHERE id_rol = ?", activo, idRol);
    }

    public void eliminar(int idRol) {
        jdbc.update("DELETE FROM roles WHERE id_rol = ?", idRol);
    }

    /** Crea el rol Administrador de una empresa nueva con todos los permisos. */
    public int crearAdministrador(int idEmpresa) {
        int idRol = insertar(idEmpresa, "Administrador");
        jdbc.update("INSERT INTO rol_permisos SELECT ?, id_permiso FROM permisos WHERE ambito = 'EMPRESA'", idRol);
        return idRol;
    }

    // ---- Roles internos de la plataforma ----

    public List<RolInterno> listarInternos() {
        return lista(RolInterno.class, "SELECT r.*, (SELECT COUNT(*) FROM usuarios_internos u WHERE u.id_rol_interno = r.id_rol_interno) AS usuarios "
                + "FROM roles_internos r ORDER BY r.id_rol_interno");
    }

    public RolInterno buscarInterno(int idRolInterno) {
        return uno(RolInterno.class, "SELECT * FROM roles_internos WHERE id_rol_interno = ?", idRolInterno);
    }

    public boolean existeNombreInterno(String nombre, Integer excluirId) {
        return contar("SELECT COUNT(*) FROM roles_internos WHERE nombre = ? AND id_rol_interno <> ?",
                nombre, excluirId == null ? 0 : excluirId) > 0;
    }

    public int insertarInterno(String nombre) {
        return insertarConId("INSERT INTO roles_internos (nombre) VALUES (?)", nombre);
    }

    public void cambiarNombreInterno(int idRolInterno, String nombre) {
        jdbc.update("UPDATE roles_internos SET nombre = ? WHERE id_rol_interno = ?", nombre, idRolInterno);
    }

    public void eliminarInterno(int idRolInterno) {
        jdbc.update("DELETE FROM roles_internos WHERE id_rol_interno = ?", idRolInterno);
    }

    public int contarUsuariosInternos(int idRolInterno) {
        return contar("SELECT COUNT(*) FROM usuarios_internos WHERE id_rol_interno = ?", idRolInterno);
    }
}
