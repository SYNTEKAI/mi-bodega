package pe.mibodega.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.mibodega.dao.AuditoriaDAO;
import pe.mibodega.dao.LoteDAO;
import pe.mibodega.dao.ReporteDAO;
import pe.mibodega.dao.UsuarioDAO;
import pe.mibodega.model.EstadoStock;
import pe.mibodega.model.RegistroAuditoria;

/**
 * Indicadores (KPI) y datos de los graficos del dashboard.
 */
@Service
public class DashboardService {

    @Autowired
    private ReporteDAO reporteDAO;
    @Autowired
    private UsuarioDAO usuarioDAO;
    @Autowired
    private LoteDAO loteDAO;
    @Autowired
    private AuditoriaDAO auditoriaDAO;

    /** Fecha de inicio segun el periodo elegido en el filtro. */
    public static LocalDate inicioPeriodo(String periodo) {
        LocalDate hoy = LocalDate.now();
        switch (periodo == null ? "mes" : periodo) {
            case "hoy":
                return hoy;
            case "7d":
                return hoy.minusDays(6);
            case "90d":
                return hoy.minusDays(89);
            case "30d":
                return hoy.minusDays(29);
            default:
                return hoy.withDayOfMonth(1);
        }
    }

    /** Variacion porcentual respecto al periodo anterior. */
    public static BigDecimal variacion(BigDecimal actual, BigDecimal anterior) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return actual.subtract(anterior).multiply(BigDecimal.valueOf(100)).divide(anterior, 1, RoundingMode.HALF_UP);
    }

    public Map<String, Object> kpis(int idEmpresa) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate manana = hoy.plusDays(1);
        Map<String, Object> ventasHoy = reporteDAO.totalVentas(idEmpresa, hoy, manana);
        Map<String, Object> ventasMes = reporteDAO.totalVentas(idEmpresa, inicioMes, manana);
        // Mes anterior hasta el mismo dia, para comparar periodos iguales
        LocalDate inicioAnterior = inicioMes.minusMonths(1);
        LocalDate finAnterior = inicioAnterior.plusDays(hoy.getDayOfMonth());
        if (finAnterior.isAfter(inicioMes)) {
            finAnterior = inicioMes;
        }
        Map<String, Object> ventasAnterior = reporteDAO.totalVentas(idEmpresa, inicioAnterior, finAnterior);

        BigDecimal ingresosMes = (BigDecimal) ventasMes.get("total");
        long ticketsMes = ((Number) ventasMes.get("cantidad")).longValue();
        BigDecimal ingresosAnterior = (BigDecimal) ventasAnterior.get("total");
        long ticketsAnterior = ((Number) ventasAnterior.get("cantidad")).longValue();

        int bajo = 0;
        int sin = 0;
        for (Map<String, Object> p : reporteDAO.stockProductos(idEmpresa, null)) {
            EstadoStock e = EstadoStock.calcular(((Number) p.get("stock")).intValue(), ((Number) p.get("stock_minimo")).intValue());
            if (e == EstadoStock.STOCK_BAJO) {
                bajo++;
            } else if (e == EstadoStock.SIN_STOCK) {
                sin++;
            }
        }

        Map<String, Object> k = new LinkedHashMap<>();
        k.put("ventasHoy", ventasHoy.get("total"));
        k.put("ticketsHoy", ventasHoy.get("cantidad"));
        k.put("ingresosMes", ingresosMes);
        k.put("ticketsMes", ticketsMes);
        k.put("variacionIngresos", variacion(ingresosMes, ingresosAnterior));
        k.put("variacionTickets", variacion(BigDecimal.valueOf(ticketsMes), BigDecimal.valueOf(ticketsAnterior)));
        k.put("ticketPromedio", ticketsMes == 0 ? BigDecimal.ZERO
                : ingresosMes.divide(BigDecimal.valueOf(ticketsMes), 2, RoundingMode.HALF_UP));
        k.put("unidadesMes", reporteDAO.unidadesVendidas(idEmpresa, inicioMes, manana));
        k.put("usuariosActivos", usuarioDAO.contarActivos(idEmpresa));
        k.put("stockBajo", bajo);
        k.put("sinStock", sin);
        k.put("porVencer", loteDAO.contarPorVencer(idEmpresa, 30));
        return k;
    }

    /** Datos de los graficos (Chart.js los dibuja en la vista). */
    public Map<String, Object> graficos(int idEmpresa, String periodo, Integer idSede) {
        LocalDate desde = inicioPeriodo(periodo);
        Map<String, Object> g = new LinkedHashMap<>();
        g.put("evolucion", reporteDAO.ventasPorDia(idEmpresa, desde, idSede));
        g.put("sedes", reporteDAO.ventasPorSede(idEmpresa, desde));
        g.put("categorias", reporteDAO.ventasPorCategoria(idEmpresa, desde, idSede));
        g.put("top", reporteDAO.topProductos(idEmpresa, desde, idSede));
        g.put("pagos", reporteDAO.ventasPorMetodo(idEmpresa, desde, idSede));
        g.put("vendedores", reporteDAO.ventasPorVendedor(idEmpresa, desde, idSede));
        int disponible = 0;
        int bajo = 0;
        int sin = 0;
        for (Map<String, Object> p : reporteDAO.stockProductos(idEmpresa, idSede)) {
            EstadoStock e = EstadoStock.calcular(((Number) p.get("stock")).intValue(), ((Number) p.get("stock_minimo")).intValue());
            if (e == EstadoStock.DISPONIBLE) {
                disponible++;
            } else if (e == EstadoStock.STOCK_BAJO) {
                bajo++;
            } else {
                sin++;
            }
        }
        g.put("inventario", List.of(disponible, bajo, sin));
        return g;
    }

    // ---- Plataforma ----

    public Map<String, Object> totalesPlataforma() {
        return reporteDAO.totalesPlataforma();
    }

    public List<Map<String, Object>> ventasPlataforma() {
        return reporteDAO.ventasPlataformaPorDia(LocalDate.now().minusDays(29));
    }

    public List<Map<String, Object>> actividadEmpresas() {
        return reporteDAO.actividadPorEmpresa(LocalDate.now().minusDays(29));
    }

    public List<RegistroAuditoria> actividadReciente() {
        return auditoriaDAO.recientes(8);
    }
}
