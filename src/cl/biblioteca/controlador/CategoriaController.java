package cl.biblioteca.controlador;

import cl.biblioteca.dao.CategoriaDAO;
import cl.biblioteca.modelo.Categoria;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador de Categorías (Patrón MVC).
 * Gestiona la lógica de negocio y mediación entre vistas y CategoriaDAO.
 */
public class CategoriaController {

    private final CategoriaDAO categoriaDAO;

    public CategoriaController() {
        this.categoriaDAO = new CategoriaDAO();
    }

    public List<Categoria> obtenerTodas() throws SQLException {
        return categoriaDAO.readAll();
    }

    public Categoria obtenerPorId(int id) throws SQLException {
        return categoriaDAO.readById(id);
    }

    public boolean registrar(String nombre) throws SQLException, IllegalArgumentException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        Categoria c = new Categoria(nombre.trim());
        return categoriaDAO.create(c);
    }

    public boolean actualizar(int id, String nombre) throws SQLException, IllegalArgumentException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        Categoria c = new Categoria(id, nombre.trim());
        return categoriaDAO.update(c);
    }

    public boolean eliminar(int id) throws SQLException {
        return categoriaDAO.delete(id);
    }
}
