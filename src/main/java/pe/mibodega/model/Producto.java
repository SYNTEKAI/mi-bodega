package pe.mibodega.model;

import java.math.BigDecimal;

/**
 * Producto del catalogo. El stock no se guarda aqui, se calcula de los lotes.
 */
public class Producto {

    private Integer idProducto;
    private Integer idEmpresa;
    private Integer idCategoria;
    private String codigoBarras;
    private String nombre;
    private String marca;
    private String descripcion;
    private String presentacion;
    private String unidad;
    private String empaque;
    private int unidadesPorEmpaque;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private int stockMinimo;
    private boolean activo;
    private String categoriaNombre;
    private int stock;

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Integer idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public String getEmpaque() {
        return empaque;
    }

    public void setEmpaque(String empaque) {
        this.empaque = empaque;
    }

    public int getUnidadesPorEmpaque() {
        return unidadesPorEmpaque;
    }

    public void setUnidadesPorEmpaque(int unidadesPorEmpaque) {
        this.unidadesPorEmpaque = unidadesPorEmpaque;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public EstadoStock getEstadoStock() {
        return EstadoStock.calcular(stock, stockMinimo);
    }

    public BigDecimal getMargen() {
        if (precioVenta == null || precioCompra == null) {
            return BigDecimal.ZERO;
        }
        return precioVenta.subtract(precioCompra);
    }
}
