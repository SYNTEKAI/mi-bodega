package pe.mibodega.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.mibodega.dao.CorrelativoDAO;
import pe.mibodega.dao.KardexDAO;
import pe.mibodega.dao.LoteDAO;
import pe.mibodega.dao.ProductoDAO;
import pe.mibodega.dao.ProveedorDAO;
import pe.mibodega.dao.RecepcionDAO;
import pe.mibodega.model.Lote;
import pe.mibodega.model.Producto;
import pe.mibodega.model.Proveedor;
import pe.mibodega.model.Recepcion;
import pe.mibodega.model.RecepcionDetalle;
import pe.mibodega.model.RecepcionModificacion;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.NegocioException;

/**
 * Recepcion de mercaderia.
 * Estados: BORRADOR (no afecta stock) -> CONFIRMADA (crea lotes y kardex)
 * -> MODIFICADA (corregida con motivo) / ANULADA.
 */
@Service
public class RecepcionService {

    @Autowired
    private RecepcionDAO recepcionDAO;
    @Autowired
    private LoteDAO loteDAO;
    @Autowired
    private KardexDAO kardexDAO;
    @Autowired
    private CorrelativoDAO correlativoDAO;
    @Autowired
    private ProveedorDAO proveedorDAO;
    @Autowired
    private ProductoDAO productoDAO;
    @Autowired
    private AuditoriaService auditoria;

    /** Cambio de stock que produce una correccion. */
    public static class Ajuste {
        private final Integer idLote;
        private final int idProducto;
        private final String producto;
        private final int antes;
        private final int despues;

        public Ajuste(Integer idLote, int idProducto, String producto, int antes, int despues) {
            this.idLote = idLote;
            this.idProducto = idProducto;
            this.producto = producto;
            this.antes = antes;
            this.despues = despues;
        }

        public Integer getIdLote() {
            return idLote;
        }

        public int getIdProducto() {
            return idProducto;
        }

        public String getProducto() {
            return producto;
        }

        public int getAntes() {
            return antes;
        }

        public int getDespues() {
            return despues;
        }

        public int getDiferencia() {
            return despues - antes;
        }
    }

    /**
     * Compara las lineas originales con las corregidas (se emparejan por lote).
     * Una linea quitada pasa a 0 y una linea nueva (sin lote) entra completa.
     */
    public static List<Ajuste> calcularAjustes(List<RecepcionDetalle> antes, List<RecepcionDetalle> despues) {
        List<Ajuste> ajustes = new ArrayList<>();
        for (RecepcionDetalle original : antes) {
            RecepcionDetalle nueva = despues.stream()
                    .filter(d -> d.getIdLote() != null && d.getIdLote().equals(original.getIdLote()))
                    .findFirst().orElse(null);
            int cantidadNueva = nueva == null ? 0 : nueva.getTotalUnidades();
            if (cantidadNueva != original.getTotalUnidades()) {
                ajustes.add(new Ajuste(original.getIdLote(), original.getIdProducto(), original.getProductoNombre(),
                        original.getTotalUnidades(), cantidadNueva));
            }
        }
        for (RecepcionDetalle d : despues) {
            if (d.getIdLote() == null) {
                ajustes.add(new Ajuste(null, d.getIdProducto(), d.getProductoNombre(), 0, d.getTotalUnidades()));
            }
        }
        return ajustes;
    }

    public Recepcion buscar(UsuarioSesion s, int idRecepcion) {
        Recepcion r = recepcionDAO.buscar(idRecepcion, s.getIdEmpresa());
        if (r == null) {
            throw new NegocioException("La recepción no existe.");
        }
        return r;
    }

    public String proximoNumero(UsuarioSesion s) {
        return CorrelativoDAO.formatear("GR", correlativoDAO.proximo(s.getIdEmpresa(), "GR"));
    }

    /** Valida cabecera y lineas, y calcula el total en unidades de cada linea. */
    private void validar(UsuarioSesion s, Recepcion cab, List<RecepcionDetalle> lineas) {
        cab.setGuiaProveedor(StringUtils.upperCase(StringUtils.trimToNull(cab.getGuiaProveedor())));
        if (cab.getGuiaProveedor() == null) {
            throw new NegocioException("Ingresa el N° de guía del proveedor.");
        }
        Proveedor prov = cab.getIdProveedor() == null ? null : proveedorDAO.buscar(cab.getIdProveedor(), s.getIdEmpresa());
        if (prov == null) {
            throw new NegocioException("Selecciona el proveedor.");
        }
        if (lineas.isEmpty()) {
            throw new NegocioException("Agrega al menos un producto.");
        }
        if (recepcionDAO.existeGuia(s.getIdEmpresa(), prov.getIdProveedor(), cab.getGuiaProveedor(), cab.getIdRecepcion())) {
            throw new NegocioException("La guía " + cab.getGuiaProveedor() + " de este proveedor ya está registrada.");
        }
        for (RecepcionDetalle d : lineas) {
            Producto p = productoDAO.buscar(d.getIdProducto(), s.getIdEmpresa());
            if (p == null) {
                throw new NegocioException("Producto no válido.");
            }
            if (d.getContenido() <= 0 || d.getCantidadEmpaques() <= 0) {
                throw new NegocioException(p.getNombre() + ": la cantidad y las unidades por empaque deben ser mayores a 0.");
            }
            d.setProductoNombre(p.getNombre());
            d.setTotalUnidades(d.getContenido() * d.getCantidadEmpaques());
            if (d.getCostoUnitario() == null) {
                d.setCostoUnitario(BigDecimal.ZERO);
            }
        }
    }

    /**
     * Guarda una recepcion nueva o un borrador.
     * confirmar = false: queda como BORRADOR (no toca el stock).
     * confirmar = true: crea un lote por linea y registra la ENTRADA en el kardex.
     */
    @Transactional
    public int guardar(UsuarioSesion s, Recepcion cab, List<RecepcionDetalle> lineas, boolean confirmar) {
        s.exigir("recepcion", "crear");
        validar(s, cab, lineas);
        Recepcion r;
        if (cab.getIdRecepcion() == null) {
            r = cab;
            r.setIdEmpresa(s.getIdEmpresa());
            r.setIdSede(s.getSedeActual());
            r.setIdUsuario(s.getIdUsuarioEmpresa());
            r.setIdUsuarioInterno(s.getIdUsuarioInterno());
            r.setEstado("BORRADOR");
            r.setNumero(CorrelativoDAO.formatear("GR", correlativoDAO.siguiente(s.getIdEmpresa(), "GR")));
            r.setIdRecepcion(recepcionDAO.insertar(r));
        } else {
            r = buscar(s, cab.getIdRecepcion());
            if (!r.isBorrador()) {
                throw new NegocioException("Solo se puede editar un borrador.");
            }
            r.setIdProveedor(cab.getIdProveedor());
            r.setGuiaProveedor(cab.getGuiaProveedor());
            r.setObservaciones(cab.getObservaciones());
            recepcionDAO.actualizarCabecera(r);
        }

        recepcionDAO.eliminarDetalles(r.getIdRecepcion());
        if (!confirmar) {
            for (RecepcionDetalle d : lineas) {
                d.setIdLote(null);
                recepcionDAO.insertarDetalle(r.getIdRecepcion(), d);
            }
            auditoria.registrar(s, "Recepción", "GUARDÓ BORRADOR", "Guardó el borrador " + r.getNumero()
                    + " (guía prov. " + r.getGuiaProveedor() + ") - aún no afecta el stock");
            return r.getIdRecepcion();
        }

        recepcionDAO.confirmar(r.getIdRecepcion());
        int totalUnidades = 0;
        for (RecepcionDetalle d : lineas) {
            int idLote = crearLote(s, r, d, "RECEPCION " + r.getNumero() + " - guia " + r.getGuiaProveedor());
            d.setIdLote(idLote);
            recepcionDAO.insertarDetalle(r.getIdRecepcion(), d);
            totalUnidades += d.getTotalUnidades();
        }
        auditoria.registrar(s, "Recepción", "CONFIRMÓ RECEPCIÓN", "Confirmó la recepción " + r.getNumero() + " (guía prov. "
                + r.getGuiaProveedor() + "): ingresaron " + totalUnidades + " unidades en " + lineas.size() + " línea(s)");
        return r.getIdRecepcion();
    }

    private int crearLote(UsuarioSesion s, Recepcion r, RecepcionDetalle d, String documento) {
        Lote lote = new Lote();
        lote.setIdEmpresa(r.getIdEmpresa());
        lote.setIdSede(r.getIdSede());
        lote.setIdProducto(d.getIdProducto());
        lote.setIdRecepcion(r.getIdRecepcion());
        lote.setCantidadIngresada(d.getTotalUnidades());
        lote.setCantidadActual(d.getTotalUnidades());
        lote.setCostoUnitario(d.getCostoUnitario());
        lote.setFechaVencimiento(d.getFechaVencimiento());
        int idLote = loteDAO.insertar(lote);
        kardexDAO.registrar(r.getIdEmpresa(), r.getIdSede(), d.getIdProducto(), idLote, "ENTRADA", d.getTotalUnidades(),
                d.getCostoUnitario(), documento, null, r.getIdRecepcion(), r.getIdProveedor(), s);
        return idLote;
    }

    /**
     * Corrige una recepcion ya confirmada. El stock se ajusta POR DIFERENCIA:
     * el movimiento original no se borra, se agrega un AJUSTE (o una ENTRADA si se agrego un producto).
     * El motivo es obligatorio y queda en el historial y la auditoria.
     */
    @Transactional
    public void corregir(UsuarioSesion s, Recepcion cab, List<RecepcionDetalle> lineas, String motivo) {
        s.exigir("recepcion", "editar_confirmada");
        Recepcion r = buscar(s, cab.getIdRecepcion());
        if (!r.isAfectaStock()) {
            throw new NegocioException("Solo se corrigen recepciones confirmadas.");
        }
        if (StringUtils.trimToEmpty(motivo).length() < 5) {
            throw new NegocioException("El motivo de la corrección es obligatorio.");
        }
        validar(s, cab, lineas);

        // Las lineas con lote no pueden bajar de lo que ya se vendio o ajusto
        for (RecepcionDetalle d : lineas) {
            if (d.getIdLote() != null) {
                RecepcionDetalle original = r.getDetalles().stream()
                        .filter(o -> d.getIdLote().equals(o.getIdLote())).findFirst()
                        .orElseThrow(() -> new NegocioException("Línea no válida."));
                d.setIdProducto(original.getIdProducto());
                if (d.getTotalUnidades() < original.getConsumido()) {
                    throw new NegocioException(original.getProductoNombre() + ": ya salieron " + original.getConsumido()
                            + " unidades de este lote, el total no puede ser menor.");
                }
            }
        }
        for (RecepcionDetalle original : r.getDetalles()) {
            boolean sigue = lineas.stream().anyMatch(d -> Objects.equals(d.getIdLote(), original.getIdLote()));
            if (!sigue && original.getConsumido() > 0) {
                throw new NegocioException("No se puede quitar " + original.getProductoNombre() + ": ya salieron "
                        + original.getConsumido() + " unidades.");
            }
        }

        List<Ajuste> ajustes = calcularAjustes(r.getDetalles(), lineas);
        boolean cambioCabecera = !r.getGuiaProveedor().equals(cab.getGuiaProveedor())
                || !r.getIdProveedor().equals(cab.getIdProveedor())
                || !StringUtils.equals(StringUtils.trimToEmpty(r.getObservaciones()), StringUtils.trimToEmpty(cab.getObservaciones()));
        if (ajustes.isEmpty() && !cambioCabecera && !cambioCostoOVencimiento(r.getDetalles(), lineas)) {
            throw new NegocioException("No hay cambios que guardar.");
        }

        String documento = "AJUSTE POR CORRECCION DE RECEPCION " + r.getNumero();
        r.setIdProveedor(cab.getIdProveedor());
        for (RecepcionDetalle original : r.getDetalles()) {
            RecepcionDetalle nueva = lineas.stream()
                    .filter(d -> Objects.equals(d.getIdLote(), original.getIdLote())).findFirst().orElse(null);
            int diferencia = (nueva == null ? 0 : nueva.getTotalUnidades()) - original.getTotalUnidades();
            BigDecimal costo = nueva == null ? original.getCostoUnitario() : nueva.getCostoUnitario();
            loteDAO.corregir(original.getIdLote(), diferencia, costo,
                    nueva == null ? original.getFechaVencimiento() : nueva.getFechaVencimiento());
            if (diferencia != 0) {
                kardexDAO.registrar(r.getIdEmpresa(), r.getIdSede(), original.getIdProducto(), original.getIdLote(), "AJUSTE",
                        diferencia, costo, documento, null, r.getIdRecepcion(), cab.getIdProveedor(), s);
            }
        }

        recepcionDAO.eliminarDetalles(r.getIdRecepcion());
        for (RecepcionDetalle d : lineas) {
            if (d.getIdLote() == null) {
                d.setIdLote(crearLote(s, r, d, "RECEPCION " + r.getNumero() + " (correccion: producto omitido)"));
            }
            recepcionDAO.insertarDetalle(r.getIdRecepcion(), d);
        }
        r.setGuiaProveedor(cab.getGuiaProveedor());
        r.setObservaciones(cab.getObservaciones());
        recepcionDAO.actualizarCabecera(r);
        recepcionDAO.cambiarEstado(r.getIdRecepcion(), "MODIFICADA");

        StringBuilder cambios = new StringBuilder();
        for (Ajuste a : ajustes) {
            cambios.append(a.getProducto()).append(": ").append(a.getAntes()).append(" -> ").append(a.getDespues())
                    .append(" (").append(a.getDiferencia() > 0 ? "+" : "").append(a.getDiferencia()).append(")\n");
        }
        if (cambioCabecera) {
            cambios.append("Datos de la guía actualizados\n");
        }
        if (cambios.length() == 0) {
            cambios.append("Costo o vencimiento corregido\n");
        }
        RecepcionModificacion m = new RecepcionModificacion();
        m.setIdRecepcion(r.getIdRecepcion());
        m.setUsuarioNombre(s.getNombre());
        m.setRolNombre(s.getRolNombre());
        m.setMotivo(motivo.trim());
        m.setCambios(cambios.toString().trim());
        recepcionDAO.insertarModificacion(m);

        auditoria.registrar(s, "Recepción", "MODIFICÓ RECEPCIÓN", "Corrigió " + r.getNumero() + " (guía prov. " + r.getGuiaProveedor()
                + "). Cambios: " + cambios.toString().trim().replace("\n", "; ") + ". Motivo: " + motivo.trim());
    }

    private boolean cambioCostoOVencimiento(List<RecepcionDetalle> antes, List<RecepcionDetalle> despues) {
        for (RecepcionDetalle o : antes) {
            for (RecepcionDetalle d : despues) {
                if (Objects.equals(o.getIdLote(), d.getIdLote())
                        && (o.getCostoUnitario().compareTo(d.getCostoUnitario()) != 0
                        || !Objects.equals(o.getFechaVencimiento(), d.getFechaVencimiento()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Anular: nunca se borra. Un borrador se anula directo.
     * Una confirmada solo si su mercaderia no se movio (se retira del stock).
     */
    @Transactional
    public void anular(UsuarioSesion s, int idRecepcion) {
        s.exigir("recepcion", "anular");
        Recepcion r = buscar(s, idRecepcion);
        if ("ANULADA".equals(r.getEstado())) {
            throw new NegocioException("La recepción ya está anulada.");
        }
        if (r.isAfectaStock()) {
            List<Lote> lotes = loteDAO.porRecepcion(idRecepcion);
            for (Lote l : lotes) {
                if (l.getCantidadActual() != l.getCantidadIngresada()) {
                    throw new NegocioException("No se puede anular " + r.getNumero()
                            + ": parte de su mercadería ya se vendió o ajustó. Usa la corrección con motivo.");
                }
            }
            for (Lote l : lotes) {
                if (l.getCantidadActual() > 0) {
                    kardexDAO.registrar(l.getIdEmpresa(), l.getIdSede(), l.getIdProducto(), l.getIdLote(), "SALIDA",
                            l.getCantidadActual(), l.getCostoUnitario(), "ANULACION RECEPCION " + r.getNumero(),
                            null, r.getIdRecepcion(), r.getIdProveedor(), s);
                    loteDAO.cambiarCantidad(l.getIdLote(), 0);
                }
            }
        }
        recepcionDAO.cambiarEstado(idRecepcion, "ANULADA");
        auditoria.registrar(s, "Recepción", "ANULÓ RECEPCIÓN", "Anuló " + r.getNumero() + " (guía prov. " + r.getGuiaProveedor() + ")");
    }
}
