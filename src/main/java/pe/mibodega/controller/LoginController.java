package pe.mibodega.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.AuditoriaService;
import pe.mibodega.service.AuthService;
import pe.mibodega.util.NegocioException;

@Controller
public class LoginController {

    @Autowired
    private AuthService authService;
    @Autowired
    private AuditoriaService auditoria;

    @GetMapping("/login")
    public String login(HttpSession session) {
        if (session.getAttribute("sesion") != null) {
            return "redirect:/";
        }
        return "login";
    }

    @PostMapping("/login")
    public String ingresar(@RequestParam String usuario, @RequestParam String clave, HttpSession session, Model model) {
        try {
            UsuarioSesion s = authService.login(usuario.trim(), clave);
            session.setAttribute("sesion", s);
            return "redirect:/";
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuario);
            return "login";
        }
    }

    /** Pagina de inicio segun el tipo de usuario y sus permisos. */
    @GetMapping("/")
    public String inicio(@SessionAttribute("sesion") UsuarioSesion s) {
        if (!s.isEnEmpresa()) {
            return s.puede("dashboard_global", "ver") ? "redirect:/plataforma/dashboard" : "redirect:/plataforma/empresas";
        }
        if (s.puede("dashboard", "ver")) {
            return "redirect:/dashboard";
        }
        return s.puede("ventas", "ver") ? "redirect:/ventas" : "redirect:/perfil";
    }

    @PostMapping("/logout")
    public String salir(HttpSession session) {
        UsuarioSesion s = (UsuarioSesion) session.getAttribute("sesion");
        if (s != null) {
            auditoria.registrar(s, "Sistema", "LOGOUT", "Cerró sesión");
        }
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/cambiar-clave")
    public String cambiarClave() {
        return "cambiar-clave";
    }

    @PostMapping("/cambiar-clave")
    public String guardarClave(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam String nueva,
            @RequestParam String confirmacion, Model model) {
        try {
            authService.cambiarClave(s, null, nueva, confirmacion);
            return "redirect:/";
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            return "cambiar-clave";
        }
    }

    @GetMapping("/sin-acceso")
    public String sinAcceso() {
        return "sin-acceso";
    }

    /** El administrador cambia la sede con la que trabaja (selector de la barra superior). */
    @PostMapping("/sede-vista")
    public String cambiarSede(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam int idSede,
            @RequestParam(defaultValue = "/") String volver) {
        authService.cambiarSedeVista(s, idSede);
        return "redirect:" + (volver.startsWith("/") ? volver : "/");
    }
}
