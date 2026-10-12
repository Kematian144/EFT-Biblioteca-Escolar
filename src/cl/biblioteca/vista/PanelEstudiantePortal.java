package cl.biblioteca.vista;

import cl.biblioteca.controlador.AutenticacionController;
import cl.biblioteca.controlador.CategoriaController;
import cl.biblioteca.controlador.LibroController;
import cl.biblioteca.controlador.PrestamoController;
import cl.biblioteca.dao.EstudianteDAO;
import cl.biblioteca.modelo.Categoria;
import cl.biblioteca.modelo.Estudiante;
import cl.biblioteca.modelo.Libro;
import cl.biblioteca.modelo.Prestamo;
import cl.biblioteca.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Portal del Estudiante (Módulo con privilegios de Estudiante).
 * Según la pauta: "solo préstamo, devolución y consulta".
 * Permite al alumno explorar el catálogo, solicitar préstamos en segundo plano,
 * devolver libros y revisar su historial personal.
 */
public class PanelEstudiantePortal extends JPanel {

    private final LibroController libroController;
    private final PrestamoController prestamoController;
    private final CategoriaController categoriaController;
    private final EstudianteDAO estudianteDAO;

    private Estudiante estudianteActual;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Catálogo
    private JTable tablaCatalogo;
    private DefaultTableModel modeloCatalogo;
    private JTextField txtBuscarLibro;
    private JComboBox<ItemCombo<Integer>> cbFiltroCat;
    private JSpinner spDias;
    private JButton btnSolicitar;

    // Mis Préstamos
    private JTable tablaMisPrestamos;
    private DefaultTableModel modeloMisPrestamos;
    private JButton btnDevolverLibro;
    private JLabel lblResumenPersonal;

    public PanelEstudiantePortal() {
        this.libroController = new LibroController();
        this.prestamoController = new PrestamoController();
        this.categoriaController = new CategoriaController();
        this.estudianteDAO = new EstudianteDAO();

        vincularEstudianteSesion();
        inicializarUI();
        cargarCategorias();
        cargarCatalogo();
        cargarMisPrestamos();
    }

    private void vincularEstudianteSesion() {
        Usuario user = AutenticacionController.getUsuarioActual();
        if (user != null) {
            try {
                // Buscar estudiante por RUT o correo
                List<Estudiante> lista = estudianteDAO.search(user.getRut());
                if (!lista.isEmpty()) {
                    this.estudianteActual = lista.get(0);
                } else {
                    List<Estudiante> porCorreo = estudianteDAO.search(user.getCorreo());
                    if (!porCorreo.isEmpty()) {
                        this.estudianteActual = porCorreo.get(0);
                    }
                }
            } catch (Exception ignored) {}
        }

        // Si no se encuentra vinculación exacta, tomar el primer estudiante como fallback
        if (this.estudianteActual == null) {
            try {
                List<Estudiante> todos = estudianteDAO.readAll();
                if (!todos.isEmpty()) {
                    this.estudianteActual = todos.get(0);
                }
            } catch (Exception ignored) {}
        }
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.COLOR_FONDO_CLARO);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Banner del estudiante
        JPanel panelBanner = new JPanel(new BorderLayout());
        panelBanner.setBackground(new Color(238, 242, 255));
        panelBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(199, 210, 254), 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        String nombreEst = estudianteActual != null ? estudianteActual.getNombre() : "Estudiante";
        String rutEst = estudianteActual != null ? estudianteActual.getRut() : "";
        String cursoEst = estudianteActual != null ? estudianteActual.getCurso() : "";

        JLabel lblBienvenida = new JLabel("Portal de Autoservicio del Estudiante: " + nombreEst + " | RUT: " + rutEst + " | Curso: " + cursoEst);
        lblBienvenida.setFont(UIHelper.FUENTE_SUBTITULO);
        lblBienvenida.setForeground(new Color(30, 58, 138));
        panelBanner.add(lblBienvenida, BorderLayout.WEST);

        JButton btnRefrescar = UIHelper.crearBoton("Actualizar", UIHelper.COLOR_PRIMARIO);
        btnRefrescar.addActionListener(e -> {
            cargarCatalogo();
            cargarMisPrestamos();
        });
        panelBanner.add(btnRefrescar, BorderLayout.EAST);

        add(panelBanner, BorderLayout.NORTH);

        // Pestañas del estudiante
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.FUENTE_SUBTITULO);

        tabs.addTab("Catálogo de Libros y Solicitud", crearPanelCatalogo());
        tabs.addTab("Mis Préstamos y Devolución", crearPanelMisPrestamos());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel crearPanelCatalogo() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Barra de búsqueda
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setBackground(Color.WHITE);

        top.add(new JLabel("Buscar libro:"));
        txtBuscarLibro = new JTextField(15);
        top.add(txtBuscarLibro);

        JButton btnBuscar = UIHelper.crearBoton("Buscar", UIHelper.COLOR_PRIMARIO);
        btnBuscar.addActionListener(e -> buscarCatalogo());
        top.add(btnBuscar);

        top.add(new JLabel("Categoría:"));
        cbFiltroCat = new JComboBox<>();
        cbFiltroCat.setPreferredSize(new Dimension(170, 28));
        cbFiltroCat.addActionListener(e -> filtrarCatalogoCat());
        top.add(cbFiltroCat);

        p.add(top, BorderLayout.NORTH);

        // Tabla de catálogo
        String[] cols = {"ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock Disponible", "Estado"};
        modeloCatalogo = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaCatalogo = new JTable(modeloCatalogo);
        UIHelper.aplicarEstiloTabla(tablaCatalogo);
        p.add(new JScrollPane(tablaCatalogo), BorderLayout.CENTER);

        // Panel inferior para solicitar préstamo
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        bottom.setBackground(Color.WHITE);

        bottom.add(new JLabel("Plazo de préstamo (días):"));
        spDias = new JSpinner(new SpinnerNumberModel(7, 1, 30, 1));
        bottom.add(spDias);

        btnSolicitar = UIHelper.crearBoton("Pedir Libro Prestado (Multihilo)", UIHelper.COLOR_EXITO);
        btnSolicitar.addActionListener(e -> solicitarPrestamoEstudiante());
        bottom.add(btnSolicitar);

        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private JPanel crearPanelMisPrestamos() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID Préstamo", "Título del Libro", "Autor", "ISBN", "Fecha Préstamo", "Fecha Devolución", "Estado"};
        modeloMisPrestamos = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaMisPrestamos = new JTable(modeloMisPrestamos);
        UIHelper.aplicarEstiloTabla(tablaMisPrestamos);

        tablaMisPrestamos.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (value != null) {
                    String v = value.toString();
                    if (v.contains("ATRASADO")) {
                        c.setForeground(new Color(185, 28, 28));
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if (v.contains("DEVUELTO")) {
                        c.setForeground(new Color(22, 101, 52));
                    } else {
                        c.setForeground(new Color(30, 64, 175));
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        p.add(new JScrollPane(tablaMisPrestamos), BorderLayout.CENTER);

        // Panel de devolución y estado
        JPanel bot = new JPanel(new BorderLayout());
        bot.setBackground(Color.WHITE);

        lblResumenPersonal = new JLabel("Historial de préstamos del estudiante.");
        lblResumenPersonal.setFont(UIHelper.FUENTE_LABEL);
        bot.add(lblResumenPersonal, BorderLayout.WEST);

        btnDevolverLibro = UIHelper.crearBoton("Registrar Devolución de Libro", UIHelper.COLOR_ADVERTENCIA);
        btnDevolverLibro.addActionListener(e -> devolverLibroEstudiante());
        bot.add(btnDevolverLibro, BorderLayout.EAST);

        p.add(bot, BorderLayout.SOUTH);

        return p;
    }

    private void cargarCategorias() {
        cbFiltroCat.removeAllItems();
        cbFiltroCat.addItem(new ItemCombo<>(0, "-- Todas las Categorías --"));
        try {
            List<Categoria> cats = categoriaController.obtenerTodas();
            for (Categoria c : cats) {
                cbFiltroCat.addItem(new ItemCombo<>(c.getId(), c.getNombre()));
            }
        } catch (Exception ignored) {}
    }

    private void cargarCatalogo() {
        modeloCatalogo.setRowCount(0);
        try {
            List<Libro> lista = libroController.listarTodos();
            for (Libro l : lista) {
                String estado = l.isDisponible() ? "DISPONIBLE" : "AGOTADO";
                modeloCatalogo.addRow(new Object[]{
                        l.getId(),
                        l.getTitulo(),
                        l.getAutor(),
                        l.getIsbn(),
                        l.getEditorial(),
                        l.getNombreCategoria() != null ? l.getNombreCategoria() : "Sin categoría",
                        l.getStock(),
                        estado
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar catálogo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarCatalogo() {
        String criterio = txtBuscarLibro.getText().trim();
        modeloCatalogo.setRowCount(0);
        try {
            List<Libro> lista = libroController.buscar(criterio);
            for (Libro l : lista) {
                String estado = l.isDisponible() ? "DISPONIBLE" : "AGOTADO";
                modeloCatalogo.addRow(new Object[]{
                        l.getId(),
                        l.getTitulo(),
                        l.getAutor(),
                        l.getIsbn(),
                        l.getEditorial(),
                        l.getNombreCategoria() != null ? l.getNombreCategoria() : "Sin categoría",
                        l.getStock(),
                        estado
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en búsqueda: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarCatalogoCat() {
        ItemCombo<Integer> sel = (ItemCombo<Integer>) cbFiltroCat.getSelectedItem();
        if (sel == null) return;
        modeloCatalogo.setRowCount(0);
        try {
            List<Libro> lista = libroController.filtrarPorCategoria(sel.getValor());
            for (Libro l : lista) {
                String estado = l.isDisponible() ? "DISPONIBLE" : "AGOTADO";
                modeloCatalogo.addRow(new Object[]{
                        l.getId(),
                        l.getTitulo(),
                        l.getAutor(),
                        l.getIsbn(),
                        l.getEditorial(),
                        l.getNombreCategoria() != null ? l.getNombreCategoria() : "Sin categoría",
                        l.getStock(),
                        estado
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cargarMisPrestamos() {
        modeloMisPrestamos.setRowCount(0);
        if (estudianteActual == null) return;

        int total = 0;
        int devueltos = 0;
        int atrasados = 0;

        try {
            List<Prestamo> lista = prestamoController.listarPorEstudiante(estudianteActual.getId());
            for (Prestamo p : lista) {
                total++;
                if (p.isDevuelto()) devueltos++;
                if (p.isAtrasado()) atrasados++;

                modeloMisPrestamos.addRow(new Object[]{
                        p.getId(),
                        p.getTituloLibro(),
                        p.getAutorLibro(),
                        p.getIsbnLibro(),
                        p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_FECHA) : "",
                        p.getFechaDevolucion() != null ? p.getFechaDevolucion().format(FORMATO_FECHA) : "",
                        p.getEstadoTexto()
                });
            }

            int activos = total - devueltos;
            lblResumenPersonal.setText("Mis Préstamos: " + total + " | Libros en mi poder: " + activos + " | Atrasos: " + atrasados);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar mis préstamos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void solicitarPrestamoEstudiante() {
        int fila = tablaCatalogo.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro del catálogo para solicitar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idLibro = (Integer) modeloCatalogo.getValueAt(fila, 0);
        String titulo = (String) modeloCatalogo.getValueAt(fila, 1);
        int stock = (Integer) modeloCatalogo.getValueAt(fila, 6);

        if (stock <= 0) {
            JOptionPane.showMessageDialog(this, "Lo sentimos, el libro '" + titulo + "' se encuentra agotado actualmente.",
                    "Sin Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (estudianteActual == null) {
            JOptionPane.showMessageDialog(this, "No se encontró registro de estudiante para la cuenta activa.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int dias = (Integer) spDias.getValue();
        LocalDate fPrestamo = LocalDate.now();
        LocalDate fDevolucion = fPrestamo.plusDays(dias);

        btnSolicitar.setEnabled(false);

        prestamoController.registrarPrestamoAsync(
                estudianteActual.getId(),
                idLibro,
                fPrestamo,
                fDevolucion,
                new PrestamoController.CallbackOperacion() {
                    @Override
                    public void onSuccess(String mensaje) {
                        btnSolicitar.setEnabled(true);
                        JOptionPane.showMessageDialog(PanelEstudiantePortal.this,
                                "¡Préstamo concedido con éxito! Recuerda devolverlo antes del " + fDevolucion.format(FORMATO_FECHA),
                                "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        cargarCatalogo();
                        cargarMisPrestamos();
                    }

                    @Override
                    public void onError(String error, Exception ex) {
                        btnSolicitar.setEnabled(true);
                        JOptionPane.showMessageDialog(PanelEstudiantePortal.this, error, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
        );
    }

    private void devolverLibroEstudiante() {
        int fila = tablaMisPrestamos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo de su lista para devolver.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPrestamo = (Integer) modeloMisPrestamos.getValueAt(fila, 0);
        String estado = (String) modeloMisPrestamos.getValueAt(fila, 6);

        if (estado.contains("DEVUELTO")) {
            JOptionPane.showMessageDialog(this, "Este libro ya fue devuelto con anterioridad.", "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int idLibro = 0;
        try {
            Prestamo p = new cl.biblioteca.dao.PrestamoDAO().readById(idPrestamo);
            if (p != null) idLibro = p.getIdLibro();
        } catch (Exception ignored) {}

        btnDevolverLibro.setEnabled(false);
        final int idLibroFinal = idLibro;

        prestamoController.registrarDevolucionAsync(idPrestamo, idLibroFinal, new PrestamoController.CallbackOperacion() {
            @Override
            public void onSuccess(String mensaje) {
                btnDevolverLibro.setEnabled(true);
                JOptionPane.showMessageDialog(PanelEstudiantePortal.this,
                        "¡Libro devuelto exitosamente a la biblioteca! Muchas gracias.",
                        "Devolución Exitosa", JOptionPane.INFORMATION_MESSAGE);
                cargarCatalogo();
                cargarMisPrestamos();
            }

            @Override
            public void onError(String error, Exception ex) {
                btnDevolverLibro.setEnabled(true);
                JOptionPane.showMessageDialog(PanelEstudiantePortal.this, error, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
