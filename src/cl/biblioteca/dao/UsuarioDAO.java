package cl.biblioteca.dao;

import cl.biblioteca.conexion.DatabaseConnection;
import cl.biblioteca.modelo.RolUsuario;
import cl.biblioteca.modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Usuario.
 * Gestiona autenticación, verificación de roles y operaciones CRUD en la tabla 'usuarios'.
 */
public class UsuarioDAO implements DAO<Usuario> {

    @Override
    public boolean create(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, rut, correo, `contraseña`, rol) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol().getValorDb());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    u.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<Usuario> readAll() throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol FROM usuarios ORDER BY id ASC";
        List<Usuario> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setRut(rs.getString("rut"));
                u.setCorreo(rs.getString("correo"));
                u.setContrasena(rs.getString("contraseña"));
                u.setRol(RolUsuario.desdeTexto(rs.getString("rol")));
                lista.add(u);
            }
            return lista;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public Usuario readById(int id) throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol FROM usuarios WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setRut(rs.getString("rut"));
                u.setCorreo(rs.getString("correo"));
                u.setContrasena(rs.getString("contraseña"));
                u.setRol(RolUsuario.desdeTexto(rs.getString("rol")));
                return u;
            }
            return null;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean update(Usuario u) throws SQLException {
        String sql = "UPDATE usuarios SET nombre = ?, rut = ?, correo = ?, `contraseña` = ?, rol = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getRut());
            ps.setString(3, u.getCorreo());
            ps.setString(4, u.getContrasena());
            ps.setString(5, u.getRol().getValorDb());
            ps.setInt(6, u.getId());

            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeResources(null, ps, conn);
        }
    }

    /**
     * Valida credenciales de acceso para el módulo de autenticación.
     * @param correo Correo electrónico del usuario
     * @param contrasena Contraseña ingresada
     * @return Objeto Usuario si la autenticación es correcta, null en caso contrario
     * @throws SQLException si ocurre un error de base de datos
     */
    public Usuario login(String correo, String contrasena) throws SQLException {
        String sql = "SELECT id, nombre, rut, correo, `contraseña`, rol FROM usuarios WHERE correo = ? AND `contraseña` = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, correo.trim());
            ps.setString(2, contrasena);
            rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setRut(rs.getString("rut"));
                u.setCorreo(rs.getString("correo"));
                u.setContrasena(rs.getString("contraseña"));
                u.setRol(RolUsuario.desdeTexto(rs.getString("rol")));
                return u;
            }
            return null;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }
}
