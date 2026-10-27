package pe.mibodega.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.ProductoService;
import pe.mibodega.service.ReporteService;
import pe.mibodega.service.UsuarioService;
import pe.mibodega.service.VentaService;

/**
 * Reporte de ventas (por dia, semana o mes) y ganancia por producto.
 */
@Controller
public class ReporteController {

    @Autowired
    private ReporteService reporteService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/reportes")
    public String reportes(@SessionAttribute("sesion") UsuarioSesion s,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer sede, @RequestParam(required = false) Integer vendedor,
            @RequestParam(required = false) Integer categoria, @RequestParam(required = false) Integer producto,
            @RequestParam(required = false) String pago, @RequestParam(defaultValue = "detalle") String vista, Model model) {
        if (desde == null) {
            desde = LocalDate.now().minusDays(29);
        }
        if (hasta == null) {
            hasta = LocalDate.now();
        }
        List<Map<String, Object>> lineas = reporteService.lineasDeVenta(s, desde, hasta, sede, vendedor, categoria, producto, pago);
        BigDecimal total = BigDecimal.ZERO;
        int unidades = 0;
        Set<Object> comprobantes = new HashSet<>();
        for (Map<String, Object> l : lineas) {
            total = total.add((BigDecimal) l.get("subtotal"));
            unidades += ((Number) l.get("cantidad")).intValue();
            comprobantes.add(l.get("numero_comprobante"));
        }
        model.addAttribute("lineas", lineas.size() > 200 ? lineas.subList(0, 200) : lineas);
        model.addAttribute("totalLineas", lineas.size());
        model.addAttribute("total", total);
        model.addAttribute("unidades", unidades);
        model.addAttribute("comprobantes", comprobantes.size());
        model.addAttribute("ticketPromedio", comprobantes.isEmpty() ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(comprobantes.size()), 2, java.math.RoundingMode.HALF_UP));
        model.addAttribute("ganancias", reporteService.gananciaPorProducto(s, desde, hasta, sede));
        model.addAttribute("vendedores", usuarioService.listar(s));
        model.addAttribute("categorias", productoService.categorias(s));
        model.addAttribute("productos", productoService.buscar(s, null, null, null, null, null));
        model.addAttribute("metodos", VentaService.METODOS_PAGO);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("sede", sede);
        model.addAttribute("vendedor", vendedor);
        model.addAttribute("categoria", categoria);
        model.addAttribute("producto", producto);
        model.addAttribute("pago", pago);
        model.addAttribute("vista", vista);
        return "reportes";
    }

    @GetMapping("/reportes/excel")
    public ResponseEntity<byte[]> excel(@SessionAttribute("sesion") UsuarioSesion s,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer sede, @RequestParam(required = false) Integer vendedor,
            @RequestParam(required = false) Integer categoria, @RequestParam(required = false) Integer producto,
            @RequestParam(required = false) String pago, @RequestParam(defaultValue = "detalle") String vista) {
        List<String[]> filtros = List.of(new String[]{"Periodo", desde + " al " + hasta}, new String[]{"Pago", pago});
        if ("ganancia".equals(vista)) {
            return Descargas.excel(reporteService.gananciaExcel(s, reporteService.gananciaPorProducto(s, desde, hasta, sede), filtros),
                    "ganancia_productos_" + LocalDate.now() + ".xlsx");
        }
        List<Map<String, Object>> lineas = reporteService.lineasDeVenta(s, desde, hasta, sede, vendedor, categoria, producto, pago);
        return Descargas.excel(reporteService.ventasExcel(s, lineas, filtros), "reporte_ventas_" + LocalDate.now() + ".xlsx");
    }
}
