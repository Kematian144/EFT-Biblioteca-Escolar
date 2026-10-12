package cl.biblioteca.vista;

import cl.biblioteca.controlador.UsuarioController;
import cl.biblioteca.modelo.RolUsuario;
import cl.biblioteca.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de Gestión de Cuentas de Usuarios del Sistema (Módulo Bibliotecario).
 * Permite administrar las credenciales y roles (bibliotecario / estudiante) en la tabla 'usuarios'.
 */
public class PanelUsuarios extends JPanel {

    private final UsuarioController usuarioController;

    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtRut;
    private JTextField txtCorreo;
    private JPasswordField txtContrasena;
    private JComboBox<ItemCombo<RolUsuario>> cbRol;

    private JButton btnRegistrar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;

    public PanelUsuarios() {
        this.usuarioController = new UsuarioController();
        inicializarUI();
        cargarUsuarios();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.COLOR_FONDO_CLARO);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Título
        JLabel lblTitulo = new JLabel("Administración de Cuentas de Acceso al Sistema");
        lblTitulo.setFont(UIHelper.FUENTE_TITULO);
        lblTitulo.setForeground(new Color(30, 41, 59));
        add(lblTitulo, BorderLayout.NORTH);

        // Formulario lateral
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setPreferredSize(new Dimension(340, 400));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        "Datos de la Cuenta",
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        UIHelper.FUENTE_SUBTITULO,
                        new Color(30, 41, 59)
                ),
                new EmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
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
        panelFormulario.add(new JLabel("Nombre:"), gbc);
        txtNombre = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtNombre, gbc);

        // RUT
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("RUT:"), gbc);
        txtRut = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtRut, gbc);

        // Correo
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Correo:"), gbc);
        txtCorreo = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtCorreo, gbc);

        // Contraseña
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Contraseña:"), gbc);
        txtContrasena = new JPasswordField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtContrasena, gbc);

        // Rol
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Rol:"), gbc);
        cbRol = new JComboBox<>();
        cbRol.addItem(new ItemCombo<>(RolUsuario.BIBLIOTECARIO, "Bibliotecario (Gestión Total)"));
        cbRol.addItem(new ItemCombo<>(RolUsuario.ESTUDIANTE, "Estudiante (Consulta y Préstamos)"));
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(cbRol, gbc);

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
        gbc.insets = new Insets(14, 5, 5, 5);
        panelFormulario.add(panelBotones, gbc);

        add(panelFormulario, BorderLayout.WEST);

        // Tabla
        String[] columnas = {"ID", "Nombre", "RUT", "Correo Institucional", "Rol Asignado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tablaUsuarios = new JTable(modeloTabla);
        UIHelper.aplicarEstiloTabla(tablaUsuarios);

        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() != -1) {
                seleccionarFila();
            }
        });

        JScrollPane scroll = new JScrollPane(tablaUsuarios);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        add(scroll, BorderLayout.CENTER);

        // Listeners
        btnRegistrar.addActionListener(e -> registrarUsuario());
        btnActualizar.addActionListener(e -> actualizarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarCampos());
    }

    public void cargarUsuarios() {
        modeloTabla.setRowCount(0);
        try {
            List<Usuario> lista = usuarioController.listarTodos();
            for (Usuario u : lista) {
                modeloTabla.addRow(new Object[]{
                        u.getId(),
                        u.getNombre(),
                        u.getRut(),
                        u.getCorreo(),
                        u.getRol() == RolUsuario.BIBLIOTECARIO ? "BIBLIOTECARIO" : "ESTUDIANTE"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar usuarios: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarUsuario() {
        try {
            String nombre = txtNombre.getText().trim();
            String rut = txtRut.getText().trim();
            String correo = txtCorreo.getText().trim();
            String pass = new String(txtContrasena.getPassword()).trim();
            ItemCombo<RolUsuario> selRol = (ItemCombo<RolUsuario>) cbRol.getSelectedItem();

            if (selRol == null) {
                JOptionPane.showMessageDialog(this, "Seleccione un rol.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = usuarioController.registrar(nombre, rut, correo, pass, selRol.getValor());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Usuario registrado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar el usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarUsuario() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String nombre = txtNombre.getText().trim();
            String rut = txtRut.getText().trim();
            String correo = txtCorreo.getText().trim();
            String pass = new String(txtContrasena.getPassword()).trim();
            ItemCombo<RolUsuario> selRol = (ItemCombo<RolUsuario>) cbRol.getSelectedItem();

            if (selRol == null) {
                JOptionPane.showMessageDialog(this, "Seleccione un rol.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = usuarioController.actualizar(id, nombre, rut, correo, pass, selRol.getValor());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Usuario actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarUsuarios();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarUsuario() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un usuario de la tabla.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el usuario '" + txtNombre.getText() + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                boolean ok = usuarioController.eliminar(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Usuario eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarUsuarios();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el usuario.", "Aviso", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void seleccionarFila() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila != -1) {
            try {
                int id = (Integer) modeloTabla.getValueAt(fila, 0);
                Usuario u = usuarioController.obtenerPorId(id);
                if (u != null) {
                    txtId.setText(String.valueOf(u.getId()));
                    txtNombre.setText(u.getNombre());
                    txtRut.setText(u.getRut());
                    txtCorreo.setText(u.getCorreo());
                    txtContrasena.setText(u.getContrasena());

                    for (int i = 0; i < cbRol.getItemCount(); i++) {
                        if (cbRol.getItemAt(i).getValor() == u.getRol()) {
                            cbRol.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtRut.setText("");
        txtCorreo.setText("");
        txtContrasena.setText("");
        if (cbRol.getItemCount() > 0) cbRol.setSelectedIndex(0);
        tablaUsuarios.clearSelection();
    }
}
