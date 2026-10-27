package pe.mibodega.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.mibodega.dao.ProveedorDAO;
import pe.mibodega.model.Proveedor;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.Validar;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorDAO proveedorDAO;
    @Autowired
    private AuditoriaService auditoria;

    public List<Proveedor> buscar(UsuarioSesion s, String texto, String estado) {
        return proveedorDAO.buscarConFiltros(s.getIdEmpresa(), texto, estado);
    }

    public List<Proveedor> activos(UsuarioSesion s) {
        return proveedorDAO.listarActivos(s.getIdEmpresa());
    }

    public Proveedor buscar(UsuarioSesion s, int id) {
        Proveedor p = proveedorDAO.buscar(id, s.getIdEmpresa());
        if (p == null) {
            throw new NegocioException("El proveedor no existe.");
        }
        return p;
    }

    public int guardar(UsuarioSesion s, Proveedor p) {
        s.exigir("proveedores", p.getIdProveedor() == null ? "crear" : "editar");
        Validar.requerido(p.getRazonSocial(), "razón social");
        p.setDocumento(p.getDocumento() == null ? null : p.getDocumento().trim());
        Validar.documento(p.getTipoDoc(), p.getDocumento());
        p.setIdEmpresa(s.getIdEmpresa());
        if (proveedorDAO.existeDocumento(s.getIdEmpresa(), p.getDocumento(), p.getIdProveedor())) {
            throw new NegocioException("Ya existe un proveedor con el documento " + p.getDocumento() + ".");
        }
        if (p.getIdProveedor() == null) {
            int id = proveedorDAO.insertar(p);
            auditoria.registrar(s, "Proveedores", "CREÓ PROVEEDOR", "Registró al proveedor " + p.getRazonSocial()
                    + " (" + p.getTipoDoc() + " " + p.getDocumento() + ")");
            return id;
        }
        buscar(s, p.getIdProveedor());
        proveedorDAO.actualizar(p);
        auditoria.registrar(s, "Proveedores", "EDITÓ PROVEEDOR", "Editó al proveedor " + p.getRazonSocial());
        return p.getIdProveedor();
    }

    public void cambiarActivo(UsuarioSesion s, int id) {
        s.exigir("proveedores", "editar");
        Proveedor p = buscar(s, id);
        proveedorDAO.cambiarActivo(id, !p.isActivo());
        auditoria.registrar(s, "Proveedores", p.isActivo() ? "DESACTIVAR" : "REACTIVAR",
                "Proveedor " + p.getRazonSocial() + " pasó a " + (p.isActivo() ? "INACTIVO" : "ACTIVO"));
    }

    public String eliminar(UsuarioSesion s, int id) {
        s.exigir("proveedores", "eliminar");
        Proveedor p = buscar(s, id);
        if (p.getGuias() > 0) {
            proveedorDAO.cambiarActivo(id, false);
            auditoria.registrar(s, "Proveedores", "DESACTIVAR", "Desactivó a " + p.getRazonSocial()
                    + " (eliminación bloqueada: " + p.getGuias() + " guías)");
            return "El proveedor tiene " + p.getGuias() + " guías registradas: no se eliminó, se desactivó.";
        }
        proveedorDAO.eliminar(id);
        auditoria.registrar(s, "Proveedores", "ELIMINÓ PROVEEDOR", "Eliminó al proveedor " + p.getRazonSocial());
        return "Proveedor eliminado.";
    }
}
