package pe.mibodega.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.model.Recepcion;
import pe.mibodega.model.RecepcionDetalle;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.EmpresaService;
import pe.mibodega.service.ProductoService;
import pe.mibodega.service.ProveedorService;
import pe.mibodega.service.RecepcionService;
import pe.mibodega.service.ReporteService;
import pe.mibodega.util.NegocioException;

/**
 * Recepcion de mercaderia y guias recibidas.
 */
@Controller
public class RecepcionController {

    public static final List<String> MOTIVOS = List.of("Producto omitido durante la recepción", "Cantidad ingresada incorrectamente",
            "Error de digitación", "Corrección de proveedor", "Otro");

    @Autowired
    private RecepcionService recepcionService;
    @Autowired
    private ProveedorService proveedorService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private ReporteService reporteService;
    @Autowired
    private EmpresaService empresaService;

    /** Formulario: nueva recepcion, borrador o correccion de una confirmada. */
    @GetMapping("/recepcion")
    public String formulario(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) Integer id, Model model) {
        Recepcion r;
        if (id == null) {
            s.exigir("recepcion", "crear");
            r = new Recepcion();
            r.setEstado("NUEVA");
            model.addAttribute("numero", "Se asigna al guardar (próximo " + recepcionService.proximoNumero(s) + ")");
        } else {
            r = recepcionService.buscar(s, id);
            if ("ANULADA".equals(r.getEstado())) {
                throw new NegocioException("Una recepción anulada no se puede editar.");
            }
            s.exigir("recepcion", r.isBorrador() ? "crear" : "editar_confirmada");
            model.addAttribute("numero", r.getNumero());
        }
        model.addAttribute("recepcion", r);
        model.addAttribute("correccion", r.isAfectaStock());
        model.addAttribute("proveedores", proveedorService.activos(s));
        model.addAttribute("productos", productoService.paraVenta(s));
        model.addAttribute("empaques", ProductoService.EMPAQUES);
        model.addAttribute("motivos", MOTIVOS);
        return "recepcion/form";
    }

    @PostMapping("/recepcion/guardar")
    public String guardar(@SessionAttribute("sesion") UsuarioSesion s, HttpServletRequest req, RedirectAttributes flash) {
        Recepcion cab = new Recepcion();
        String id = req.getParameter("idRecepcion");
        cab.setIdRecepcion(StringUtils.isBlank(id) ? null : Integer.valueOf(id));
        cab.setGuiaProveedor(req.getParameter("guiaProveedor"));
        String prov = req.getParameter("idProveedor");
        cab.setIdProveedor(StringUtils.isBlank(prov) ? null : Integer.valueOf(prov));
        cab.setObservaciones(StringUtils.trimToNull(req.getParameter("observaciones")));
        List<RecepcionDetalle> lineas = leerLineas(req);
        String accion = req.getParameter("accion");

        if ("corregir".equals(accion)) {
            String motivo = req.getParameter("motivo");
            if ("Otro".equals(motivo)) {
                motivo = "Otro: " + StringUtils.trimToEmpty(req.getParameter("motivoOtro"));
            }
            String detalle = StringUtils.trimToEmpty(req.getParameter("motivoDetalle"));
            if (!detalle.isEmpty()) {
                motivo = motivo + " - " + detalle;
            }
            recepcionService.corregir(s, cab, lineas, motivo);
            flash.addFlashAttribute("ok", "Recepción corregida. El stock se ajustó por diferencia.");
            return "redirect:/guias";
        }
        boolean confirmar = "confirmar".equals(accion);
        int idRecepcion = recepcionService.guardar(s, cab, lineas, confirmar);
        if (confirmar) {
            flash.addFlashAttribute("ok", "Recepción confirmada: la mercadería ingresó al stock.");
            return "redirect:/guias/" + idRecepcion;
        }
        flash.addFlashAttribute("ok", "Borrador guardado (todavía no afecta el stock).");
        return "redirect:/guias";
    }

    /** Lee las lineas del formulario (cada linea es una fila de la tabla). */
    private List<RecepcionDetalle> leerLineas(HttpServletRequest req) {
        List<RecepcionDetalle> lineas = new ArrayList<>();
        String[] productos = req.getParameterValues("productoId");
        if (productos == null) {
            return lineas;
        }
        String[] empaques = req.getParameterValues("empaque");
        String[] contenidos = req.getParameterValues("contenido");
        String[] cantidades = req.getParameterValues("cantidadEmpaques");
        String[] costos = req.getParameterValues("costoEmpaque");
        String[] vencimientos = req.getParameterValues("vencimiento");
        String[] lotes = req.getParameterValues("idLote");
        try {
            for (int i = 0; i < productos.length; i++) {
                RecepcionDetalle d = new RecepcionDetalle();
                d.setIdProducto(Integer.valueOf(productos[i]));
                d.setEmpaque(empaques[i]);
                d.setContenido("Unidad".equals(empaques[i]) ? 1 : Integer.parseInt(contenidos[i]));
                d.setCantidadEmpaques(Integer.parseInt(cantidades[i]));
                BigDecimal costoEmpaque = StringUtils.isBlank(costos[i]) ? BigDecimal.ZERO : new BigDecimal(costos[i]);
                d.setCostoUnitario(d.getContenido() > 0
                        ? costoEmpaque.divide(BigDecimal.valueOf(d.getContenido()), 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);
                d.setFechaVencimiento(StringUtils.isBlank(vencimientos[i]) ? null : LocalDate.parse(vencimientos[i]));
                d.setIdLote(StringUtils.isBlank(lotes[i]) ? null : Integer.valueOf(lotes[i]));
                lineas.add(d);
            }
        } catch (NumberFormatException e) {
            throw new NegocioException("Revisa las cantidades y costos: deben ser números.");
        }
        return lineas;
    }

    // ---------------- Guias recibidas ----------------

    @GetMapping("/guias")
    public String guias(@SessionAttribute("sesion") UsuarioSesion s,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer proveedor, @RequestParam(required = false) Integer producto,
            @RequestParam(required = false) String numero, @RequestParam(required = false) String estado, Model model) {
        List<Recepcion> guias = reporteService.guias(s, desde, hasta, proveedor, producto, numero, estado);
        model.addAttribute("guias", guias);
        model.addAttribute("totalUnidades", guias.stream().filter(Recepcion::isAfectaStock).mapToInt(Recepcion::getUnidades).sum());
        model.addAttribute("proveedores", proveedorService.buscar(s, null, null));
        model.addAttribute("productos", productoService.buscar(s, null, null, null, null, null));
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("proveedor", proveedor);
        model.addAttribute("producto", producto);
        model.addAttribute("numero", numero);
        model.addAttribute("estado", estado);
        return "recepcion/guias";
    }

    @GetMapping("/guias/excel")
    public ResponseEntity<byte[]> guiasExcel(@SessionAttribute("sesion") UsuarioSesion s,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer proveedor, @RequestParam(required = false) Integer producto,
            @RequestParam(required = false) String numero, @RequestParam(required = false) String estado) {
        List<Recepcion> guias = reporteService.guias(s, desde, hasta, proveedor, producto, numero, estado);
        List<String[]> filtros = List.of(new String[]{"Desde", desde == null ? "inicio" : desde.toString()},
                new String[]{"Hasta", hasta == null ? "hoy" : hasta.toString()}, new String[]{"Estado", estado});
        return Descargas.excel(reporteService.guiasExcel(s, guias, filtros), "guias_recibidas_" + LocalDate.now() + ".xlsx");
    }

    /** Cargo de recepcion (pagina lista para imprimir o guardar como PDF). */
    @GetMapping("/guias/{id}")
    public String cargo(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        Recepcion r = recepcionService.buscar(s, id);
        model.addAttribute("r", r);
        model.addAttribute("empresa", empresaService.buscar(s.getIdEmpresa()));
        model.addAttribute("proveedor", proveedorService.buscar(s, r.getIdProveedor()));
        model.addAttribute("modificaciones", reporteService.modificaciones(id));
        model.addAttribute("eventos", reporteService.historialRecepcion(s, r.getNumero()));
        return "recepcion/cargo";
    }

    @PostMapping("/guias/{id}/anular")
    public String anular(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        recepcionService.anular(s, id);
        flash.addFlashAttribute("ok", "Recepción anulada.");
        return "redirect:/guias";
    }
}
