package pe.mibodega.dao;

import java.util.List;

/**
 * Operaciones basicas que implementan los DAO de los catalogos.
 * Todas las consultas filtran por id_empresa para que una empresa
 * nunca vea los datos de otra.
 */
public interface GenericDAO<T> {

    List<T> listar(int idEmpresa);

    T buscar(int id, int idEmpresa);

    int insertar(T obj);

    void actualizar(T obj);
}
