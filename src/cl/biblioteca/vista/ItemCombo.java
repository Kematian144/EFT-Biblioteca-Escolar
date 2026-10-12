package cl.biblioteca.vista;

/**
 * Elemento genérico para JComboBox que almacena un identificador u objeto
 * y muestra una descripción amigable al usuario.
 * @param <T> Tipo del valor o identificador almacenado
 */
public class ItemCombo<T> {

    private final T valor;
    private final String etiqueta;

    public ItemCombo(T valor, String etiqueta) {
        this.valor = valor;
        this.etiqueta = etiqueta;
    }

    public T getValor() {
        return valor;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemCombo<?> itemCombo = (ItemCombo<?>) o;
        return valor != null ? valor.equals(itemCombo.valor) : itemCombo.valor == null;
    }

    @Override
    public int hashCode() {
        return valor != null ? valor.hashCode() : 0;
    }
}
