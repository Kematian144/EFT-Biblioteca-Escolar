package cl.biblioteca.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestiona la conexión a la base de datos MySQL aplicando el patrón de diseño Singleton.
 * Asegura una única instancia de configuración y conexión para todo el sistema,
 * previniendo fugas de memoria y sobrecarga en el servidor de base de datos.
 */
public class DatabaseConnection {

    // Única instancia de la clase (Patrón Singleton)
    private static DatabaseConnection instance;

    // Configuración de la base de datos
    private String host = "localhost";
    private int puerto = 3306;
    private String baseDatos = "biblioteca";
    private String usuario = "root";
    private String contrasena = "ML1272005..";

    // Constructor privado para impedir instanciación externa
    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                System.err.println("Advertencia: Driver JDBC no encontrado en classpath: " + ex.getMessage());
            }
        }
    }

    /**
     * Retorna la única instancia de DatabaseConnection (Singleton thread-safe).
     * @return Instancia única de DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Construye la URL JDBC de conexión con parámetros recomendados.
     */
    public String getUrl() {
        return "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Santiago&characterEncoding=UTF-8";
    }

    /**
     * Obtiene una conexión activa a la base de datos MySQL.
     * Incorpora mecanismo de fallback automático para compatibilidad con entornos de evaluación (XAMPP root).
     * @return Connection activa
     * @throws SQLException si falla la conexión
     */
    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(getUrl(), usuario, contrasena);
        } catch (SQLException e) {
            // Fallback 1: Intentar con root sin contraseña (estándar XAMPP / Duoc UC)
            try {
                return DriverManager.getConnection(getUrl(), "root", "");
            } catch (SQLException ex1) {
                // Fallback 2: Intentar con root y clave administrativa del equipo
                try {
                    return DriverManager.getConnection(getUrl(), "root", "Ml1272005..2107");
                } catch (SQLException ex2) {
                    throw e; // Lanzar excepción original si ninguno funcionó
                }
            }
        }
    }

    /**
     * Verifica si la conexión con la base de datos es exitosa.
     * @return true si conecta correctamente, false en caso contrario
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error al probar conexión con base de datos: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cierra de forma segura los recursos de base de datos en bloques finally.
     */
    public static void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    // Getters y Setters para configuración dinámica si fuera necesaria
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public int getPuerto() { return puerto; }
    public void setPuerto(int puerto) { this.puerto = puerto; }
    public String getBaseDatos() { return baseDatos; }
    public void setBaseDatos(String baseDatos) { this.baseDatos = baseDatos; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
