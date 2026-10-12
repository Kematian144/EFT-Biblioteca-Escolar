package cl.biblioteca.modelo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Representa un préstamo de libro realizado por un estudiante.
 * Mapea la tabla 'prestamos' de la base de datos e incluye lógica de negocio
 * para cálculo de atrasos y vencimientos.
 */
public class Prestamo {

    private int id;
    private int idEstudiante;
    private int idLibro;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private boolean devuelto;

    // Campos complementarios para JOINs relacionales y reportes
    private String nombreEstudiante;
    private String rutEstudiante;
    private String cursoEstudiante;
    private String tituloLibro;
    private String autorLibro;
    private String isbnLibro;

    public Prestamo() {
        this.devuelto = false;
    }

    public Prestamo(int id, int idEstudiante, int idLibro, LocalDate fechaPrestamo, LocalDate fechaDevolucion, boolean devuelto) {
        this.id = id;
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = devuelto;
    }

    public Prestamo(int idEstudiante, int idLibro, LocalDate fechaPrestamo, LocalDate fechaDevolucion) {
        this.idEstudiante = idEstudiante;
        this.idLibro = idLibro;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.devuelto = false;
    }

    /**
     * Evalúa si el préstamo se encuentra actualmente con retraso en la devolución.
     * @return true si no ha sido devuelto y la fecha actual supera la fecha de vencimiento
     */
    public boolean isAtrasado() {
        if (devuelto || fechaDevolucion == null) {
            return false;
        }
        return LocalDate.now().isAfter(fechaDevolucion);
    }

    /**
     * Calcula la cantidad de días de retraso respecto a la fecha pactada.
     * @return Días de atraso (0 si está al día o ya devuelto)
     */
    public long getDiasAtraso() {
        if (!isAtrasado()) {
            return 0;
        }
        return ChronoUnit.DAYS.between(fechaDevolucion, LocalDate.now());
    }

    /**
     * Retorna una descripción textual legible del estado del préstamo.
     */
    public String getEstadoTexto() {
        if (devuelto) {
            return "DEVUELTO";
        } else if (isAtrasado()) {
            return "ATRASADO (" + getDiasAtraso() + " días)";
        } else {
            return "VIGENTE (Al día)";
        }
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }

    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }

    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }

    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }

    public boolean isDevuelto() { return devuelto; }
    public void setDevuelto(boolean devuelto) { this.devuelto = devuelto; }

    public String getNombreEstudiante() { return nombreEstudiante; }
    public void setNombreEstudiante(String nombreEstudiante) { this.nombreEstudiante = nombreEstudiante; }

    public String getRutEstudiante() { return rutEstudiante; }
    public void setRutEstudiante(String rutEstudiante) { this.rutEstudiante = rutEstudiante; }

    public String getCursoEstudiante() { return cursoEstudiante; }
    public void setCursoEstudiante(String cursoEstudiante) { this.cursoEstudiante = cursoEstudiante; }

    public String getTituloLibro() { return tituloLibro; }
    public void setTituloLibro(String tituloLibro) { this.tituloLibro = tituloLibro; }

    public String getAutorLibro() { return autorLibro; }
    public void setAutorLibro(String autorLibro) { this.autorLibro = autorLibro; }

    public String getIsbnLibro() { return isbnLibro; }
    public void setIsbnLibro(String isbnLibro) { this.isbnLibro = isbnLibro; }
}
