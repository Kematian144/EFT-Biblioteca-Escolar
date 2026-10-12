# Sistema de Gestión de Biblioteca Escolar

**Evaluación Final Transversal – Desarrollo Orientado a Objetos II (PRY2203)**  
**Duoc UC – Semana 9**

---

## Descripción

Aplicación de escritorio en Java con interfaz gráfica (Swing / JFrame) para gestionar la circulación de libros en una biblioteca escolar. Implementa los patrones MVC, DAO y Singleton, con concurrencia para el registro de préstamos sin bloquear la interfaz.

---

## Tecnologías

| Componente | Detalle |
|---|---|
| Lenguaje | Java 21 |
| Interfaz | Swing (JFrame) |
| Base de Datos | MySQL / MariaDB |
| Driver JDBC | mysql-connector-j-8.3.0 |
| IDE | IntelliJ IDEA |

---

## Estructura del Proyecto

```
EFT/
├── src/
│   └── cl/biblioteca/
│       ├── conexion/     → DatabaseConnection (Singleton)
│       ├── modelo/       → Persona (abstracta), Usuario, Estudiante, Libro, Prestamo, Categoria
│       ├── dao/          → DAO<T> (interfaz genérica), UsuarioDAO, EstudianteDAO, LibroDAO, PrestamoDAO, CategoriaDAO
│       ├── controlador/  → AutenticacionController, LibroController, EstudianteController, PrestamoController, ReporteController
│       ├── vista/        → LoginFrame, MainFrame, PanelLibros, PanelEstudiantes, PanelPrestamos, PanelReportes
│       ├── test/         → PruebasSistemaEFT (suite de validación automática)
│       └── main/         → Main.java
├── database/
│   ├── PRY2203_EFT_S9_Script_crea_tablas_biblioteca.sql
│   └── PRY2203_EFT_S9_Script_poblado_tablas_biblioteca.sql
├── lib/
│   └── mysql-connector-j-8.3.0.jar
├── biblioteca_escolar.jar   ← Ejecutable final
└── README.md
```

---

## Configuración de la Base de Datos

1. Abrir MySQL Workbench, HeidiSQL o similar.
2. Ejecutar primero el script de creación de tablas:
   ```
   database/PRY2203_EFT_S9_Script_crea_tablas_biblioteca.sql
   ```
3. Ejecutar el script de datos de ejemplo:
   ```
   database/PRY2203_EFT_S9_Script_poblado_tablas_biblioteca.sql
   ```

El sistema se conecta automáticamente a `localhost:3306/biblioteca`.  
Si el usuario `speedfast`/`speedfast123` no existe, intentará con `root` (vacío) y luego con credenciales alternativas.

---

## Cómo Ejecutar

**Con JAR ejecutable:**
```bash
java -jar biblioteca_escolar.jar
```

**Con IntelliJ IDEA:**
- Abrir el proyecto, configurar el SDK (Java 21) y agregar `lib/mysql-connector-j-8.3.0.jar` al classpath.
- Ejecutar `cl.biblioteca.main.Main`.

---

## Usuarios de Prueba (Script oficial Duoc UC)

| Correo | Contraseña | Rol |
|---|---|---|
| antonia@correo.cl | clave123 | Bibliotecario |
| carlos@correo.cl | clave123 | Estudiante |
| maria@correo.cl | clave123 | Estudiante |

> Los botones "Rol Bibliotecario" y "Rol Estudiante" en el login rellenan los campos automáticamente.

---

## Funcionalidades

### Rol Bibliotecario (Acceso Total)
- **Inventario de Libros:** Registro, búsqueda, modificación y eliminación con categorías.
- **Gestión de Estudiantes:** CRUD completo con búsqueda por nombre, RUT, curso o correo.
- **Préstamos y Devoluciones:** Registro en hilo independiente, filtrado por estado, detección de atrasos.
- **Reportes:**
  1. Libros más prestados (ranking de demanda).
  2. Historial de préstamos por estudiante.
  3. Libros actualmente en circulación con días de atraso automático.
- **Cuentas de Usuarios:** Gestión de cuentas con asignación de roles.

### Rol Estudiante (Autoservicio)
- Consulta del catálogo de libros con búsqueda y filtro por categoría.
- Solicitud de préstamos en segundo plano (multihilo).
- Historial personal y devolución propia.

---

## Patrones y Principios Implementados

### Singleton – `DatabaseConnection`
```java
public static synchronized DatabaseConnection getInstance() {
    if (instance == null) {
        instance = new DatabaseConnection();
    }
    return instance;
}
```

### Herencia y Clase Abstracta – `Persona`
```java
// Persona (abstracta) → Usuario, Estudiante
public abstract String getDescripcionRol();
```

### Interfaz Genérica – `DAO<T>`
```java
public interface DAO<T> {
    boolean create(T entidad) throws SQLException;
    List<T> readAll() throws SQLException;
    T readById(int id) throws SQLException;
    boolean update(T entidad) throws SQLException;
    boolean delete(int id) throws SQLException;
}
```

### Concurrencia + Sincronización – `PrestamoController` + `LibroDAO`
```java
// Hilo independiente en segundo plano (no congela la UI)
Thread hiloPrestamo = new Thread(() -> {
    libroDAO.modificarStockSincronizado(idLibro, -1);  // synchronized
    prestamoDAO.create(prestamo);
});
hiloPrestamo.start();

// Exclusión mutua con synchronized + transacción
public synchronized boolean modificarStockSincronizado(int idLibro, int delta) { ... }
```

---

## Suite de Pruebas Automáticas

Ejecutar `cl.biblioteca.test.PruebasSistemaEFT` para validar los 33 requisitos de la pauta:

```
Resultado: 33 DE 33 PRUEBAS SUPERADAS
Estado: CUMPLIMIENTO TOTAL AL 100% DE LA PAUTA DE EVALUACIÓN
```
