package pe.mibodega.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClaveUtilTest {

    @Test
    void claveValida() {
        assertNull(ClaveUtil.validar("bodega2026", "bodega2026"));
    }

    @Test
    void claveCortaSinNumerosONoCoincide() {
        assertNotNull(ClaveUtil.validar("abc1", "abc1"));
        assertNotNull(ClaveUtil.validar("soloLetras", "soloLetras"));
        assertNotNull(ClaveUtil.validar("bodega2026", "bodega2027"));
    }

    @Test
    void claveTemporalTieneFormato() {
        assertTrue(ClaveUtil.generarTemporal().matches("MB-[A-Z]{4}\\d{2}"));
    }

    @Test
    void validaDniYRuc() {
        Validar.documento("DNI", "45871236");
        Validar.documento("RUC", "20601234567");
        assertThrows(NegocioException.class, () -> Validar.documento("DNI", "1234"));
        assertThrows(NegocioException.class, () -> Validar.documento("RUC", "2060123456"));
    }
}
