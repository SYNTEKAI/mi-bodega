package pe.mibodega.model;

/**
 * Rol del equipo interno de la plataforma.
 */
public class RolInterno {

    private Integer idRolInterno;
    private String nombre;
    private boolean principal;
    private boolean activo;
    private int usuarios;

    public Integer getIdRolInterno() {
        return idRolInterno;
    }

    public void setIdRolInterno(Integer idRolInterno) {
        this.idRolInterno = idRolInterno;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
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
}
