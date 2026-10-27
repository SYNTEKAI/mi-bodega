package pe.mibodega.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.mibodega.dao.KardexDAO;
import pe.mibodega.dao.LoteDAO;
import pe.mibodega.model.Lote;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.NegocioException;

/**
 * Inventario por lotes y ajustes por conteo fisico.
 */
@Service
public class InventarioService {

    @Autowired
    private LoteDAO loteDAO;
    @Autowired
    private KardexDAO kardexDAO;
    @Autowired
    private AuditoriaService auditoria;

    public List<Lote> lotesConStock(UsuarioSesion s) {
        return loteDAO.listarConStock(s.getIdEmpresa(), s.getSedeActual());
    }

    /** Ajuste tras un conteo fisico: cambia el lote y registra un AJUSTE en el kardex. */
    @Transactional
    public void ajustar(UsuarioSesion s, int idLote, Integer cantidadReal, String motivo) {
        s.exigir("inventario", "ajustar_stock");
        Lote lote = loteDAO.buscar(idLote, s.getIdEmpresa());
        if (lote == null) {
            throw new NegocioException("El lote no existe.");
        }
        if (cantidadReal == null || cantidadReal < 0) {
            throw new NegocioException("Ingresa una cantidad válida (entero mayor o igual a 0).");
        }
        int anterior = lote.getCantidadActual();
        if (cantidadReal == anterior) {
            throw new NegocioException("La cantidad contada es igual al stock: no hay nada que ajustar.");
        }
        String razon = StringUtils.defaultIfBlank(motivo, "Conteo físico").trim();
        loteDAO.cambiarCantidad(idLote, cantidadReal);
        kardexDAO.registrar(lote.getIdEmpresa(), lote.getIdSede(), lote.getIdProducto(), idLote, "AJUSTE", cantidadReal - anterior,
                lote.getCostoUnitario(), "AJUSTE: " + razon, null, null, null, s);
        auditoria.registrar(s, "Inventario", "MODIFICAR STOCK", "Lote L-" + idLote + " (" + lote.getProductoNombre() + "): de "
                + anterior + " a " + cantidadReal + " unidades. Motivo: " + razon);
    }
}
