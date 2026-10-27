package pe.mibodega.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Formatos usados en las vistas. En Thymeleaf se usa como ${@fmt.soles(valor)}
 */
@Component("fmt")
public class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    public String soles(BigDecimal monto) {
        DecimalFormat df = new DecimalFormat("#,##0.00", new DecimalFormatSymbols(Locale.US));
        return "S/ " + df.format(monto == null ? BigDecimal.ZERO : monto);
    }

    public String soles(Number monto) {
        return soles(monto == null ? BigDecimal.ZERO : new BigDecimal(monto.toString()));
    }

    public String fecha(LocalDate fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }

    public String fecha(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FECHA);
    }

    public String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FECHA_HORA);
    }

    public String hora(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(HORA);
    }
}
