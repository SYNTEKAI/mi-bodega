package pe.mibodega.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.mibodega.dao.ClienteDAO;
import pe.mibodega.model.Cliente;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.model.Venta;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.Validar;

@Service
public class ClienteService {

    @Autowired
    private ClienteDAO clienteDAO;
    @Autowired
    private AuditoriaService auditoria;

    public List<Cliente> buscar(UsuarioSesion s, String texto, String tipo) {
        return clienteDAO.buscarConFiltros(s.getIdEmpresa(), texto, tipo);
    }

    public Cliente buscar(UsuarioSesion s, int id) {
        Cliente c = clienteDAO.buscar(id, s.getIdEmpresa());
        if (c == null) {
            throw new NegocioException("El cliente no existe.");
        }
        return c;
    }

    public Cliente buscarPorDocumento(UsuarioSesion s, String documento) {
        return clienteDAO.buscarPorDocumento(s.getIdEmpresa(), documento == null ? "" : documento.trim());
    }

    public List<Map<String, Object>> productosFrecuentes(int idCliente) {
        return clienteDAO.productosFrecuentes(idCliente);
    }

    public List<Venta> historial(int idCliente) {
        return clienteDAO.historial(idCliente);
    }

    public int guardar(UsuarioSesion s, Cliente c) {
        s.exigir("clientes", c.getIdCliente() == null ? "crear" : "editar");
        Validar.requerido(c.getNombre(), "nombre");
        c.setDocumento(c.getDocumento() == null ? null : c.getDocumento().trim());
        Validar.documento(c.getTipoDoc(), c.getDocumento());
        c.setIdEmpresa(s.getIdEmpresa());
        if (clienteDAO.existeDocumento(s.getIdEmpresa(), c.getDocumento(), c.getIdCliente())) {
            throw new NegocioException("Ya existe un cliente con el documento " + c.getDocumento() + ".");
        }
        if (c.getIdCliente() == null) {
            int id = clienteDAO.insertar(c);
            auditoria.registrar(s, "Clientes", "CREÓ CLIENTE", "Registró al cliente " + c.getNombre()
                    + " (" + c.getTipoDoc() + " " + c.getDocumento() + ")");
            return id;
        }
        buscar(s, c.getIdCliente());
        clienteDAO.actualizar(c);
        auditoria.registrar(s, "Clientes", "EDITÓ CLIENTE", "Editó al cliente " + c.getNombre());
        return c.getIdCliente();
    }

    public void eliminar(UsuarioSesion s, int id) {
        s.exigir("clientes", "eliminar");
        Cliente c = buscar(s, id);
        int ventas = clienteDAO.contarVentas(id);
        if (ventas > 0) {
            throw new NegocioException(c.getNombre() + " tiene " + ventas + " compras registradas: no se elimina para conservar el historial.");
        }
        clienteDAO.eliminar(id);
        auditoria.registrar(s, "Clientes", "ELIMINAR", "Eliminó al cliente " + c.getNombre());
    }
}
