package pe.mibodega.model;

import java.time.LocalDateTime;

/**
 * Historial de correcciones de una recepcion.
 */
public class RecepcionModificacion {

    private Integer idModificacion;
    private Integer idRecepcion;
    private LocalDateTime fechaHora;
    private String usuarioNombre;
    private String rolNombre;
    private String motivo;
    private String cambios;

    public Integer getIdModificacion() {
        return idModificacion;
    }

    public void setIdModificacion(Integer idModificacion) {
        this.idModificacion = idModificacion;
    }

    public Integer getIdRecepcion() {
        return idRecepcion;
    }

    public void setIdRecepcion(Integer idRecepcion) {
        this.idRecepcion = idRecepcion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getCambios() {
        return cambios;
    }

    public void setCambios(String cambios) {
        this.cambios = cambios;
    }
}
