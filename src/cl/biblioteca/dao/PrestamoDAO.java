package cl.biblioteca.dao;

import cl.biblioteca.conexion.DatabaseConnection;
import cl.biblioteca.modelo.Prestamo;
import cl.biblioteca.modelo.ReporteLibroPrestado;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object para la entidad Prestamo.
 * Administra el ciclo de vida de los préstamos, devoluciones y consultas para reportes del sistema.
 */
public class PrestamoDAO implements DAO<Prestamo> {

    @Override
    public boolean create(Prestamo p) throws SQLException {
        String sql = "INSERT INTO prestamos (id_estudiante, id_libro, fecha_prestamo, fecha_devolucion, devuelto) "
                   + "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<Prestamo> readAll() throws SQLException {
        String sql = "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                   + "e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, e.curso AS estudiante_curso, "
                   + "l.titulo AS libro_titulo, l.autor AS libro_autor, l.isbn AS libro_isbn "
                   + "FROM prestamos p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id "
                   + "INNER JOIN libros l ON p.id_libro = l.id "
                   + "ORDER BY p.id DESC";
        List<Prestamo> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapRow(rs));
            }
            return lista;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    @Override
    public Prestamo readById(int id) throws SQLException {
        String sql = "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                   + "e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, e.curso AS estudiante_curso, "
                   + "l.titulo AS libro_titulo, l.autor AS libro_autor, l.isbn AS libro_isbn "
                   + "FROM prestamos p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id "
                   + "INNER JOIN libros l ON p.id_libro = l.id "
                   + "WHERE p.id = ?";
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
    public boolean update(Prestamo p) throws SQLException {
        String sql = "UPDATE prestamos SET id_estudiante = ?, id_libro = ?, fecha_prestamo = ?, "
                   + "fecha_devolucion = ?, devuelto = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, p.getIdEstudiante());
            ps.setInt(2, p.getIdLibro());
            ps.setDate(3, Date.valueOf(p.getFechaPrestamo()));
            ps.setDate(4, Date.valueOf(p.getFechaDevolucion()));
            ps.setBoolean(5, p.isDevuelto());
            ps.setInt(6, p.getId());

            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM prestamos WHERE id = ?";
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
     * Marca un préstamo como devuelto (devuelto = true).
     */
    public boolean registrarDevolucion(int idPrestamo) throws SQLException {
        String sql = "UPDATE prestamos SET devuelto = TRUE WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate() > 0;
        } finally {
            DatabaseConnection.closeResources(null, ps, conn);
        }
    }

    /**
     * Obtiene el listado de libros actualmente en préstamo (pendientes de devolución).
     */
    public List<Prestamo> readPrestamosActivos() throws SQLException {
        String sql = "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                   + "e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, e.curso AS estudiante_curso, "
                   + "l.titulo AS libro_titulo, l.autor AS libro_autor, l.isbn AS libro_isbn "
                   + "FROM prestamos p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id "
                   + "INNER JOIN libros l ON p.id_libro = l.id "
                   + "WHERE p.devuelto = FALSE "
                   + "ORDER BY p.fecha_devolucion ASC";
        List<Prestamo> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
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
     * Obtiene el historial completo de préstamos para un estudiante determinado.
     */
    public List<Prestamo> readByEstudiante(int idEstudiante) throws SQLException {
        String sql = "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                   + "e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, e.curso AS estudiante_curso, "
                   + "l.titulo AS libro_titulo, l.autor AS libro_autor, l.isbn AS libro_isbn "
                   + "FROM prestamos p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id "
                   + "INNER JOIN libros l ON p.id_libro = l.id "
                   + "WHERE p.id_estudiante = ? "
                   + "ORDER BY p.fecha_prestamo DESC";
        List<Prestamo> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idEstudiante);
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
     * Obtiene el historial de préstamos buscando por correo o RUT del estudiante.
     */
    public List<Prestamo> readByCorreoEstudiante(String correo) throws SQLException {
        String sql = "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                   + "e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, e.curso AS estudiante_curso, "
                   + "l.titulo AS libro_titulo, l.autor AS libro_autor, l.isbn AS libro_isbn "
                   + "FROM prestamos p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id "
                   + "INNER JOIN libros l ON p.id_libro = l.id "
                   + "WHERE e.correo = ? "
                   + "ORDER BY p.fecha_prestamo DESC";
        List<Prestamo> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, correo.trim());
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
     * Reporte estadístico: Libros más prestados en el sistema con su cantidad y stock actual.
     */
    public List<ReporteLibroPrestado> readLibrosMasPrestados() throws SQLException {
        String sql = "SELECT l.id, l.titulo, l.autor, c.nombre AS categoria, l.stock, COUNT(p.id) AS total_prestamos "
                   + "FROM libros l "
                   + "LEFT JOIN categorias c ON l.id_categoria = c.id "
                   + "LEFT JOIN prestamos p ON l.id = p.id_libro "
                   + "GROUP BY l.id, l.titulo, l.autor, c.nombre, l.stock "
                   + "ORDER BY total_prestamos DESC, l.titulo ASC";
        List<ReporteLibroPrestado> lista = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                ReporteLibroPrestado r = new ReporteLibroPrestado();
                r.setIdLibro(rs.getInt("id"));
                r.setTitulo(rs.getString("titulo"));
                r.setAutor(rs.getString("autor"));
                r.setCategoria(rs.getString("categoria"));
                r.setStockActual(rs.getInt("stock"));
                r.setTotalPrestamos(rs.getInt("total_prestamos"));
                lista.add(r);
            }
            return lista;
        } finally {
            DatabaseConnection.closeResources(rs, ps, conn);
        }
    }

    /**
     * Búsqueda por filtro libre en préstamos.
     */
    public List<Prestamo> search(String criterio) throws SQLException {
        if (criterio == null || criterio.trim().isEmpty()) {
            return readAll();
        }
        String sql = "SELECT p.id, p.id_estudiante, p.id_libro, p.fecha_prestamo, p.fecha_devolucion, p.devuelto, "
                   + "e.nombre AS estudiante_nombre, e.rut AS estudiante_rut, e.curso AS estudiante_curso, "
                   + "l.titulo AS libro_titulo, l.autor AS libro_autor, l.isbn AS libro_isbn "
                   + "FROM prestamos p "
                   + "INNER JOIN estudiantes e ON p.id_estudiante = e.id "
                   + "INNER JOIN libros l ON p.id_libro = l.id "
                   + "WHERE e.nombre LIKE ? OR e.rut LIKE ? OR l.titulo LIKE ? OR l.autor LIKE ? "
                   + "ORDER BY p.id DESC";
        List<Prestamo> lista = new ArrayList<>();
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

    private Prestamo mapRow(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setId(rs.getInt("id"));
        p.setIdEstudiante(rs.getInt("id_estudiante"));
        p.setIdLibro(rs.getInt("id_libro"));

        Date fp = rs.getDate("fecha_prestamo");
        if (fp != null) {
            p.setFechaPrestamo(fp.toLocalDate());
        }

        Date fd = rs.getDate("fecha_devolucion");
        if (fd != null) {
            p.setFechaDevolucion(fd.toLocalDate());
        }

        p.setDevuelto(rs.getBoolean("devuelto"));
        p.setNombreEstudiante(rs.getString("estudiante_nombre"));
        p.setRutEstudiante(rs.getString("estudiante_rut"));
        p.setCursoEstudiante(rs.getString("estudiante_curso"));
        p.setTituloLibro(rs.getString("libro_titulo"));
        p.setAutorLibro(rs.getString("libro_autor"));
        p.setIsbnLibro(rs.getString("libro_isbn"));
        return p;
    }
}
