package pe.mibodega.model;

/**
 * Rol de una empresa.
 */
public class Rol {

    private Integer idRol;
    private Integer idEmpresa;
    private String nombre;
    private boolean activo;
    private int usuarios;
    private int totalPermisos;

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public int getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(int usuarios) {
        this.usuarios = usuarios;
    }

    public int getTotalPermisos() {
        return totalPermisos;
    }

    public void setTotalPermisos(int totalPermisos) {
        this.totalPermisos = totalPermisos;
    }

    // El rol Administrador no se edita ni se elimina
    public boolean isProtegido() {
        return "Administrador".equals(nombre);
    }
}
