package pe.mibodega.controller;

import java.util.List;
import java.util.stream.Collectors;
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
import pe.mibodega.model.Rol;
import pe.mibodega.model.Usuario;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.RolService;
import pe.mibodega.service.UsuarioService;
import pe.mibodega.util.NegocioException;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private RolService rolService;

    @GetMapping
    public String listar(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        model.addAttribute("usuarios", usuarioService.listar(s));
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        s.exigir("usuarios", "crear");
        return formulario(s, new Usuario(), List.of(), model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        s.exigir("usuarios", "editar");
        List<Integer> asignados = usuarioService.rolesDe(id).stream().map(Rol::getIdRol).collect(Collectors.toList());
        return formulario(s, usuarioService.buscar(s, id), asignados, model);
    }

    private String formulario(UsuarioSesion s, Usuario u, List<Integer> asignados, Model model) {
        model.addAttribute("usuario", u);
        model.addAttribute("asignados", asignados);
        model.addAttribute("roles", rolService.activos(s));
        return "usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@SessionAttribute("sesion") UsuarioSesion s, @ModelAttribute Usuario usuario,
            @RequestParam(name = "roles", required = false) List<Integer> roles, @RequestParam(required = false) String clave,
            @RequestParam(defaultValue = "false") boolean forzarCambio, Model model, RedirectAttributes flash) {
        try {
            usuarioService.guardar(s, usuario, roles, clave, forzarCambio);
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(s, usuario, roles == null ? List.of() : roles, model);
        }
        flash.addFlashAttribute("ok", "Usuario guardado.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/restablecer")
    public String restablecer(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id,
            @RequestParam(defaultValue = "false") boolean forzarCambio, Model model) {
        Usuario u = usuarioService.buscar(s, id);
        model.addAttribute("nombre", u.getNombre());
        model.addAttribute("temporal", usuarioService.restablecerClave(s, id, forzarCambio));
        model.addAttribute("forzado", forzarCambio);
        model.addAttribute("volver", "/usuarios");
        return "clave-temporal";
    }

    @PostMapping("/{id}/forzar")
    public String forzar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        usuarioService.forzarCambio(s, id);
        flash.addFlashAttribute("ok", "El usuario deberá cambiar su contraseña en el próximo ingreso.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        usuarioService.cambiarEstado(s, id);
        flash.addFlashAttribute("ok", "Estado del usuario actualizado.");
        return "redirect:/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        flash.addFlashAttribute("ok", usuarioService.eliminar(s, id));
        return "redirect:/usuarios";
    }
}
