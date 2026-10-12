package cl.biblioteca.controlador;

import cl.biblioteca.dao.UsuarioDAO;
import cl.biblioteca.modelo.RolUsuario;
import cl.biblioteca.modelo.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Controlador de Usuarios del Sistema (Patrón MVC).
 * Permite listar, registrar, modificar y eliminar cuentas de usuario.
 */
public class UsuarioController {

    private final UsuarioDAO usuarioDAO;

    public UsuarioController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public List<Usuario> listarTodos() throws SQLException {
        return usuarioDAO.readAll();
    }

    public Usuario obtenerPorId(int id) throws SQLException {
        return usuarioDAO.readById(id);
    }

    public boolean registrar(String nombre, String rut, String correo, String contrasena, RolUsuario rol)
            throws SQLException, IllegalArgumentException {
        validarCampos(nombre, rut, correo, contrasena, rol);
        Usuario u = new Usuario(nombre.trim(), rut.trim(), correo.trim(), contrasena, rol);
        return usuarioDAO.create(u);
    }

    public boolean actualizar(int id, String nombre, String rut, String correo, String contrasena, RolUsuario rol)
            throws SQLException, IllegalArgumentException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido.");
        }
        validarCampos(nombre, rut, correo, contrasena, rol);
        Usuario u = new Usuario(id, nombre.trim(), rut.trim(), correo.trim(), contrasena, rol);
        return usuarioDAO.update(u);
    }

    public boolean eliminar(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("ID de usuario inválido.");
        }
        return usuarioDAO.delete(id);
    }

    private void validarCampos(String nombre, String rut, String correo, String contrasena, RolUsuario rol) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario es obligatorio.");
        }
        if (rut == null || rut.trim().isEmpty()) {
            throw new IllegalArgumentException("El RUT es obligatorio.");
        }
        if (correo == null || correo.trim().isEmpty() || !correo.contains("@")) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        if (rol == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol válido.");
        }
    }
}
