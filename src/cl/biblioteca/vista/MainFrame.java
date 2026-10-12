package cl.biblioteca.vista;

import cl.biblioteca.controlador.AutenticacionController;
import cl.biblioteca.modelo.RolUsuario;
import cl.biblioteca.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ventana Principal de la Aplicación (Patrón MVC - Vista Principal con JFrame).
 * Se adapta dinámicamente según el rol del usuario autenticado:
 * - Bibliotecario: Acceso total (Libros, Estudiantes, Préstamos, Reportes, Cuentas).
 * - Estudiante: Autoservicio limitado a catálogo, préstamos, devoluciones y consulta personal.
 */
public class MainFrame extends JFrame {

    private final Usuario usuarioActual;

    public MainFrame(Usuario usuario) {
        this.usuarioActual = usuario;
        inicializarVentana();
    }

    private void inicializarVentana() {
        setTitle("Sistema de Gestión de Biblioteca Escolar - Duoc UC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);

        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBackground(UIHelper.COLOR_FONDO_CLARO);

        // Barra superior de encabezado
        JPanel panelEncabezado = new JPanel(new BorderLayout(15, 0));
        panelEncabezado.setBackground(new Color(15, 23, 42)); // Azul marino elegante
        panelEncabezado.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Título del sistema en encabezado
        JLabel lblTitulo = new JLabel("BIBLIOTECA ESCOLAR");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Desarrollo Orientado a Objetos II - Evaluación Final Transversal");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(148, 163, 184));

        JPanel panelTitulos = new JPanel(new GridLayout(2, 1, 0, 2));
        panelTitulos.setOpaque(false);
        panelTitulos.add(lblTitulo);
        panelTitulos.add(lblSubtitulo);
        panelEncabezado.add(panelTitulos, BorderLayout.WEST);

        // Panel de información de sesión y botón de salida
        JPanel panelUsuario = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        panelUsuario.setOpaque(false);

        String nombre = (usuarioActual != null) ? usuarioActual.getNombre() : "Usuario";
        String correo = (usuarioActual != null) ? usuarioActual.getCorreo() : "";
        boolean esAdmin = (usuarioActual != null && usuarioActual.getRol() == RolUsuario.BIBLIOTECARIO);

        JLabel lblInfoUser = new JLabel("<html><div style='text-align: right;'><b style='color: white; font-size: 13px;'>"
                + nombre + "</b><br><span style='color: #94A3B8; font-size: 11px;'>" + correo + "</span></div></html>");
        panelUsuario.add(lblInfoUser);

        JLabel lblBadgeRol = new JLabel(esAdmin ? "  BIBLIOTECARIO  " : "  ESTUDIANTE  ");
        lblBadgeRol.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblBadgeRol.setOpaque(true);
        lblBadgeRol.setBackground(esAdmin ? new Color(34, 197, 94) : new Color(59, 130, 246));
        lblBadgeRol.setForeground(Color.WHITE);
        lblBadgeRol.setBorder(new EmptyBorder(4, 8, 4, 8));
        panelUsuario.add(lblBadgeRol);

        JButton btnCerrarSesion = UIHelper.crearBoton("Cerrar Sesión", UIHelper.COLOR_PELIGRO);
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        panelUsuario.add(btnCerrarSesion);

        panelEncabezado.add(panelUsuario, BorderLayout.EAST);
        panelContenedor.add(panelEncabezado, BorderLayout.NORTH);

        // Contenido según el rol
        if (esAdmin) {
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setFont(UIHelper.FUENTE_SUBTITULO);

            PanelLibros panelLibros = new PanelLibros();
            PanelEstudiantes panelEstudiantes = new PanelEstudiantes();
            PanelPrestamos panelPrestamos = new PanelPrestamos();
            PanelReportes panelReportes = new PanelReportes();
            PanelUsuarios panelUsuarios = new PanelUsuarios();

            tabbedPane.addTab("Inventario de Libros", panelLibros);
            tabbedPane.addTab("Gestión de Estudiantes", panelEstudiantes);
            tabbedPane.addTab("Préstamos y Devoluciones", panelPrestamos);
            tabbedPane.addTab("Reportes e Informes", panelReportes);
            tabbedPane.addTab("Cuentas de Usuarios", panelUsuarios);

            // Refrescar paneles al cambiar de pestaña
            tabbedPane.addChangeListener(e -> {
                int idx = tabbedPane.getSelectedIndex();
                if (idx == 0) panelLibros.cargarLibros();
                else if (idx == 1) panelEstudiantes.cargarEstudiantes();
                else if (idx == 2) { panelPrestamos.cargarSelectores(); panelPrestamos.cargarPrestamos(); }
                else if (idx == 3) panelReportes.cargarDatosReportes();
                else if (idx == 4) panelUsuarios.cargarUsuarios();
            });

            panelContenedor.add(tabbedPane, BorderLayout.CENTER);
        } else {
            // Interfaz adaptada al rol estudiante
            PanelEstudiantePortal portal = new PanelEstudiantePortal();
            panelContenedor.add(portal, BorderLayout.CENTER);
        }

        setContentPane(panelContenedor);
    }

    private void cerrarSesion() {
        int resp = JOptionPane.showConfirmDialog(this,
                "¿Desea cerrar la sesión actual?",
                "Cerrar Sesión", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            AutenticacionController.cerrarSesion();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
