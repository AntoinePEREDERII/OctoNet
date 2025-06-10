package fr.octonet;

import javax.swing.*;
import java.awt.*;

public class ClientWindow extends JFrame {
    private final String clientName;
    private final JTextArea chatArea;
    private final JTextField messageField;
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
        JButton sendButton = new JButton("Envoyer");

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty()) {
            chatArea.append("Moi: " + message + "\n");
            messageField.setText("");
            // Envoyer le message via l'admin
            admin.sendMessageFromClientToClient(clientName, "ClientB", message);
        }
    }

    public void receiveMessage(String from, String message) {
        chatArea.append(from + ": " + message + "\n");
    }

    public String getClientName() {
        return clientName;
    }
} 