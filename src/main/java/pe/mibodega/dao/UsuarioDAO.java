package pe.mibodega.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Usuario;

@Repository
public class UsuarioDAO extends BaseDAO {

    private static final String SELECT = "SELECT u.*, s.nombre AS sede_nombre, r.nombre AS rol_activo_nombre, "
            + "(SELECT GROUP_CONCAT(r2.nombre ORDER BY r2.id_rol SEPARATOR ', ') FROM usuario_roles ur "
            + " JOIN roles r2 ON r2.id_rol = ur.id_rol WHERE ur.id_usuario = u.id_usuario) AS roles "
            + "FROM usuarios u JOIN sedes s ON s.id_sede = u.id_sede LEFT JOIN roles r ON r.id_rol = u.id_rol_activo ";

    public Usuario buscarPorLogin(String login) {
        return uno(Usuario.class, SELECT + "WHERE u.usuario = ?", login);
    }

    public Usuario buscar(int idUsuario, int idEmpresa) {
        return uno(Usuario.class, SELECT + "WHERE u.id_usuario = ? AND u.id_empresa = ?", idUsuario, idEmpresa);
    }

    public Usuario buscarPorId(int idUsuario) {
        return uno(Usuario.class, SELECT + "WHERE u.id_usuario = ?", idUsuario);
    }

    public List<Usuario> listar(int idEmpresa) {
        return lista(Usuario.class, SELECT + "WHERE u.id_empresa = ? ORDER BY u.id_usuario", idEmpresa);
    }

    /** El login no se puede repetir ni entre usuarios de empresa ni internos. */
    public boolean existeLogin(String login) {
        return contar("SELECT (SELECT COUNT(*) FROM usuarios WHERE usuario = ?) + (SELECT COUNT(*) FROM usuarios_internos WHERE usuario = ?)",
                login, login) > 0;
    }

    public int insertar(Usuario u) {
        return insertarConId("INSERT INTO usuarios (id_empresa, id_sede, usuario, clave_hash, nombre, debe_cambiar_clave, id_rol_activo) "
                + "VALUES (?,?,?,?,?,?,?)",
                u.getIdEmpresa(), u.getIdSede(), u.getUsuario(), u.getClaveHash(), u.getNombre(),
                u.isDebeCambiarClave(), u.getIdRolActivo());
    }

    public void actualizar(Usuario u) {
        jdbc.update("UPDATE usuarios SET nombre = ?, id_sede = ?, id_rol_activo = ? WHERE id_usuario = ? AND id_empresa = ?",
                u.getNombre(), u.getIdSede(), u.getIdRolActivo(), u.getIdUsuario(), u.getIdEmpresa());
    }

    public void asignarRoles(int idUsuario, List<Integer> idsRol) {
        jdbc.update("DELETE FROM usuario_roles WHERE id_usuario = ?", idUsuario);
        for (Integer idRol : idsRol) {
            jdbc.update("INSERT INTO usuario_roles (id_usuario, id_rol) VALUES (?, ?)", idUsuario, idRol);
        }
    }

    public void cambiarClave(int idUsuario, String hash, boolean debeCambiar) {
        jdbc.update("UPDATE usuarios SET clave_hash = ?, debe_cambiar_clave = ? WHERE id_usuario = ?", hash, debeCambiar, idUsuario);
    }

    public void forzarCambioClave(int idUsuario) {
        jdbc.update("UPDATE usuarios SET debe_cambiar_clave = TRUE WHERE id_usuario = ?", idUsuario);
    }

    public void cambiarEstado(int idUsuario, String estado) {
        jdbc.update("UPDATE usuarios SET estado = ? WHERE id_usuario = ?", estado, idUsuario);
    }

    public void cambiarRolActivo(int idUsuario, int idRol) {
        jdbc.update("UPDATE usuarios SET id_rol_activo = ? WHERE id_usuario = ?", idRol, idUsuario);
    }

    public void cambiarTema(int idUsuario, String tema) {
        jdbc.update("UPDATE usuarios SET tema = ? WHERE id_usuario = ?", tema, idUsuario);
    }

    public void registrarAcceso(int idUsuario) {
        jdbc.update("UPDATE usuarios SET ultimo_acceso = NOW() WHERE id_usuario = ?", idUsuario);
    }

    /** Ventas, recepciones, kardex y auditoria del usuario. */
    public int contarHistorial(int idUsuario) {
        return contar("SELECT (SELECT COUNT(*) FROM ventas WHERE id_usuario = ?) + (SELECT COUNT(*) FROM recepciones WHERE id_usuario = ?) "
                + "+ (SELECT COUNT(*) FROM kardex WHERE id_usuario = ?) "
                + "+ (SELECT COUNT(*) FROM auditoria WHERE usuario_tipo = 'EMPRESA' AND id_usuario = ?)",
                idUsuario, idUsuario, idUsuario, idUsuario);
    }

    public void eliminar(int idUsuario) {
        jdbc.update("DELETE FROM usuarios WHERE id_usuario = ?", idUsuario);
    }

    public int contarActivos(int idEmpresa) {
        return contar("SELECT COUNT(*) FROM usuarios WHERE id_empresa = ? AND estado = 'ACTIVO'", idEmpresa);
    }
}
