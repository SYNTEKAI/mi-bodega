package pe.mibodega.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.mibodega.dao.SedeDAO;
import pe.mibodega.model.Sede;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.Validar;

/**
 * Sedes de la empresa. Solo el Administrador las edita o elimina.
 */
@Service
public class SedeService {

    @Autowired
    private SedeDAO sedeDAO;
    @Autowired
    private AuditoriaService auditoria;

    public List<Sede> listar(UsuarioSesion s) {
        return sedeDAO.listar(s.getIdEmpresa());
    }

    public Sede buscar(UsuarioSesion s, int id) {
        Sede sede = sedeDAO.buscar(id, s.getIdEmpresa());
        if (sede == null) {
            throw new NegocioException("La sede no existe.");
        }
        return sede;
    }

    private void exigirAdmin(UsuarioSesion s, String accion) {
        s.exigir("sedes", accion);
        if (!s.isAdminEmpresa()) {
            throw new NegocioException("Solo el Administrador puede modificar las sedes.");
        }
    }

    public void guardar(UsuarioSesion s, Sede sede) {
        exigirAdmin(s, sede.getIdSede() == null ? "crear" : "editar");
        Validar.requerido(sede.getNombre(), "nombre");
        Validar.requerido(sede.getCodigo(), "código");
        Validar.requerido(sede.getDireccion(), "dirección");
        Validar.requerido(sede.getDistrito(), "distrito");
        sede.setCodigo(sede.getCodigo().trim().toUpperCase());
        sede.setProvincia(StringUtils.defaultIfBlank(sede.getProvincia(), "Lima"));
        sede.setIdEmpresa(s.getIdEmpresa());
        if (sedeDAO.existeCodigo(s.getIdEmpresa(), sede.getCodigo(), sede.getIdSede())) {
            throw new NegocioException("Ya existe una sede con el código " + sede.getCodigo() + ".");
        }
        if (sede.getIdSede() == null) {
            sedeDAO.insertar(sede);
            auditoria.registrar(s, "Sedes", "CREAR SEDE", "Creó la sede " + sede.getNombre() + " (" + sede.getCodigo() + ")");
        } else {
            buscar(s, sede.getIdSede());
            sedeDAO.actualizar(sede);
            auditoria.registrar(s, "Sedes", "EDITAR SEDE", "Editó la sede " + sede.getNombre() + " (" + sede.getCodigo() + ")");
        }
    }

    public void cambiarActivo(UsuarioSesion s, int id) {
        exigirAdmin(s, "editar");
        Sede sede = buscar(s, id);
        sedeDAO.cambiarActivo(id, !sede.isActivo());
        auditoria.registrar(s, "Sedes", sede.isActivo() ? "DESACTIVAR" : "REACTIVAR",
                (sede.isActivo() ? "Desactivó" : "Reactivó") + " la sede " + sede.getNombre());
    }

    /** Con datos relacionados no se borra: se desactiva. Devuelve el mensaje para el usuario. */
    public String eliminar(UsuarioSesion s, int id) {
        exigirAdmin(s, "eliminar");
        Sede sede = buscar(s, id);
        int relacionados = sedeDAO.contarRelacionados(id);
        if (relacionados > 0) {
            sedeDAO.cambiarActivo(id, false);
            auditoria.registrar(s, "Sedes", "DESACTIVAR", "Desactivó la sede " + sede.getNombre()
                    + " (eliminación bloqueada: " + relacionados + " registros relacionados)");
            return "La sede tiene " + relacionados + " registros relacionados: no se eliminó, se desactivó.";
        }
        sedeDAO.eliminar(id);
        auditoria.registrar(s, "Sedes", "ELIMINAR SEDE", "Eliminó la sede " + sede.getNombre());
        return "Sede eliminada.";
    }

    public int contarUsuarios(int idSede) {
        return sedeDAO.contarUsuarios(idSede);
    }

    public int contarVentas(int idSede) {
        return sedeDAO.contarVentas(idSede);
    }
}
