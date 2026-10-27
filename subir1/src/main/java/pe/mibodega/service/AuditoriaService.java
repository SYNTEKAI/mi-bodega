package pe.mibodega.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.mibodega.dao.AuditoriaDAO;
import pe.mibodega.model.RegistroAuditoria;
import pe.mibodega.model.UsuarioSesion;

/**
 * Registra las acciones de todos los modulos. Guarda el usuario, su rol activo,
 * la empresa, la sede, la IP y la fecha. Se usa desde todos los servicios.
 */
@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    @Autowired
    private AuditoriaDAO auditoriaDAO;

    public void registrar(UsuarioSesion s, String modulo, String accion, String descripcion) {
        RegistroAuditoria a = new RegistroAuditoria();
        a.setIdEmpresa(s.getIdEmpresa());
        a.setIdSede(s.isEnEmpresa() ? s.getSedeActual() : null);
        a.setUsuarioTipo(s.getTipo());
        a.setIdUsuario(s.getIdUsuario());
        a.setUsuarioNombre(s.getNombre());
        a.setRolNombre(s.getRolNombre());
        a.setModulo(modulo);
        a.setAccion(accion);
        a.setDescripcion(descripcion);
        a.setIp(ipActual());
        auditoriaDAO.insertar(a);
        log.info("[{}] {} ({}) - {}: {}", modulo, s.getUsuario(), s.getRolNombre(), accion, descripcion);
    }

    private String ipActual() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            return null;
        }
        HttpServletRequest request = attr.getRequest();
        return request.getRemoteAddr();
    }
}
