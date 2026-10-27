package pe.mibodega.controller;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import pe.mibodega.model.Producto;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.ProductoService;
import pe.mibodega.util.NegocioException;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String listar(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) String texto,
            @RequestParam(required = false) Integer categoria, @RequestParam(required = false) String marca,
            @RequestParam(required = false) String estado, @RequestParam(required = false) String inventario, Model model) {
        model.addAttribute("productos", productoService.buscar(s, texto, categoria, marca, estado, inventario));
        model.addAttribute("categorias", productoService.categorias(s));
        model.addAttribute("marcas", productoService.marcas(s));
        model.addAttribute("texto", texto);
        model.addAttribute("categoria", categoria);
        model.addAttribute("marca", marca);
        model.addAttribute("estado", estado);
        model.addAttribute("inventario", inventario);
        return "productos/lista";
    }

    @GetMapping("/{id}")
    public String ver(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        model.addAttribute("producto", productoService.buscar(s, id));
        model.addAttribute("stockSedes", productoService.stockPorSede(s, id));
        return "productos/ver";
    }

    @GetMapping("/nuevo")
    public String nuevo(@SessionAttribute("sesion") UsuarioSesion s, Model model) {
        s.exigir("productos", "crear");
        Producto p = new Producto();
        p.setEmpaque("Caja");
        p.setUnidad("unidad");
        p.setUnidadesPorEmpaque(12);
        p.setStockMinimo(5);
        return formulario(s, p, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, Model model) {
        s.exigir("productos", "editar");
        return formulario(s, productoService.buscar(s, id), model);
    }

    private String formulario(UsuarioSesion s, Producto p, Model model) {
        model.addAttribute("producto", p);
        model.addAttribute("categorias", productoService.categoriasActivas(s));
        model.addAttribute("unidades", ProductoService.UNIDADES);
        model.addAttribute("empaques", ProductoService.EMPAQUES);
        return "productos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@SessionAttribute("sesion") UsuarioSesion s, @ModelAttribute Producto producto, Model model,
            RedirectAttributes flash) {
        try {
            productoService.guardar(s, producto);
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
            return formulario(s, producto, model);
        }
        flash.addFlashAttribute("ok", "Producto guardado.");
        return "redirect:/productos";
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        productoService.cambiarActivo(s, id);
        flash.addFlashAttribute("ok", "Estado del producto actualizado.");
        return "redirect:/productos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        flash.addFlashAttribute("ok", productoService.eliminar(s, id));
        return "redirect:/productos";
    }

    // ---------------- Categorias ----------------

    @GetMapping("/categorias")
    public String categorias(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) Integer editar, Model model) {
        s.exigir("productos", "categorias");
        model.addAttribute("categorias", productoService.categorias(s));
        model.addAttribute("editar", editar);
        return "productos/categorias";
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoria(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam(required = false) Integer id,
            @RequestParam String nombre, RedirectAttributes flash) {
        productoService.guardarCategoria(s, id, nombre);
        flash.addFlashAttribute("ok", "Categoría guardada.");
        return "redirect:/productos/categorias";
    }

    @PostMapping("/categorias/{id}/estado")
    public String estadoCategoria(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        productoService.cambiarActivoCategoria(s, id);
        flash.addFlashAttribute("ok", "Estado de la categoría actualizado.");
        return "redirect:/productos/categorias";
    }

    @PostMapping("/categorias/{id}/eliminar")
    public String eliminarCategoria(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id, RedirectAttributes flash) {
        productoService.eliminarCategoria(s, id);
        flash.addFlashAttribute("ok", "Categoría eliminada.");
        return "redirect:/productos/categorias";
    }

    // ---------------- Importar desde Excel ----------------

    @GetMapping("/importar")
    public String importar(@SessionAttribute("sesion") UsuarioSesion s) {
        s.exigir("productos", "crear");
        return "productos/importar";
    }

    @PostMapping("/importar")
    public String subirExcel(@SessionAttribute("sesion") UsuarioSesion s, @RequestParam("archivo") MultipartFile archivo,
            Model model) throws IOException {
        if (archivo.isEmpty()) {
            model.addAttribute("error", "Selecciona un archivo .xlsx");
            return "productos/importar";
        }
        model.addAttribute("resultado", productoService.importarExcel(s, archivo.getInputStream()));
        return "productos/importar";
    }

    @GetMapping("/plantilla")
    public ResponseEntity<byte[]> plantilla() {
        return Descargas.excel(productoService.plantillaExcel(), "plantilla_productos.xlsx");
    }
}
