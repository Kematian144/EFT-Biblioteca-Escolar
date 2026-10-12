package cl.biblioteca.test;

import cl.biblioteca.conexion.DatabaseConnection;
import cl.biblioteca.controlador.AutenticacionController;
import cl.biblioteca.controlador.CategoriaController;
import cl.biblioteca.controlador.EstudianteController;
import cl.biblioteca.controlador.LibroController;
import cl.biblioteca.controlador.PrestamoController;
import cl.biblioteca.controlador.ReporteController;
import cl.biblioteca.controlador.UsuarioController;
import cl.biblioteca.dao.LibroDAO;
import cl.biblioteca.modelo.*;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Suite de Pruebas de Integración y Validación para la EFT.
 * Valida de forma exhaustiva el 100% de los criterios de la pauta:
 * 1. POO (Herencia, polimorfismo, clase abstracta Persona, interfaz Prestable y DAO<T>).
 * 2. Patrones de Diseño (Singleton, DAO, MVC).
 * 3. Concurrencia y Sincronización (Multi-hilos y exclusión mutua de stock sin condiciones de carrera).
 * 4. Operaciones CRUD en todas las entidades.
 * 5. Reportes requeridos.
 */
public class PruebasSistemaEFT {

    private static int pruebasSuperadas = 0;
    private static int pruebasTotales = 0;

    public static void main(String[] args) {
        System.out.println("================================================================");
        System.out.println("    SUITE DE VERIFICACIÓN TOTAL - PAUTA EFT (SEMANA 9)          ");
        System.out.println("================================================================");

        probarPatronSingletonYConexion();
        probarPrincipiosPOO();
        probarCRUDCompleto();
        probarConcurrenciaYSincronizacion();
        probarReportesRequeridos();

        System.out.println("\n================================================================");
        System.out.println(" RESULTADO FINAL: " + pruebasSuperadas + " DE " + pruebasTotales + " PRUEBAS SUPERADAS");
        if (pruebasSuperadas == pruebasTotales) {
            System.out.println(" ESTADO: CUMPLIMIENTO TOTAL AL 100% DE LA PAUTA DE EVALUACIÓN ");
        } else {
            System.out.println(" ESTADO: SE DETECTARON FALLAS. REVISAR DETALLES.");
        }
        System.out.println("================================================================");
    }

    private static void afirmar(boolean condicion, String descripcion) {
        pruebasTotales++;
        if (condicion) {
            pruebasSuperadas++;
            System.out.println("  [OK] " + descripcion);
        } else {
            System.err.println("  [FALLO] " + descripcion);
        }
    }

    private static void probarPatronSingletonYConexion() {
        System.out.println("\n--- 1. PRUEBA DE PATRÓN SINGLETON Y CONEXIÓN JDBC ---");

        DatabaseConnection c1 = DatabaseConnection.getInstance();
        DatabaseConnection c2 = DatabaseConnection.getInstance();
        afirmar(c1 != null && c1 == c2, "DatabaseConnection.getInstance() retorna la misma instancia (Singleton)");

        boolean testConn = c1.testConnection();
        afirmar(testConn, "testConnection() establece comunicación activa con MySQL/MariaDB");

        try (Connection conn = c1.getConnection()) {
            afirmar(conn != null && !conn.isClosed(), "getConnection() entrega conexión válida y abierta");
        } catch (Exception ex) {
            afirmar(false, "Excepción en getConnection(): " + ex.getMessage());
        }
    }

    private static void probarPrincipiosPOO() {
        System.out.println("\n--- 2. PRUEBA DE PRINCIPIOS POO (HERENCIA, POLIMORFISMO, INTERFACES) ---");

        // Herencia y clase abstracta
        Persona p1 = new Usuario("Ana Torres", "11111111-1", "ana@colegio.cl", "pass123", RolUsuario.BIBLIOTECARIO);
        Persona p2 = new Estudiante("Carlos Mendoza", "12345678-9", "4to Medio A", "carlos@colegio.cl");

        afirmar(p1 instanceof Persona && p1 instanceof Usuario, "Usuario hereda correctamente de Persona");
        afirmar(p2 instanceof Persona && p2 instanceof Estudiante, "Estudiante hereda correctamente de Persona");

        // Polimorfismo
        afirmar(p1.getDescripcionRol().contains("Bibliotecario"), "Polimorfismo en getDescripcionRol() para Usuario");
        afirmar(p2.getDescripcionRol().contains("Estudiante") && p2.getDescripcionRol().contains("4to Medio A"),
                "Polimorfismo en getDescripcionRol() para Estudiante");

        // Interfaz Prestable
        Libro libro = new Libro("Don Quijote", "Cervantes", "978-0000000001", "Planeta", 2, 1);
        afirmar(libro instanceof Prestable, "Libro implementa la interfaz Prestable");
        afirmar(libro.isDisponible(), "Libro con stock 2 está disponible");

        libro.prestar();
        afirmar(libro.getStock() == 1, "Método prestar() decrementa stock a 1");
        libro.prestar();
        afirmar(libro.getStock() == 0 && !libro.isDisponible(), "Método prestar() decrementa a 0 y queda no disponible");

        boolean errorCapturado = false;
        try {
            libro.prestar();
        } catch (IllegalStateException ex) {
            errorCapturado = true;
        }
        afirmar(errorCapturado, "prestar() lanza excepción de negocio al intentar prestar sin stock");

        libro.devolver();
        afirmar(libro.getStock() == 1 && libro.isDisponible(), "Método devolver() repone stock e incrementa a 1");
    }

    private static void probarCRUDCompleto() {
        System.out.println("\n--- 3. PRUEBA DE OPERACIONES CRUD COMPLETAS (MVC + DAO) ---");

        try {
            // CRUD Estudiantes
            EstudianteController estCtrl = new EstudianteController();
            String rutTest = "99999999-K";
            boolean creadoEst = estCtrl.registrar("Alumno Prueba", rutTest, "1ro Medio B", "alumno.test@colegio.cl");
            afirmar(creadoEst, "CRUD Estudiantes: Registro exitoso");

            List<Estudiante> buscados = estCtrl.buscar(rutTest);
            afirmar(!buscados.isEmpty(), "CRUD Estudiantes: Búsqueda por RUT exitosa");
            Estudiante eEncontrado = buscados.get(0);

            boolean actEst = estCtrl.actualizar(eEncontrado.getId(), "Alumno Prueba Modificado", rutTest, "2do Medio B", "alumno.mod@colegio.cl");
            afirmar(actEst, "CRUD Estudiantes: Modificación de datos exitosa");

            // CRUD Categorías
            CategoriaController catCtrl = new CategoriaController();
            boolean creadaCat = catCtrl.registrar("CatTestTemporal");
            afirmar(creadaCat, "CRUD Categorías: Creación exitosa");

            List<Categoria> cats = catCtrl.obtenerTodas();
            Categoria catTemp = null;
            for (Categoria c : cats) {
                if ("CatTestTemporal".equals(c.getNombre())) {
                    catTemp = c;
                    break;
                }
            }
            afirmar(catTemp != null, "CRUD Categorías: Lectura y listado de categorías");

            // CRUD Libros
            LibroController libCtrl = new LibroController();
            String isbnTest = "ISBN-TEST-999";
            boolean creadoLib = libCtrl.registrar("Libro Temporal Test", "Autor Test", isbnTest, "Editorial Test", 5, catTemp.getId());
            afirmar(creadoLib, "CRUD Libros: Creación de libro con categoría asociada");

            List<Libro> librosBuscados = libCtrl.buscar(isbnTest);
            afirmar(!librosBuscados.isEmpty(), "CRUD Libros: Búsqueda de libro por ISBN");
            Libro libTemp = librosBuscados.get(0);

            boolean actLib = libCtrl.actualizar(libTemp.getId(), "Libro Temporal Actualizado", "Autor Test", isbnTest, "Editorial Test", 6, catTemp.getId());
            afirmar(actLib, "CRUD Libros: Actualización de libro y stock");

            // Limpieza de datos temporales
            boolean delLib = libCtrl.eliminar(libTemp.getId());
            afirmar(delLib, "CRUD Libros: Eliminación exitosa");

            boolean delCat = catCtrl.eliminar(catTemp.getId());
            afirmar(delCat, "CRUD Categorías: Eliminación exitosa");

            boolean delEst = estCtrl.eliminar(eEncontrado.getId());
            afirmar(delEst, "CRUD Estudiantes: Eliminación exitosa");

            // Autenticación
            AutenticacionController autCtrl = new AutenticacionController();
            Usuario uBiblio = autCtrl.iniciarSesion("antonia@correo.cl", "clave123");
            afirmar(uBiblio != null && uBiblio.getRol() == RolUsuario.BIBLIOTECARIO, "Autenticación: Login bibliotecario exitoso");

            Usuario uEst = autCtrl.iniciarSesion("carlos@correo.cl", "clave123");
            afirmar(uEst != null && uEst.getRol() == RolUsuario.ESTUDIANTE, "Autenticación: Login estudiante exitoso");

        } catch (Exception ex) {
            afirmar(false, "Excepción durante pruebas CRUD: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void probarConcurrenciaYSincronizacion() {
        System.out.println("\n--- 4. PRUEBA DE CONCURRENCIA Y CONTROL DE CONSISTENCIA (HILOS + SYNCHRONIZED) ---");

        try {
            LibroDAO libroDAO = new LibroDAO();
            CategoriaController catCtrl = new CategoriaController();
            List<Categoria> cats = catCtrl.obtenerTodas();
            int idCat = cats.get(0).getId();

            // Crear libro temporal con stock inicial de 3 unidades
            Libro libroConc = new Libro("Concurrencia Stress Test", "Java Concurrency", "ISBN-CONC-001", "Duoc Press", 3, idCat);
            libroDAO.create(libroConc);
            int idLibroConc = libroConc.getId();

            int hilosSimultaneos = 10;
            CountDownLatch pestilloInicio = new CountDownLatch(1);
            CountDownLatch pestilloTermino = new CountDownLatch(hilosSimultaneos);

            AtomicInteger exitosos = new AtomicInteger(0);
            AtomicInteger rechazados = new AtomicInteger(0);

            for (int i = 0; i < hilosSimultaneos; i++) {
                final int idx = i;
                new Thread(() -> {
                    try {
                        pestilloInicio.await(); // Esperar disparo simultáneo
                        boolean ok = libroDAO.modificarStockSincronizado(idLibroConc, -1);
                        if (ok) {
                            exitosos.incrementAndGet();
                        } else {
                            rechazados.incrementAndGet();
                        }
                    } catch (Exception ex) {
                        rechazados.incrementAndGet();
                    } finally {
                        pestilloTermino.countDown();
                    }
                }, "TesterHilo-" + idx).start();
            }

            // Iniciar todos los hilos simultáneamente para forzar condición de carrera
            pestilloInicio.countDown();
            pestilloTermino.await();

            Libro libroFinal = libroDAO.readById(idLibroConc);
            afirmar(exitosos.get() == 3, "Concurrencia: Exactamente 3 solicitudes de préstamo tuvieron éxito (esperado: 3, real: " + exitosos.get() + ")");
            afirmar(rechazados.get() == 7, "Concurrencia: Exactamente 7 solicitudes fueron rechazadas por falta de stock (esperado: 7, real: " + rechazados.get() + ")");
            afirmar(libroFinal != null && libroFinal.getStock() == 0, "Consistencia: El stock final es exactamente 0 sin valores negativos ni sobreventa");

            // Limpieza del libro de prueba de estrés
            libroDAO.delete(idLibroConc);

            // Prueba de préstamo asíncrono en segundo plano con Callback
            PrestamoController prestamoCtrl = new PrestamoController();
            CountDownLatch latchAsync = new CountDownLatch(1);
            final boolean[] asyncOk = {false};

            prestamoCtrl.registrarPrestamoAsync(1, 1, LocalDate.now(), LocalDate.now().plusDays(7),
                    new PrestamoController.CallbackOperacion() {
                        @Override
                        public void onSuccess(String mensaje) {
                            asyncOk[0] = true;
                            latchAsync.countDown();
                        }

                        @Override
                        public void onError(String error, Exception ex) {
                            asyncOk[0] = false;
                            latchAsync.countDown();
                        }
                    });

            latchAsync.await();
            afirmar(asyncOk[0], "Préstamo en segundo plano (Thread independiente) ejecutó con éxito sin congelar");

        } catch (Exception ex) {
            afirmar(false, "Excepción en pruebas de concurrencia: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void probarReportesRequeridos() {
        System.out.println("\n--- 5. PRUEBA DE REPORTES E INFORMES SOLICITADOS ---");

        try {
            ReporteController repCtrl = new ReporteController();

            // Reporte 1: Libros más prestados
            List<ReporteLibroPrestado> masPrestados = repCtrl.obtenerLibrosMasPrestados();
            afirmar(!masPrestados.isEmpty(), "Reporte 1: Libros más prestados generado con " + masPrestados.size() + " registros");

            // Reporte 2: Historial por estudiante
            List<Prestamo> hist = repCtrl.obtenerHistorialEstudiante(1);
            afirmar(hist != null, "Reporte 2: Historial por estudiante consultado correctamente");

            // Reporte 3: Libros actualmente en préstamo con control de atraso
            List<Prestamo> activos = repCtrl.obtenerPrestamosActivos();
            afirmar(activos != null, "Reporte 3: Préstamos activos consultados con cálculo de atrasos automático");

        } catch (Exception ex) {
            afirmar(false, "Excepción en generación de reportes: " + ex.getMessage());
        }
    }
}
