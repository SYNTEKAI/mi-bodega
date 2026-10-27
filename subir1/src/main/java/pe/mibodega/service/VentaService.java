package pe.mibodega.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.mibodega.dao.ClienteDAO;
import pe.mibodega.dao.CorrelativoDAO;
import pe.mibodega.dao.KardexDAO;
import pe.mibodega.dao.LoteDAO;
import pe.mibodega.dao.ProductoDAO;
import pe.mibodega.dao.VentaDAO;
import pe.mibodega.model.Cliente;
import pe.mibodega.model.Lote;
import pe.mibodega.model.Producto;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.model.Venta;
import pe.mibodega.model.VentaDetalle;
import pe.mibodega.util.NegocioException;

/**
 * Registro y anulacion de ventas (punto de venta).
 */
@Service
public class VentaService {

    public static final List<String> METODOS_PAGO = List.of("EFECTIVO", "YAPE", "TARJETA", "TRANSFERENCIA");

    @Autowired
    private VentaDAO ventaDAO;
    @Autowired
    private ProductoDAO productoDAO;
    @Autowired
    private LoteDAO loteDAO;
    @Autowired
    private KardexDAO kardexDAO;
    @Autowired
    private CorrelativoDAO correlativoDAO;
    @Autowired
    private ClienteDAO clienteDAO;
    @Autowired
    private AuditoriaService auditoria;

    /** Lo que se toma de un lote al vender. */
    public static class Descuento {
        private final Lote lote;
        private final int cantidad;

        public Descuento(Lote lote, int cantidad) {
            this.lote = lote;
            this.cantidad = cantidad;
        }

        public Lote getLote() {
            return lote;
        }

        public int getCantidad() {
            return cantidad;
        }
    }

    /**
     * FEFO: reparte la cantidad vendida empezando por el lote que vence primero.
     * Los lotes ya vienen ordenados por fecha de vencimiento.
     */
    public static List<Descuento> repartirFEFO(List<Lote> lotes, int cantidad) {
        List<Descuento> resultado = new ArrayList<>();
        int pendiente = cantidad;
        for (Lote lote : lotes) {
            if (pendiente <= 0) {
                break;
            }
            int tomo = Math.min(lote.getCantidadActual(), pendiente);
            if (tomo > 0) {
                resultado.add(new Descuento(lote, tomo));
                pendiente -= tomo;
            }
        }
        if (pendiente > 0) {
            throw new NegocioException("Stock insuficiente: faltan " + pendiente + " unidades.");
        }
        return resultado;
    }

    /** Vuelto = recibido - total. En efectivo el monto recibido no puede ser menor al total. */
    public static BigDecimal calcularVuelto(BigDecimal total, BigDecimal recibido) {
        if (recibido == null || recibido.compareTo(total) < 0) {
            throw new NegocioException("El monto recibido es menor al total de la venta.");
        }
        return recibido.subtract(total).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Registra la venta en una sola transaccion:
     * valida stock, genera el numero de boleta, guarda la venta y su detalle,
     * descuenta los lotes (FEFO) y registra la SALIDA en el kardex.
     * Si algo falla no queda nada a medias.
     */
    @Transactional
    public int registrar(UsuarioSesion s, List<Integer> productos, List<Integer> cantidades, String metodoPago,
            BigDecimal montoRecibido, Integer idCliente) {
        s.exigir("ventas", "crear");
        if (productos == null || productos.isEmpty()) {
            throw new NegocioException("El carrito está vacío.");
        }
        if (!METODOS_PAGO.contains(metodoPago)) {
            throw new NegocioException("Elige un medio de pago.");
        }
        int idEmpresa = s.getIdEmpresa();
        int idSede = s.getSedeActual();

        // Junta las cantidades por producto
        Map<Integer, Integer> carrito = new LinkedHashMap<>();
        for (int i = 0; i < productos.size(); i++) {
            int cant = cantidades.get(i);
            if (cant <= 0) {
                throw new NegocioException("Las cantidades deben ser mayores a cero.");
            }
            carrito.merge(productos.get(i), cant, Integer::sum);
        }

        // Valida stock y calcula el total con los precios de la BD
        BigDecimal total = BigDecimal.ZERO;
        Map<Integer, Producto> datos = new LinkedHashMap<>();
        for (Map.Entry<Integer, Integer> linea : carrito.entrySet()) {
            Producto p = productoDAO.buscar(linea.getKey(), idEmpresa);
            if (p == null || !p.isActivo()) {
                throw new NegocioException("Producto no disponible.");
            }
            int stock = loteDAO.stock(p.getIdProducto(), idSede);
            if (linea.getValue() > stock) {
                throw new NegocioException("Stock insuficiente de " + p.getNombre() + ": disponible " + stock + ".");
            }
            datos.put(p.getIdProducto(), p);
            total = total.add(p.getPrecioVenta().multiply(BigDecimal.valueOf(linea.getValue())));
        }

        Venta v = new Venta();
        v.setIdEmpresa(idEmpresa);
        v.setIdSede(idSede);
        v.setIdUsuario(s.getIdUsuarioEmpresa());
        v.setIdUsuarioInterno(s.getIdUsuarioInterno());
        v.setMetodoPago(metodoPago);
        v.setTotal(total);
        if ("EFECTIVO".equals(metodoPago)) {
            v.setMontoRecibido(montoRecibido);
            v.setVuelto(calcularVuelto(total, montoRecibido));
        }
        if (idCliente != null) {
            Cliente c = clienteDAO.buscar(idCliente, idEmpresa);
            v.setIdCliente(c != null ? c.getIdCliente() : null);
        }
        v.setNumeroComprobante(CorrelativoDAO.formatear("B001", correlativoDAO.siguiente(idEmpresa, "B001")));
        int idVenta = ventaDAO.insertar(v);

        for (Map.Entry<Integer, Integer> linea : carrito.entrySet()) {
            Producto p = datos.get(linea.getKey());
            VentaDetalle d = new VentaDetalle();
            d.setIdVenta(idVenta);
            d.setIdProducto(p.getIdProducto());
            d.setCantidad(linea.getValue());
            d.setPrecioUnitario(p.getPrecioVenta());
            d.setSubtotal(p.getPrecioVenta().multiply(BigDecimal.valueOf(linea.getValue())));
            int idDetalle = ventaDAO.insertarDetalle(d);

            List<Lote> lotes = loteDAO.disponiblesFEFO(p.getIdProducto(), idSede);
            for (Descuento desc : repartirFEFO(lotes, linea.getValue())) {
                Lote lote = desc.getLote();
                loteDAO.cambiarCantidad(lote.getIdLote(), lote.getCantidadActual() - desc.getCantidad());
                ventaDAO.insertarDetalleLote(idDetalle, lote.getIdLote(), desc.getCantidad());
                kardexDAO.registrar(idEmpresa, idSede, p.getIdProducto(), lote.getIdLote(), "SALIDA", desc.getCantidad(),
                        lote.getCostoUnitario(), "BOLETA " + v.getNumeroComprobante(), idVenta, null, null, s);
            }
        }

        auditoria.registrar(s, "Ventas", "REGISTRÓ VENTA", "Registró la venta " + v.getNumeroComprobante() + " por S/ "
                + total.setScale(2, RoundingMode.HALF_UP) + " (" + metodoPago + ")");
        return idVenta;
    }

    /** Anulacion logica: la venta no se borra y el stock vuelve a los mismos lotes. */
    @Transactional
    public void anular(UsuarioSesion s, int idVenta, String motivo) {
        s.exigir("ventas", "anular");
        Venta v = ventaDAO.buscar(idVenta, s.getIdEmpresa());
        if (v == null || v.isAnulada()) {
            throw new NegocioException("La venta no existe o ya está anulada.");
        }
        for (Map<String, Object> fila : ventaDAO.lotesDeVenta(idVenta)) {
            int idLote = ((Number) fila.get("id_lote")).intValue();
            int cantidad = ((Number) fila.get("cantidad")).intValue();
            int idProducto = ((Number) fila.get("id_producto")).intValue();
            loteDAO.sumarCantidad(idLote, cantidad);
            kardexDAO.registrar(v.getIdEmpresa(), v.getIdSede(), idProducto, idLote, "ENTRADA", cantidad,
                    (BigDecimal) fila.get("costo_unitario"), "ANULACION " + v.getNumeroComprobante(), idVenta, null, null, s);
        }
        ventaDAO.anular(idVenta, motivo == null || motivo.isBlank() ? "Anulada por " + s.getNombre() : motivo);
        auditoria.registrar(s, "Ventas", "ANULAR VENTA", "Anuló " + v.getNumeroComprobante() + " por S/ " + v.getTotal());
    }

    public Venta buscar(UsuarioSesion s, int idVenta) {
        Venta v = ventaDAO.buscar(idVenta, s.getIdEmpresa());
        if (v == null) {
            throw new NegocioException("La venta no existe.");
        }
        return v;
    }

    public List<Venta> ventasDelDia(UsuarioSesion s) {
        return ventaDAO.ventasDelDia(s.getIdEmpresa(), s.getSedeActual());
    }

    public List<Map<String, Object>> cajaDelDia(UsuarioSesion s) {
        return ventaDAO.cajaDelDia(s.getIdEmpresa(), s.getSedeActual());
    }
}
