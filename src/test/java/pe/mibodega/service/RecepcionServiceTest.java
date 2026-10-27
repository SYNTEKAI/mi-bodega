package pe.mibodega.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.mibodega.model.RecepcionDetalle;

class RecepcionServiceTest {

    private RecepcionDetalle linea(Integer idLote, int idProducto, int unidades) {
        RecepcionDetalle d = new RecepcionDetalle();
        d.setIdLote(idLote);
        d.setIdProducto(idProducto);
        d.setProductoNombre("Producto " + idProducto);
        d.setTotalUnidades(unidades);
        d.setCostoUnitario(BigDecimal.ONE);
        return d;
    }

    @Test
    void sinCambiosNoHayAjustes() {
        List<RecepcionDetalle> antes = List.of(linea(1, 3, 60));
        List<RecepcionDetalle> despues = List.of(linea(1, 3, 60));
        assertTrue(RecepcionService.calcularAjustes(antes, despues).isEmpty());
    }

    @Test
    void corregirCantidadAjustaPorDiferencia() {
        List<RecepcionService.Ajuste> a = RecepcionService.calcularAjustes(List.of(linea(1, 3, 60)), List.of(linea(1, 3, 48)));
        assertEquals(1, a.size());
        assertEquals(-12, a.get(0).getDiferencia());
    }

    @Test
    void productoOmitidoEntraCompleto() {
        List<RecepcionService.Ajuste> a = RecepcionService.calcularAjustes(List.of(linea(1, 3, 60)),
                List.of(linea(1, 3, 60), linea(null, 5, 20)));
        assertEquals(1, a.size());
        assertEquals(20, a.get(0).getDiferencia());
        assertEquals(null, a.get(0).getIdLote());
    }

    @Test
    void lineaQuitadaPasaACero() {
        List<RecepcionService.Ajuste> a = RecepcionService.calcularAjustes(List.of(linea(1, 3, 60), linea(2, 4, 24)), List.of(linea(1, 3, 60)));
        assertEquals(1, a.size());
        assertEquals(-24, a.get(0).getDiferencia());
    }
}
