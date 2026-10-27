package pe.mibodega.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cliente de la empresa (identificado por DNI o RUC).
 */
public class Cliente {

    private Integer idCliente;
    private Integer idEmpresa;
    private String tipoDoc;
    private String documento;
    private String nombre;
    private String telefono;
    private String correo;
    private String direccion;
    private LocalDate fechaRegistro;
    private int compras;
    private BigDecimal totalComprado;
    private LocalDateTime ultimaCompra;

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public int getCompras() {
        return compras;
    }

    public void setCompras(int compras) {
        this.compras = compras;
    }

    public BigDecimal getTotalComprado() {
        return totalComprado;
    }

    public void setTotalComprado(BigDecimal totalComprado) {
        this.totalComprado = totalComprado;
    }

    public LocalDateTime getUltimaCompra() {
        return ultimaCompra;
    }

    public void setUltimaCompra(LocalDateTime ultimaCompra) {
        this.ultimaCompra = ultimaCompra;
    }

    public BigDecimal getTicketPromedio() {
        if (compras == 0 || totalComprado == null) {
            return BigDecimal.ZERO;
        }
        return totalComprado.divide(BigDecimal.valueOf(compras), 2, java.math.RoundingMode.HALF_UP);
    }
}
