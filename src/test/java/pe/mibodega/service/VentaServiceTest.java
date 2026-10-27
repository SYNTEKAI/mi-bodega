package pe.mibodega.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.mibodega.dao.LoteDAO;
import pe.mibodega.dao.ProductoDAO;
import pe.mibodega.dao.VentaDAO;
import pe.mibodega.model.Lote;
import pe.mibodega.model.Producto;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.SinPermisoException;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private ProductoDAO productoDAO;
    @Mock
    private LoteDAO loteDAO;
    @Mock
    private VentaDAO ventaDAO;
    @InjectMocks
    private VentaService ventaService;

    private UsuarioSesion cajero;

    @BeforeEach
    void preparar() {
        cajero = new UsuarioSesion();
        cajero.setTipo(UsuarioSesion.EMPRESA);
        cajero.setIdUsuario(2);
        cajero.setRolNombre("Vendedor");
        cajero.setIdEmpresa(1);
        cajero.setIdSedeUsuario(1);
        cajero.setPermisos(Set.of("ventas.ver", "ventas.crear"));
    }

    private Lote lote(int id, int cantidad, LocalDate vence) {
        Lote l = new Lote();
        l.setIdLote(id);
        l.setCantidadActual(cantidad);
        l.setFechaVencimiento(vence);
        l.setCostoUnitario(new BigDecimal("3.40"));
        return l;
    }

    @Test
    void fefoDescuentaPrimeroDelLoteQueVenceAntes() {
        List<Lote> lotes = List.of(lote(2, 30, LocalDate.now().plusDays(10)), lote(3, 48, LocalDate.now().plusDays(40)));
        List<VentaService.Descuento> r = VentaService.repartirFEFO(lotes, 5);
        assertEquals(1, r.size());
        assertEquals(2, r.get(0).getLote().getIdLote());
        assertEquals(5, r.get(0).getCantidad());
    }

    @Test
    void fefoUsaVariosLotesCuandoUnoNoAlcanza() {
        List<Lote> lotes = List.of(lote(2, 30, LocalDate.now().plusDays(10)), lote(3, 48, LocalDate.now().plusDays(40)));
        List<VentaService.Descuento> r = VentaService.repartirFEFO(lotes, 35);
        assertEquals(2, r.size());
        assertEquals(30, r.get(0).getCantidad());
        assertEquals(5, r.get(1).getCantidad());
    }

    @Test
    void fefoSinStockSuficienteLanzaError() {
        List<Lote> lotes = List.of(lote(2, 3, null));
        assertThrows(NegocioException.class, () -> VentaService.repartirFEFO(lotes, 5));
    }

    @Test
    void calculaElVuelto() {
        assertEquals(new BigDecimal("17.10"), VentaService.calcularVuelto(new BigDecimal("32.90"), new BigDecimal("50")));
    }

    @Test
    void montoRecibidoMenorAlTotalLanzaError() {
        assertThrows(NegocioException.class, () -> VentaService.calcularVuelto(new BigDecimal("32.90"), new BigDecimal("20")));
    }

    @Test
    void carritoVacioNoSeRegistra() {
        assertThrows(NegocioException.class, () -> ventaService.registrar(cajero, List.of(), List.of(), "EFECTIVO", null, null));
    }

    @Test
    void ventaConStockInsuficienteNoSeGuarda() {
        Producto leche = new Producto();
        leche.setIdProducto(2);
        leche.setNombre("Leche Gloria Evaporada");
        leche.setPrecioVenta(new BigDecimal("4.20"));
        leche.setActivo(true);
        when(productoDAO.buscar(2, 1)).thenReturn(leche);
        when(loteDAO.stock(2, 1)).thenReturn(3);

        NegocioException e = assertThrows(NegocioException.class,
                () -> ventaService.registrar(cajero, List.of(2), List.of(10), "YAPE", null, null));
        assertTrue(e.getMessage().contains("Stock insuficiente"));
        verify(ventaDAO, never()).insertar(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void vendedorSinPermisoNoPuedeAnular() {
        assertThrows(SinPermisoException.class, () -> ventaService.anular(cajero, 1, "error"));
        verify(ventaDAO, never()).anular(anyInt(), org.mockito.ArgumentMatchers.anyString());
    }
}
