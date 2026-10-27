package pe.mibodega.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.model.Cliente;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.ClienteService;
import pe.mibodega.service.ComprobanteService;
import pe.mibodega.service.EmpresaService;
import pe.mibodega.service.ProductoService;
import pe.mibodega.service.VentaService;
import pe.mibodega.util.NegocioException;

/**
 * Punto de venta (POS), boleta y anulacion.
 */
@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ComprobanteService comprobanteService;
    @Autowired
    private EmpresaService empresaService;

    @GetMapping
    public String pos(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        model.addAttribute("productos", productoService.paraVenta(s));
        model.addAttribute("ventas", ventaService.ventasDelDia(s));
        model.addAttribute("caja", ventaService.cajaDelDia(s));
        model.addAttribute("metodos", VentaService.METODOS_PAGO);
        return "ventas/pos";
    }

    @PostMapping
    public String registrar(@SessionAttribute("sesion") UsuarioSesion s,
            @RequestParam(name = "productoId", required = false) List<Integer> productos,
            @RequestParam(name = "cantidad", required = false) List<Integer> cantidades,
            @RequestParam String metodoPago, @RequestParam(required = false) BigDecimal montoRecibido,
            @RequestParam(required = false) Integer idCliente, RedirectAttributes flash) {
        int idVenta = ventaService.registrar(s, productos, cantidades, metodoPago, montoRecibido, idCliente);
        flash.addFlashAttribute("ok", "Venta guardada.");
        return "redirect:/ventas/" + idVenta + "/comprobante";
    }

    @GetMapping("/{id}/comprobante")
    public String comprobante(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id,
            @RequestParam(required = false) Integer volverCliente, Model model) {
        model.addAttribute("venta", ventaService.buscar(s, id));
        model.addAttribute("empresa", empresaService.buscar(s.getIdEmpresa()));
        model.addAttribute("config", comprobanteService.config(s.getIdEmpresa()));
        model.addAttribute("volverCliente", volverCliente);
        return "ventas/comprobante";
    }

    @PostMapping("/{id}/anular")
    public String anular(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id,
            @RequestParam(required = false) String motivo, RedirectAttributes flash) {
        ventaService.anular(s, id, motivo);
        flash.addFlashAttribute("ok", "Venta anulada. El stock volvió a sus lotes.");
        return "redirect:/ventas";
    }

    /** Busca un cliente por DNI/RUC mientras se escribe en el POS (respuesta JSON). */
    @GetMapping("/cliente")
    @ResponseBody
    public Map<String, Object> buscarCliente(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam String documento) {
        Map<String, Object> r = new HashMap<>();
        Cliente c = clienteService.buscarPorDocumento(s, documento);
        r.put("encontrado", c != null);
        r.put("puedeCrear", s.puede("clientes", "crear"));
        if (c != null) {
            r.put("id", c.getIdCliente());
            r.put("nombre", c.getNombre());
            r.put("documento", c.getTipoDoc() + " " + c.getDocumento());
            r.put("compras", c.getCompras());
        }
        return r;
    }

    /** Registro rapido de un cliente desde el POS (respuesta JSON). */
    @PostMapping("/cliente")
    @ResponseBody
    public Map<String, Object> registrarCliente(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam String documento,
            @RequestParam String nombre) {
        Map<String, Object> r = new HashMap<>();
        try {
            Cliente c = new Cliente();
            c.setDocumento(documento.trim());
            c.setTipoDoc(documento.trim().length() == 11 ? "RUC" : "DNI");
            c.setNombre(nombre);
            r.put("id", clienteService.guardar(s, c));
            r.put("nombre", nombre.trim());
            r.put("documento", c.getTipoDoc() + " " + c.getDocumento());
        } catch (NegocioException e) {
            r.put("error", e.getMessage());
        }
        return r;
    }
}
