package pe.mibodega.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.AuthService;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private AuthService authService;

    @GetMapping
    public String ver() {
        return "perfil";
    }

    @PostMapping("/rol")
    public String cambiarRol(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam int idRol, RedirectAttributes flash) {
        authService.cambiarRolActivo(s, idRol);
        flash.addFlashAttribute("ok", "Ahora trabajas como " + s.getRolNombre() + ".");
        return "redirect:/";
    }

    @PostMapping("/clave")
    public String cambiarClave(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam String actual,
            @RequestParam String nueva, @RequestParam String confirmacion, RedirectAttributes flash) {
        authService.cambiarClave(s, actual, nueva, confirmacion);
        flash.addFlashAttribute("ok", "Contraseña actualizada.");
        return "redirect:/perfil";
    }

    @PostMapping("/tema")
    public String cambiarTema(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam String valor,
            @RequestParam(defaultValue = "/perfil") String volver) {
        authService.cambiarTema(s, valor);
        return "redirect:" + (volver.startsWith("/") ? volver : "/perfil");
    }
}
