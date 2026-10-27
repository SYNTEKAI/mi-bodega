package pe.mibodega.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.model.Proveedor;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.ProveedorService;
import pe.mibodega.util.NegocioException;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @GetMapping
    public String listar(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) String texto,
            @RequestParam(required = false) String estado, Model model) {
        model.addAttribute("proveedores", proveedorService.buscar(s, texto, estado));
        model.addAttribute("texto", texto);
        model.addAttribute("estado", estado);
        return "proveedores/lista";
    }

    @GetMapping("/{id}")
    public String ver(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscar(s, id));
        return "proveedores/ver";
    }

    @GetMapping("/nuevo")
    public String nuevo(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) String volver, Model model) {
        s.exigir("proveedores", "crear");
        Proveedor p = new Proveedor();
        p.setTipoDoc("RUC");
        model.addAttribute("proveedor", p);
        model.addAttribute("volver", volver);
        return "proveedores/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        s.exigir("proveedores", "editar");
        model.addAttribute("proveedor", proveedorService.buscar(s, id));
        return "proveedores/form";
    }

    @PostMapping("/guardar")
    public String guardar(@SessionAttribute("sesion") UsuarioSesion s, @ModelAttribute Proveedor proveedor,
            @RequestParam(required = false) String volver, Model model, RedirectAttributes flash) {
        try {
            proveedorService.guardar(s, proveedor);
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("proveedor", proveedor);
            model.addAttribute("volver", volver);
            return "proveedores/form";
        }
        flash.addFlashAttribute("ok", "Proveedor guardado.");
        return "recepcion".equals(volver) ? "redirect:/recepcion" : "redirect:/proveedores";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        proveedorService.cambiarActivo(s, id);
        flash.addFlashAttribute("ok", "Estado del proveedor actualizado.");
        return "redirect:/proveedores";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        flash.addFlashAttribute("ok", proveedorService.eliminar(s, id));
        return "redirect:/proveedores";
    }
}
