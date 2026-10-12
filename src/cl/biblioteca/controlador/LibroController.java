package cl.biblioteca.controlador;

import cl.biblioteca.dao.LibroDAO;
import cl.biblioteca.modelo.Libro;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador de Libros e Inventario (Patrón MVC).
 * Aplica reglas de validación y media entre las vistas y LibroDAO.
 */
public class LibroController {

    private final LibroDAO libroDAO;

    public LibroController() {
        this.libroDAO = new LibroDAO();
    }

    public List<Libro> listarTodos() throws SQLException {
        return libroDAO.readAll();
    }

    public Libro obtenerPorId(int id) throws SQLException {
        return libroDAO.readById(id);
    }

    public List<Libro> buscar(String criterio) throws SQLException {
        return libroDAO.search(criterio);
    }

    public List<Libro> filtrarPorCategoria(int idCategoria) throws SQLException {
        if (idCategoria <= 0) {
            return libroDAO.readAll();
        }
        return libroDAO.readByCategoria(idCategoria);
    }

    public boolean registrar(String titulo, String autor, String isbn, String editorial, int stock, int idCategoria)
            throws SQLException, IllegalArgumentException {
        validarCampos(titulo, autor, isbn, editorial, stock, idCategoria);
        Libro l = new Libro(titulo.trim(), autor.trim(), isbn.trim(), editorial.trim(), stock, idCategoria);
        return libroDAO.create(l);
    }

    public boolean actualizar(int id, String titulo, String autor, String isbn, String editorial, int stock, int idCategoria)
            throws SQLException, IllegalArgumentException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de libro inválido.");
        }
        validarCampos(titulo, autor, isbn, editorial, stock, idCategoria);
        Libro l = new Libro(id, titulo.trim(), autor.trim(), isbn.trim(), editorial.trim(), stock, idCategoria);
        return libroDAO.update(l);
    }

    public boolean eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de libro inválido.");
        }
        return libroDAO.delete(id);
    }

    private void validarCampos(String titulo, String autor, String isbn, String editorial, int stock, int idCategoria) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título del libro es obligatorio.");
        }
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("El autor del libro es obligatorio.");
        }
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("El código ISBN es obligatorio.");
        }
        if (editorial == null || editorial.trim().isEmpty()) {
            throw new IllegalArgumentException("La editorial es obligatoria.");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("El stock de libros no puede ser negativo.");
        }
        if (idCategoria <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una categoría válida.");
        }
    }
}
