package pe.mibodega.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pe.mibodega.dao.EmpresaDAO;
import pe.mibodega.dao.RolDAO;
import pe.mibodega.model.Empresa;
import pe.mibodega.model.UsuarioSesion;
import pe.mibodega.util.NegocioException;
import pe.mibodega.util.Validar;

/**
 * Empresas clientes de la plataforma (las administra el equipo interno).
 */
@Service
public class EmpresaService {

    public static final List<String> ESTADOS = List.of("ACTIVA", "EN PRUEBA", "SUSPENDIDA", "INACTIVA");
    public static final List<String> PLANES = List.of("Básico", "Estándar", "Premium", "Prueba");

    @Autowired
    private EmpresaDAO empresaDAO;
    @Autowired
    private RolDAO rolDAO;
    @Autowired
    private AuditoriaService auditoria;

    public List<Empresa> listar() {
        return empresaDAO.listar();
    }

    public Empresa buscar(int id) {
        Empresa e = empresaDAO.buscar(id);
        if (e == null) {
            throw new NegocioException("La empresa no existe.");
        }
        return e;
    }

    /** Una empresa nueva empieza EN PRUEBA y con su rol Administrador. */
    @Transactional
    public void guardar(UsuarioSesion s, Empresa e, MultipartFile logo) {
        s.exigir("empresas", e.getIdEmpresa() == null ? "crear" : "editar");
        Validar.requerido(e.getRazonSocial(), "razón social");
        Validar.ruc(e.getRuc());
        if (empresaDAO.existeRuc(e.getRuc(), e.getIdEmpresa())) {
            throw new NegocioException("Ya existe una empresa con el RUC " + e.getRuc() + ".");
        }
        int id;
        if (e.getIdEmpresa() == null) {
            id = empresaDAO.insertar(e);
            rolDAO.crearAdministrador(id);
            auditoria.registrar(s, "Empresas", "CREAR EMPRESA", "Registró la empresa " + e.getRazonSocial() + " (RUC " + e.getRuc() + ")");
        } else {
            id = e.getIdEmpresa();
            empresaDAO.actualizar(e);
            auditoria.registrar(s, "Empresas", "EDITAR EMPRESA", "Editó los datos de " + e.getRazonSocial());
        }
        if (logo != null && !logo.isEmpty()) {
            if (logo.getSize() > 1024 * 1024) {
                throw new NegocioException("El logo supera 1 MB.");
            }
            if (logo.getContentType() == null || !logo.getContentType().startsWith("image/")) {
                throw new NegocioException("El logo debe ser una imagen (PNG o JPG).");
            }
            try {
                empresaDAO.guardarLogo(id, logo.getBytes(), logo.getContentType());
            } catch (IOException ex) {
                throw new NegocioException("No se pudo leer el logo.");
            }
            auditoria.registrar(s, "Empresas", "SUBIÓ LOGO EMPRESA", "Subió el logo de " + e.getRazonSocial());
        }
    }

    public void quitarLogo(UsuarioSesion s, int id) {
        s.exigir("empresas", "editar");
        Empresa e = buscar(id);
        empresaDAO.quitarLogo(id);
        auditoria.registrar(s, "Empresas", "CAMBIÓ LOGO", "Quitó el logo de " + e.getRazonSocial());
    }

    public void cambiarEstado(UsuarioSesion s, int id, String estado) {
        s.exigir("empresas", "cambiar_estado");
        if (!ESTADOS.contains(estado)) {
            throw new NegocioException("Estado no válido.");
        }
        Empresa e = buscar(id);
        if (!estado.equals(e.getEstado())) {
            empresaDAO.cambiarEstado(id, estado);
            auditoria.registrar(s, "Empresas", "CAMBIAR ESTADO", "Empresa " + e.getRazonSocial() + ": de " + e.getEstado() + " a " + estado);
        }
    }

    public Map<String, Object> logo(int id) {
        return empresaDAO.obtenerLogo(id);
    }
}
