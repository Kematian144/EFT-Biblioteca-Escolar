package cl.biblioteca.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz genérica para el patrón Data Access Object (DAO).
 * Define el contrato estándar para operaciones CRUD en la base de datos.
 * @param <T> Tipo de entidad del modelo
 */
public interface DAO<T> {

    /**
     * Inserta un nuevo registro en la base de datos.
     * @param entidad Objeto a insertar
     * @return true si la inserción fue exitosa, false en caso contrario
     * @throws SQLException si ocurre un error de base de datos
     */
    boolean create(T entidad) throws SQLException;

    /**
     * Obtiene todos los registros de la entidad.
     * @return Lista de entidades
     * @throws SQLException si ocurre un error de base de datos
     */
    List<T> readAll() throws SQLException;

    /**
     * Obtiene una entidad por su identificador primario.
     * @param id Identificador único
     * @return Entidad encontrada o null si no existe
     * @throws SQLException si ocurre un error de base de datos
     */
    T readById(int id) throws SQLException;

    /**
     * Actualiza un registro existente en la base de datos.
     * @param entidad Objeto con datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     * @throws SQLException si ocurre un error de base de datos
     */
    boolean update(T entidad) throws SQLException;

    /**
     * Elimina un registro de la base de datos por su ID.
     * @param id Identificador único a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     * @throws SQLException si ocurre un error de base de datos
     */
    boolean delete(int id) throws SQLException;
}
