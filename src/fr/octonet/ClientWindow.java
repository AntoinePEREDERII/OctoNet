package fr.octonet;

import javax.swing.*;
import java.awt.*;

public class ClientWindow extends JFrame {
    private String clientName;
    private JTextArea messageArea;
    private JTextField destField;
    private JTextField messageField;
    private Admin admin;
    private String lastSender = "";

    public ClientWindow(String clientName, Admin admin) {
        super("Client: " + clientName);
        this.clientName = clientName;
        this.admin = admin;

        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Zone de messages
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        mainPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Panel pour envoyer des messages
        JPanel inputPanel = new JPanel(new BorderLayout());

        // Champ pour le destinataire
        JPanel destPanel = new JPanel(new BorderLayout());
        destPanel.add(new JLabel("Destinataire : "), BorderLayout.WEST);
        destField = new JTextField();
        destPanel.add(destField, BorderLayout.CENTER);

        inputPanel.add(destPanel, BorderLayout.NORTH);

        // Champ pour le message
        messageField = new JTextField();
        JButton sendButton = new JButton("Envoyer");

        // Ajout de l'action pour envoyer le message en appuyant sur Entrée
        messageField.addActionListener(e -> sendButton.doClick());

        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.add(messageField, BorderLayout.CENTER);
        msgPanel.add(sendButton, BorderLayout.EAST);

        inputPanel.add(msgPanel, BorderLayout.SOUTH);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Action pour envoyer un message
        sendButton.addActionListener(e -> {
            String dest = destField.getText();
            String message = messageField.getText();
            if (!dest.isEmpty() && !message.isEmpty()) {
                admin.sendMessage(clientName, dest, message);
                messageArea.append("Vous à " + dest + " : " + message + "\n");
                messageField.setText("");
            }
        });

        add(mainPanel);
    }

    public void addMessage(String message) {
        if (message.startsWith("De ")) {
            int idx = message.indexOf(':');
            if (idx > 3) {
                lastSender = message.substring(3, idx).trim();
                destField.setText(lastSender);
            }
        }
        messageArea.append(message + "\n");
    }
} 