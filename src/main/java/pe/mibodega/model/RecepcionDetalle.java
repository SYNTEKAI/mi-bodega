package pe.mibodega.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Linea de una recepcion: empaques x unidades por empaque = total en unidades.
 */
public class RecepcionDetalle {

    private Integer idDetalle;
    private Integer idRecepcion;
    private Integer idProducto;
    private String empaque;
    private int contenido;
    private int cantidadEmpaques;
    private int totalUnidades;
    private BigDecimal costoUnitario;
    private LocalDate fechaVencimiento;
    private Integer idLote;
    private String productoNombre;
    private String marca;
    private String presentacion;
    private String unidad;
    private int consumido;

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public void setIdDetalle(Integer idDetalle) {
        this.idDetalle = idDetalle;
    }

    public Integer getIdRecepcion() {
        return idRecepcion;
    }

    public void setIdRecepcion(Integer idRecepcion) {
        this.idRecepcion = idRecepcion;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public String getEmpaque() {
        return empaque;
    }

    public void setEmpaque(String empaque) {
        this.empaque = empaque;
    }

    public int getContenido() {
        return contenido;
    }

    public void setContenido(int contenido) {
        this.contenido = contenido;
    }

    public int getCantidadEmpaques() {
        return cantidadEmpaques;
    }

    public void setCantidadEmpaques(int cantidadEmpaques) {
        this.cantidadEmpaques = cantidadEmpaques;
    }

    public int getTotalUnidades() {
        return totalUnidades;
    }

    public void setTotalUnidades(int totalUnidades) {
        this.totalUnidades = totalUnidades;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public int getConsumido() {
        return consumido;
    }

    public void setConsumido(int consumido) {
        this.consumido = consumido;
    }

    // Costo por empaque (lo que se escribe en el formulario)
    public BigDecimal getCostoEmpaque() {
        if (costoUnitario == null) {
            return BigDecimal.ZERO;
        }
        return costoUnitario.multiply(BigDecimal.valueOf(contenido)).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
