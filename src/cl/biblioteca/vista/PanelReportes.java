package cl.biblioteca.vista;

import cl.biblioteca.controlador.EstudianteController;
import cl.biblioteca.controlador.ReporteController;
import cl.biblioteca.modelo.Estudiante;
import cl.biblioteca.modelo.Prestamo;
import cl.biblioteca.modelo.ReporteLibroPrestado;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel de Reportes e Informes Estadísticos (Módulo de Toma de Decisiones).
 * Implementa los tres reportes requeridos por la pauta:
 * 1. Libros más prestados.
 * 2. Historial de préstamos por estudiante.
 * 3. Listado de libros actualmente en circulación con cálculo de atrasos.
 */
public class PanelReportes extends JPanel {

    private final ReporteController reporteController;
    private final EstudianteController estudianteController;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Tab 1: Libros más prestados
    private JTable tablaLibrosMasPrestados;
    private DefaultTableModel modeloLibrosMasPrestados;

    // Tab 2: Historial por estudiante
    private JComboBox<ItemCombo<Integer>> cbEstudiantesHistorial;
    private JTable tablaHistorialEstudiante;
    private DefaultTableModel modeloHistorialEstudiante;
    private JLabel lblResumenEstudiante;

    // Tab 3: Préstamos activos
    private JTable tablaPrestamosActivos;
    private DefaultTableModel modeloPrestamosActivos;
    private JLabel lblResumenActivos;

    public PanelReportes() {
        this.reporteController = new ReporteController();
        this.estudianteController = new EstudianteController();

        inicializarUI();
        cargarDatosReportes();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.COLOR_FONDO_CLARO);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Cabecera superior
        JPanel panelCabecera = new JPanel(new BorderLayout());
        panelCabecera.setBackground(UIHelper.COLOR_FONDO_CLARO);

        JLabel lblTitulo = new JLabel("Informes y Estadísticas de Biblioteca");
        lblTitulo.setFont(UIHelper.FUENTE_TITULO);
        lblTitulo.setForeground(new Color(30, 41, 59));
        panelCabecera.add(lblTitulo, BorderLayout.WEST);

        JButton btnActualizarTodo = UIHelper.crearBoton("Actualizar Todos los Reportes", UIHelper.COLOR_PRIMARIO);
        btnActualizarTodo.addActionListener(e -> cargarDatosReportes());
        panelCabecera.add(btnActualizarTodo, BorderLayout.EAST);

        add(panelCabecera, BorderLayout.NORTH);

        // Pestañas de Reportes
        JTabbedPane tabbedReportes = new JTabbedPane();
        tabbedReportes.setFont(UIHelper.FUENTE_SUBTITULO);

        tabbedReportes.addTab("1. Libros Más Prestados", crearPanelLibrosMasPrestados());
        tabbedReportes.addTab("2. Historial por Estudiante", crearPanelHistorialEstudiante());
        tabbedReportes.addTab("3. Libros Actualmente en Préstamo", crearPanelPrestamosActivos());

        add(tabbedReportes, BorderLayout.CENTER);
    }

    private JPanel crearPanelLibrosMasPrestados() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel desc = new JLabel("Ranking de demanda de libros para decisiones de reposición y adquisiciones.");
        desc.setFont(UIHelper.FUENTE_TEXTO);
        p.add(desc, BorderLayout.NORTH);

        String[] cols = {"Ranking", "ID Libro", "Título", "Autor", "Categoría", "Stock Actual", "Total de Préstamos"};
        modeloLibrosMasPrestados = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaLibrosMasPrestados = new JTable(modeloLibrosMasPrestados);
        UIHelper.aplicarEstiloTabla(tablaLibrosMasPrestados);
        p.add(new JScrollPane(tablaLibrosMasPrestados), BorderLayout.CENTER);

        return p;
    }

    private JPanel crearPanelHistorialEstudiante() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        top.setBackground(Color.WHITE);
        top.add(new JLabel("Seleccione Estudiante:"));

        cbEstudiantesHistorial = new JComboBox<>();
        cbEstudiantesHistorial.setPreferredSize(new Dimension(320, 28));
        cbEstudiantesHistorial.addActionListener(e -> cargarHistorialEstudianteSeleccionado());
        top.add(cbEstudiantesHistorial);

        JButton btnConsultar = UIHelper.crearBoton("Consultar", UIHelper.COLOR_PRIMARIO);
        btnConsultar.addActionListener(e -> cargarHistorialEstudianteSeleccionado());
        top.add(btnConsultar);

        p.add(top, BorderLayout.NORTH);

        String[] cols = {"ID Préstamo", "Título Libro", "Autor", "ISBN", "Fecha Préstamo", "Fecha Devolución", "Estado"};
        modeloHistorialEstudiante = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaHistorialEstudiante = new JTable(modeloHistorialEstudiante);
        UIHelper.aplicarEstiloTabla(tablaHistorialEstudiante);
        p.add(new JScrollPane(tablaHistorialEstudiante), BorderLayout.CENTER);

        lblResumenEstudiante = new JLabel("Seleccione un estudiante para visualizar su historial consolidado.");
        lblResumenEstudiante.setFont(UIHelper.FUENTE_LABEL);
        lblResumenEstudiante.setBorder(new EmptyBorder(6, 4, 4, 4));
        p.add(lblResumenEstudiante, BorderLayout.SOUTH);

        return p;
    }

    private JPanel crearPanelPrestamosActivos() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel desc = new JLabel("Control de libros en circulación pendientes de entrega con detección automática de atrasos.");
        desc.setFont(UIHelper.FUENTE_TEXTO);
        p.add(desc, BorderLayout.NORTH);

        String[] cols = {"ID Préstamo", "Estudiante", "RUT", "Curso", "Título Libro", "ISBN", "Fecha Préstamo", "Fecha Vencimiento", "Días Atraso", "Estado"};
        modeloPrestamosActivos = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaPrestamosActivos = new JTable(modeloPrestamosActivos);
        UIHelper.aplicarEstiloTabla(tablaPrestamosActivos);

        tablaPrestamosActivos.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (value != null) {
                    String v = value.toString();
                    if (v.contains("ATRASADO")) {
                        c.setForeground(new Color(185, 28, 28));
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else {
                        c.setForeground(new Color(21, 101, 192));
                        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        p.add(new JScrollPane(tablaPrestamosActivos), BorderLayout.CENTER);

        lblResumenActivos = new JLabel("Calculando resumen de préstamos activos...");
        lblResumenActivos.setFont(UIHelper.FUENTE_LABEL);
        lblResumenActivos.setBorder(new EmptyBorder(6, 4, 4, 4));
        p.add(lblResumenActivos, BorderLayout.SOUTH);

        return p;
    }

    public void cargarDatosReportes() {
        cargarReporteLibrosMasPrestados();
        cargarComboEstudiantes();
        cargarReportePrestamosActivos();
    }

    private void cargarReporteLibrosMasPrestados() {
        modeloLibrosMasPrestados.setRowCount(0);
        try {
            List<ReporteLibroPrestado> lista = reporteController.obtenerLibrosMasPrestados();
            int rank = 1;
            for (ReporteLibroPrestado r : lista) {
                modeloLibrosMasPrestados.addRow(new Object[]{
                        "#" + rank++,
                        r.getIdLibro(),
                        r.getTitulo(),
                        r.getAutor(),
                        r.getCategoria() != null ? r.getCategoria() : "Sin categoría",
                        r.getStockActual(),
                        r.getTotalPrestamos()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en reporte de libros más prestados: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarComboEstudiantes() {
        cbEstudiantesHistorial.removeAllItems();
        try {
            List<Estudiante> lista = estudianteController.listarTodos();
            for (Estudiante e : lista) {
                cbEstudiantesHistorial.addItem(new ItemCombo<>(e.getId(), e.getNombre() + " (" + e.getRut() + ")"));
            }
            if (cbEstudiantesHistorial.getItemCount() > 0) {
                cargarHistorialEstudianteSeleccionado();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar estudiantes para reporte: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarHistorialEstudianteSeleccionado() {
        ItemCombo<Integer> sel = (ItemCombo<Integer>) cbEstudiantesHistorial.getSelectedItem();
        if (sel == null) return;

        modeloHistorialEstudiante.setRowCount(0);
        int total = 0;
        int devueltos = 0;
        int atrasados = 0;

        try {
            List<Prestamo> lista = reporteController.obtenerHistorialEstudiante(sel.getValor());
            for (Prestamo p : lista) {
                total++;
                if (p.isDevuelto()) devueltos++;
                if (p.isAtrasado()) atrasados++;

                modeloHistorialEstudiante.addRow(new Object[]{
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
            lblResumenEstudiante.setText("Total Préstamos: " + total + " | Devueltos: " + devueltos +
                    " | Pendientes: " + activos + " | Atrasados: " + atrasados);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al consultar historial del estudiante: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarReportePrestamosActivos() {
        modeloPrestamosActivos.setRowCount(0);
        int total = 0;
        int alDia = 0;
        int atrasados = 0;

        try {
            List<Prestamo> lista = reporteController.obtenerPrestamosActivos();
            for (Prestamo p : lista) {
                total++;
                if (p.isAtrasado()) {
                    atrasados++;
                } else {
                    alDia++;
                }

                modeloPrestamosActivos.addRow(new Object[]{
                        p.getId(),
                        p.getNombreEstudiante(),
                        p.getRutEstudiante(),
                        p.getCursoEstudiante(),
                        p.getTituloLibro(),
                        p.getIsbnLibro(),
                        p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_FECHA) : "",
                        p.getFechaDevolucion() != null ? p.getFechaDevolucion().format(FORMATO_FECHA) : "",
                        p.getDiasAtraso() > 0 ? p.getDiasAtraso() + " días" : "0 (Al día)",
                        p.getEstadoTexto()
                });
            }

            lblResumenActivos.setText("Libros en circulación: " + total + " | Al día: " + alDia +
                    " | Con atraso: " + atrasados);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en reporte de préstamos activos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
