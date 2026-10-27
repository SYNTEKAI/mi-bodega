package pe.mibodega.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EstadoStockTest {

    @Test
    void calculaElEstadoSegunElStockMinimo() {
        assertEquals(EstadoStock.SIN_STOCK, EstadoStock.calcular(0, 10));
        assertEquals(EstadoStock.STOCK_BAJO, EstadoStock.calcular(10, 10));
        assertEquals(EstadoStock.STOCK_BAJO, EstadoStock.calcular(4, 10));
        assertEquals(EstadoStock.DISPONIBLE, EstadoStock.calcular(11, 10));
    }
}
