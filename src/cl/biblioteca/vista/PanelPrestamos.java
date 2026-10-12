package cl.biblioteca.vista;

import cl.biblioteca.controlador.EstudianteController;
import cl.biblioteca.controlador.LibroController;
import cl.biblioteca.controlador.PrestamoController;
import cl.biblioteca.modelo.Estudiante;
import cl.biblioteca.modelo.Libro;
import cl.biblioteca.modelo.Prestamo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel de Gestión de Préstamos y Devoluciones (Módulo Bibliotecario).
 * Integra el flujo concurrente (hilos independientes en segundo plano) y sincronización
 * del stock sin congelar la interfaz de usuario.
 */
public class PanelPrestamos extends JPanel {

    private final PrestamoController prestamoController;
    private final EstudianteController estudianteController;
    private final LibroController libroController;

    private JTable tablaPrestamos;
    private DefaultTableModel modeloTabla;

    private JComboBox<ItemCombo<Integer>> cbEstudiantes;
    private JComboBox<ItemCombo<Integer>> cbLibros;
    private JTextField txtFechaPrestamo;
    private JSpinner spDiasPrestamo;

    private JTextField txtBuscar;
    private JComboBox<String> cbFiltroEstado;
    private JLabel lblEstadoProceso;

    private JButton btnRegistrarPrestamo;
    private JButton btnDevolver;
    private JButton btnEliminar;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PanelPrestamos() {
        this.prestamoController = new PrestamoController();
        this.estudianteController = new EstudianteController();
        this.libroController = new LibroController();

        inicializarUI();
        cargarSelectores();
        cargarPrestamos();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.COLOR_FONDO_CLARO);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panel Superior: Título y Filtros
        JPanel panelNorte = new JPanel(new BorderLayout(8, 8));
        panelNorte.setBackground(UIHelper.COLOR_FONDO_CLARO);

        JLabel lblTitulo = new JLabel("Circulación: Préstamos y Control de Devoluciones");
        lblTitulo.setFont(UIHelper.FUENTE_TITULO);
        lblTitulo.setForeground(new Color(30, 41, 59));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelFiltros.setBackground(UIHelper.COLOR_FONDO_CLARO);

        panelFiltros.add(new JLabel("Buscar (Alumno o Libro):"));
        txtBuscar = new JTextField(16);
        panelFiltros.add(txtBuscar);

        JButton btnBuscar = UIHelper.crearBoton("Buscar", UIHelper.COLOR_PRIMARIO);
        btnBuscar.addActionListener(e -> buscarPrestamos());
        panelFiltros.add(btnBuscar);

        panelFiltros.add(new JLabel("Filtrar Estado:"));
        cbFiltroEstado = new JComboBox<>(new String[]{"Todos los registros", "Solo Activos (Pendientes)", "Solo Devueltos", "Solo Atrasados"});
        cbFiltroEstado.addActionListener(e -> filtrarPorEstado());
        panelFiltros.add(cbFiltroEstado);

        JButton btnRecargar = UIHelper.crearBoton("Actualizar Todo", UIHelper.COLOR_SECUNDARIO);
        btnRecargar.addActionListener(e -> {
            txtBuscar.setText("");
            cbFiltroEstado.setSelectedIndex(0);
            cargarSelectores();
            cargarPrestamos();
        });
        panelFiltros.add(btnRecargar);

        panelNorte.add(panelFiltros, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // Panel Izquierdo: Formulario de Préstamo Concurrente
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setPreferredSize(new Dimension(360, 420));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        "Nuevo Préstamo de Material",
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        UIHelper.FUENTE_SUBTITULO,
                        new Color(30, 41, 59)
                ),
                new EmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        // Estudiante
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panelFormulario.add(new JLabel("Estudiante:"), gbc);
        cbEstudiantes = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(cbEstudiantes, gbc);

        // Libro
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Libro a Prestar:"), gbc);
        cbLibros = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(cbLibros, gbc);

        // Fecha Préstamo
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Fecha Préstamo:"), gbc);
        txtFechaPrestamo = new JTextField(LocalDate.now().format(FORMATO_FECHA));
        txtFechaPrestamo.setEditable(false);
        txtFechaPrestamo.setBackground(new Color(241, 245, 249));
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtFechaPrestamo, gbc);

        // Días de Préstamo
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Plazo de días:"), gbc);
        spDiasPrestamo = new JSpinner(new SpinnerNumberModel(7, 1, 60, 1));
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(spDiasPrestamo, gbc);

        // Botón Registrar Préstamo (Multihilo)
        btnRegistrarPrestamo = UIHelper.crearBoton("Registrar Préstamo (Multihilo)", UIHelper.COLOR_EXITO);
        btnRegistrarPrestamo.addActionListener(e -> registrarPrestamoConcurrente());
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        panelFormulario.add(btnRegistrarPrestamo, gbc);

        // Separador
        JSeparator sep = new JSeparator();
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 6, 10, 6);
        panelFormulario.add(sep, gbc);

        // Acciones para registros seleccionados
        JLabel lblAcciones = new JLabel("Acciones de Devolución:");
        lblAcciones.setFont(UIHelper.FUENTE_LABEL);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(2, 6, 4, 6);
        panelFormulario.add(lblAcciones, gbc);

        JPanel panelAccionesFila = new JPanel(new GridLayout(1, 2, 8, 8));
        panelAccionesFila.setBackground(Color.WHITE);

        btnDevolver = UIHelper.crearBoton("Devolver Libro", UIHelper.COLOR_ADVERTENCIA);
        btnDevolver.addActionListener(e -> registrarDevolucionConcurrente());
        panelAccionesFila.add(btnDevolver);

        btnEliminar = UIHelper.crearBoton("Eliminar", UIHelper.COLOR_PELIGRO);
        btnEliminar.addActionListener(e -> eliminarPrestamo());
        panelAccionesFila.add(btnEliminar);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panelFormulario.add(panelAccionesFila, gbc);

        // Barra de estado de concurrencia
        lblEstadoProceso = new JLabel("Sistema en espera (Listo)");
        lblEstadoProceso.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblEstadoProceso.setForeground(new Color(71, 85, 105));
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 6, 2, 6);
        panelFormulario.add(lblEstadoProceso, gbc);

        add(panelFormulario, BorderLayout.WEST);

        // Panel Centro: Tabla de Préstamos
        String[] columnas = {"ID", "Estudiante", "RUT", "Curso", "Libro", "ISBN", "F. Préstamo", "F. Devolución", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablaPrestamos = new JTable(modeloTabla);
        UIHelper.aplicarEstiloTabla(tablaPrestamos);

        // Renderizado visual con colores para el estado (Vigente / Atrasado / Devuelto)
        tablaPrestamos.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String val = value.toString();
                    if (val.contains("ATRASADO")) {
                        c.setForeground(new Color(185, 28, 28));
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else if (val.contains("DEVUELTO")) {
                        c.setForeground(new Color(22, 101, 52));
                        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    } else {
                        c.setForeground(new Color(30, 64, 175));
                        c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaPrestamos);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        add(scrollTabla, BorderLayout.CENTER);
    }

    public void cargarSelectores() {
        cbEstudiantes.removeAllItems();
        cbLibros.removeAllItems();

        try {
            List<Estudiante> estudiantes = estudianteController.listarTodos();
            for (Estudiante e : estudiantes) {
                cbEstudiantes.addItem(new ItemCombo<>(e.getId(), e.getNombre() + " (" + e.getRut() + ")"));
            }

            List<Libro> libros = libroController.listarTodos();
            for (Libro l : libros) {
                cbLibros.addItem(new ItemCombo<>(l.getId(), l.getTitulo() + " [Stock: " + l.getStock() + "]"));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar listas: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cargarPrestamos() {
        modeloTabla.setRowCount(0);
        try {
            List<Prestamo> lista = prestamoController.listarTodos();
            for (Prestamo p : lista) {
                modeloTabla.addRow(new Object[]{
                        p.getId(),
                        p.getNombreEstudiante(),
                        p.getRutEstudiante(),
                        p.getCursoEstudiante(),
                        p.getTituloLibro(),
                        p.getIsbnLibro(),
                        p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_FECHA) : "",
                        p.getFechaDevolucion() != null ? p.getFechaDevolucion().format(FORMATO_FECHA) : "",
                        p.getEstadoTexto()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al listar préstamos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarPrestamos() {
        String criterio = txtBuscar.getText().trim();
        modeloTabla.setRowCount(0);
        try {
            List<Prestamo> lista = prestamoController.buscar(criterio);
            for (Prestamo p : lista) {
                modeloTabla.addRow(new Object[]{
                        p.getId(),
                        p.getNombreEstudiante(),
                        p.getRutEstudiante(),
                        p.getCursoEstudiante(),
                        p.getTituloLibro(),
                        p.getIsbnLibro(),
                        p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_FECHA) : "",
                        p.getFechaDevolucion() != null ? p.getFechaDevolucion().format(FORMATO_FECHA) : "",
                        p.getEstadoTexto()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en búsqueda: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarPorEstado() {
        int idx = cbFiltroEstado.getSelectedIndex();
        modeloTabla.setRowCount(0);
        try {
            List<Prestamo> lista;
            if (idx == 1) { // Solo activos
                lista = prestamoController.listarActivos();
            } else {
                lista = prestamoController.listarTodos();
            }

            for (Prestamo p : lista) {
                if (idx == 2 && !p.isDevuelto()) continue; // Solo devueltos
                if (idx == 3 && !p.isAtrasado()) continue; // Solo atrasados

                modeloTabla.addRow(new Object[]{
                        p.getId(),
                        p.getNombreEstudiante(),
                        p.getRutEstudiante(),
                        p.getCursoEstudiante(),
                        p.getTituloLibro(),
                        p.getIsbnLibro(),
                        p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_FECHA) : "",
                        p.getFechaDevolucion() != null ? p.getFechaDevolucion().format(FORMATO_FECHA) : "",
                        p.getEstadoTexto()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar préstamos: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarPrestamoConcurrente() {
        ItemCombo<Integer> itemEstudiante = (ItemCombo<Integer>) cbEstudiantes.getSelectedItem();
        ItemCombo<Integer> itemLibro = (ItemCombo<Integer>) cbLibros.getSelectedItem();

        if (itemEstudiante == null || itemLibro == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un estudiante y un libro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int dias = (Integer) spDiasPrestamo.getValue();
        LocalDate fPrestamo = LocalDate.now();
        LocalDate fDevolucion = fPrestamo.plusDays(dias);

        // Deshabilitar botón durante el procesamiento en segundo plano
        btnRegistrarPrestamo.setEnabled(false);
        lblEstadoProceso.setText("Hilo en ejecución: procesando préstamo en segundo plano...");
        lblEstadoProceso.setForeground(UIHelper.COLOR_PRIMARIO);

        // Invocación asíncrona mediante el controlador
        prestamoController.registrarPrestamoAsync(
                itemEstudiante.getValor(),
                itemLibro.getValor(),
                fPrestamo,
                fDevolucion,
                new PrestamoController.CallbackOperacion() {
                    @Override
                    public void onSuccess(String mensaje) {
                        btnRegistrarPrestamo.setEnabled(true);
                        lblEstadoProceso.setText("Listo: Préstamo registrado y stock sincronizado.");
                        lblEstadoProceso.setForeground(UIHelper.COLOR_EXITO);
                        JOptionPane.showMessageDialog(PanelPrestamos.this, mensaje, "Préstamo Exitoso", JOptionPane.INFORMATION_MESSAGE);
                        cargarSelectores();
                        cargarPrestamos();
                    }

                    @Override
                    public void onError(String error, Exception ex) {
                        btnRegistrarPrestamo.setEnabled(true);
                        lblEstadoProceso.setText("Error en operación: " + error);
                        lblEstadoProceso.setForeground(UIHelper.COLOR_PELIGRO);
                        JOptionPane.showMessageDialog(PanelPrestamos.this, error, "Error en Préstamo", JOptionPane.ERROR_MESSAGE);
                    }
                }
        );
    }

    private void registrarDevolucionConcurrente() {
        int fila = tablaPrestamos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un préstamo de la tabla para registrar su devolución.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPrestamo = (Integer) modeloTabla.getValueAt(fila, 0);
        String estado = (String) modeloTabla.getValueAt(fila, 8);

        if (estado.contains("DEVUELTO")) {
            JOptionPane.showMessageDialog(this, "El libro seleccionado ya fue devuelto previamente.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        btnDevolver.setEnabled(false);
        lblEstadoProceso.setText("Hilo en ejecución: registrando devolución en segundo plano...");
        lblEstadoProceso.setForeground(UIHelper.COLOR_ADVERTENCIA);

        // Obtener el ID del libro asociado para reponer stock
        int idLibro = 0;
        try {
            Prestamo p = new cl.biblioteca.dao.PrestamoDAO().readById(idPrestamo);
            if (p != null) {
                idLibro = p.getIdLibro();
            }
        } catch (Exception ignored) {}

        final int idLibroFinal = idLibro;
        prestamoController.registrarDevolucionAsync(idPrestamo, idLibroFinal, new PrestamoController.CallbackOperacion() {
            @Override
            public void onSuccess(String mensaje) {
                btnDevolver.setEnabled(true);
                lblEstadoProceso.setText("Listo: Devolución procesada y stock repuesto.");
                lblEstadoProceso.setForeground(UIHelper.COLOR_EXITO);
                JOptionPane.showMessageDialog(PanelPrestamos.this, mensaje, "Devolución Exitosa", JOptionPane.INFORMATION_MESSAGE);
                cargarSelectores();
                cargarPrestamos();
            }

            @Override
            public void onError(String error, Exception ex) {
                btnDevolver.setEnabled(true);
                lblEstadoProceso.setText("Error: " + error);
                lblEstadoProceso.setForeground(UIHelper.COLOR_PELIGRO);
                JOptionPane.showMessageDialog(PanelPrestamos.this, error, "Error en Devolución", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void eliminarPrestamo() {
        int fila = tablaPrestamos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un registro de préstamo para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idPrestamo = (Integer) modeloTabla.getValueAt(fila, 0);
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el registro de préstamo #" + idPrestamo + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                boolean ok = prestamoController.eliminar(idPrestamo);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Registro eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    cargarPrestamos();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el registro.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
