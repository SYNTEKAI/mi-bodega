package pe.mibodega.model;

/**
 * Estado del inventario de un producto. No se guarda en la BD, se calcula con el stock.
 */
public enum EstadoStock {

    DISPONIBLE("Disponible", "verde"),
    STOCK_BAJO("Stock bajo", "ambar"),
    SIN_STOCK("Sin stock", "roja");

    private final String texto;
    private final String color;

    EstadoStock(String texto, String color) {
        this.texto = texto;
        this.color = color;
    }

    public static EstadoStock calcular(int stock, int minimo) {
        if (stock <= 0) {
            return SIN_STOCK;
        }
        if (stock <= minimo) {
            return STOCK_BAJO;
        }
        return DISPONIBLE;
    }

    public String getTexto() {
        return texto;
    }

    public String getColor() {
        return color;
    }
}
