package pe.mibodega.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.InventarioService;

@Controller
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @GetMapping("/inventario")
    public String inventario(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        model.addAttribute("lotes", inventarioService.lotesConStock(s));
        return "inventario";
    }

    @PostMapping("/inventario/ajuste")
    public String ajustar(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam int idLote,
            @RequestParam(required = false) Integer cantidad, @RequestParam(required = false) String motivo, RedirectAttributes flash) {
        inventarioService.ajustar(s, idLote, cantidad, motivo);
        flash.addFlashAttribute("ok", "Ajuste aplicado y registrado en el kardex.");
        return "redirect:/inventario";
    }
}
