package pe.mibodega.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.model.Empresa;
import pe.mibodega.model.RolInterno;
import pe.mibodega.model.UsuarioInterno;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.AuthService;
import pe.mibodega.service.DashboardService;
import pe.mibodega.service.EmpresaService;
import pe.mibodega.service.RolService;
import pe.mibodega.service.UsuarioInternoService;
import pe.mibodega.util.NegocioException;

/**
 * Panel de la plataforma (equipo interno): empresas, usuarios internos y roles internos.
 */
@Controller
@RequestMapping("/plataforma")
public class PlataformaController {

    @Autowired
    private DashboardService dashboardService;
    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private UsuarioInternoService usuarioInternoService;
    @Autowired
    private RolService rolService;
    @Autowired
    private AuthService authService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("t", dashboardService.totalesPlataforma());
        model.addAttribute("ventasDia", dashboardService.ventasPlataforma());
        model.addAttribute("ranking", dashboardService.actividadEmpresas());
        model.addAttribute("actividad", dashboardService.actividadReciente());
        model.addAttribute("empresas", empresaService.listar());
        return "plataforma/dashboard";
    }

    // ---------------- Empresas ----------------

    @GetMapping("/empresas")
    public String empresas(Model model) {
        model.addAttribute("empresas", empresaService.listar());
        model.addAttribute("estados", EmpresaService.ESTADOS);
        return "plataforma/empresas";
    }

    @GetMapping("/empresas/nueva")
    public String nuevaEmpresa(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        s.exigir("empresas", "crear");
        Empresa e = new Empresa();
        e.setPlan("Estándar");
        model.addAttribute("empresa", e);
        model.addAttribute("planes", EmpresaService.PLANES);
        return "plataforma/empresa-form";
    }

    @GetMapping("/empresas/{id}/editar")
    public String editarEmpresa(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        s.exigir("empresas", "editar");
        model.addAttribute("empresa", empresaService.buscar(id));
        model.addAttribute("planes", EmpresaService.PLANES);
        return "plataforma/empresa-form";
    }

    @PostMapping("/empresas/guardar")
    public String guardarEmpresa(@SessionAttribute("sesion") UsuarioSesion s, @ModelAttribute Empresa empresa,
            @RequestParam(name = "archivoLogo", required = false) MultipartFile logo, Model model, RedirectAttributes flash) {
        try {
            empresaService.guardar(s, empresa, logo);
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("empresa", empresa);
            model.addAttribute("planes", EmpresaService.PLANES);
            return "plataforma/empresa-form";
        }
        flash.addFlashAttribute("ok", "Empresa guardada.");
        return "redirect:/plataforma/empresas";
    }

    @PostMapping("/empresas/{id}/quitar-logo")
    public String quitarLogo(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        empresaService.quitarLogo(s, id);
        flash.addFlashAttribute("ok", "Logo eliminado.");
        return "redirect:/plataforma/empresas/" + id + "/editar";
    }

    @PostMapping("/empresas/{id}/estado")
    public String estadoEmpresa(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, @RequestParam String estado,
            RedirectAttributes flash) {
        empresaService.cambiarEstado(s, id, estado);
        flash.addFlashAttribute("ok", "Estado actualizado.");
        return "redirect:/plataforma/empresas";
    }

    /** Entrar al entorno de una empresa (modo empresa). */
    @PostMapping("/empresas/{id}/entrar")
    public String entrar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id) {
        authService.entrarEmpresa(s, id);
        return "redirect:/dashboard";
    }

    // ---------------- Usuarios internos ----------------

    @GetMapping("/usuarios")
    public String usuarios(Model model) {
        model.addAttribute("usuarios", usuarioInternoService.listar());
        return "plataforma/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        s.exigir("usuarios_sistema", "crear");
        model.addAttribute("usuario", new UsuarioInterno());
        model.addAttribute("roles", rolService.rolesInternos());
        return "plataforma/usuario-form";
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuario(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        s.exigir("usuarios_sistema", "editar");
        model.addAttribute("usuario", usuarioInternoService.buscar(id));
        model.addAttribute("roles", rolService.rolesInternos());
        return "plataforma/usuario-form";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@SessionAttribute("sesion") UsuarioSesion s, @ModelAttribute UsuarioInterno usuario,
            @RequestParam(required = false) String clave, Model model, RedirectAttributes flash) {
        try {
            usuarioInternoService.guardar(s, usuario, clave);
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("usuario", usuario);
            model.addAttribute("roles", rolService.rolesInternos());
            return "plataforma/usuario-form";
        }
        flash.addFlashAttribute("ok", "Usuario guardado.");
        return "redirect:/plataforma/usuarios";
    }

    @PostMapping("/usuarios/{id}/estado")
    public String estadoUsuario(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        usuarioInternoService.cambiarEstado(s, id);
        flash.addFlashAttribute("ok", "Estado actualizado.");
        return "redirect:/plataforma/usuarios";
    }

    @PostMapping("/usuarios/{id}/restablecer")
    public String restablecer(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        UsuarioInterno u = usuarioInternoService.buscar(id);
        model.addAttribute("nombre", u.getNombre());
        model.addAttribute("temporal", usuarioInternoService.restablecerClave(s, id));
        model.addAttribute("forzado", true);
        model.addAttribute("volver", "/plataforma/usuarios");
        return "clave-temporal";
    }

    // ---------------- Roles internos (solo lectura) ----------------

    @GetMapping("/roles")
    public String roles(Model model) {
        List<RolInterno> roles = rolService.rolesInternos();
        Map<Integer, Set<String>> permisos = new HashMap<>();
        for (RolInterno r : roles) {
            permisos.put(r.getIdRolInterno(), rolService.permisosDeRolInterno(r.getIdRolInterno()));
        }
        model.addAttribute("roles", roles);
        model.addAttribute("permisosRol", permisos);
        model.addAttribute("modulos", rolService.permisosPorModulo("PLATAFORMA"));
        return "plataforma/roles";
    }

    @GetMapping("/roles/nuevo")
    public String nuevoRol(Model model) {
        model.addAttribute("rol", new RolInterno());
        model.addAttribute("marcados", new java.util.HashSet<String>());
        model.addAttribute("modulos", rolService.permisosPorModulo("PLATAFORMA"));
        model.addAttribute("nombres", RolService.NOMBRES_MODULO);
        return "plataforma/rol-form";
    }

    @GetMapping("/roles/{id}/editar")
    public String editarRol(@PathVariable int id, Model model) {
        model.addAttribute("rol", rolService.buscarInterno(id));
        model.addAttribute("marcados", rolService.permisosDeRolInterno(id));
        model.addAttribute("modulos", rolService.permisosPorModulo("PLATAFORMA"));
        model.addAttribute("nombres", RolService.NOMBRES_MODULO);
        return "plataforma/rol-form";
    }

    @PostMapping("/roles/guardar")
    public String guardarRol(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) Integer idRolInterno,
            @RequestParam String nombre, @RequestParam(name = "permisos", required = false) List<Integer> permisos,
            RedirectAttributes flash) {
        rolService.guardarInterno(s, idRolInterno, nombre, permisos);
        flash.addFlashAttribute("ok", "Rol interno guardado.");
        return "redirect:/plataforma/roles";
    }

    @PostMapping("/roles/{id}/eliminar")
    public String eliminarRol(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        rolService.eliminarInterno(s, id);
        flash.addFlashAttribute("ok", "Rol interno eliminado.");
        return "redirect:/plataforma/roles";
    }
}
