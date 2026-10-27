package pe.mibodega.util;

import com.google.common.base.Strings;

/**
 * Validaciones comunes de los formularios.
 */
public class Validar {

    private Validar() {
    }

    public static void requerido(String valor, String campo) {
        if (Strings.isNullOrEmpty(valor) || valor.isBlank()) {
            throw new NegocioException("El campo " + campo + " es obligatorio.");
        }
    }

    /** DNI = 8 digitos, RUC = 11 digitos */
    public static void documento(String tipo, String numero) {
        if ("RUC".equals(tipo) && (numero == null || !numero.matches("\\d{11}"))) {
            throw new NegocioException("El RUC debe tener 11 dígitos.");
        }
        if ("DNI".equals(tipo) && (numero == null || !numero.matches("\\d{8}"))) {
            throw new NegocioException("El DNI debe tener 8 dígitos.");
        }
    }

    public static void ruc(String ruc) {
        documento("RUC", ruc);
    }
}
