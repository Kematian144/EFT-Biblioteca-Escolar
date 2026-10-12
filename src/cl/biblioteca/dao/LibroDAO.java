package cl.biblioteca.dao;

import cl.biblioteca.conexion.DatabaseConnection;
import cl.biblioteca.modelo.Libro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Libro.
 * Implementa operaciones CRUD y control de stock thread-safe con métodos sincronizados
 * para evitar condiciones de carrera en operaciones concurrentes de préstamo.
 */
public class LibroDAO implements DAO<Libro> {

    // Bloqueo estático para sincronización a nivel de proceso sobre operaciones críticas de stock
    private static final Object LOCK_STOCK = new Object();

    @Override
    public boolean create(Libro l) throws SQLException {
        String sql = "INSERT INTO libros (titulo, autor, isbn, editorial, stock, id_categoria) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    l.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<Libro> readAll() throws SQLException {
        String sql = "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre AS categoria_nombre "
                   + "FROM libros l "
                   + "LEFT JOIN categorias c ON l.id_categoria = c.id "
                   + "ORDER BY l.id ASC";
        List<Libro> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                Libro l = mapRow(rs);
                lista.add(l);
            }
            return lista;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public Libro readById(int id) throws SQLException {
        String sql = "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre AS categoria_nombre "
                   + "FROM libros l "
                   + "LEFT JOIN categorias c ON l.id_categoria = c.id "
                   + "WHERE l.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();

            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean update(Libro l) throws SQLException {
        String sql = "UPDATE libros SET titulo = ?, autor = ?, isbn = ?, editorial = ?, stock = ?, id_categoria = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, l.getTitulo());
            ps.setString(2, l.getAutor());
            ps.setString(3, l.getIsbn());
            ps.setString(4, l.getEditorial());
            ps.setInt(5, l.getStock());
            ps.setInt(6, l.getIdCategoria());
            ps.setInt(7, l.getId());

            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM libros WHERE id = ?";
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
     * Búsqueda por coincidencia en título, autor, isbn o editorial.
     */
    public List<Libro> search(String criterio) throws SQLException {
        if (criterio == null || criterio.trim().isEmpty()) {
            return readAll();
        }
        String sql = "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre AS categoria_nombre "
                   + "FROM libros l "
                   + "LEFT JOIN categorias c ON l.id_categoria = c.id "
                   + "WHERE l.titulo LIKE ? OR l.autor LIKE ? OR l.isbn LIKE ? OR l.editorial LIKE ? "
                   + "ORDER BY l.id ASC";
        List<Libro> lista = new ArrayList<>();
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
                lista.add(mapRow(rs));
            }
            return lista;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    /**
     * Filtra libros por categoría.
     */
    public List<Libro> readByCategoria(int idCategoria) throws SQLException {
        String sql = "SELECT l.id, l.titulo, l.autor, l.isbn, l.editorial, l.stock, l.id_categoria, c.nombre AS categoria_nombre "
                   + "FROM libros l "
                   + "LEFT JOIN categorias c ON l.id_categoria = c.id "
                   + "WHERE l.id_categoria = ? "
                   + "ORDER BY l.id ASC";
        List<Libro> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idCategoria);
            rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    /**
     * Método SINCRONIZADO para modificar el stock de un libro.
     * Garantiza exclusión mutua para evitar condiciones de carrera (Race Conditions)
     * cuando múltiples hilos de préstamo solicitan ejemplares simultáneamente.
     *
     * @param idLibro ID del libro
     * @param delta Cantidad a sumar (positivo para devolución, negativo para préstamo)
     * @return true si la modificación fue exitosa, false si no hay suficiente stock o no existe el libro
     * @throws SQLException si ocurre un error de base de datos
     */
    public synchronized boolean modificarStockSincronizado(int idLibro, int delta) throws SQLException {
        synchronized (LOCK_STOCK) {
            Connection conn = null;
            PreparedStatement psCheck = null;
            PreparedStatement psUpdate = null;
            ResultSet rs = null;

            try {
                conn = DatabaseConnection.getInstance().getConnection();
                // Bloqueo transaccional
                conn.setAutoCommit(false);

                // Verificar stock actual con FOR UPDATE para consistencia en BD
                String sqlCheck = "SELECT stock FROM libros WHERE id = ? FOR UPDATE";
                psCheck = conn.prepareStatement(sqlCheck);
                psCheck.setInt(1, idLibro);
                rs = psCheck.executeQuery();

                if (!rs.next()) {
                    conn.rollback();
                    return false;
                }

                int stockActual = rs.getInt("stock");
                int nuevoStock = stockActual + delta;

                if (nuevoStock < 0) {
                    // No hay suficiente inventario disponible
                    conn.rollback();
                    return false;
                }

                // Actualizar stock
                String sqlUpdate = "UPDATE libros SET stock = ? WHERE id = ?";
                psUpdate = conn.prepareStatement(sqlUpdate);
                psUpdate.setInt(1, nuevoStock);
                psUpdate.setInt(2, idLibro);
                int filas = psUpdate.executeUpdate();

                conn.commit();
                return filas > 0;
            } catch (SQLException ex) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ignored) {}
                }
                throw ex;
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                }
                DatabaseConnection.closeResources(rs, psCheck, null);
                if (psUpdate != null) {
                    try { psUpdate.close(); } catch (SQLException ignored) {}
                }
                if (conn != null) {
                    try { conn.close(); } catch (SQLException ignored) {}
                }
            }
        }
    }

    /**
     * Mapea un ResultSet a un objeto Libro.
     */
    private Libro mapRow(ResultSet rs) throws SQLException {
        Libro l = new Libro();
        l.setId(rs.getInt("id"));
        l.setTitulo(rs.getString("titulo"));
        l.setAutor(rs.getString("autor"));
        l.setIsbn(rs.getString("isbn"));
        l.setEditorial(rs.getString("editorial"));
        l.setStock(rs.getInt("stock"));
        l.setIdCategoria(rs.getInt("id_categoria"));
        l.setNombreCategoria(rs.getString("categoria_nombre"));
        return l;
    }
}
