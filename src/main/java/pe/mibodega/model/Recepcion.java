package pe.mibodega.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Recepcion de mercaderia (guia del proveedor).
 */
public class Recepcion {

    private Integer idRecepcion;
    private Integer idEmpresa;
    private Integer idSede;
    private Integer idProveedor;
    private String numero;
    private String guiaProveedor;
    private LocalDateTime fechaHora;
    private String estado;
    private String observaciones;
    private Integer idUsuario;
    private Integer idUsuarioInterno;
    private String proveedorNombre;
    private String proveedorDocumento;
    private String sedeNombre;
    private String usuarioNombre;
    private int productos;
    private int unidades;
    private LocalDateTime ultimaModificacion;
    private String modificoNombre;
    private List<RecepcionDetalle> detalles = new ArrayList<>();

    public Integer getIdRecepcion() {
        return idRecepcion;
    }

    public void setIdRecepcion(Integer idRecepcion) {
        this.idRecepcion = idRecepcion;
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

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getGuiaProveedor() {
        return guiaProveedor;
    }

    public void setGuiaProveedor(String guiaProveedor) {
        this.guiaProveedor = guiaProveedor;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdUsuarioInterno() {
        return idUsuarioInterno;
    }

    public void setIdUsuarioInterno(Integer idUsuarioInterno) {
        this.idUsuarioInterno = idUsuarioInterno;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public void setProveedorNombre(String proveedorNombre) {
        this.proveedorNombre = proveedorNombre;
    }

    public String getProveedorDocumento() {
        return proveedorDocumento;
    }

    public void setProveedorDocumento(String proveedorDocumento) {
        this.proveedorDocumento = proveedorDocumento;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public int getProductos() {
        return productos;
    }

    public void setProductos(int productos) {
        this.productos = productos;
    }

    public int getUnidades() {
        return unidades;
    }

    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }

    public LocalDateTime getUltimaModificacion() {
        return ultimaModificacion;
    }

    public void setUltimaModificacion(LocalDateTime ultimaModificacion) {
        this.ultimaModificacion = ultimaModificacion;
    }

    public String getModificoNombre() {
        return modificoNombre;
    }

    public void setModificoNombre(String modificoNombre) {
        this.modificoNombre = modificoNombre;
    }

    public List<RecepcionDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<RecepcionDetalle> detalles) {
        this.detalles = detalles;
    }

    public boolean isBorrador() {
        return "BORRADOR".equals(estado);
    }

    public boolean isAfectaStock() {
        return "CONFIRMADA".equals(estado) || "MODIFICADA".equals(estado);
    }
}
