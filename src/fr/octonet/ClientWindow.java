package fr.octonet;

import javax.swing.*;
import java.awt.*;

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

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

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
    }

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

    public void receiveMessage(String from, String to, String message) {
        chatArea.append(from + ": " + message + "\n");
        destField.setText(from);  // Met l'expéditeur comme destinataire
    }

    public String getClientName() {
        return clientName;
    }
} 