package pe.mibodega.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pe.mibodega.dao.RolDAO;
import pe.mibodega.dao.UsuarioDAO;
import pe.mibodega.dao.UsuarioInternoDAO;
import pe.mibodega.model.RolInterno;
import pe.mibodega.model.UsuarioInterno;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.ClaveUtil;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.Validar;

/**
 * Usuarios del equipo interno de la plataforma (Soporte, Auditor, ...).
 */
@Service
public class UsuarioInternoService {

    @Autowired
    private UsuarioInternoDAO usuarioInternoDAO;
    @Autowired
    private UsuarioDAO usuarioDAO;
    @Autowired
    private RolDAO rolDAO;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private AuditoriaService auditoria;

    public List<UsuarioInterno> listar() {
        return usuarioInternoDAO.listar();
    }

    public UsuarioInterno buscar(int id) {
        UsuarioInterno u = usuarioInternoDAO.buscar(id);
        if (u == null) {
            throw new NegocioException("El usuario no existe.");
        }
        return u;
    }

    public void guardar(UsuarioSesion s, UsuarioInterno u, String clave) {
        s.exigir("usuarios_sistema", u.getIdUsuarioInterno() == null ? "crear" : "editar");
        Validar.requerido(u.getNombre(), "nombre");
        Validar.requerido(u.getCorreo(), "correo");
        RolInterno rol = u.getIdRolInterno() == null ? null : rolDAO.buscarInterno(u.getIdRolInterno());
        if (rol == null) {
            throw new NegocioException("Elige un rol interno.");
        }
        if (rol.isPrincipal() && !s.isRolPrincipal()) {
            throw new NegocioException("Solo el Super Admin puede asignar el rol Super Admin.");
        }
        if (u.getIdUsuarioInterno() == null) {
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
            usuarioInternoDAO.insertar(u);
            auditoria.registrar(s, "Usuarios internos", "CREAR USUARIO INTERNO", "Creó al usuario interno " + u.getNombre()
                    + " (" + rol.getNombre() + ")");
        } else {
            UsuarioInterno actual = buscar(u.getIdUsuarioInterno());
            if (actual.isRolPrincipal() && !rol.isPrincipal()) {
                throw new NegocioException("No se puede quitar el rol al Super Admin principal.");
            }
            usuarioInternoDAO.actualizar(u);
            auditoria.registrar(s, "Usuarios internos", "EDITAR USUARIO INTERNO", "Editó a " + u.getNombre());
        }
    }

    public void cambiarEstado(UsuarioSesion s, int id) {
        s.exigir("usuarios_sistema", "editar");
        UsuarioInterno u = buscar(id);
        if (u.isRolPrincipal()) {
            throw new NegocioException("No se puede desactivar al Super Admin.");
        }
        String nuevo = u.isActivo() ? "INACTIVO" : "ACTIVO";
        usuarioInternoDAO.cambiarEstado(id, nuevo);
        auditoria.registrar(s, "Usuarios internos", u.isActivo() ? "DESACTIVAR" : "REACTIVAR", "Usuario interno " + u.getNombre() + " pasó a " + nuevo);
    }

    public String restablecerClave(UsuarioSesion s, int id) {
        s.exigir("usuarios_sistema", "restablecer_password");
        UsuarioInterno u = buscar(id);
        if (u.isRolPrincipal() && !s.isRolPrincipal()) {
            throw new NegocioException("No puedes restablecer la contraseña del Super Admin.");
        }
        String temporal = ClaveUtil.generarTemporal();
        usuarioInternoDAO.cambiarClave(id, encoder.encode(temporal), true);
        auditoria.registrar(s, "Usuarios internos", "RESTABLECIÓ CONTRASEÑA", "Restableció la contraseña de " + u.getNombre());
        return temporal;
    }
}
