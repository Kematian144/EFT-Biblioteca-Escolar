package cl.biblioteca.vista;

import cl.biblioteca.controlador.EstudianteController;
import cl.biblioteca.modelo.Estudiante;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de Gestión de Estudiantes (Módulo Bibliotecario).
 * Permite registrar, buscar, actualizar y eliminar estudiantes vinculados al colegio.
 */
public class PanelEstudiantes extends JPanel {

    private final EstudianteController estudianteController;

    private JTable tablaEstudiantes;
    private DefaultTableModel modeloTabla;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtRut;
    private JTextField txtCurso;
    private JTextField txtCorreo;

    private JTextField txtBuscar;

    private JButton btnRegistrar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;

    public PanelEstudiantes() {
        this.estudianteController = new EstudianteController();
        inicializarUI();
        cargarEstudiantes();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.COLOR_FONDO_CLARO);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panel Superior: Título y Búsqueda
        JPanel panelNorte = new JPanel(new BorderLayout(8, 8));
        panelNorte.setBackground(UIHelper.COLOR_FONDO_CLARO);

        JLabel lblTitulo = new JLabel("Gestión de Estudiantes de la Institución");
        lblTitulo.setFont(UIHelper.FUENTE_TITULO);
        lblTitulo.setForeground(new Color(30, 41, 59));
        panelNorte.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelFiltros.setBackground(UIHelper.COLOR_FONDO_CLARO);

        panelFiltros.add(new JLabel("Buscar estudiante (Nombre, RUT, Curso o Correo):"));
        txtBuscar = new JTextField(20);
        panelFiltros.add(txtBuscar);

        JButton btnBuscar = UIHelper.crearBoton("Buscar", UIHelper.COLOR_PRIMARIO);
        btnBuscar.addActionListener(e -> buscarEstudiantes());
        panelFiltros.add(btnBuscar);

        JButton btnRecargar = UIHelper.crearBoton("Ver Todos", UIHelper.COLOR_SECUNDARIO);
        btnRecargar.addActionListener(e -> {
            txtBuscar.setText("");
            cargarEstudiantes();
        });
        panelFiltros.add(btnRecargar);

        panelNorte.add(panelFiltros, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // Panel Izquierdo: Formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setPreferredSize(new Dimension(340, 360));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        "Ficha del Estudiante",
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

        // ID
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panelFormulario.add(new JLabel("ID:"), gbc);
        txtId = new JTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(241, 245, 249));
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtId, gbc);

        // Nombre
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Nombre Completo:"), gbc);
        txtNombre = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtNombre, gbc);

        // RUT
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("RUT:"), gbc);
        txtRut = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtRut, gbc);

        // Curso
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Curso:"), gbc);
        txtCurso = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtCurso, gbc);

        // Correo
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Correo Electrónico:"), gbc);
        txtCorreo = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtCorreo, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new GridLayout(2, 2, 8, 8));
        panelBotones.setBackground(Color.WHITE);
        btnRegistrar = UIHelper.crearBoton("Registrar", UIHelper.COLOR_EXITO);
        btnActualizar = UIHelper.crearBoton("Actualizar", UIHelper.COLOR_ADVERTENCIA);
        btnEliminar = UIHelper.crearBoton("Eliminar", UIHelper.COLOR_PELIGRO);
        btnLimpiar = UIHelper.crearBoton("Limpiar", UIHelper.COLOR_SECUNDARIO);

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 6, 6);
        panelFormulario.add(panelBotones, gbc);

        add(panelFormulario, BorderLayout.WEST);

        // Panel Centro: Tabla
        String[] columnas = {"ID", "Nombre Completo", "RUT", "Curso", "Correo Electrónico"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablaEstudiantes = new JTable(modeloTabla);
        UIHelper.aplicarEstiloTabla(tablaEstudiantes);

        tablaEstudiantes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaEstudiantes.getSelectedRow() != -1) {
                seleccionarFila();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaEstudiantes);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        add(scrollTabla, BorderLayout.CENTER);

        // Eventos
        btnRegistrar.addActionListener(e -> registrarEstudiante());
        btnActualizar.addActionListener(e -> actualizarEstudiante());
        btnEliminar.addActionListener(e -> eliminarEstudiante());
        btnLimpiar.addActionListener(e -> limpiarCampos());
    }

    public void cargarEstudiantes() {
        modeloTabla.setRowCount(0);
        try {
            List<Estudiante> lista = estudianteController.listarTodos();
            for (Estudiante e : lista) {
                modeloTabla.addRow(new Object[]{
                        e.getId(),
                        e.getNombre(),
                        e.getRut(),
                        e.getCurso(),
                        e.getCorreo()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar estudiantes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarEstudiantes() {
        String criterio = txtBuscar.getText().trim();
        modeloTabla.setRowCount(0);
        try {
            List<Estudiante> lista = estudianteController.buscar(criterio);
            for (Estudiante e : lista) {
                modeloTabla.addRow(new Object[]{
                        e.getId(),
                        e.getNombre(),
                        e.getRut(),
                        e.getCurso(),
                        e.getCorreo()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en búsqueda: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarEstudiante() {
        try {
            String nombre = txtNombre.getText().trim();
            String rut = txtRut.getText().trim();
            String curso = txtCurso.getText().trim();
            String correo = txtCorreo.getText().trim();

            boolean ok = estudianteController.registrar(nombre, rut, curso, correo);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Estudiante registrado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarEstudiantes();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar el estudiante.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarEstudiante() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un estudiante de la tabla para modificar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String nombre = txtNombre.getText().trim();
            String rut = txtRut.getText().trim();
            String curso = txtCurso.getText().trim();
            String correo = txtCorreo.getText().trim();

            boolean ok = estudianteController.actualizar(id, nombre, rut, curso, correo);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Estudiante actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarEstudiantes();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el estudiante.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarEstudiante() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un estudiante de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar al estudiante '" + txtNombre.getText() + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                boolean ok = estudianteController.eliminar(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Estudiante eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarEstudiantes();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el estudiante.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: No se puede eliminar si posee préstamos históricos (" + ex.getMessage() + ")",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void seleccionarFila() {
        int fila = tablaEstudiantes.getSelectedRow();
        if (fila != -1) {
            try {
                int id = (Integer) modeloTabla.getValueAt(fila, 0);
                Estudiante e = estudianteController.obtenerPorId(id);
                if (e != null) {
                    txtId.setText(String.valueOf(e.getId()));
                    txtNombre.setText(e.getNombre());
                    txtRut.setText(e.getRut());
                    txtCurso.setText(e.getCurso());
                    txtCorreo.setText(e.getCorreo());
                }
            } catch (Exception ignored) {}
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtRut.setText("");
        txtCurso.setText("");
        txtCorreo.setText("");
        tablaEstudiantes.clearSelection();
    }
}
