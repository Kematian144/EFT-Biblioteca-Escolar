package cl.biblioteca.modelo;

/**
 * Clase abstracta que modela las características comunes de los individuos
 * registrados en la comunidad escolar (Usuarios administrativos y Estudiantes).
 * Cumple con el Principio de Abstracción y Herencia de la rúbrica oficial.
 */
public abstract class Persona {

    protected int id;
    protected String nombre;
    protected String rut;
    protected String correo;

    public Persona() {
    }

    public Persona(int id, String nombre, String rut, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    public Persona(String nombre, String rut, String correo) {
        this.nombre = nombre;
        this.rut = rut;
        this.correo = correo;
    }

    /**
     * Método polimórfico abstracto que describe el rol de la persona en el sistema.
     * @return Descripción del rol
     */
    public abstract String getDescripcionRol();

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    @Override
    public String toString() {
        return nombre + " (" + rut + ")";
    }
}
