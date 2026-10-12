package cl.biblioteca.vista;

import cl.biblioteca.controlador.CategoriaController;
import cl.biblioteca.controlador.LibroController;
import cl.biblioteca.modelo.Categoria;
import cl.biblioteca.modelo.Libro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel de Gestión de Libros e Inventario (Módulo Bibliotecario).
 * Permite registrar, listar, buscar, modificar y eliminar libros asociados a categorías.
 */
public class PanelLibros extends JPanel {

    private final LibroController libroController;
    private final CategoriaController categoriaController;

    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;

    private JTextField txtId;
    private JTextField txtTitulo;
    private JTextField txtAutor;
    private JTextField txtIsbn;
    private JTextField txtEditorial;
    private JSpinner spStock;
    private JComboBox<ItemCombo<Integer>> cbCategoria;

    private JTextField txtBuscar;
    private JComboBox<ItemCombo<Integer>> cbFiltroCategoria;

    private JButton btnRegistrar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar;

    public PanelLibros() {
        this.libroController = new LibroController();
        this.categoriaController = new CategoriaController();
        inicializarUI();
        cargarCategorias();
        cargarLibros();
    }

    private void inicializarUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UIHelper.COLOR_FONDO_CLARO);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Panel superior: Título y Filtros de Búsqueda
        JPanel panelNorte = new JPanel(new BorderLayout(8, 8));
        panelNorte.setBackground(UIHelper.COLOR_FONDO_CLARO);

        JLabel lblTituloSeccion = new JLabel("Gestión de Inventario de Libros");
        lblTituloSeccion.setFont(UIHelper.FUENTE_TITULO);
        lblTituloSeccion.setForeground(new Color(30, 41, 59));
        panelNorte.add(lblTituloSeccion, BorderLayout.NORTH);

        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        panelFiltros.setBackground(UIHelper.COLOR_FONDO_CLARO);

        panelFiltros.add(new JLabel("Buscar:"));
        txtBuscar = new JTextField(16);
        panelFiltros.add(txtBuscar);

        JButton btnBuscar = UIHelper.crearBoton("Buscar", UIHelper.COLOR_PRIMARIO);
        btnBuscar.addActionListener(e -> buscarLibros());
        panelFiltros.add(btnBuscar);

        panelFiltros.add(new JLabel("Categoría:"));
        cbFiltroCategoria = new JComboBox<>();
        cbFiltroCategoria.setPreferredSize(new Dimension(170, 28));
        cbFiltroCategoria.addActionListener(e -> filtrarPorCategoria());
        panelFiltros.add(cbFiltroCategoria);

        JButton btnRecargar = UIHelper.crearBoton("Restablecer", UIHelper.COLOR_SECUNDARIO);
        btnRecargar.addActionListener(e -> {
            txtBuscar.setText("");
            if (cbFiltroCategoria.getItemCount() > 0) cbFiltroCategoria.setSelectedIndex(0);
            cargarLibros();
        });
        panelFiltros.add(btnRecargar);

        panelNorte.add(panelFiltros, BorderLayout.SOUTH);
        add(panelNorte, BorderLayout.NORTH);

        // Panel Izquierdo: Formulario de Registro / Modificación
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setPreferredSize(new Dimension(340, 400));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225)),
                        "Datos del Libro",
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

        // Título
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Título:"), gbc);
        txtTitulo = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtTitulo, gbc);

        // Autor
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Autor:"), gbc);
        txtAutor = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtAutor, gbc);

        // ISBN
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("ISBN:"), gbc);
        txtIsbn = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtIsbn, gbc);

        // Editorial
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Editorial:"), gbc);
        txtEditorial = new JTextField();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(txtEditorial, gbc);

        // Stock
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Stock:"), gbc);
        spStock = new JSpinner(new SpinnerNumberModel(1, 0, 9999, 1));
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(spStock, gbc);

        // Categoría
        gbc.gridx = 0; gbc.gridy = row;
        panelFormulario.add(new JLabel("Categoría:"), gbc);
        cbCategoria = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = row++;
        panelFormulario.add(cbCategoria, gbc);

        // Panel de botones de acción
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
        gbc.insets = new Insets(15, 5, 5, 5);
        panelFormulario.add(panelBotones, gbc);

        add(panelFormulario, BorderLayout.WEST);

        // Panel Centro: Tabla de Libros
        String[] columnas = {"ID", "Título", "Autor", "ISBN", "Editorial", "Categoría", "Stock", "Disponible"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaLibros = new JTable(modeloTabla);
        UIHelper.aplicarEstiloTabla(tablaLibros);

        tablaLibros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaLibros.getSelectedRow() != -1) {
                seleccionarFila();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaLibros);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        add(scrollTabla, BorderLayout.CENTER);

        // Eventos de botones
        btnRegistrar.addActionListener(e -> registrarLibro());
        btnActualizar.addActionListener(e -> actualizarLibro());
        btnEliminar.addActionListener(e -> eliminarLibro());
        btnLimpiar.addActionListener(e -> limpiarCampos());
    }

    public void cargarCategorias() {
        cbCategoria.removeAllItems();
        cbFiltroCategoria.removeAllItems();

        cbFiltroCategoria.addItem(new ItemCombo<>(0, "-- Todas las Categorías --"));

        try {
            List<Categoria> lista = categoriaController.obtenerTodas();
            for (Categoria c : lista) {
                ItemCombo<Integer> item = new ItemCombo<>(c.getId(), c.getNombre());
                cbCategoria.addItem(item);
                cbFiltroCategoria.addItem(item);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar categorías: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cargarLibros() {
        modeloTabla.setRowCount(0);
        try {
            List<Libro> lista = libroController.listarTodos();
            for (Libro l : lista) {
                String disp = l.isDisponible() ? "SÍ (" + l.getStock() + ")" : "AGOTADO";
                modeloTabla.addRow(new Object[]{
                        l.getId(),
                        l.getTitulo(),
                        l.getAutor(),
                        l.getIsbn(),
                        l.getEditorial(),
                        l.getNombreCategoria() != null ? l.getNombreCategoria() : "Sin categoría",
                        l.getStock(),
                        disp
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar libros: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buscarLibros() {
        String criterio = txtBuscar.getText().trim();
        modeloTabla.setRowCount(0);
        try {
            List<Libro> lista = libroController.buscar(criterio);
            for (Libro l : lista) {
                String disp = l.isDisponible() ? "SÍ (" + l.getStock() + ")" : "AGOTADO";
                modeloTabla.addRow(new Object[]{
                        l.getId(),
                        l.getTitulo(),
                        l.getAutor(),
                        l.getIsbn(),
                        l.getEditorial(),
                        l.getNombreCategoria() != null ? l.getNombreCategoria() : "Sin categoría",
                        l.getStock(),
                        disp
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en búsqueda: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarPorCategoria() {
        ItemCombo<Integer> seleccionado = (ItemCombo<Integer>) cbFiltroCategoria.getSelectedItem();
        if (seleccionado == null) return;

        int idCat = seleccionado.getValor();
        modeloTabla.setRowCount(0);
        try {
            List<Libro> lista = libroController.filtrarPorCategoria(idCat);
            for (Libro l : lista) {
                String disp = l.isDisponible() ? "SÍ (" + l.getStock() + ")" : "AGOTADO";
                modeloTabla.addRow(new Object[]{
                        l.getId(),
                        l.getTitulo(),
                        l.getAutor(),
                        l.getIsbn(),
                        l.getEditorial(),
                        l.getNombreCategoria() != null ? l.getNombreCategoria() : "Sin categoría",
                        l.getStock(),
                        disp
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar por categoría: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarLibro() {
        try {
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            String isbn = txtIsbn.getText().trim();
            String editorial = txtEditorial.getText().trim();
            int stock = (Integer) spStock.getValue();

            ItemCombo<Integer> itemCat = (ItemCombo<Integer>) cbCategoria.getSelectedItem();
            if (itemCat == null) {
                JOptionPane.showMessageDialog(this, "Seleccione una categoría.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = libroController.registrar(titulo, autor, isbn, editorial, stock, itemCat.getValor());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Libro registrado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarLibros();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar el libro.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarLibro() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro de la tabla para modificar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText().trim());
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            String isbn = txtIsbn.getText().trim();
            String editorial = txtEditorial.getText().trim();
            int stock = (Integer) spStock.getValue();

            ItemCombo<Integer> itemCat = (ItemCombo<Integer>) cbCategoria.getSelectedItem();
            if (itemCat == null) {
                JOptionPane.showMessageDialog(this, "Seleccione una categoría.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = libroController.actualizar(id, titulo, autor, isbn, editorial, stock, itemCat.getValor());
            if (ok) {
                JOptionPane.showMessageDialog(this, "Libro actualizado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                limpiarCampos();
                cargarLibros();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el libro.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarLibro() {
        if (txtId.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un libro de la tabla para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de eliminar el libro '" + txtTitulo.getText() + "'?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(txtId.getText().trim());
                boolean ok = libroController.eliminar(id);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Libro eliminado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    limpiarCampos();
                    cargarLibros();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar el libro.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: No se puede eliminar si posee préstamos asociados (" + ex.getMessage() + ")",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void seleccionarFila() {
        int fila = tablaLibros.getSelectedRow();
        if (fila != -1) {
            try {
                int id = (Integer) modeloTabla.getValueAt(fila, 0);
                Libro l = libroController.obtenerPorId(id);
                if (l != null) {
                    txtId.setText(String.valueOf(l.getId()));
                    txtTitulo.setText(l.getTitulo());
                    txtAutor.setText(l.getAutor());
                    txtIsbn.setText(l.getIsbn());
                    txtEditorial.setText(l.getEditorial());
                    spStock.setValue(l.getStock());

                    for (int i = 0; i < cbCategoria.getItemCount(); i++) {
                        ItemCombo<Integer> item = cbCategoria.getItemAt(i);
                        if (item.getValor() == l.getIdCategoria()) {
                            cbCategoria.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtIsbn.setText("");
        txtEditorial.setText("");
        spStock.setValue(1);
        if (cbCategoria.getItemCount() > 0) cbCategoria.setSelectedIndex(0);
        tablaLibros.clearSelection();
    }
}
