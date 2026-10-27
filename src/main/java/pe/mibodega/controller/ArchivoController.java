package pe.mibodega.controller;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.service.EmpresaService;

/**
 * Entrega el logo de la empresa guardado en la base de datos.
 */
@Controller
public class ArchivoController {

    @Autowired
    private EmpresaService empresaService;

    @GetMapping("/archivos/empresa/{id}/logo")
    public ResponseEntity<byte[]> logo(@SessionAttribute("sesion") UsuarioSesion s, @PathVariable int id) {
        if (!s.isInterno() && (s.getIdEmpresa() == null || s.getIdEmpresa() != id)) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> logo = empresaService.logo(id);
        if (logo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.parseMediaType((String) logo.get("logo_tipo")))
                .body((byte[]) logo.get("logo"));
    }
}
