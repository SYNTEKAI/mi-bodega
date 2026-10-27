package pe.mibodega.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/**
 * Metodos comunes de los DAO.
 */
public abstract class BaseDAO {

    @Autowired
    protected JdbcTemplate jdbc;

    /** Ejecuta un INSERT y devuelve el id generado (AUTO_INCREMENT). */
    protected int insertarConId(String sql, Object... params) {
        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, kh);
        return kh.getKey().intValue();
    }

    protected <T> List<T> lista(Class<T> clase, String sql, Object... params) {
        return jdbc.query(sql, new BeanPropertyRowMapper<>(clase), params);
    }

    protected <T> T uno(Class<T> clase, String sql, Object... params) {
        List<T> lista = lista(clase, sql, params);
        return lista.isEmpty() ? null : lista.get(0);
    }

    protected int contar(String sql, Object... params) {
        Integer n = jdbc.queryForObject(sql, Integer.class, params);
        return n == null ? 0 : n;
    }
}
