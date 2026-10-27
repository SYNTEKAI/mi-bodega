package pe.mibodega.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.dao.SedeDAO;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.SinPermisoException;

/**
 * Datos comunes para todas las vistas (usuario, menu, sedes)
 * y manejo de errores de negocio.
 */
@ControllerAdvice
public class ControladorGlobal {

    private static final Logger log = LoggerFactory.getLogger(ControladorGlobal.class);

    private static final List<ItemMenu> MENU_EMPRESA = List.of(
            new ItemMenu("/dashboard", "Dashboard", "dashboard", "ver"),
            new ItemMenu("/ventas", "Ventas (POS)", "ventas", "ver"),
            new ItemMenu("/clientes", "Clientes", "clientes", "ver"),
            new ItemMenu("/productos", "Productos", "productos", "ver"),
            new ItemMenu("/inventario", "Inventario / Lotes", "inventario", "ver"),
            new ItemMenu("/proveedores", "Proveedores", "proveedores", "ver"),
            new ItemMenu("/recepcion", "Recepción", "recepcion", "crear"),
            new ItemMenu("/guias", "Guías recibidas", "recepcion", "ver"),
            new ItemMenu("/kardex", "Kardex", "kardex", "ver"),
            new ItemMenu("/reportes", "Reportes", "reportes", "ver"),
            new ItemMenu("/sedes", "Sedes", "sedes", "ver"),
            new ItemMenu("/usuarios", "Usuarios", "usuarios", "ver"),
            new ItemMenu("/roles", "Roles y permisos", "roles", "ver"),
            new ItemMenu("/comprobantes", "Comprobantes", "comprobantes", "ver"),
            new ItemMenu("/auditoria", "Auditoría", "auditoria", "ver"));

    private static final List<ItemMenu> MENU_PLATAFORMA = List.of(
            new ItemMenu("/plataforma/dashboard", "Dashboard global", "dashboard_global", "ver"),
            new ItemMenu("/plataforma/empresas", "Empresas", "empresas", "ver"),
            new ItemMenu("/plataforma/usuarios", "Usuarios del sistema", "usuarios_sistema", "ver"),
            new ItemMenu("/plataforma/roles", "Roles internos", "roles_sistema", "ver"),
            new ItemMenu("/plataforma/auditoria", "Auditoría global", "auditoria_global", "ver"));

    @Autowired
    private SedeDAO sedeDAO;

    @ModelAttribute
    public void datosComunes(HttpSession session, HttpServletRequest request, Model model) {
        UsuarioSesion sesion = (UsuarioSesion) session.getAttribute("sesion");
        if (sesion == null) {
            return;
        }
        model.addAttribute("sesion", sesion);
        model.addAttribute("rutaActual", request.getRequestURI());
        List<ItemMenu> menu = new ArrayList<>();
        for (ItemMenu item : sesion.isEnEmpresa() ? MENU_EMPRESA : MENU_PLATAFORMA) {
            if (sesion.puede(item.getModulo(), item.getAccion())) {
                menu.add(item);
            }
        }
        model.addAttribute("menu", menu);
        if (sesion.isEnEmpresa()) {
            model.addAttribute("sedesActivas", sedeDAO.listarActivas(sesion.getIdEmpresa()));
        }
    }

    // Errores de negocio: se vuelve a la pagina anterior con el mensaje
    @ExceptionHandler(NegocioException.class)
    public String errorNegocio(NegocioException e, HttpServletRequest request, RedirectAttributes flash) {
        flash.addFlashAttribute("error", e.getMessage());
        String anterior = request.getHeader("Referer");
        return "redirect:" + (anterior != null ? anterior : "/");
    }

    @ExceptionHandler(SinPermisoException.class)
    public String errorPermiso(SinPermisoException e, RedirectAttributes flash) {
        log.warn(e.getMessage());
        flash.addFlashAttribute("mensaje", e.getMessage());
        return "redirect:/sin-acceso";
    }
}
