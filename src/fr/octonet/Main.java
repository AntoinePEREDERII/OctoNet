// Point d'entrée de l'application
// Initialise l'interface administrateur et le serveur
// Lance l'application dans le thread EDT (Event Dispatch Thread) de Swing
package fr.octonet;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Admin admin = new Admin();
                AdminUI adminUI = new AdminUI(admin);
                admin.setAdminUI(adminUI);
                adminUI.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
