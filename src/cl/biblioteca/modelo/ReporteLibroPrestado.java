package cl.biblioteca.modelo;

/**
 * Representa los datos estadísticos para el reporte de libros más solicitados.
 */
public class ReporteLibroPrestado {

    private int idLibro;
    private String titulo;
    private String autor;
    private String categoria;
    private int totalPrestamos;
    private int stockActual;

    public ReporteLibroPrestado() {
    }

    public ReporteLibroPrestado(int idLibro, String titulo, String autor, String categoria, int totalPrestamos, int stockActual) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.totalPrestamos = totalPrestamos;
        this.stockActual = stockActual;
    }

    public int getIdLibro() { return idLibro; }
    public void setIdLibro(int idLibro) { this.idLibro = idLibro; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getTotalPrestamos() { return totalPrestamos; }
    public void setTotalPrestamos(int totalPrestamos) { this.totalPrestamos = totalPrestamos; }

    public int getStockActual() { return stockActual; }
    public void setStockActual(int stockActual) { this.stockActual = stockActual; }
}
