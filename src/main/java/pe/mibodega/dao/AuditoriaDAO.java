package pe.mibodega.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.RegistroAuditoria;

/**
 * La tabla auditoria solo admite INSERT (los triggers bloquean UPDATE y DELETE).
 */
@Repository
public class AuditoriaDAO extends BaseDAO {

    public static final int FILAS_POR_PAGINA = 25;

    public void insertar(RegistroAuditoria a) {
        jdbc.update("INSERT INTO auditoria (id_empresa, id_sede, usuario_tipo, id_usuario, usuario_nombre, rol_nombre, modulo, accion, "
                + "descripcion, ip, fecha_hora) VALUES (?,?,?,?,?,?,?,?,?,?,NOW())",
                a.getIdEmpresa(), a.getIdSede(), a.getUsuarioTipo(), a.getIdUsuario(), a.getUsuarioNombre(), a.getRolNombre(),
                a.getModulo(), a.getAccion(), StringUtils.abbreviate(a.getDescripcion(), 500), a.getIp());
    }

    /**
     * empresa: id de la empresa, 0 = solo plataforma, null = todas (auditoria global).
     * usuario: "EMPRESA:3" o "INTERNO:1".
     */
    public List<RegistroAuditoria> buscar(Integer empresa, Integer idSede, String usuario, String modulo, String accion,
            LocalDate desde, LocalDate hasta, String texto, int pagina) {
        List<Object> params = new ArrayList<>();
        String where = filtros(params, empresa, idSede, usuario, modulo, accion, desde, hasta, texto);
        params.add(FILAS_POR_PAGINA);
        params.add((Math.max(pagina, 1) - 1) * FILAS_POR_PAGINA);
        return lista(RegistroAuditoria.class, "SELECT a.*, COALESCE(e.nombre_comercial, e.razon_social) AS empresa_nombre, "
                + "s.nombre AS sede_nombre FROM auditoria a LEFT JOIN empresas e ON e.id_empresa = a.id_empresa "
                + "LEFT JOIN sedes s ON s.id_sede = a.id_sede " + where
                + "ORDER BY a.fecha_hora DESC, a.id_auditoria DESC LIMIT ? OFFSET ?", params.toArray());
    }

    public int contar(Integer empresa, Integer idSede, String usuario, String modulo, String accion,
            LocalDate desde, LocalDate hasta, String texto) {
        List<Object> params = new ArrayList<>();
        String where = filtros(params, empresa, idSede, usuario, modulo, accion, desde, hasta, texto);
        return contar("SELECT COUNT(*) FROM auditoria a " + where, params.toArray());
    }

    private String filtros(List<Object> params, Integer empresa, Integer idSede, String usuario, String modulo, String accion,
            LocalDate desde, LocalDate hasta, String texto) {
        StringBuilder w = new StringBuilder("WHERE 1 = 1 ");
        if (empresa != null && empresa == 0) {
            w.append("AND a.id_empresa IS NULL ");
        } else if (empresa != null) {
            w.append("AND a.id_empresa = ? ");
            params.add(empresa);
        }
        if (idSede != null) {
            w.append("AND a.id_sede = ? ");
            params.add(idSede);
        }
        if (StringUtils.contains(usuario, ":")) {
            w.append("AND a.usuario_tipo = ? AND a.id_usuario = ? ");
            params.add(usuario.split(":")[0]);
            params.add(Integer.parseInt(usuario.split(":")[1]));
        }
        if (StringUtils.isNotBlank(modulo)) {
            w.append("AND a.modulo = ? ");
            params.add(modulo);
        }
        if (StringUtils.isNotBlank(accion)) {
            w.append("AND a.accion = ? ");
            params.add(accion);
        }
        if (desde != null) {
            w.append("AND DATE(a.fecha_hora) >= ? ");
            params.add(desde);
        }
        if (hasta != null) {
            w.append("AND DATE(a.fecha_hora) <= ? ");
            params.add(hasta);
        }
        if (StringUtils.isNotBlank(texto)) {
            w.append("AND a.descripcion LIKE ? ");
            params.add("%" + texto.trim() + "%");
        }
        return w.toString();
    }

    /** Usuarios que aparecen en la auditoria (para el filtro). */
    public List<Map<String, Object>> usuarios(Integer empresa) {
        if (empresa == null) {
            return jdbc.queryForList("SELECT DISTINCT CONCAT(usuario_tipo, ':', id_usuario) AS clave, usuario_nombre "
                    + "FROM auditoria ORDER BY usuario_nombre");
        }
        return jdbc.queryForList("SELECT DISTINCT CONCAT(usuario_tipo, ':', id_usuario) AS clave, usuario_nombre "
                + "FROM auditoria WHERE id_empresa = ? ORDER BY usuario_nombre", empresa);
    }

    public List<String> modulos(Integer empresa) {
        if (empresa == null) {
            return jdbc.queryForList("SELECT DISTINCT modulo FROM auditoria ORDER BY modulo", String.class);
        }
        return jdbc.queryForList("SELECT DISTINCT modulo FROM auditoria WHERE id_empresa = ? ORDER BY modulo", String.class, empresa);
    }

    public List<String> acciones(Integer empresa) {
        if (empresa == null) {
            return jdbc.queryForList("SELECT DISTINCT accion FROM auditoria ORDER BY accion", String.class);
        }
        return jdbc.queryForList("SELECT DISTINCT accion FROM auditoria WHERE id_empresa = ? ORDER BY accion", String.class, empresa);
    }

    /** Eventos de auditoria que mencionan un documento (historial de una recepcion). */
    public List<RegistroAuditoria> porDocumento(int idEmpresa, String modulo, String documento) {
        return lista(RegistroAuditoria.class, "SELECT * FROM auditoria WHERE id_empresa = ? AND modulo = ? AND descripcion LIKE ? "
                + "ORDER BY fecha_hora, id_auditoria", idEmpresa, modulo, "%" + documento + "%");
    }

    public List<RegistroAuditoria> recientes(int cantidad) {
        return lista(RegistroAuditoria.class, "SELECT a.*, COALESCE(e.nombre_comercial, e.razon_social, 'Plataforma') AS empresa_nombre "
                + "FROM auditoria a LEFT JOIN empresas e ON e.id_empresa = a.id_empresa ORDER BY a.fecha_hora DESC, a.id_auditoria DESC LIMIT ?",
                cantidad);
    }
}
