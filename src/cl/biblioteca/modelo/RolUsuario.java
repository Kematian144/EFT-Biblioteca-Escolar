package cl.biblioteca.modelo;

/**
 * Enumeración que define los roles de acceso en el sistema de biblioteca.
 */
public enum RolUsuario {
    BIBLIOTECARIO("bibliotecario"),
    ESTUDIANTE("estudiante");

    private final String valorDb;

    RolUsuario(String valorDb) {
        this.valorDb = valorDb;
    }

    public String getValorDb() {
        return valorDb;
    }

    public static RolUsuario desdeTexto(String texto) {
        if (texto == null) return ESTUDIANTE;
        String t = texto.trim().toLowerCase();
        for (RolUsuario r : values()) {
            if (r.valorDb.equalsIgnoreCase(t) || r.name().equalsIgnoreCase(t)) {
                return r;
            }
        }
        return ESTUDIANTE;
    }
}
