package pe.mibodega.config;

/**
 * Opcion del menu lateral y el permiso que necesita.
 */
public class ItemMenu {

    private final String url;
    private final String texto;
    private final String modulo;
    private final String accion;

    public ItemMenu(String url, String texto, String modulo, String accion) {
        this.url = url;
        this.texto = texto;
        this.modulo = modulo;
        this.accion = accion;
    }

    public String getUrl() {
        return url;
    }

    public String getTexto() {
        return texto;
    }

    public String getModulo() {
        return modulo;
    }

    public String getAccion() {
        return accion;
    }
}
