package pe.mibodega.service;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.mibodega.dao.RolDAO;
import pe.mibodega.dao.SedeDAO;
import pe.mibodega.dao.UsuarioDAO;
import pe.mibodega.model.Rol;
import pe.mibodega.model.Usuario;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.ClaveUtil;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.Validar;

/**
 * Usuarios de la empresa: datos, sede, roles asignados y contrasenas.
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioDAO usuarioDAO;
    @Autowired
    private RolDAO rolDAO;
    @Autowired
    private SedeDAO sedeDAO;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private AuditoriaService auditoria;

    public List<Usuario> listar(UsuarioSesion s) {
        return usuarioDAO.listar(s.getIdEmpresa());
    }

    public Usuario buscar(UsuarioSesion s, int id) {
        Usuario u = usuarioDAO.buscar(id, s.getIdEmpresa());
        if (u == null) {
            throw new NegocioException("El usuario no existe.");
        }
        return u;
    }

    public List<Rol> rolesDe(int idUsuario) {
        return rolDAO.rolesDeUsuario(idUsuario);
    }

    @Transactional
    public void guardar(UsuarioSesion s, Usuario u, List<Integer> roles, String clave, boolean forzarCambio) {
        s.exigir("usuarios", u.getIdUsuario() == null ? "crear" : "editar");
        Validar.requerido(u.getNombre(), "nombre");
        if (u.getIdSede() == null || sedeDAO.buscar(u.getIdSede(), s.getIdEmpresa()) == null) {
            throw new NegocioException("Elige una sede.");
        }
        List<Integer> asignados = new ArrayList<>(roles == null ? List.of() : roles);
        if (u.getIdRolActivo() == null || rolDAO.buscar(u.getIdRolActivo(), s.getIdEmpresa()) == null) {
            throw new NegocioException("Elige el rol activo.");
        }
        if (!asignados.contains(u.getIdRolActivo())) {
            asignados.add(u.getIdRolActivo());
        }
        for (Integer idRol : asignados) {
            if (rolDAO.buscar(idRol, s.getIdEmpresa()) == null) {
                throw new NegocioException("Rol no válido.");
            }
        }
        u.setIdEmpresa(s.getIdEmpresa());
        u.setNombre(u.getNombre().trim());

        if (u.getIdUsuario() == null) {
            u.setUsuario(StringUtils.trimToEmpty(u.getUsuario()).toLowerCase());
            Validar.requerido(u.getUsuario(), "usuario");
            if (usuarioDAO.existeLogin(u.getUsuario())) {
                throw new NegocioException("El usuario " + u.getUsuario() + " ya existe.");
            }
            String error = ClaveUtil.validar(clave, clave);
            if (error != null) {
                throw new NegocioException(error);
            }
            u.setClaveHash(encoder.encode(clave));
            u.setDebeCambiarClave(forzarCambio);
            int id = usuarioDAO.insertar(u);
            usuarioDAO.asignarRoles(id, asignados);
            auditoria.registrar(s, "Usuarios", "CREAR USUARIO", "Creó al usuario " + u.getNombre() + " (" + u.getUsuario() + ")");
        } else {
            buscar(s, u.getIdUsuario());
            usuarioDAO.actualizar(u);
            usuarioDAO.asignarRoles(u.getIdUsuario(), asignados);
            auditoria.registrar(s, "Usuarios", "MODIFICAR USUARIO", "Editó al usuario " + u.getNombre());
        }
    }

    /** Genera una clave temporal y la devuelve para mostrarla una sola vez. */
    public String restablecerClave(UsuarioSesion s, int id, boolean forzarCambio) {
        s.exigir("usuarios", "restablecer_password");
        Usuario u = buscar(s, id);
        String temporal = ClaveUtil.generarTemporal();
        usuarioDAO.cambiarClave(id, encoder.encode(temporal), forzarCambio);
        auditoria.registrar(s, "Usuarios", "RESTABLECIÓ CONTRASEÑA", "Restableció la contraseña de " + u.getNombre());
        return temporal;
    }

    public void forzarCambio(UsuarioSesion s, int id) {
        s.exigir("usuarios", "restablecer_password");
        Usuario u = buscar(s, id);
        usuarioDAO.forzarCambioClave(id);
        auditoria.registrar(s, "Usuarios", "FORZÓ CAMBIO DE CONTRASEÑA", "Obligó a " + u.getNombre() + " a cambiar su contraseña");
    }

    public void cambiarEstado(UsuarioSesion s, int id) {
        s.exigir("usuarios", "editar");
        Usuario u = buscar(s, id);
        if (!s.isInterno() && s.getIdUsuario() == id) {
            throw new NegocioException("No puedes desactivar tu propio usuario.");
        }
        String nuevo = u.isActivo() ? "INACTIVO" : "ACTIVO";
        usuarioDAO.cambiarEstado(id, nuevo);
        auditoria.registrar(s, "Usuarios", u.isActivo() ? "DESACTIVAR" : "REACTIVAR", "Usuario " + u.getNombre() + " pasó a " + nuevo);
    }

    /** Si tiene ventas o auditoria no se borra: se desactiva. */
    public String eliminar(UsuarioSesion s, int id) {
        s.exigir("usuarios", "eliminar");
        Usuario u = buscar(s, id);
        if (!s.isInterno() && s.getIdUsuario() == id) {
            throw new NegocioException("No puedes eliminar tu propio usuario.");
        }
        int historial = usuarioDAO.contarHistorial(id);
        if (historial > 0) {
            usuarioDAO.cambiarEstado(id, "INACTIVO");
            auditoria.registrar(s, "Usuarios", "DESACTIVAR", "Desactivó a " + u.getNombre() + " (eliminación bloqueada: " + historial + " registros)");
            return u.getNombre() + " tiene " + historial + " registros en el historial: no se eliminó, se desactivó.";
        }
        usuarioDAO.eliminar(id);
        auditoria.registrar(s, "Usuarios", "ELIMINAR", "Eliminó al usuario " + u.getNombre());
        return "Usuario eliminado.";
    }
}
