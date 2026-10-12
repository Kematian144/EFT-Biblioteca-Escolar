package cl.biblioteca.controlador;

import cl.biblioteca.dao.UsuarioDAO;
import cl.biblioteca.modelo.RolUsuario;
import cl.biblioteca.modelo.Usuario;

import java.sql.SQLException;

/**
 * Controlador de Autenticación y Gestión de Sesión (Patrón MVC).
 * Administra el inicio de sesión, verificación de roles y estado del usuario autenticado.
 */
public class AutenticacionController {

    private final UsuarioDAO usuarioDAO;
    private static Usuario usuarioActual;

    public AutenticacionController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Intenta autenticar un usuario en el sistema.
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña ingresada
     * @return Objeto Usuario si la autenticación es exitosa
     * @throws IllegalArgumentException si los campos están vacíos
     * @throws SQLException si ocurre un error con la base de datos
     * @throws SecurityException si las credenciales son inválidas
     */
    public Usuario iniciarSesion(String correo, String contrasena) throws SQLException, IllegalArgumentException, SecurityException {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar su correo institucional.");
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar su contraseña.");
        }

        Usuario usuario = usuarioDAO.login(correo.trim(), contrasena);
        if (usuario == null) {
            throw new SecurityException("Credenciales incorrectas. Verifique correo y contraseña.");
        }

        usuarioActual = usuario;
        return usuario;
    }

    /**
     * Cierra la sesión activa actual.
     */
    public static void cerrarSesion() {
        usuarioActual = null;
    }

    /**
     * Obtiene el usuario que tiene sesión activa.
     */
    public static Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Comprueba si hay una sesión iniciada.
     */
    public static boolean haySesionActiva() {
        return usuarioActual != null;
    }

    /**
     * Comprueba si el usuario autenticado tiene rol Bibliotecario (acceso total).
     */
    public static boolean esBibliotecario() {
        return usuarioActual != null && usuarioActual.getRol() == RolUsuario.BIBLIOTECARIO;
    }

    /**
     * Comprueba si el usuario autenticado tiene rol Estudiante.
     */
    public static boolean esEstudiante() {
        return usuarioActual != null && usuarioActual.getRol() == RolUsuario.ESTUDIANTE;
    }
}
