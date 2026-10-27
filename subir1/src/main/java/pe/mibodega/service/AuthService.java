package pe.mibodega.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pe.mibodega.dao.EmpresaDAO;
import pe.mibodega.dao.PermisoDAO;
import pe.mibodega.dao.RolDAO;
import pe.mibodega.dao.SedeDAO;
import pe.mibodega.dao.UsuarioDAO;
import pe.mibodega.dao.UsuarioInternoDAO;
import pe.mibodega.model.Empresa;
import pe.mibodega.model.Rol;
import pe.mibodega.model.Sede;
import pe.mibodega.model.Usuario;
import pe.mibodega.model.UsuarioInterno;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.ClaveUtil;
import pe.mibodega.util.NegocioException;

/**
 * Inicio de sesion, roles activos y contrasenas.
 */
@Service
public class AuthService {

    @Autowired
    private UsuarioDAO usuarioDAO;
    @Autowired
    private UsuarioInternoDAO usuarioInternoDAO;
    @Autowired
    private EmpresaDAO empresaDAO;
    @Autowired
    private RolDAO rolDAO;
    @Autowired
    private PermisoDAO permisoDAO;
    @Autowired
    private SedeDAO sedeDAO;
    @Autowired
    private BCryptPasswordEncoder encoder;
    @Autowired
    private AuditoriaService auditoria;

    /**
     * Valida usuario y contrasena (hash BCrypt) y arma la sesion con los permisos del rol activo.
     * Primero busca en usuarios internos y luego en usuarios de empresa.
     */
    
    
    public UsuarioSesion login(String login, String clave) {
        /* AVANCE 3: ingreso de la plataforma (Super Admin) desactivado. Se activa en el proyecto final.
        UsuarioInterno interno = usuarioInternoDAO.buscarPorLogin(login);
        if (interno != null && encoder.matches(clave, interno.getClaveHash())) {
            if (!interno.isActivo()) {
                throw new NegocioException("El usuario está inactivo.");
            }
            UsuarioSesion s = new UsuarioSesion();
            s.setTipo(UsuarioSesion.INTERNO);
            s.setIdUsuario(interno.getIdUsuarioInterno());
            s.setNombre(interno.getNombre());
            s.setUsuario(interno.getUsuario());
            s.setIdRol(interno.getIdRolInterno());
            s.setRolNombre(interno.getRolNombre());
            s.setRolPrincipal(interno.isRolPrincipal());
            s.setPermisos(permisoDAO.permisosDeRolInterno(interno.getIdRolInterno()));
            s.setDebeCambiarClave(interno.isDebeCambiarClave());
            s.setTema(interno.getTema());
            usuarioInternoDAO.registrarAcceso(interno.getIdUsuarioInterno());
            auditoria.registrar(s, "Sistema", "LOGIN", "Inicio de sesión correcto");
            return s;
        }
        */
        Usuario u = usuarioDAO.buscarPorLogin(login);
        if (u == null || !encoder.matches(clave, u.getClaveHash())) {
            throw new NegocioException("Usuario o contraseña incorrectos.");
        }
        if (!u.isActivo()) {
            throw new NegocioException("El usuario está inactivo. Contacta al administrador.");
        }
        Empresa e = empresaDAO.buscar(u.getIdEmpresa());
        if (!e.isOperable()) {
            throw new NegocioException("La empresa está " + e.getEstado().toLowerCase() + ".");
        }
        Rol rol = rolDAO.buscar(u.getIdRolActivo(), u.getIdEmpresa());
        if (rol == null || !rol.isActivo()) {
            throw new NegocioException("Tu rol está desactivado. Contacta al administrador.");
        }

        UsuarioSesion s = new UsuarioSesion();
        s.setTipo(UsuarioSesion.EMPRESA);
        s.setIdUsuario(u.getIdUsuario());
        s.setNombre(u.getNombre());
        s.setUsuario(u.getUsuario());
        s.setDebeCambiarClave(u.isDebeCambiarClave());
        s.setTema(u.getTema());
        s.setIdEmpresa(e.getIdEmpresa());
        s.setEmpresaNombre(e.getRazonSocial());
        s.setEmpresaRuc(e.getRuc());
        s.setEmpresaTieneLogo(e.isTieneLogo());
        s.setIdSedeUsuario(u.getIdSede());
        aplicarRol(s, rol);
        usuarioDAO.registrarAcceso(u.getIdUsuario());
        auditoria.registrar(s, "Sistema", "LOGIN", "Inicio de sesión correcto");
        return s;
    }
    

    private void aplicarRol(UsuarioSesion s, Rol rol) {
        s.setIdRol(rol.getIdRol());
        s.setRolNombre(rol.getNombre());
        s.setPermisos(permisoDAO.permisosDeRol(rol.getIdRol()));
        s.setRolesAsignados(rolDAO.rolesDeUsuario(s.getIdUsuario()));
    }

    /** El usuario cambia su rol activo por otro de sus roles asignados. */
    public void cambiarRolActivo(UsuarioSesion s, int idRol) {
        List<Rol> roles = rolDAO.rolesDeUsuario(s.getIdUsuario());
        Rol nuevo = roles.stream().filter(r -> r.getIdRol() == idRol && r.isActivo()).findFirst().orElse(null);
        if (nuevo == null) {
            throw new NegocioException("Ese rol no está asignado a tu usuario.");
        }
        String anterior = s.getRolNombre();
        usuarioDAO.cambiarRolActivo(s.getIdUsuario(), idRol);
        aplicarRol(s, nuevo);
        auditoria.registrar(s, "Perfil", "CAMBIÓ ROL ACTIVO", "Cambió su rol activo de " + anterior + " a " + nuevo.getNombre());
    }

    /** Cambio de contrasena. Si actual es null es el cambio obligatorio del primer ingreso. */
    public void cambiarClave(UsuarioSesion s, String actual, String nueva, String confirmacion) {
        String hashActual = s.isInterno()
                ? usuarioInternoDAO.buscar(s.getIdUsuario()).getClaveHash()
                : usuarioDAO.buscarPorId(s.getIdUsuario()).getClaveHash();
        if (actual != null && !encoder.matches(actual, hashActual)) {
            throw new NegocioException("La contraseña actual no es correcta.");
        }
        if (encoder.matches(nueva, hashActual)) {
            throw new NegocioException("La nueva contraseña debe ser distinta de la actual.");
        }
        String error = ClaveUtil.validar(nueva, confirmacion);
        if (error != null) {
            throw new NegocioException(error);
        }
        if (s.isInterno()) {
            usuarioInternoDAO.cambiarClave(s.getIdUsuario(), encoder.encode(nueva), false);
        } else {
            usuarioDAO.cambiarClave(s.getIdUsuario(), encoder.encode(nueva), false);
        }
        s.setDebeCambiarClave(false);
        auditoria.registrar(s, "Perfil", "CAMBIÓ CONTRASEÑA", actual == null
                ? "Cambió la contraseña temporal en su primer ingreso" : "Cambió su contraseña desde el perfil");
    }

    public void cambiarTema(UsuarioSesion s, String tema) {
        String valor = "oscuro".equals(tema) ? "oscuro" : "claro";
        if (s.isInterno()) {
            usuarioInternoDAO.cambiarTema(s.getIdUsuario(), valor);
        } else {
            usuarioDAO.cambiarTema(s.getIdUsuario(), valor);
        }
        s.setTema(valor);
    }

    /** Un usuario interno entra a administrar una empresa. */
    public void entrarEmpresa(UsuarioSesion s, int idEmpresa) {
        s.exigir("empresas", "entrar");
        Empresa e = empresaDAO.buscar(idEmpresa);
        if (e == null || !e.isOperable()) {
            throw new NegocioException("La empresa no está activa.");
        }
        List<Sede> sedes = sedeDAO.listarActivas(idEmpresa);
        s.setIdEmpresa(e.getIdEmpresa());
        s.setEmpresaNombre(e.getRazonSocial());
        s.setEmpresaRuc(e.getRuc());
        s.setEmpresaTieneLogo(e.isTieneLogo());
        s.setIdSedeUsuario(sedes.isEmpty() ? null : sedes.get(0).getIdSede());
        s.setIdSedeVista(null);
        auditoria.registrar(s, "Empresas", "ENTRÓ A EMPRESA", "Ingresó a administrar la empresa " + e.getRazonSocial());
    }

    public void cambiarSedeVista(UsuarioSesion s, int idSede) {
        if (!s.isAdminEmpresa() && !s.isInterno()) {
            return;
        }
        if (sedeDAO.buscar(idSede, s.getIdEmpresa()) != null) {
            s.setIdSedeVista(idSede);
        }
    }
}
