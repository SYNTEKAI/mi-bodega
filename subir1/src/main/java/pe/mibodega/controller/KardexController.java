package pe.mibodega.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import pe.mibodega.model.MovimientoKardex;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.ProductoService;
import pe.mibodega.service.ProveedorService;
import pe.mibodega.service.ReporteService;

@Controller
public class KardexController {

    private static final int FILAS_POR_PAGINA = 25;

    @Autowired
    private ReporteService reporteService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private ProveedorService proveedorService;

    @GetMapping("/kardex")
    public String kardex(@SessionAttribute("sesion") UsuarioSesion s,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer producto, @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) Integer sede, @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer proveedor, @RequestParam(defaultValue = "1") int pagina, Model model) {
        List<MovimientoKardex> lista = reporteService.kardex(s, desde, hasta, producto, categoria, sede, tipo, proveedor);
        int totalPaginas = Math.max(1, (int) Math.ceil(lista.size() / (double) FILAS_POR_PAGINA));
        pagina = Math.min(Math.max(pagina, 1), totalPaginas);
        model.addAttribute("movimientos", lista.subList((pagina - 1) * FILAS_POR_PAGINA, Math.min(lista.size(), pagina * FILAS_POR_PAGINA)));
        model.addAttribute("total", lista.size());
        model.addAttribute("entradas", lista.stream().mapToInt(MovimientoKardex::getEntrada).sum());
        model.addAttribute("salidas", lista.stream().mapToInt(MovimientoKardex::getSalida).sum());
        model.addAttribute("pagina", pagina);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("productos", productoService.buscar(s, null, null, null, null, null));
        model.addAttribute("categorias", productoService.categorias(s));
        model.addAttribute("proveedores", proveedorService.buscar(s, null, null));
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("producto", producto);
        model.addAttribute("categoria", categoria);
        model.addAttribute("sede", sede);
        model.addAttribute("tipo", tipo);
        model.addAttribute("proveedor", proveedor);
        return "kardex";
    }

    /** Exporta exactamente lo filtrado. */
    @GetMapping("/kardex/excel")
    public ResponseEntity<byte[]> excel(@SessionAttribute("sesion") UsuarioSesion s,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer producto, @RequestParam(required = false) Integer categoria,
            @RequestParam(required = false) Integer sede, @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer proveedor) {
        List<MovimientoKardex> lista = reporteService.kardex(s, desde, hasta, producto, categoria, sede, tipo, proveedor);
        List<String[]> filtros = List.of(new String[]{"Desde", desde == null ? "inicio" : desde.toString()},
                new String[]{"Hasta", hasta == null ? "hoy" : hasta.toString()}, new String[]{"Tipo", tipo});
        return Descargas.excel(reporteService.kardexExcel(s, lista, filtros), "kardex_" + LocalDate.now() + ".xlsx");
    }
}
