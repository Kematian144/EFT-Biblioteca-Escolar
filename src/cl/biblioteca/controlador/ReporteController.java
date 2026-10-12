package cl.biblioteca.controlador;

import cl.biblioteca.dao.PrestamoDAO;
import cl.biblioteca.modelo.Prestamo;
import cl.biblioteca.modelo.ReporteLibroPrestado;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador de Reportes e Informes (Patrón MVC).
 * Facilita la generación de informes estadísticos para la gestión bibliotecaria:
 * 1. Libros más prestados.
 * 2. Historial por estudiante.
 * 3. Listado de libros actualmente en préstamo con cálculo de atrasos.
 */
public class ReporteController {

    private final PrestamoDAO prestamoDAO;

    public ReporteController() {
        this.prestamoDAO = new PrestamoDAO();
    }

    /**
     * Reporte 1: Libros más prestados.
     */
    public List<ReporteLibroPrestado> obtenerLibrosMasPrestados() throws SQLException {
        return prestamoDAO.readLibrosMasPrestados();
    }

    /**
     * Reporte 2: Historial por estudiante buscando por ID.
     */
    public List<Prestamo> obtenerHistorialEstudiante(int idEstudiante) throws SQLException {
        return prestamoDAO.readByEstudiante(idEstudiante);
    }

    /**
     * Reporte 2 alternativo: Historial por estudiante buscando por correo.
     */
    public List<Prestamo> obtenerHistorialEstudiantePorCorreo(String correo) throws SQLException {
        return prestamoDAO.readByCorreoEstudiante(correo);
    }

    /**
     * Reporte 3: Listado de libros actualmente en préstamo (vigentes y con atraso).
     */
    public List<Prestamo> obtenerPrestamosActivos() throws SQLException {
        return prestamoDAO.readPrestamosActivos();
    }
}
