package pe.mibodega.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.UsuarioInterno;

@Repository
public class UsuarioInternoDAO extends BaseDAO {

    private static final String SELECT = "SELECT u.*, r.nombre AS rol_nombre, r.principal AS rol_principal "
            + "FROM usuarios_internos u JOIN roles_internos r ON r.id_rol_interno = u.id_rol_interno ";

    public UsuarioInterno buscarPorLogin(String login) {
        return uno(UsuarioInterno.class, SELECT + "WHERE u.usuario = ?", login);
    }

    public UsuarioInterno buscar(int id) {
        return uno(UsuarioInterno.class, SELECT + "WHERE u.id_usuario_interno = ?", id);
    }

    public List<UsuarioInterno> listar() {
        return lista(UsuarioInterno.class, SELECT + "ORDER BY u.id_usuario_interno");
    }

    public int insertar(UsuarioInterno u) {
        return insertarConId("INSERT INTO usuarios_internos (id_rol_interno, nombre, correo, usuario, clave_hash, debe_cambiar_clave) "
                + "VALUES (?,?,?,?,?,TRUE)",
                u.getIdRolInterno(), u.getNombre(), u.getCorreo(), u.getUsuario(), u.getClaveHash());
    }

    public void actualizar(UsuarioInterno u) {
        jdbc.update("UPDATE usuarios_internos SET nombre = ?, correo = ?, id_rol_interno = ? WHERE id_usuario_interno = ?",
                u.getNombre(), u.getCorreo(), u.getIdRolInterno(), u.getIdUsuarioInterno());
    }

    public void cambiarClave(int id, String hash, boolean debeCambiar) {
        jdbc.update("UPDATE usuarios_internos SET clave_hash = ?, debe_cambiar_clave = ? WHERE id_usuario_interno = ?", hash, debeCambiar, id);
    }

    public void cambiarEstado(int id, String estado) {
        jdbc.update("UPDATE usuarios_internos SET estado = ? WHERE id_usuario_interno = ?", estado, id);
    }

    public void cambiarTema(int id, String tema) {
        jdbc.update("UPDATE usuarios_internos SET tema = ? WHERE id_usuario_interno = ?", tema, id);
    }

    public void registrarAcceso(int id) {
        jdbc.update("UPDATE usuarios_internos SET ultimo_acceso = NOW() WHERE id_usuario_interno = ?", id);
    }
}
