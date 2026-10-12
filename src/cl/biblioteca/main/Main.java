package cl.biblioteca.main;

import cl.biblioteca.conexion.DatabaseConnection;
import cl.biblioteca.vista.LoginFrame;

import javax.swing.*;

/**
 * Punto de entrada principal del Sistema de Gestión de Biblioteca Escolar.
 * Desarrollo Orientado a Objetos II - Evaluación Final Transversal (EFT).
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("============================================================");
        System.out.println("   SISTEMA DE GESTIÓN DE BIBLIOTECA ESCOLAR - DUOC UC       ");
        System.out.println("   Desarrollo Orientado a Objetos II - Evaluación Final     ");
        System.out.println("============================================================");

        // Configuración de Look and Feel nativo del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Continuar con Look and Feel por defecto si falla
        }

        // Prueba preliminar de conexión en consola
        System.out.println("URL JDBC: " + DatabaseConnection.getInstance().getUrl());
        boolean bdOk = DatabaseConnection.getInstance().testConnection();
        if (bdOk) {
            System.out.println("Estado BD: CONECTADO EXITOSAMENTE a base de datos 'biblioteca'");
        } else {
            System.err.println("Estado BD: ADVERTENCIA - No se pudo conectar a MySQL. Verifique el servicio.");
        }

        System.out.println("Iniciando Interfaz Gráfica Swing (JFrame)...");

        // Lanzar interfaz en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}
