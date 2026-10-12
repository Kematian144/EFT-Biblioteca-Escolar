package cl.biblioteca.vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Utilitario de estilos y componentes para la interfaz gráfica Swing.
 * Garantiza botones con alto contraste, legibilidad clara y tablas estilizadas.
 */
public class UIHelper {

    public static final Color COLOR_PRIMARIO = new Color(25, 118, 210);    // Azul
    public static final Color COLOR_EXITO = new Color(46, 125, 50);        // Verde
    public static final Color COLOR_PELIGRO = new Color(198, 40, 40);      // Rojo
    public static final Color COLOR_ADVERTENCIA = new Color(230, 81, 0);    // Naranja
    public static final Color COLOR_SECUNDARIO = new Color(100, 116, 139); // Gris azulado
    public static final Color COLOR_FONDO_CLARO = new Color(248, 249, 250); // Fondo general

    public static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_LABEL = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FUENTE_TEXTO = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FUENTE_BOTON = new Font("Segoe UI", Font.BOLD, 12);

    /**
     * Crea un botón estilizado con color sólido, texto blanco y padding adecuado.
     */
    public static JButton crearBoton(String texto, Color fondo) {
        JButton btn = new JButton(texto);
        btn.setFont(FUENTE_BOTON);
        btn.setBackground(fondo);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fondo.darker(), 1),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover sutil
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(fondo.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(fondo);
            }
        });

        return btn;
    }

    /**
     * Aplica estilo moderno a un JTable.
     */
    public static void aplicarEstiloTabla(JTable tabla) {
        tabla.setFont(FUENTE_TEXTO);
        tabla.setRowHeight(26);
        tabla.setGridColor(new Color(224, 224, 224));
        tabla.setSelectionBackground(new Color(227, 242, 253));
        tabla.setSelectionForeground(Color.BLACK);
        tabla.setShowGrid(true);

        JTableHeader header = tabla.getTableHeader();
        header.setFont(FUENTE_LABEL);
        header.setBackground(new Color(33, 37, 41));
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tabla.setDefaultRenderer(Integer.class, centerRenderer);
        tabla.setDefaultRenderer(Long.class, centerRenderer);
    }
}
