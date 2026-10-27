package pe.mibodega.controller;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import pe.mibodega.dao.AuditoriaDAO;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.EmpresaService;
import pe.mibodega.service.ReporteService;

/**
 * Auditoria de la empresa y auditoria global de la plataforma.
 */
@Controller
public class AuditoriaController {

    @Autowired
    private ReporteService reporteService;
    @Autowired
    private EmpresaService empresaService;

    @GetMapping("/auditoria")
    public String empresa(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) Integer sede,
            @RequestParam(required = false) String usuario, @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String texto, @RequestParam(defaultValue = "1") int pagina, Model model) {
        cargar(model, s.getIdEmpresa(), sede, usuario, modulo, accion, desde, hasta, texto, pagina);
        model.addAttribute("global", false);
        return "auditoria";
    }

    @GetMapping("/plataforma/auditoria")
    public String global(@RequestParam(required = false) Integer empresa, @RequestParam(required = false) String usuario,
            @RequestParam(required = false) String modulo, @RequestParam(required = false) String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String texto, @RequestParam(defaultValue = "1") int pagina, Model model) {
        cargar(model, empresa, null, usuario, modulo, accion, desde, hasta, texto, pagina);
        model.addAttribute("global", true);
        model.addAttribute("empresas", empresaService.listar());
        model.addAttribute("empresa", empresa);
        return "auditoria";
    }

    private void cargar(Model model, Integer empresa, Integer sede, String usuario, String modulo, String accion,
            LocalDate desde, LocalDate hasta, String texto, int pagina) {
        int total = reporteService.contarAuditoria(empresa, sede, usuario, modulo, accion, desde, hasta, texto);
        int totalPaginas = Math.max(1, (int) Math.ceil(total / (double) AuditoriaDAO.FILAS_POR_PAGINA));
        pagina = Math.min(Math.max(pagina, 1), totalPaginas);
        Integer filtroListas = empresa != null && empresa == 0 ? null : empresa;
        model.addAttribute("registros", reporteService.auditoria(empresa, sede, usuario, modulo, accion, desde, hasta, texto, pagina));
        model.addAttribute("total", total);
        model.addAttribute("pagina", pagina);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("usuarios", reporteService.usuariosAuditoria(filtroListas));
        model.addAttribute("modulos", reporteService.modulosAuditoria(filtroListas));
        model.addAttribute("acciones", reporteService.accionesAuditoria(filtroListas));
        model.addAttribute("sede", sede);
        model.addAttribute("usuario", usuario);
        model.addAttribute("modulo", modulo);
        model.addAttribute("accion", accion);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("texto", texto);
    }
}
