package pe.mibodega.model;

import java.time.LocalDateTime;

/**
 * Usuario del equipo de la plataforma (no pertenece a una empresa).
 */
public class UsuarioInterno {

    private Integer idUsuarioInterno;
    private Integer idRolInterno;
    private String nombre;
    private String correo;
    private String usuario;
    private String claveHash;
    private String estado;
    private boolean debeCambiarClave;
    private String tema;
    private LocalDateTime ultimoAcceso;
    private String rolNombre;
    private boolean rolPrincipal;

    public Integer getIdUsuarioInterno() {
        return idUsuarioInterno;
    }

    public void setIdUsuarioInterno(Integer idUsuarioInterno) {
        this.idUsuarioInterno = idUsuarioInterno;
    }

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

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
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

    public boolean isActivo() {
        return "ACTIVO".equals(estado);
    }
}
