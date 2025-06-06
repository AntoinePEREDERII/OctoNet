package fr.octonet;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;

class ClientWindow extends JFrame {
    private String clientName;
    private JTextArea messageArea;
    private JTextField destField;
    private JTextField messageField;
    private Admin admin;
    private String lastSender = ""; // Nouveau : mémorise le dernier expéditeur

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
                admin.sendMessageFromClientToClient(clientName, dest, message);
                messageArea.append("Vous à " + dest + " : " + message + "\n");
                messageField.setText("");
            }
        });

        add(mainPanel);
    }

    public void addMessage(String message) {
        // Recherche du nom de l'expéditeur dans le message (format attendu : "Reçu: <message>" ou "De <nom>: <message>")
        // Ici, on suppose que le format est "De <nom>: <message>"
        if (message.startsWith("De ")) {
            int idx = message.indexOf(':');
            if (idx > 3) {
                lastSender = message.substring(3, idx).trim();
                destField.setText(lastSender); // Met à jour automatiquement le champ destinataire
            }
        }
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
    public static Map<String, ClientWindow> clientWindows;
    private JLabel serverInfoLabel;

    public AdminUI(Admin admin) {
        super("Admin Interface");
        this.admin = admin;
        clientWindows = new HashMap<>();

        setSize(900, 600); // Taille augmentée
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Affichage de l'IP et port du serveur local
        serverInfoLabel = new JLabel("Serveur local : " + admin.getLocalIP() + ":" + admin.getPortListenCl());
        mainPanel.add(serverInfoLabel, BorderLayout.NORTH);

        // Panel pour les clients
        clientListModel = new DefaultListModel<>();
        clientJList = new JList<>(clientListModel);
        JScrollPane clientScrollPane = new JScrollPane(clientJList);
        clientScrollPane.setPreferredSize(new Dimension(180, 0));
        mainPanel.add(clientScrollPane, BorderLayout.WEST);

        // Panel pour les serveurs distants et les messages
        JPanel controlPanel = new JPanel(new GridLayout(12, 1, 5, 5)); // 12 lignes pour tout afficher

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

        JScrollPane controlScrollPane = new JScrollPane(controlPanel);
        mainPanel.add(controlScrollPane, BorderLayout.CENTER);

        // Ajout d'un panneau pour la table de routage
        JTextArea routingTableArea = new JTextArea(10, 20);
        routingTableArea.setEditable(false);
        JScrollPane routingScrollPane = new JScrollPane(routingTableArea);
        routingScrollPane.setPreferredSize(new Dimension(220, 0));
        mainPanel.add(routingScrollPane, BorderLayout.EAST);

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
                // Utilise la table de routage pour router le message
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
        try {
            Admin admin = new Admin();
            AdminUI adminUI = new AdminUI(admin);
            adminUI.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
