package pe.mibodega.util;

import java.security.SecureRandom;
import org.apache.commons.lang3.StringUtils;

/**
 * Reglas de las contrasenas.
 */
public class ClaveUtil {

    private static final String LETRAS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private ClaveUtil() {
    }

    /** Devuelve el error encontrado o null si la clave es valida. */
    public static String validar(String nueva, String confirmacion) {
        if (StringUtils.length(nueva) < 8) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (!nueva.matches(".*[0-9].*") || !nueva.matches(".*[a-zA-Z].*")) {
            return "La contraseña debe combinar letras y números.";
        }
        if (!nueva.equals(confirmacion)) {
            return "La confirmación no coincide.";
        }
        return null;
    }

    /** Clave temporal del tipo MB-ABCD12 */
    public static String generarTemporal() {
        StringBuilder sb = new StringBuilder("MB-");
        for (int i = 0; i < 4; i++) {
            sb.append(LETRAS.charAt(RANDOM.nextInt(LETRAS.length())));
        }
        sb.append(10 + RANDOM.nextInt(90));
        return sb.toString();
    }
}
