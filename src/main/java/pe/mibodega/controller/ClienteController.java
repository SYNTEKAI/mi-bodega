package pe.mibodega.controller;

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
import pe.mibodega.model.Cliente;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.ClienteService;
import pe.mibodega.util.NegocioException;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public String listar(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) String texto,
            @RequestParam(required = false) String tipo, Model model) {
        model.addAttribute("clientes", clienteService.buscar(s, texto, tipo));
        model.addAttribute("texto", texto);
        model.addAttribute("tipo", tipo);
        return "clientes/lista";
    }

    @GetMapping("/{id}")
    public String ficha(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        model.addAttribute("cliente", clienteService.buscar(s, id));
        model.addAttribute("frecuentes", clienteService.productosFrecuentes(id));
        model.addAttribute("historial", clienteService.historial(id));
        return "clientes/ficha";
    }

    @GetMapping("/nuevo")
    public String nuevo(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        s.exigir("clientes", "crear");
        Cliente c = new Cliente();
        c.setTipoDoc("DNI");
        model.addAttribute("cliente", c);
        return "clientes/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        s.exigir("clientes", "editar");
        model.addAttribute("cliente", clienteService.buscar(s, id));
        return "clientes/form";
    }

    @PostMapping("/guardar")
    public String guardar(@SessionAttribute("sesion") UsuarioSesion s, @ModelAttribute Cliente cliente, Model model,
            RedirectAttributes flash) {
        try {
            clienteService.guardar(s, cliente);
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cliente", cliente);
            return "clientes/form";
        }
        flash.addFlashAttribute("ok", "Cliente guardado.");
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        clienteService.eliminar(s, id);
        flash.addFlashAttribute("ok", "Cliente eliminado.");
        return "redirect:/clientes";
    }
}
