package pe.mibodega.dao;

import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Numeracion propia de cada empresa: GR-000001 (recepciones) y B001-000001 (boletas).
 */
@Repository
public class CorrelativoDAO extends BaseDAO {

    /**
     * Reserva el siguiente numero. Se llama dentro de una transaccion:
     * el SELECT ... FOR UPDATE bloquea la fila y dos usuarios nunca obtienen el mismo numero.
     */
    public int siguiente(int idEmpresa, String serie) {
        List<Integer> filas = jdbc.queryForList("SELECT ultimo FROM correlativos WHERE id_empresa = ? AND serie = ? FOR UPDATE",
                Integer.class, idEmpresa, serie);
        if (filas.isEmpty()) {
            jdbc.update("INSERT INTO correlativos (id_empresa, serie, ultimo) VALUES (?, ?, 1)", idEmpresa, serie);
            return 1;
        }
        int nuevo = filas.get(0) + 1;
        jdbc.update("UPDATE correlativos SET ultimo = ? WHERE id_empresa = ? AND serie = ?", nuevo, idEmpresa, serie);
        return nuevo;
    }

    /** Solo muestra el proximo numero (no lo reserva). */
    public int proximo(int idEmpresa, String serie) {
        List<Integer> filas = jdbc.queryForList("SELECT ultimo FROM correlativos WHERE id_empresa = ? AND serie = ?",
                Integer.class, idEmpresa, serie);
        return filas.isEmpty() ? 1 : filas.get(0) + 1;
    }

    public static String formatear(String serie, int numero) {
        return serie + "-" + String.format("%06d", numero);
    }
}
