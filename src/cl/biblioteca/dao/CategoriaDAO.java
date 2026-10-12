package cl.biblioteca.dao;

import cl.biblioteca.conexion.DatabaseConnection;
import cl.biblioteca.modelo.Categoria;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Categoria.
 * Gestiona las operaciones de persistencia en la tabla 'categorias'.
 */
public class CategoriaDAO implements DAO<Categoria> {

    @Override
    public boolean create(Categoria c) throws SQLException {
        String sql = "INSERT INTO categorias (nombre) VALUES (?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getNombre());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<Categoria> readAll() throws SQLException {
        String sql = "SELECT id, nombre FROM categorias ORDER BY id ASC";
        List<Categoria> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Categoria c = new Categoria();
                c.setId(rs.getInt("id"));
                c.setNombre(rs.getString("nombre"));
                lista.add(c);
            }
            return lista;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public Categoria readById(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM categorias WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                Categoria c = new Categoria();
                c.setId(rs.getInt("id"));
                c.setNombre(rs.getString("nombre"));
                return c;
            }
            return null;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean update(Categoria c) throws SQLException {
        String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, c.getNombre());
            ps.setInt(2, c.getId());

            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id = ?";
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
}
