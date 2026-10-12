package cl.biblioteca.controlador;

import cl.biblioteca.dao.LibroDAO;
import cl.biblioteca.dao.PrestamoDAO;
import cl.biblioteca.modelo.Libro;
import cl.biblioteca.modelo.Prestamo;

import javax.swing.SwingUtilities;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de Préstamos y Devoluciones (Patrón MVC + Concurrencia).
 * Gestiona el ciclo de vida de los préstamos ejecutando operaciones críticas
 * en hilos independientes (Thread / segundo plano) y sincronizando el acceso
 * al inventario mediante métodos synchronized para evitar condiciones de carrera.
 */
public class PrestamoController {

    private final PrestamoDAO prestamoDAO;
    private final LibroDAO libroDAO;

    /**
     * Interfaz de callback para comunicar resultados asíncronos a la interfaz gráfica Swing.
     */
    public interface CallbackOperacion {
        void onSuccess(String mensaje);
        void onError(String error, Exception ex);
    }

    public PrestamoController() {
        this.prestamoDAO = new PrestamoDAO();
        this.libroDAO = new LibroDAO();
    }

    public List<Prestamo> listarTodos() throws SQLException {
        return prestamoDAO.readAll();
    }

    public List<Prestamo> listarActivos() throws SQLException {
        return prestamoDAO.readPrestamosActivos();
    }

    public List<Prestamo> listarPorEstudiante(int idEstudiante) throws SQLException {
        return prestamoDAO.readByEstudiante(idEstudiante);
    }

    public List<Prestamo> listarPorCorreoEstudiante(String correo) throws SQLException {
        return prestamoDAO.readByCorreoEstudiante(correo);
    }

    public List<Prestamo> buscar(String criterio) throws SQLException {
        return prestamoDAO.search(criterio);
    }

    public boolean eliminar(int id) throws SQLException {
        return prestamoDAO.delete(id);
    }

    /**
     * Registra un préstamo en SEGUNDO PLANO mediante un HILO INDEPENDIENTE.
     * Esto evita que la interfaz gráfica Swing (Event Dispatch Thread) se congele durante
     * el acceso a la base de datos y la sincronización del stock.
     *
     * @param idEstudiante ID del estudiante solicitante
     * @param idLibro ID del libro a prestar
     * @param fechaPrestamo Fecha en que se otorga el préstamo
     * @param fechaDevolucion Fecha límite pactada de devolución
     * @param callback Notificador de término para la interfaz visual
     */
    public void registrarPrestamoAsync(int idEstudiante, int idLibro, LocalDate fechaPrestamo,
                                       LocalDate fechaDevolucion, CallbackOperacion callback) {
        // Validación previa en hilo actual
        if (idEstudiante <= 0) {
            if (callback != null) callback.onError("Debe seleccionar un estudiante válido.", null);
            return;
        }
        if (idLibro <= 0) {
            if (callback != null) callback.onError("Debe seleccionar un libro válido.", null);
            return;
        }
        if (fechaPrestamo == null || fechaDevolucion == null) {
            if (callback != null) callback.onError("Las fechas de préstamo y devolución son obligatorias.", null);
            return;
        }
        if (fechaDevolucion.isBefore(fechaPrestamo)) {
            if (callback != null) callback.onError("La fecha de devolución no puede ser anterior a la fecha de préstamo.", null);
            return;
        }

        // Hilo independiente para ejecución en segundo plano (Requisito de Concurrencia)
        Thread hiloPrestamo = new Thread(() -> {
            try {
                // Paso 1: Modificar stock con método sincronizado (Evita condición de carrera)
                boolean stockModificado = libroDAO.modificarStockSincronizado(idLibro, -1);
                if (!stockModificado) {
                    notificarError(callback, "No hay stock disponible para prestar este libro o el ejemplar no existe.", null);
                    return;
                }

                // Paso 2: Registrar el préstamo en la base de datos
                Prestamo prestamo = new Prestamo(idEstudiante, idLibro, fechaPrestamo, fechaDevolucion);
                boolean creado = prestamoDAO.create(prestamo);

                if (!creado) {
                    // Revertir stock sincronizado si falló la creación del registro
                    libroDAO.modificarStockSincronizado(idLibro, 1);
                    notificarError(callback, "No se pudo registrar el préstamo en el sistema.", null);
                    return;
                }

                // Paso 3: Notificar éxito a la interfaz Swing
                Libro libro = libroDAO.readById(idLibro);
                String titulo = libro != null ? libro.getTitulo() : "ID #" + idLibro;
                notificarExito(callback, "Préstamo registrado exitosamente para el libro '" + titulo + "'.");

            } catch (SQLException ex) {
                // Revertir posible decremento en caso de excepción
                try {
                    libroDAO.modificarStockSincronizado(idLibro, 1);
                } catch (Exception ignored) {}
                notificarError(callback, "Error de base de datos al registrar préstamo: " + ex.getMessage(), ex);
            } catch (Exception ex) {
                notificarError(callback, "Error inesperado en proceso concurrente: " + ex.getMessage(), ex);
            }
        }, "Hilo-RegistroPrestamo-" + System.currentTimeMillis());

        hiloPrestamo.start();
    }

    /**
     * Registra la devolución de un libro en SEGUNDO PLANO mediante un HILO INDEPENDIENTE.
     * Actualiza el estado a devuelto y repone el stock utilizando el método sincronizado.
     *
     * @param idPrestamo ID del préstamo a finalizar
     * @param idLibro ID del libro asociado para restaurar stock
     * @param callback Notificador de término para la interfaz visual
     */
    public void registrarDevolucionAsync(int idPrestamo, int idLibro, CallbackOperacion callback) {
        if (idPrestamo <= 0) {
            if (callback != null) callback.onError("ID de préstamo inválido.", null);
            return;
        }

        Thread hiloDevolucion = new Thread(() -> {
            try {
                // Paso 1: Marcar préstamo como devuelto
                boolean devuelto = prestamoDAO.registrarDevolucion(idPrestamo);
                if (!devuelto) {
                    notificarError(callback, "No se encontró el préstamo para registrar la devolución.", null);
                    return;
                }

                // Paso 2: Reponer stock con método sincronizado
                if (idLibro > 0) {
                    libroDAO.modificarStockSincronizado(idLibro, 1);
                }

                // Paso 3: Notificar éxito
                notificarExito(callback, "Devolución registrada exitosamente. Stock actualizado en biblioteca.");

            } catch (SQLException ex) {
                notificarError(callback, "Error al registrar la devolución: " + ex.getMessage(), ex);
            } catch (Exception ex) {
                notificarError(callback, "Error inesperado en proceso de devolución: " + ex.getMessage(), ex);
            }
        }, "Hilo-DevolucionPrestamo-" + System.currentTimeMillis());

        hiloDevolucion.start();
    }

    private void notificarExito(CallbackOperacion callback, String mensaje) {
        if (callback != null) {
            SwingUtilities.invokeLater(() -> callback.onSuccess(mensaje));
        }
    }

    private void notificarError(CallbackOperacion callback, String error, Exception ex) {
        if (callback != null) {
            SwingUtilities.invokeLater(() -> callback.onError(error, ex));
        }
    }
}
