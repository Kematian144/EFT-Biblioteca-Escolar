package cl.biblioteca.modelo;

/**
 * Representa a un estudiante beneficiario de la biblioteca escolar.
 * Extiende de Persona demostrando herencia y especialización.
 */
public class Estudiante extends Persona {

    private String curso;

    public Estudiante() {
        super();
    }

    public Estudiante(int id, String nombre, String rut, String curso, String correo) {
        super(id, nombre, rut, correo);
        this.curso = curso;
    }

    public Estudiante(String nombre, String rut, String curso, String correo) {
        super(nombre, rut, correo);
        this.curso = curso;
    }

    @Override
    public String getDescripcionRol() {
        return "Estudiante Escolar - Curso: " + (curso != null ? curso : "Sin Asignar");
    }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    @Override
    public String toString() {
        return nombre + " (" + rut + ") - " + curso;
    }
}
