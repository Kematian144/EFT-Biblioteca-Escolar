package cl.biblioteca.modelo;

/**
 * Representa a un usuario del sistema (con credenciales de acceso y rol).
 * Extiende de Persona demostrando herencia y polimorfismo.
 */
public class Usuario extends Persona {

    private String contrasena;
    private RolUsuario rol;

    public Usuario() {
        super();
        this.rol = RolUsuario.ESTUDIANTE;
    }

    public Usuario(int id, String nombre, String rut, String correo, String contrasena, RolUsuario rol) {
        super(id, nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = (rol != null) ? rol : RolUsuario.ESTUDIANTE;
    }

    public Usuario(String nombre, String rut, String correo, String contrasena, RolUsuario rol) {
        super(nombre, rut, correo);
        this.contrasena = contrasena;
        this.rol = (rol != null) ? rol : RolUsuario.ESTUDIANTE;
    }

    @Override
    public String getDescripcionRol() {
        if (rol == RolUsuario.BIBLIOTECARIO) {
            return "Bibliotecario (Gestión Total del Sistema)";
        } else {
            return "Estudiante (Autoservicio y Consulta)";
        }
    }

    public boolean esBibliotecario() {
        return this.rol == RolUsuario.BIBLIOTECARIO;
    }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public RolUsuario getRol() { return rol; }
    public void setRol(RolUsuario rol) { this.rol = rol; }
}
