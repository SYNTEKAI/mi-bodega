package pe.mibodega.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Lote;

@Repository
public class LoteDAO extends BaseDAO {

    private static final String SELECT = "SELECT l.*, p.nombre AS producto_nombre, p.presentacion, p.unidad, r.numero AS recepcion_numero "
            + "FROM lotes l JOIN productos p ON p.id_producto = l.id_producto LEFT JOIN recepciones r ON r.id_recepcion = l.id_recepcion ";

    /** stock(producto, sede) = suma de cantidad_actual de sus lotes */
    public int stock(int idProducto, int idSede) {
        return contar("SELECT COALESCE(SUM(cantidad_actual), 0) FROM lotes WHERE id_producto = ? AND id_sede = ?", idProducto, idSede);
    }

    /**
     * Lotes con stock ordenados por vencimiento (FEFO: primero el que vence antes).
     * FOR UPDATE bloquea los lotes hasta que termine la transaccion de la venta.
     */
    public List<Lote> disponiblesFEFO(int idProducto, int idSede) {
        return lista(Lote.class, "SELECT * FROM lotes WHERE id_producto = ? AND id_sede = ? AND cantidad_actual > 0 "
                + "ORDER BY fecha_vencimiento IS NULL, fecha_vencimiento, id_lote FOR UPDATE", idProducto, idSede);
    }

    public List<Lote> listarConStock(int idEmpresa, int idSede) {
        return lista(Lote.class, SELECT + "WHERE l.id_empresa = ? AND l.id_sede = ? AND l.cantidad_actual > 0 "
                + "ORDER BY l.fecha_vencimiento IS NULL, l.fecha_vencimiento, l.id_lote", idEmpresa, idSede);
    }

    public Lote buscar(int idLote, int idEmpresa) {
        return uno(Lote.class, SELECT + "WHERE l.id_lote = ? AND l.id_empresa = ?", idLote, idEmpresa);
    }

    public List<Lote> porRecepcion(int idRecepcion) {
        return lista(Lote.class, SELECT + "WHERE l.id_recepcion = ?", idRecepcion);
    }

    public int insertar(Lote l) {
        return insertarConId("INSERT INTO lotes (id_empresa, id_sede, id_producto, id_recepcion, cantidad_ingresada, cantidad_actual, "
                + "costo_unitario, fecha_vencimiento) VALUES (?,?,?,?,?,?,?,?)",
                l.getIdEmpresa(), l.getIdSede(), l.getIdProducto(), l.getIdRecepcion(), l.getCantidadIngresada(),
                l.getCantidadActual(), l.getCostoUnitario(), l.getFechaVencimiento());
    }

    public void cambiarCantidad(int idLote, int cantidadActual) {
        jdbc.update("UPDATE lotes SET cantidad_actual = ? WHERE id_lote = ?", cantidadActual, idLote);
    }

    /** Suma (o resta) unidades al stock actual del lote. */
    public void sumarCantidad(int idLote, int cantidad) {
        jdbc.update("UPDATE lotes SET cantidad_actual = cantidad_actual + ? WHERE id_lote = ?", cantidad, idLote);
    }

    /** Correccion de una recepcion: cambia lo ingresado y el stock por la diferencia. */
    public void corregir(int idLote, int diferencia, BigDecimal costo, LocalDate vencimiento) {
        jdbc.update("UPDATE lotes SET cantidad_ingresada = cantidad_ingresada + ?, cantidad_actual = cantidad_actual + ?, "
                + "costo_unitario = ?, fecha_vencimiento = ? WHERE id_lote = ?", diferencia, diferencia, costo, vencimiento, idLote);
    }

    /** Lotes por vencer en los proximos dias (con stock). */
    public int contarPorVencer(int idEmpresa, int dias) {
        return contar("SELECT COUNT(*) FROM lotes WHERE id_empresa = ? AND cantidad_actual > 0 AND fecha_vencimiento IS NOT NULL "
                + "AND fecha_vencimiento <= CURDATE() + INTERVAL ? DAY", idEmpresa, dias);
    }
}
