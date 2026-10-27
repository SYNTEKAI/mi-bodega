package pe.mibodega.dao;

import org.springframework.stereotype.Repository;
import pe.mibodega.model.ConfigComprobante;

@Repository
public class ConfigComprobanteDAO extends BaseDAO {

    /** Devuelve la configuracion de la empresa o una por defecto. */
    public ConfigComprobante buscar(int idEmpresa) {
        ConfigComprobante c = uno(ConfigComprobante.class, "SELECT * FROM config_comprobantes WHERE id_empresa = ?", idEmpresa);
        if (c == null) {
            c = new ConfigComprobante();
            c.setIdEmpresa(idEmpresa);
            c.setPlantilla("CLASICO");
            c.setAncho("80");
            c.setPie("Gracias por su compra");
            c.setMostrarLogo(true);
            c.setMostrarRazonSocial(true);
            c.setMostrarDireccion(true);
            c.setMostrarCliente(true);
            c.setMostrarCajero(true);
            c.setMostrarVuelto(true);
        }
        return c;
    }

    public void guardar(ConfigComprobante c) {
        jdbc.update("INSERT INTO config_comprobantes (id_empresa, plantilla, ancho, encabezado, pie, mensaje, notas, mostrar_logo, "
                + "mostrar_razon_social, mostrar_direccion, mostrar_cliente, mostrar_cajero, mostrar_vuelto) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE plantilla = VALUES(plantilla), ancho = VALUES(ancho), encabezado = VALUES(encabezado), "
                + "pie = VALUES(pie), mensaje = VALUES(mensaje), notas = VALUES(notas), mostrar_logo = VALUES(mostrar_logo), "
                + "mostrar_razon_social = VALUES(mostrar_razon_social), mostrar_direccion = VALUES(mostrar_direccion), "
                + "mostrar_cliente = VALUES(mostrar_cliente), mostrar_cajero = VALUES(mostrar_cajero), mostrar_vuelto = VALUES(mostrar_vuelto)",
                c.getIdEmpresa(), c.getPlantilla(), c.getAncho(), c.getEncabezado(), c.getPie(), c.getMensaje(), c.getNotas(),
                c.isMostrarLogo(), c.isMostrarRazonSocial(), c.isMostrarDireccion(), c.isMostrarCliente(), c.isMostrarCajero(),
                c.isMostrarVuelto());
    }
}
