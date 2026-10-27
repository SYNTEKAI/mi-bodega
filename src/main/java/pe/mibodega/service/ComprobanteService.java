package pe.mibodega.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.mibodega.dao.ConfigComprobanteDAO;
import pe.mibodega.dao.VentaDAO;
import pe.mibodega.model.ConfigComprobante;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.model.Venta;
import pe.mibodega.util.NegocioException;

/**
 * Diseno de la boleta de venta de cada empresa.
 */
@Service
public class ComprobanteService {

    public static final List<String> PLANTILLAS = List.of("CLASICO", "MODERNO", "COMPACTO");

    @Autowired
    private ConfigComprobanteDAO configDAO;
    @Autowired
    private VentaDAO ventaDAO;
    @Autowired
    private AuditoriaService auditoria;

    public ConfigComprobante config(int idEmpresa) {
        return configDAO.buscar(idEmpresa);
    }

    public void guardar(UsuarioSesion s, ConfigComprobante c) {
        s.exigir("comprobantes", "configurar");
        if (!PLANTILLAS.contains(c.getPlantilla())) {
            throw new NegocioException("Modelo de boleta no válido.");
        }
        if (!"58".equals(c.getAncho())) {
            c.setAncho("80");
        }
        c.setIdEmpresa(s.getIdEmpresa());
        configDAO.guardar(c);
        auditoria.registrar(s, "Comprobantes", "MODIFICÓ DISEÑO DE BOLETA", "Guardó el diseño de la boleta (modelo " + c.getPlantilla() + ")");
    }

    /** Ultima venta de la empresa, para la vista previa. */
    public Venta ventaDeEjemplo(UsuarioSesion s) {
        return ventaDAO.ultimaVenta(s.getIdEmpresa());
    }
}
