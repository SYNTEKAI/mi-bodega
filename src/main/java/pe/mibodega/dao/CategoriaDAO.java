package pe.mibodega.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import pe.mibodega.model.Categoria;

@Repository
public class CategoriaDAO extends BaseDAO implements GenericDAO<Categoria> {

    private static final String SELECT = "SELECT c.*, (SELECT COUNT(*) FROM productos p WHERE p.id_categoria = c.id_categoria) AS productos "
            + "FROM categorias c ";

    @Override
    public List<Categoria> listar(int idEmpresa) {
        return lista(Categoria.class, SELECT + "WHERE c.id_empresa = ? ORDER BY c.nombre", idEmpresa);
    }

    public List<Categoria> listarActivas(int idEmpresa) {
        return lista(Categoria.class, SELECT + "WHERE c.id_empresa = ? AND c.activo = TRUE ORDER BY c.nombre", idEmpresa);
    }

    @Override
    public Categoria buscar(int id, int idEmpresa) {
        return uno(Categoria.class, SELECT + "WHERE c.id_categoria = ? AND c.id_empresa = ?", id, idEmpresa);
    }

    public Categoria buscarPorNombre(int idEmpresa, String nombre) {
        return uno(Categoria.class, SELECT + "WHERE c.id_empresa = ? AND c.nombre = ?", idEmpresa, nombre);
    }

    @Override
    public int insertar(Categoria c) {
        return insertarConId("INSERT INTO categorias (id_empresa, nombre) VALUES (?, ?)", c.getIdEmpresa(), c.getNombre());
    }

    @Override
    public void actualizar(Categoria c) {
        jdbc.update("UPDATE categorias SET nombre = ? WHERE id_categoria = ? AND id_empresa = ?",
                c.getNombre(), c.getIdCategoria(), c.getIdEmpresa());
    }

    public boolean existeNombre(int idEmpresa, String nombre, Integer excluirId) {
        return contar("SELECT COUNT(*) FROM categorias WHERE id_empresa = ? AND nombre = ? AND id_categoria <> ?",
                idEmpresa, nombre, excluirId == null ? 0 : excluirId) > 0;
    }

    public void cambiarActivo(int id, boolean activo) {
        jdbc.update("UPDATE categorias SET activo = ? WHERE id_categoria = ?", activo, id);
    }

    public void eliminar(int id) {
        jdbc.update("DELETE FROM categorias WHERE id_categoria = ?", id);
    }
}
