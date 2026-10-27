package pe.mibodega.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Datos del usuario que inicio sesion. Se guarda en la sesion HTTP.
 * Puede ser un usuario de empresa o un usuario interno de la plataforma.
 */
public class UsuarioSesion implements Serializable {

    public static final String EMPRESA = "EMPRESA";
    public static final String INTERNO = "INTERNO";

    // Modulos que pertenecen a la plataforma (no a una empresa)
    private static final Set<String> MODULOS_PLATAFORMA = Set.of(
            "dashboard_global", "empresas", "usuarios_sistema", "roles_sistema", "auditoria_global");

    private String tipo;
    private Integer idUsuario;
    private String nombre;
    private String usuario;
    private Integer idRol;
    private String rolNombre;
    private boolean rolPrincipal;
    private Set<String> permisos = new HashSet<>();
    private List<Rol> rolesAsignados = new ArrayList<>();
    private boolean debeCambiarClave;
    private String tema = "claro";

    // Empresa en la que se trabaja (null = panel de la plataforma)
    private Integer idEmpresa;
    private String empresaNombre;
    private String empresaRuc;
    private boolean empresaTieneLogo;
    private Integer idSedeUsuario;
    private Integer idSedeVista;

    public boolean isInterno() {
        return INTERNO.equals(tipo);
    }

    public boolean isEnEmpresa() {
        return idEmpresa != null;
    }

    /**
     * Revisa si el rol activo tiene el permiso modulo.accion.
     * Un usuario interno dentro de una empresa solo puede ver (salvo el Super Admin).
     */
    public boolean puede(String modulo, String accion) {
        if (isInterno()) {
            if (rolPrincipal) {
                return true;
            }
            if (MODULOS_PLATAFORMA.contains(modulo)) {
                return permisos.contains(modulo + "." + accion);
            }
            return "ver".equals(accion);
        }
        return permisos.contains(modulo + "." + accion);
    }

    /** Igual que puede(), pero lanza un error si no tiene el permiso. */
    public void exigir(String modulo, String accion) {
        if (!puede(modulo, accion)) {
            throw new pe.mibodega.util.SinPermisoException(modulo + "." + accion);
        }
    }

    public boolean isAdminEmpresa() {
        if (isInterno()) {
            return rolPrincipal;
        }
        return "Administrador".equals(rolNombre);
    }

    /** Sede con la que se trabaja: el admin puede elegir, los demas usan su sede asignada. */
    public Integer getSedeActual() {
        if (!isInterno() && !isAdminEmpresa()) {
            return idSedeUsuario;
        }
        return idSedeVista != null ? idSedeVista : idSedeUsuario;
    }

    /** Id para las columnas id_usuario (null si es interno). */
    public Integer getIdUsuarioEmpresa() {
        return isInterno() ? null : idUsuario;
    }

    /** Id para las columnas id_usuario_interno (null si es de empresa). */
    public Integer getIdUsuarioInterno() {
        return isInterno() ? idUsuario : null;
    }

    public void salirDeEmpresa() {
        idEmpresa = null;
        empresaNombre = null;
        empresaRuc = null;
        empresaTieneLogo = false;
        idSedeUsuario = null;
        idSedeVista = null;
    }

    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] partes = nombre.trim().split("\\s+");
        String ini = partes[0].substring(0, 1);
        if (partes.length > 1) {
            ini += partes[1].substring(0, 1);
        }
        return ini.toUpperCase();
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public boolean isRolPrincipal() {
        return rolPrincipal;
    }

    public void setRolPrincipal(boolean rolPrincipal) {
        this.rolPrincipal = rolPrincipal;
    }

    public Set<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<String> permisos) {
        this.permisos = permisos;
    }

    public List<Rol> getRolesAsignados() {
        return rolesAsignados;
    }

    public void setRolesAsignados(List<Rol> rolesAsignados) {
        this.rolesAsignados = rolesAsignados;
    }

    public boolean isDebeCambiarClave() {
        return debeCambiarClave;
    }

    public void setDebeCambiarClave(boolean debeCambiarClave) {
        this.debeCambiarClave = debeCambiarClave;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getEmpresaNombre() {
        return empresaNombre;
    }

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }

    public String getEmpresaRuc() {
        return empresaRuc;
    }

    public void setEmpresaRuc(String empresaRuc) {
        this.empresaRuc = empresaRuc;
    }

    public boolean isEmpresaTieneLogo() {
        return empresaTieneLogo;
    }

    public void setEmpresaTieneLogo(boolean empresaTieneLogo) {
        this.empresaTieneLogo = empresaTieneLogo;
    }

    public Integer getIdSedeUsuario() {
        return idSedeUsuario;
    }

    public void setIdSedeUsuario(Integer idSedeUsuario) {
        this.idSedeUsuario = idSedeUsuario;
    }

    public Integer getIdSedeVista() {
        return idSedeVista;
    }

    public void setIdSedeVista(Integer idSedeVista) {
        this.idSedeVista = idSedeVista;
    }
}
