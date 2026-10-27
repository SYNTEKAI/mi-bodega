package pe.mibodega.util;

/**
 * Se lanza cuando el rol activo no tiene el permiso necesario.
 */
public class SinPermisoException extends RuntimeException {

    public SinPermisoException(String permiso) {
        super("Tu rol no tiene el permiso: " + permiso);
    }
}
