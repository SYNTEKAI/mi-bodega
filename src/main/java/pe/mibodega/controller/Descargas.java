package pe.mibodega.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Respuestas para descargar archivos.
 */
public class Descargas {

    private Descargas() {
    }

    public static ResponseEntity<byte[]> excel(byte[] datos, String nombre) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(datos);
    }
}
