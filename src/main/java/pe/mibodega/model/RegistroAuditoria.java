package pe.mibodega.model;

import java.time.LocalDateTime;

/**
 * Registro de auditoria (no se puede modificar).
 */
public class RegistroAuditoria {

    private Long idAuditoria;
    private Integer idEmpresa;
    private Integer idSede;
    private String usuarioTipo;
    private Integer idUsuario;
    private String usuarioNombre;
    private String rolNombre;
    private String modulo;
    private String accion;
    private String descripcion;
    private String ip;
    private LocalDateTime fechaHora;
    private String empresaNombre;
    private String sedeNombre;

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
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

    public String getUsuarioTipo() {
        return usuarioTipo;
    }

    public void setUsuarioTipo(String usuarioTipo) {
        this.usuarioTipo = usuarioTipo;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEmpresaNombre() {
        return empresaNombre;
    }

    public void setEmpresaNombre(String empresaNombre) {
        this.empresaNombre = empresaNombre;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    // Color del badge segun el tipo de accion
    public String getColor() {
        if (accion == null) {
            return "gris";
        }
        if (accion.startsWith("ELIMIN") || accion.startsWith("DESACTIV") || accion.startsWith("ANUL")) {
            return "roja";
        }
        if (accion.startsWith("EDIT") || accion.startsWith("CAMBI") || accion.startsWith("MODIFIC")
                || accion.startsWith("RESTABLEC") || accion.startsWith("FORZ") || accion.startsWith("CORRIG")) {
            return "ambar";
        }
        if (accion.startsWith("LOGIN") || accion.startsWith("LOGOUT") || accion.startsWith("EXPORT")) {
            return "gris";
        }
        return "verde";
    }
}
