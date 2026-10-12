package cl.biblioteca.modelo;

/**
 * Interfaz que define el comportamiento de elementos que pueden ser prestados
 * en la biblioteca escolar, aplicando el principio de abstracción y polimorfismo.
 */
public interface Prestable {

    /**
     * Determina si el ejemplar cuenta con stock disponible para préstamo.
     * @return true si tiene stock mayor a cero, false en caso contrario
     */
    boolean isDisponible();

    /**
     * Aplica la disminución de unidades al realizarse un préstamo.
     */
    void prestar();

    /**
     * Aplica el incremento de unidades al recibirse una devolución.
     */
    void devolver();
}
