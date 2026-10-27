package pe.mibodega.model;

/**
 * Diseno de la boleta de una empresa.
 */
public class ConfigComprobante {

    private Integer idEmpresa;
    private String plantilla;
    private String ancho;
    private String encabezado;
    private String pie;
    private String mensaje;
    private String notas;
    private boolean mostrarLogo;
    private boolean mostrarRazonSocial;
    private boolean mostrarDireccion;
    private boolean mostrarCliente;
    private boolean mostrarCajero;
    private boolean mostrarVuelto;

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(String plantilla) {
        this.plantilla = plantilla;
    }

    public String getAncho() {
        return ancho;
    }

    public void setAncho(String ancho) {
        this.ancho = ancho;
    }

    public String getEncabezado() {
        return encabezado;
    }

    public void setEncabezado(String encabezado) {
        this.encabezado = encabezado;
    }

    public String getPie() {
        return pie;
    }

    public void setPie(String pie) {
        this.pie = pie;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public boolean isMostrarLogo() {
        return mostrarLogo;
    }

    public void setMostrarLogo(boolean mostrarLogo) {
        this.mostrarLogo = mostrarLogo;
    }

    public boolean isMostrarRazonSocial() {
        return mostrarRazonSocial;
    }

    public void setMostrarRazonSocial(boolean mostrarRazonSocial) {
        this.mostrarRazonSocial = mostrarRazonSocial;
    }

    public boolean isMostrarDireccion() {
        return mostrarDireccion;
    }

    public void setMostrarDireccion(boolean mostrarDireccion) {
        this.mostrarDireccion = mostrarDireccion;
    }

    public boolean isMostrarCliente() {
        return mostrarCliente;
    }

    public void setMostrarCliente(boolean mostrarCliente) {
        this.mostrarCliente = mostrarCliente;
    }

    public boolean isMostrarCajero() {
        return mostrarCajero;
    }

    public void setMostrarCajero(boolean mostrarCajero) {
        this.mostrarCajero = mostrarCajero;
    }

    public boolean isMostrarVuelto() {
        return mostrarVuelto;
    }

    public void setMostrarVuelto(boolean mostrarVuelto) {
        this.mostrarVuelto = mostrarVuelto;
    }
}
