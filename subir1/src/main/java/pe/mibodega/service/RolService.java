package pe.mibodega.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.mibodega.dao.PermisoDAO;
import pe.mibodega.dao.RolDAO;
import pe.mibodega.model.Permiso;
import pe.mibodega.model.Rol;
import pe.mibodega.model.RolInterno;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.Validar;

/**
 * Roles de la empresa y su matriz de permisos (modulo.accion).
 */
@Service
public class RolService {

    /** Nombre legible de cada modulo para la matriz de permisos. */
    public static final Map<String, String> NOMBRES_MODULO = Map.ofEntries(
            Map.entry("dashboard", "Dashboard"), Map.entry("ventas", "Ventas (POS)"), Map.entry("clientes", "Clientes"),
            Map.entry("productos", "Productos"), Map.entry("inventario", "Inventario / Lotes"), Map.entry("proveedores", "Proveedores"),
            Map.entry("recepcion", "Recepción / Guías"), Map.entry("kardex", "Kardex"), Map.entry("reportes", "Reportes"),
            Map.entry("sedes", "Sedes"), Map.entry("usuarios", "Usuarios"), Map.entry("roles", "Roles y permisos"),
            Map.entry("comprobantes", "Comprobantes"), Map.entry("auditoria", "Auditoría"),
            Map.entry("dashboard_global", "Dashboard global"), Map.entry("empresas", "Empresas"),
            Map.entry("usuarios_sistema", "Usuarios del sistema"), Map.entry("roles_sistema", "Roles internos"),
            Map.entry("auditoria_global", "Auditoría global"));

    @Autowired
    private RolDAO rolDAO;
    @Autowired
    private PermisoDAO permisoDAO;
    @Autowired
    private AuditoriaService auditoria;

    public List<Rol> listar(UsuarioSesion s) {
        return rolDAO.listar(s.getIdEmpresa());
    }

    public List<Rol> activos(UsuarioSesion s) {
        return rolDAO.listarActivos(s.getIdEmpresa());
    }

    public Rol buscar(UsuarioSesion s, int id) {
        Rol r = rolDAO.buscar(id, s.getIdEmpresa());
        if (r == null) {
            throw new NegocioException("El rol no existe.");
        }
        return r;
    }

    public Set<String> permisosDe(int idRol) {
        return permisoDAO.permisosDeRol(idRol);
    }

    /** Permisos agrupados por modulo para dibujar la matriz. */
    public Map<String, List<Permiso>> permisosPorModulo(String ambito) {
        Map<String, List<Permiso>> mapa = new LinkedHashMap<>();
        for (Permiso p : permisoDAO.listarPorAmbito(ambito)) {
            mapa.computeIfAbsent(p.getModulo(), k -> new java.util.ArrayList<>()).add(p);
        }
        return mapa;
    }

    private void exigirAdmin(UsuarioSesion s) {
        s.exigir("roles", "administrar");
        if (!s.isAdminEmpresa()) {
            throw new NegocioException("Solo el Administrador de la empresa puede administrar roles.");
        }
    }

    @Transactional
    public void guardar(UsuarioSesion s, Integer id, String nombre, List<Integer> permisos) {
        exigirAdmin(s);
        Validar.requerido(nombre, "nombre");
        nombre = nombre.trim();
        if (rolDAO.existeNombre(s.getIdEmpresa(), nombre, id)) {
            throw new NegocioException("Ya existe un rol con ese nombre.");
        }
        List<Integer> lista = permisos == null ? List.of() : permisos;
        if (id == null) {
            int nuevo = rolDAO.insertar(s.getIdEmpresa(), nombre);
            permisoDAO.reemplazarPermisosDeRol(nuevo, lista);
            auditoria.registrar(s, "Roles", "CREAR ROL", "Creó el rol " + nombre + " con " + lista.size() + " permisos");
        } else {
            Rol r = buscar(s, id);
            if (r.isProtegido()) {
                throw new NegocioException("El rol Administrador no se puede modificar.");
            }
            rolDAO.cambiarNombre(id, nombre);
            permisoDAO.reemplazarPermisosDeRol(id, lista);
            auditoria.registrar(s, "Roles", "CAMBIAR PERMISOS", "Editó el rol " + nombre + " (" + lista.size() + " permisos)");
        }
    }

    @Transactional
    public void duplicar(UsuarioSesion s, int id) {
        exigirAdmin(s);
        Rol r = buscar(s, id);
        String nombre = r.getNombre() + " (copia)";
        int n = 2;
        while (rolDAO.existeNombre(s.getIdEmpresa(), nombre, null)) {
            nombre = r.getNombre() + " (copia " + n++ + ")";
        }
        int nuevo = rolDAO.insertar(s.getIdEmpresa(), nombre);
        permisoDAO.copiarPermisos(id, nuevo);
        auditoria.registrar(s, "Roles", "CREAR ROL", "Duplicó el rol " + r.getNombre() + " como " + nombre);
    }

    public void cambiarActivo(UsuarioSesion s, int id) {
        exigirAdmin(s);
        Rol r = buscar(s, id);
        if (r.isProtegido()) {
            throw new NegocioException("El rol Administrador no se puede desactivar.");
        }
        rolDAO.cambiarActivo(id, !r.isActivo());
        auditoria.registrar(s, "Roles", r.isActivo() ? "DESACTIVAR" : "REACTIVAR",
                (r.isActivo() ? "Desactivó" : "Activó") + " el rol " + r.getNombre());
    }

    public void eliminar(UsuarioSesion s, int id) {
        exigirAdmin(s);
        Rol r = buscar(s, id);
        if (r.isProtegido()) {
            throw new NegocioException("El rol Administrador no se puede eliminar.");
        }
        if (r.getUsuarios() > 0) {
            throw new NegocioException("El rol " + r.getNombre() + " tiene " + r.getUsuarios()
                    + " usuario(s) asignados. Reasígnalos o desactiva el rol.");
        }
        rolDAO.eliminar(id);
        auditoria.registrar(s, "Roles", "ELIMINAR ROL", "Eliminó el rol " + r.getNombre());
    }

    // Roles internos (solo lectura)
    public List<RolInterno> rolesInternos() {
        return rolDAO.listarInternos();
    }

    public Set<String> permisosDeRolInterno(int idRolInterno) {
        return permisoDAO.permisosDeRolInterno(idRolInterno);
    }

    public RolInterno buscarInterno(int id) {
        RolInterno r = rolDAO.buscarInterno(id);
        if (r == null) {
            throw new NegocioException("El rol no existe.");
        }
        return r;
    }

    /** Solo el Super Admin administra los roles internos. */
    private void exigirSuperAdmin(UsuarioSesion s) {
        if (!s.isInterno() || !s.isRolPrincipal()) {
            throw new NegocioException("Solo el Super Admin puede administrar los roles internos.");
        }
    }

    @Transactional
    public void guardarInterno(UsuarioSesion s, Integer id, String nombre, List<Integer> permisos) {
        exigirSuperAdmin(s);
        Validar.requerido(nombre, "nombre");
        nombre = nombre.trim();
        if (rolDAO.existeNombreInterno(nombre, id)) {
            throw new NegocioException("Ya existe un rol interno con ese nombre.");
        }
        List<Integer> lista = permisos == null ? List.of() : permisos;
        if (id == null) {
            int nuevo = rolDAO.insertarInterno(nombre);
            permisoDAO.reemplazarPermisosDeRolInterno(nuevo, lista);
            auditoria.registrar(s, "Roles internos", "CREAR ROL", "Creó el rol interno " + nombre + " con " + lista.size() + " permisos");
        } else {
            RolInterno r = buscarInterno(id);
            if (r.isPrincipal()) {
                throw new NegocioException("El rol Super Admin no se puede modificar.");
            }
            rolDAO.cambiarNombreInterno(id, nombre);
            permisoDAO.reemplazarPermisosDeRolInterno(id, lista);
            auditoria.registrar(s, "Roles internos", "CAMBIAR PERMISOS", "Editó el rol interno " + nombre + " (" + lista.size() + " permisos)");
        }
    }

    public void eliminarInterno(UsuarioSesion s, int id) {
        exigirSuperAdmin(s);
        RolInterno r = buscarInterno(id);
        if (r.isPrincipal()) {
            throw new NegocioException("El rol Super Admin no se puede eliminar.");
        }
        int usuarios = rolDAO.contarUsuariosInternos(id);
        if (usuarios > 0) {
            throw new NegocioException("El rol " + r.getNombre() + " tiene " + usuarios + " usuario(s) asignados. Cámbialos de rol antes de eliminarlo.");
        }
        rolDAO.eliminarInterno(id);
        auditoria.registrar(s, "Roles internos", "ELIMINAR ROL", "Eliminó el rol interno " + r.getNombre());
    }
}
