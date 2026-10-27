package pe.mibodega.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.model.Sede;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.SedeService;
import pe.mibodega.util.NegocioException;

@Controller
@RequestMapping("/sedes")
public class SedeController {

    @Autowired
    private SedeService sedeService;

    @GetMapping
    public String listar(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        model.addAttribute("sedes", sedeService.listar(s));
        return "sedes/lista";
    }

    @GetMapping("/{id}")
    public String ver(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        model.addAttribute("sede", sedeService.buscar(s, id));
        model.addAttribute("usuarios", sedeService.contarUsuarios(id));
        model.addAttribute("ventas", sedeService.contarVentas(id));
        return "sedes/ver";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Sede sede = new Sede();
        sede.setProvincia("Lima");
        model.addAttribute("sede", sede);
        return "sedes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        model.addAttribute("sede", sedeService.buscar(s, id));
        return "sedes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@SessionAttribute("sesion") UsuarioSesion s, @ModelAttribute Sede sede, Model model, RedirectAttributes flash) {
        try {
            sedeService.guardar(s, sede);
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("sede", sede);
            return "sedes/form";
        }
        flash.addFlashAttribute("ok", "Sede guardada.");
        return "redirect:/sedes";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        sedeService.cambiarActivo(s, id);
        flash.addFlashAttribute("ok", "Estado de la sede actualizado.");
        return "redirect:/sedes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        flash.addFlashAttribute("ok", sedeService.eliminar(s, id));
        return "redirect:/sedes";
    }
}
