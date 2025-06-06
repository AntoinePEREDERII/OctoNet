package fr.octonet;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

class ClientWindow extends JFrame {
    private String clientName;
    private JTextArea messageArea;
    private JTextField messageField;
    private Admin admin;

    public ClientWindow(String clientName, Admin admin) {
        super("Client: " + clientName);
        this.clientName = clientName;
        this.admin = admin;

        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Zone de messages
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        mainPanel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Panel pour envoyer des messages
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        JButton sendButton = new JButton("Envoyer");
        
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        // Action pour envoyer un message
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                admin.sendMessageToClient(clientName, message);
                messageArea.append("Vous: " + message + "\n");
                messageField.setText("");
            }
        });

        add(mainPanel);
    }

    public void addMessage(String message) {
        messageArea.append(message + "\n");
    }
}

public class AdminUI extends JFrame {
    private DefaultListModel<String> clientListModel;
    private JList<String> clientJList;
    private JTextField serverAddressField;
    private JTextField clientSrcField;
    private JTextField clientDestField;
    private JTextField messageField;
    private Admin admin;
    private Map<String, ClientWindow> clientWindows;

    public AdminUI(Admin admin) {
        super("Admin Interface");
        this.admin = admin;
        this.clientWindows = new HashMap<>();

        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel pour les clients
        clientListModel = new DefaultListModel<>();
        clientJList = new JList<>(clientListModel);
        clientJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedClient = clientJList.getSelectedValue();
                if (selectedClient != null) {
                    showClientWindow(selectedClient);
                }
            }
        });
        mainPanel.add(new JScrollPane(clientJList), BorderLayout.WEST);

        // Panel pour les serveurs distants et les messages
        JPanel controlPanel = new JPanel(new GridLayout(10, 1));

        serverAddressField = new JTextField();
        controlPanel.add(new JLabel("Adresse du Serveur distant (ip:port):"));
        controlPanel.add(serverAddressField);

        JButton addServerButton = new JButton("Ajouter Serveur");
        controlPanel.add(addServerButton);

        JButton addClientButton = new JButton("Ajouter Client");
        controlPanel.add(addClientButton);

        controlPanel.add(new JLabel("Nom du client source :"));
        clientSrcField = new JTextField();
        controlPanel.add(clientSrcField);

        controlPanel.add(new JLabel("Nom du client destinataire :"));
        clientDestField = new JTextField();
        controlPanel.add(clientDestField);

        controlPanel.add(new JLabel("Message :"));
        messageField = new JTextField();
        controlPanel.add(messageField);

        JButton sendMessageButton = new JButton("Envoyer Message");
        controlPanel.add(sendMessageButton);

        mainPanel.add(controlPanel, BorderLayout.CENTER);

        // Ajout d'un panneau pour la table de routage
        JTextArea routingTableArea = new JTextArea(10, 20);
        routingTableArea.setEditable(false);
        mainPanel.add(new JScrollPane(routingTableArea), BorderLayout.EAST);

        // Action pour ajouter un serveur distant
        addServerButton.addActionListener(e -> {
            String serverAddress = serverAddressField.getText();
            if (!serverAddress.isEmpty()) {
                admin.addRemoteServer(serverAddress);
                JOptionPane.showMessageDialog(AdminUI.this,
                        "Serveur ajouté: " + serverAddress);
                serverAddressField.setText("");
                updateRoutingTable(routingTableArea);
            }
        });

        // Action pour ajouter un client
        addClientButton.addActionListener(e -> {
            Client client = admin.newClient();
            String clientName = client.getName();
            clientListModel.addElement(clientName);
            showClientWindow(clientName);
            updateRoutingTable(routingTableArea);
        });

        // Action pour envoyer un message d'un client à un autre
        sendMessageButton.addActionListener(e -> {
            String clientSrc = clientSrcField.getText();
            String clientDest = clientDestField.getText();
            String message = messageField.getText();
            if (!clientSrc.isEmpty() && !clientDest.isEmpty() && !message.isEmpty()) {
                admin.sendMessageFromClientToClient(clientSrc, clientDest, message);
                JOptionPane.showMessageDialog(AdminUI.this,
                        "Message envoyé de " + clientSrc + " à " + clientDest + ": " + message);
                messageField.setText("");
            } else {
                JOptionPane.showMessageDialog(AdminUI.this,
                        "Veuillez remplir tous les champs (source, destination, message).");
            }
        });

        add(mainPanel);
    }

    private void showClientWindow(String clientName) {
        ClientWindow window = clientWindows.get(clientName);
        if (window == null) {
            window = new ClientWindow(clientName, admin);
            clientWindows.put(clientName, window);
        }
        window.setVisible(true);
    }

    // Méthode pour afficher la table de routage
    private void updateRoutingTable(JTextArea area) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : admin.getRoutingTable().entrySet()) {
            sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        area.setText(sb.toString());
    }

    public void addMessageToClientWindow(String clientName, String message) {
        ClientWindow window = clientWindows.get(clientName);
        if (window != null) {
            window.addMessage(message);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Attendre un peu pour s'assurer que les ports sont libérés
                Thread.sleep(1000);
                
                Admin admin = new Admin();
                AdminUI adminUI = new AdminUI(admin);
                admin.setAdminUI(adminUI);  // Lier l'UI à l'Admin
                admin.startSrv();
                
                // Attendre que le serveur démarre
                Thread.sleep(500);
                
                adminUI.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Erreur au démarrage : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}
