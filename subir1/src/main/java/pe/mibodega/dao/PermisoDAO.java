package pe.mibodega.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Permiso;

@Repository
public class PermisoDAO extends BaseDAO {

    public List<Permiso> listarPorAmbito(String ambito) {
        return lista(Permiso.class, "SELECT * FROM permisos WHERE ambito = ? ORDER BY id_permiso", ambito);
    }

    /** Permisos (modulo.accion) de un rol de empresa. */
    public Set<String> permisosDeRol(int idRol) {
        return new HashSet<>(jdbc.queryForList("SELECT CONCAT(p.modulo,'.',p.accion) FROM rol_permisos rp "
                + "JOIN permisos p ON p.id_permiso = rp.id_permiso WHERE rp.id_rol = ?", String.class, idRol));
    }

    /** Permisos (modulo.accion) de un rol interno. */
    public Set<String> permisosDeRolInterno(int idRolInterno) {
        return new HashSet<>(jdbc.queryForList("SELECT CONCAT(p.modulo,'.',p.accion) FROM rol_interno_permisos rp "
                + "JOIN permisos p ON p.id_permiso = rp.id_permiso WHERE rp.id_rol_interno = ?", String.class, idRolInterno));
    }

    /** Reemplaza todos los permisos de un rol (DELETE + INSERT). */
    public void reemplazarPermisosDeRol(int idRol, List<Integer> idsPermiso) {
        jdbc.update("DELETE FROM rol_permisos WHERE id_rol = ?", idRol);
        for (Integer idPermiso : idsPermiso) {
            jdbc.update("INSERT INTO rol_permisos (id_rol, id_permiso) "
                    + "SELECT ?, id_permiso FROM permisos WHERE id_permiso = ? AND ambito = 'EMPRESA'", idRol, idPermiso);
        }
    }

    public void copiarPermisos(int idRolOrigen, int idRolDestino) {
        jdbc.update("INSERT INTO rol_permisos (id_rol, id_permiso) SELECT ?, id_permiso FROM rol_permisos WHERE id_rol = ?",
                idRolDestino, idRolOrigen);
    }

    /** Reemplaza los permisos de un rol interno (solo permisos de la plataforma). */
    public void reemplazarPermisosDeRolInterno(int idRolInterno, List<Integer> idsPermiso) {
        jdbc.update("DELETE FROM rol_interno_permisos WHERE id_rol_interno = ?", idRolInterno);
        for (Integer idPermiso : idsPermiso) {
            jdbc.update("INSERT INTO rol_interno_permisos (id_rol_interno, id_permiso) "
                    + "SELECT ?, id_permiso FROM permisos WHERE id_permiso = ? AND ambito = 'PLATAFORMA'", idRolInterno, idPermiso);
        }
    }
}
