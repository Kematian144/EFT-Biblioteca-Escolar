package cl.biblioteca.controlador;

import cl.biblioteca.dao.EstudianteDAO;
import cl.biblioteca.modelo.Estudiante;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador de Estudiantes (Patrón MVC).
 * Gestiona el registro y mantenimiento de estudiantes de la institución escolar.
 */
public class EstudianteController {

    private final EstudianteDAO estudianteDAO;

    public EstudianteController() {
        this.estudianteDAO = new EstudianteDAO();
    }

    public List<Estudiante> listarTodos() throws SQLException {
        return estudianteDAO.readAll();
    }

    public Estudiante obtenerPorId(int id) throws SQLException {
        return estudianteDAO.readById(id);
    }

    public List<Estudiante> buscar(String criterio) throws SQLException {
        return estudianteDAO.search(criterio);
    }

    public boolean registrar(String nombre, String rut, String curso, String correo)
            throws SQLException, IllegalArgumentException {
        validarCampos(nombre, rut, curso, correo);
        Estudiante e = new Estudiante(nombre.trim(), rut.trim(), curso.trim(), correo.trim());
        return estudianteDAO.create(e);
    }

    public boolean actualizar(int id, String nombre, String rut, String curso, String correo)
            throws SQLException, IllegalArgumentException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de estudiante inválido.");
        }
        validarCampos(nombre, rut, curso, correo);
        Estudiante e = new Estudiante(id, nombre.trim(), rut.trim(), curso.trim(), correo.trim());
        return estudianteDAO.update(e);
    }

    public boolean eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de estudiante inválido.");
        }
        return estudianteDAO.delete(id);
    }

    private void validarCampos(String nombre, String rut, String curso, String correo) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del estudiante es obligatorio.");
        }
        if (rut == null || rut.trim().isEmpty()) {
            throw new IllegalArgumentException("El RUT del estudiante es obligatorio.");
        }
        if (curso == null || curso.trim().isEmpty()) {
            throw new IllegalArgumentException("El curso del estudiante es obligatorio.");
        }
        if (correo == null || correo.trim().isEmpty() || !correo.contains("@")) {
            throw new IllegalArgumentException("Debe ingresar un correo electrónico válido.");
        }
    }
}
