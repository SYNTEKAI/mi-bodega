package pe.mibodega.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.model.ConfigComprobante;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.ComprobanteService;
import pe.mibodega.service.EmpresaService;

@Controller
public class ComprobanteController {

    @Autowired
    private ComprobanteService comprobanteService;
    @Autowired
    private EmpresaService empresaService;

    @GetMapping("/comprobantes")
    public String ver(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        model.addAttribute("config", comprobanteService.config(s.getIdEmpresa()));
        model.addAttribute("venta", comprobanteService.ventaDeEjemplo(s));
        model.addAttribute("empresa", empresaService.buscar(s.getIdEmpresa()));
        model.addAttribute("plantillas", ComprobanteService.PLANTILLAS);
        return "comprobantes";
    }

    /** Boleta con el diseno del formulario, sin guardarlo (vista previa al instante). */
    @PostMapping("/comprobantes/vista-previa")
    public String vistaPrevia(@SessionAttribute("sesion") UsuarioSesion s, @ModelAttribute ConfigComprobante config, Model model) {
        config.setIdEmpresa(s.getIdEmpresa());
        model.addAttribute("config", config);
        model.addAttribute("venta", comprobanteService.ventaDeEjemplo(s));
        model.addAttribute("empresa", empresaService.buscar(s.getIdEmpresa()));
        return "comprobantes :: vistaPrevia";
    }

    @PostMapping("/comprobantes")
    public String guardar(@SessionAttribute("sesion") UsuarioSesion s, @ModelAttribute ConfigComprobante config, RedirectAttributes flash) {
        comprobanteService.guardar(s, config);
        flash.addFlashAttribute("ok", "Diseño de boleta guardado: el POS ya lo usa.");
        return "redirect:/comprobantes";
    }
}
