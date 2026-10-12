package cl.biblioteca.vista;

import cl.biblioteca.conexion.DatabaseConnection;
import cl.biblioteca.controlador.AutenticacionController;
import cl.biblioteca.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Ventana de Autenticación de Usuarios (Módulo de Login).
 * Valida las credenciales contra la base de datos MySQL y da paso al sistema
 * adaptando los permisos al rol correspondiente (Bibliotecario o Estudiante).
 */
public class LoginFrame extends JFrame {

    private final AutenticacionController autController;

    private JTextField txtCorreo;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JLabel lblEstadoBd;

    public LoginFrame() {
        this.autController = new AutenticacionController();
        inicializarUI();
        verificarConexionBd();
    }

    private void inicializarUI() {
        setTitle("Acceso al Sistema - Biblioteca Escolar");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 540);
        setResizable(false);
        setLocationRelativeTo(null);

        JPanel panelFondo = new JPanel(new BorderLayout());
        panelFondo.setBackground(UIHelper.COLOR_FONDO_CLARO);

        // Cabecera con fondo azul oscuro
        JPanel panelCabecera = new JPanel(new GridLayout(2, 1, 0, 4));
        panelCabecera.setBackground(new Color(15, 23, 42));
        panelCabecera.setBorder(new EmptyBorder(22, 20, 22, 20));

        JLabel lblTitulo = new JLabel("BIBLIOTECA ESCOLAR", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);

        JLabel lblSubtitulo = new JLabel("Sistema de Gestión de Circulación e Inventario", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(148, 163, 184));

        panelCabecera.add(lblTitulo);
        panelCabecera.add(lblSubtitulo);
        panelFondo.add(panelCabecera, BorderLayout.NORTH);

        // Formulario central
        JPanel panelCuerpo = new JPanel();
        panelCuerpo.setLayout(new BoxLayout(panelCuerpo, BoxLayout.Y_AXIS));
        panelCuerpo.setBackground(Color.WHITE);
        panelCuerpo.setBorder(new EmptyBorder(24, 32, 20, 32));

        JLabel lblCorreo = new JLabel("Correo Institucional:");
        lblCorreo.setFont(UIHelper.FUENTE_LABEL);
        lblCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelCuerpo.add(lblCorreo);
        panelCuerpo.add(Box.createVerticalStrut(6));

        txtCorreo = new JTextField();
        txtCorreo.setFont(UIHelper.FUENTE_TEXTO);
        txtCorreo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtCorreo.setPreferredSize(new Dimension(380, 36));
        txtCorreo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelCuerpo.add(txtCorreo);
        panelCuerpo.add(Box.createVerticalStrut(14));

        JLabel lblContrasena = new JLabel("Contraseña:");
        lblContrasena.setFont(UIHelper.FUENTE_LABEL);
        lblContrasena.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelCuerpo.add(lblContrasena);
        panelCuerpo.add(Box.createVerticalStrut(6));

        txtContrasena = new JPasswordField();
        txtContrasena.setFont(UIHelper.FUENTE_TEXTO);
        txtContrasena.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        txtContrasena.setPreferredSize(new Dimension(380, 36));
        txtContrasena.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelCuerpo.add(txtContrasena);
        panelCuerpo.add(Box.createVerticalStrut(20));

        // Botón principal de login
        btnIngresar = UIHelper.crearBoton("INGRESAR AL SISTEMA", UIHelper.COLOR_PRIMARIO);
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnIngresar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnIngresar.addActionListener(e -> realizarLogin());
        panelCuerpo.add(btnIngresar);
        panelCuerpo.add(Box.createVerticalStrut(16));

        // Accesos rápidos de prueba para la evaluación
        JLabel lblPrueba = new JLabel("Cuentas de prueba del poblamiento oficial:");
        lblPrueba.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblPrueba.setForeground(new Color(100, 116, 139));
        lblPrueba.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelCuerpo.add(lblPrueba);
        panelCuerpo.add(Box.createVerticalStrut(6));

        JPanel panelAccesos = new JPanel(new GridLayout(1, 2, 8, 0));
        panelAccesos.setBackground(Color.WHITE);
        panelAccesos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panelAccesos.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnDemoBiblio = UIHelper.crearBoton("Rol Bibliotecario", UIHelper.COLOR_EXITO);
        btnDemoBiblio.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDemoBiblio.addActionListener(e -> {
            txtCorreo.setText("antonia@correo.cl");
            txtContrasena.setText("clave123");
        });

        JButton btnDemoEst = UIHelper.crearBoton("Rol Estudiante", UIHelper.COLOR_ADVERTENCIA);
        btnDemoEst.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDemoEst.addActionListener(e -> {
            txtCorreo.setText("carlos@correo.cl");
            txtContrasena.setText("clave123");
        });

        panelAccesos.add(btnDemoBiblio);
        panelAccesos.add(btnDemoEst);
        panelCuerpo.add(panelAccesos);

        panelFondo.add(panelCuerpo, BorderLayout.CENTER);

        // Barra inferior: Estado de conexión
        JPanel panelPie = new JPanel(new BorderLayout());
        panelPie.setBackground(new Color(241, 245, 249));
        panelPie.setBorder(new EmptyBorder(8, 14, 8, 14));

        lblEstadoBd = new JLabel("Comprobando conexión a MySQL...");
        lblEstadoBd.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panelPie.add(lblEstadoBd, BorderLayout.WEST);

        JButton btnProbar = UIHelper.crearBoton("Probar Conexión", UIHelper.COLOR_SECUNDARIO);
        btnProbar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnProbar.addActionListener(e -> verificarConexionBd());
        panelPie.add(btnProbar, BorderLayout.EAST);

        panelFondo.add(panelPie, BorderLayout.SOUTH);

        // Atajo teclado ENTER en campos
        KeyAdapter enterKey = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    realizarLogin();
                }
            }
        };
        txtCorreo.addKeyListener(enterKey);
        txtContrasena.addKeyListener(enterKey);

        setContentPane(panelFondo);

        // Cargar por defecto bibliotecario oficial para comodidad
        txtCorreo.setText("antonia@correo.cl");
        txtContrasena.setText("clave123");
    }

    private void verificarConexionBd() {
        new Thread(() -> {
            boolean conecto = DatabaseConnection.getInstance().testConnection();
            SwingUtilities.invokeLater(() -> {
                if (conecto) {
                    lblEstadoBd.setText("Estado BD: CONECTADO (localhost:3306/biblioteca)");
                    lblEstadoBd.setForeground(new Color(22, 101, 52));
                } else {
                    lblEstadoBd.setText("Estado BD: DESCONECTADO (Inicie el servicio MySQL)");
                    lblEstadoBd.setForeground(new Color(185, 28, 28));
                }
            });
        }).start();
    }

    private void realizarLogin() {
        String correo = txtCorreo.getText().trim();
        String pass = new String(txtContrasena.getPassword());

        btnIngresar.setEnabled(false);

        new Thread(() -> {
            try {
                Usuario u = autController.iniciarSesion(correo, pass);
                SwingUtilities.invokeLater(() -> {
                    btnIngresar.setEnabled(true);
                    dispose();
                    MainFrame main = new MainFrame(u);
                    main.setVisible(true);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    btnIngresar.setEnabled(true);
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            ex.getMessage(), "Error de Autenticación", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}
