package cl.biblioteca.dao;

import cl.biblioteca.conexion.DatabaseConnection;
import cl.biblioteca.modelo.Estudiante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Estudiante.
 * Permite registrar, modificar, eliminar y listar estudiantes en la tabla 'estudiantes'.
 */
public class EstudianteDAO implements DAO<Estudiante> {

    @Override
    public boolean create(Estudiante e) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre, rut, curso, correo) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<Estudiante> readAll() throws SQLException {
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes ORDER BY id ASC";
        List<Estudiante> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Estudiante e = new Estudiante();
                e.setId(rs.getInt("id"));
                e.setNombre(rs.getString("nombre"));
                e.setRut(rs.getString("rut"));
                e.setCurso(rs.getString("curso"));
                e.setCorreo(rs.getString("correo"));
                lista.add(e);
            }
            return lista;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public Estudiante readById(int id) throws SQLException {
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                Estudiante e = new Estudiante();
                e.setId(rs.getInt("id"));
                e.setNombre(rs.getString("nombre"));
                e.setRut(rs.getString("rut"));
                e.setCurso(rs.getString("curso"));
                e.setCorreo(rs.getString("correo"));
                return e;
            }
            return null;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean update(Estudiante e) throws SQLException {
        String sql = "UPDATE estudiantes SET nombre = ?, rut = ?, curso = ?, correo = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getRut());
            ps.setString(3, e.getCurso());
            ps.setString(4, e.getCorreo());
            ps.setInt(5, e.getId());

            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
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
     * Búsqueda por coincidencia en nombre, rut, curso o correo.
     */
    public List<Estudiante> search(String criterio) throws SQLException {
        if (criterio == null || criterio.trim().isEmpty()) {
            return readAll();
        }
        String sql = "SELECT id, nombre, rut, curso, correo FROM estudiantes "
                   + "WHERE nombre LIKE ? OR rut LIKE ? OR curso LIKE ? OR correo LIKE ? ORDER BY id ASC";
        List<Estudiante> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            String term = "%" + criterio.trim() + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            ps.setString(3, term);
            ps.setString(4, term);
            rs = ps.executeQuery();

            while (rs.next()) {
                Estudiante e = new Estudiante();
                e.setId(rs.getInt("id"));
                e.setNombre(rs.getString("nombre"));
                e.setRut(rs.getString("rut"));
                e.setCurso(rs.getString("curso"));
                e.setCorreo(rs.getString("correo"));
                lista.add(e);
            }
            return lista;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }
}
