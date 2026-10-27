package pe.mibodega.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Movimiento de inventario con su saldo acumulado.
 */
public class MovimientoKardex {

    private Long idMovimiento;
    private Integer idProducto;
    private Integer idSede;
    private Integer idLote;
    private LocalDateTime fechaHora;
    private String tipo;
    private int cantidad;
    private String documento;
    private String productoNombre;
    private String categoriaNombre;
    private String presentacion;
    private int saldo;
    private BigDecimal costoUnitario;
    private String usuarioNombre;
    private String sedeNombre;
    private String proveedorNombre;

    public Long getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Long idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getIdSede() {
        return idSede;
    }

    public void setIdSede(Integer idSede) {
        this.idSede = idSede;
    }

    public Integer getIdLote() {
        return idLote;
    }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public int getEntrada() {
        if ("ENTRADA".equals(tipo) || ("AJUSTE".equals(tipo) && cantidad > 0)) {
            return Math.abs(cantidad);
        }
        return 0;
    }

    public int getSalida() {
        if ("SALIDA".equals(tipo) || ("AJUSTE".equals(tipo) && cantidad < 0)) {
            return Math.abs(cantidad);
        }
        return 0;
    }

    public BigDecimal getCostoTotal() {
        if (costoUnitario == null) {
            return BigDecimal.ZERO;
        }
        return costoUnitario.multiply(BigDecimal.valueOf(Math.abs(cantidad))).setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
