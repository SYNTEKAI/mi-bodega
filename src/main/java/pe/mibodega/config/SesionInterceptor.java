package pe.mibodega.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pe.mibodega.model.UsuarioSesion;

/**
 * Antes de abrir una pagina revisa:
 * 1) que exista sesion, 2) si debe cambiar la contrasena,
 * 3) que el rol activo tenga el permiso "ver" del modulo.
 */
@Component
public class SesionInterceptor implements HandlerInterceptor {

    // Ruta -> modulo que se necesita poder "ver"
    private static final Map<String, String> MODULOS_EMPRESA = new LinkedHashMap<>();
    private static final Map<String, String> MODULOS_PLATAFORMA = new LinkedHashMap<>();

    static {
        MODULOS_EMPRESA.put("/dashboard", "dashboard");
        MODULOS_EMPRESA.put("/ventas", "ventas");
        MODULOS_EMPRESA.put("/clientes", "clientes");
        MODULOS_EMPRESA.put("/productos", "productos");
        MODULOS_EMPRESA.put("/inventario", "inventario");
        MODULOS_EMPRESA.put("/proveedores", "proveedores");
        MODULOS_EMPRESA.put("/recepcion", "recepcion");
        MODULOS_EMPRESA.put("/guias", "recepcion");
        MODULOS_EMPRESA.put("/kardex", "kardex");
        MODULOS_EMPRESA.put("/reportes", "reportes");
        MODULOS_EMPRESA.put("/sedes", "sedes");
        MODULOS_EMPRESA.put("/usuarios", "usuarios");
        MODULOS_EMPRESA.put("/roles", "roles");
        MODULOS_EMPRESA.put("/comprobantes", "comprobantes");
        MODULOS_EMPRESA.put("/auditoria", "auditoria");

        MODULOS_PLATAFORMA.put("/plataforma/dashboard", "dashboard_global");
        MODULOS_PLATAFORMA.put("/plataforma/empresas", "empresas");
        MODULOS_PLATAFORMA.put("/plataforma/usuarios", "usuarios_sistema");
        MODULOS_PLATAFORMA.put("/plataforma/roles", "roles_sistema");
        MODULOS_PLATAFORMA.put("/plataforma/auditoria", "auditoria_global");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        UsuarioSesion sesion = (UsuarioSesion) session.getAttribute("sesion");
        String ruta = request.getRequestURI().substring(request.getContextPath().length());

        if (sesion == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (sesion.isDebeCambiarClave() && !ruta.startsWith("/cambiar-clave") && !ruta.startsWith("/logout")) {
            response.sendRedirect(request.getContextPath() + "/cambiar-clave");
            return false;
        }

        // Paginas de la plataforma: solo usuarios internos
        if (ruta.startsWith("/plataforma")) {
            if (!sesion.isInterno()) {
                return sinAcceso(request, response);
            }
            if (sesion.isEnEmpresa()) {
                sesion.salirDeEmpresa();
            }
            String modulo = buscarModulo(MODULOS_PLATAFORMA, ruta);
            if (modulo != null && !sesion.puede(modulo, "ver")) {
                return sinAcceso(request, response);
            }
            return true;
        }

        // Paginas de una empresa
        String modulo = buscarModulo(MODULOS_EMPRESA, ruta);
        if (modulo != null) {
            if (!sesion.isEnEmpresa()) {
                response.sendRedirect(request.getContextPath() + "/plataforma/empresas");
                return false;
            }
            if (!sesion.puede(modulo, "ver")) {
                return sinAcceso(request, response);
            }
        }
        return true;
    }

    private String buscarModulo(Map<String, String> mapa, String ruta) {
        for (Map.Entry<String, String> e : mapa.entrySet()) {
            if (ruta.equals(e.getKey()) || ruta.startsWith(e.getKey() + "/")) {
                return e.getValue();
            }
        }
        return null;
    }

    private boolean sinAcceso(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.sendRedirect(request.getContextPath() + "/sin-acceso");
        return false;
    }
}
