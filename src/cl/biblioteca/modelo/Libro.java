package cl.biblioteca.modelo;

/**
 * Representa la entidad Libro en el inventario de la biblioteca escolar.
 * Implementa la interfaz Prestable, demostrando polimorfismo y lógica de negocio.
 */
public class Libro implements Prestable {

    private int id;
    private String titulo;
    private String autor;
    private String isbn;
    private String editorial;
    private int stock;
    private int idCategoria;
    private String nombreCategoria; // Para despliegue visual con JOIN

    public Libro() {
    }

    public Libro(int id, String titulo, String autor, String isbn, String editorial, int stock, int idCategoria) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
        this.stock = stock;
        this.idCategoria = idCategoria;
    }

    public Libro(String titulo, String autor, String isbn, String editorial, int stock, int idCategoria) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.editorial = editorial;
        this.stock = stock;
        this.idCategoria = idCategoria;
    }

    // Métodos de la interfaz Prestable
    @Override
    public boolean isDisponible() {
        return this.stock > 0;
    }

    @Override
    public void prestar() {
        if (isDisponible()) {
            this.stock--;
        } else {
            throw new IllegalStateException("No hay stock disponible para prestar el libro: " + titulo);
        }
    }

    @Override
    public void devolver() {
        this.stock++;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getNombreCategoria() { return nombreCategoria; }
    public void setNombreCategoria(String nombreCategoria) { this.nombreCategoria = nombreCategoria; }

    @Override
    public String toString() {
        return titulo + " - " + autor + " (Stock: " + stock + ")";
    }
}
