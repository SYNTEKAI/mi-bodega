package pe.mibodega.model;

import java.time.LocalDate;

/**
 * Bodega o minimarket cliente de la plataforma.
 */
public class Empresa {

    private Integer idEmpresa;
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String direccion;
    private String rubro;
    private String telefono;
    private String correo;
    private String plan;
    private String estado;
    private String logoTipo;
    private boolean tieneLogo;
    private LocalDate fechaRegistro;
    private int sedes;
    private int usuarios;

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getLogoTipo() {
        return logoTipo;
    }

    public void setLogoTipo(String logoTipo) {
        this.logoTipo = logoTipo;
    }

    public boolean isTieneLogo() {
        return tieneLogo;
    }

    public void setTieneLogo(boolean tieneLogo) {
        this.tieneLogo = tieneLogo;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getSedes() {
        return sedes;
    }

    public void setSedes(int sedes) {
        this.sedes = sedes;
    }

    public int getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(int usuarios) {
        this.usuarios = usuarios;
    }

    public String getNombreMostrar() {
        return nombreComercial != null && !nombreComercial.isBlank() ? nombreComercial : razonSocial;
    }

    // Solo las empresas ACTIVA o EN PRUEBA pueden operar
    public boolean isOperable() {
        return "ACTIVA".equals(estado) || "EN PRUEBA".equals(estado);
    }
}
