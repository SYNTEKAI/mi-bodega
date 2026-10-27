package pe.mibodega.controller;

import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.model.Rol;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.RolService;

@Controller
@RequestMapping("/roles")
public class RolController {

    @Autowired
    private RolService rolService;

    @GetMapping
    public String listar(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        List<Rol> roles = rolService.listar(s);
        java.util.Map<Integer, java.util.Set<String>> permisos = new java.util.HashMap<>();
        for (Rol r : roles) {
            permisos.put(r.getIdRol(), rolService.permisosDe(r.getIdRol()));
        }
        model.addAttribute("roles", roles);
        model.addAttribute("permisosRol", permisos);
        model.addAttribute("modulos", rolService.permisosPorModulo("EMPRESA"));
        model.addAttribute("nombres", RolService.NOMBRES_MODULO);
        return "roles/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("rol", new Rol());
        model.addAttribute("marcados", new HashSet<String>());
        model.addAttribute("modulos", rolService.permisosPorModulo("EMPRESA"));
        model.addAttribute("nombres", RolService.NOMBRES_MODULO);
        return "roles/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        model.addAttribute("rol", rolService.buscar(s, id));
        model.addAttribute("marcados", rolService.permisosDe(id));
        model.addAttribute("modulos", rolService.permisosPorModulo("EMPRESA"));
        model.addAttribute("nombres", RolService.NOMBRES_MODULO);
        return "roles/form";
    }

    @PostMapping("/guardar")
    public String guardar(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) Integer idRol,
            @RequestParam String nombre, @RequestParam(name = "permisos", required = false) List<Integer> permisos,
            RedirectAttributes flash) {
        rolService.guardar(s, idRol, nombre, permisos);
        flash.addFlashAttribute("ok", "Rol guardado.");
        return "redirect:/roles";
    }

    @PostMapping("/{id}/duplicar")
    public String duplicar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        rolService.duplicar(s, id);
        flash.addFlashAttribute("ok", "Rol duplicado.");
        return "redirect:/roles";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        rolService.cambiarActivo(s, id);
        flash.addFlashAttribute("ok", "Estado del rol actualizado.");
        return "redirect:/roles";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        rolService.eliminar(s, id);
        flash.addFlashAttribute("ok", "Rol eliminado.");
        return "redirect:/roles";
    }
}
