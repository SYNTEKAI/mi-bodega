package pe.mibodega.model;

import java.time.LocalDateTime;

/**
 * Trabajador de una empresa.
 */
public class Usuario {

    private Integer idUsuario;
    private Integer idEmpresa;
    private Integer idSede;
    private String usuario;
    private String claveHash;
    private String nombre;
    private String estado;
    private boolean debeCambiarClave;
    private Integer idRolActivo;
    private String tema;
    private LocalDateTime ultimoAcceso;
    private String sedeNombre;
    private String rolActivoNombre;
    private String roles;

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Integer getIdSede() {
        return idSede;
    }

    public void setIdSede(Integer idSede) {
        this.idSede = idSede;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getClaveHash() {
        return claveHash;
    }

    public void setClaveHash(String claveHash) {
        this.claveHash = claveHash;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isDebeCambiarClave() {
        return debeCambiarClave;
    }

    public void setDebeCambiarClave(boolean debeCambiarClave) {
        this.debeCambiarClave = debeCambiarClave;
    }

    public Integer getIdRolActivo() {
        return idRolActivo;
    }

    public void setIdRolActivo(Integer idRolActivo) {
        this.idRolActivo = idRolActivo;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    public String getRolActivoNombre() {
        return rolActivoNombre;
    }

    public void setRolActivoNombre(String rolActivoNombre) {
        this.rolActivoNombre = rolActivoNombre;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public boolean isActivo() {
        return "ACTIVO".equals(estado);
    }
}
