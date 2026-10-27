package pe.mibodega.util;

/**
 * Error de una regla del negocio (stock insuficiente, datos invalidos, etc.).
 * El mensaje se muestra al usuario.
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
