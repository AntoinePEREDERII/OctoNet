// Interface graphique pour un client
// Permet à l'utilisateur d'envoyer et recevoir des messages
// Gère l'affichage des messages et l'interaction utilisateur
// Se ferme automatiquement quand l'utilisateur ferme la fenêtre
package fr.octonet;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

public class ClientWindow extends JFrame {
    private final String clientName;
    private final JTextArea chatArea;
    private final JTextField messageField;
    private final JTextField destField;
    private final Admin admin;

    public ClientWindow(String clientName, Admin admin) {
        this.clientName = clientName;
        this.admin = admin;
        setTitle("Chat - " + clientName);
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        destField = new JTextField();
        JButton sendButton = new JButton("Envoyer");

        sendButton.addActionListener(_ -> sendMessage());
        messageField.addActionListener(_ -> sendMessage());

        JPanel inputPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        JPanel destPanel = new JPanel(new BorderLayout());
        destPanel.add(new JLabel("Destinataire: "), BorderLayout.WEST);
        destPanel.add(destField, BorderLayout.CENTER);
        
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        inputPanel.add(destPanel);
        inputPanel.add(messagePanel);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);

        // Ajouter un WindowListener pour gérer la fermeture de la fenêtre
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Supprimer le client de la table de routage
                admin.removeClient(clientName);
                // Supprimer la fenêtre de la map des fenêtres
                AdminUI.clientWindows.remove(clientName);
            }
        });
    }

    // Envoie un message à un autre client
    private void sendMessage() {
        String message = messageField.getText().trim();
        String dest = destField.getText().trim();
        if (!message.isEmpty() && !dest.isEmpty()) {
            chatArea.append("Moi à " + dest + ": " + message + "\n");
            messageField.setText("");
            // Envoyer le message via l'admin
            admin.sendMessageFromClientToClient(clientName, dest, message);
        } else if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer un message.");
        } else if (dest.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez spécifier un destinataire.");
        }
    }

    // Reçoit et affiche un message
    public void receiveMessage(String from, String to, String message) {
        chatArea.append(from + ": " + message + "\n");
        destField.setText(from);  // Met l'expéditeur comme destinataire
    }

    public String getClientName() {
        return clientName;
    }
} 