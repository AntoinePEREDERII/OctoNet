package fr.octonet;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

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
    private JTextArea logArea;

    public AdminUI(Admin admin) {
        super("Admin Interface");
        this.admin = admin;
        clientWindows = new HashMap<>();

        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Affichage de l'IP et port du serveur local
        serverInfoLabel = new JLabel("Serveur local : " + admin.getLocalIP() + ":" + admin.getPortSrv());
        mainPanel.add(serverInfoLabel, BorderLayout.NORTH);

        // Panel pour les clients
        clientListModel = new DefaultListModel<>();
        clientJList = new JList<>(clientListModel);
        JScrollPane clientScrollPane = new JScrollPane(clientJList);
        clientScrollPane.setPreferredSize(new Dimension(180, 0));
        mainPanel.add(clientScrollPane, BorderLayout.WEST);

        // Panel pour les serveurs distants et les messages
        JPanel controlPanel = new JPanel(new GridLayout(12, 1, 5, 5));

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

        // Zone de log
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(400, 120));
        mainPanel.add(logScrollPane, BorderLayout.SOUTH);

        // Action pour ajouter un serveur distant
        addServerButton.addActionListener(e -> {
            String serverAddress = serverAddressField.getText();
            if (!serverAddress.isEmpty()) {
                addServerButton.setEnabled(false); // Désactive le bouton pendant la connexion
                new Thread(() -> {
                    boolean success = admin.addRemoteServer(serverAddress);
                    SwingUtilities.invokeLater(() -> {
                        if (success) {
                            JOptionPane.showMessageDialog(
                                this,
                                "Connexion réussie au serveur distant : " + serverAddress,
                                "Succès",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(
                                this,
                                "Impossible de joindre le serveur distant : " + serverAddress,
                                "Erreur",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                        serverAddressField.setText("");
                        updateRoutingTable(routingTableArea);
                        addServerButton.setEnabled(true); // Réactive le bouton
                    });
                }).start();
            }
        });

        // Action pour ajouter un client
        addClientButton.addActionListener(e -> {
            String clientName = generateRandomClientId();
            admin.addClient(clientName);
            clientSrcField.setText(clientName);
            updateRoutingTable(routingTableArea);
        });

        // Action pour envoyer un message d'un client à un autre
        sendMessageButton.addActionListener(e -> {
            String clientSrc = clientSrcField.getText();
            String clientDest = clientDestField.getText();
            String message = messageField.getText();
            if (!clientSrc.isEmpty() && !clientDest.isEmpty() && !message.isEmpty()) {
                admin.sendMessageFromClientToClient(clientSrc, clientDest, message);
                if (admin != null && admin.getAdminUI() != null) {
                    admin.getAdminUI().addLog("Demande d'envoi de " + clientSrc + " à " + clientDest + " : " + message);
                }
                messageField.setText("");
            } else {
                JOptionPane.showMessageDialog(AdminUI.this,
                        "Veuillez remplir tous les champs (source, destination, message).");
            }
        });

        add(mainPanel);
    }

    public void addClientToList(String clientName) {
        if (!clientListModel.contains(clientName)) {
            clientListModel.addElement(clientName);
            showClientWindow(clientName);
        }
    }

    private void showClientWindow(String clientName) {
        ClientWindow window = clientWindows.get(clientName);
        if (window == null) {
            window = new ClientWindow(clientName, admin);
            clientWindows.put(clientName, window);
        }
        window.setVisible(true);
    }

    private void updateRoutingTable(JTextArea area) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : admin.getRoutingTable().entrySet()) {
            sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        area.setText(sb.toString());
    }

    public void addMessageToClientWindow(String from, String clientName, String message) {
        ClientWindow window = clientWindows.get(clientName);
        if (window != null) {
            window.receiveMessage(from,clientName, message);
        }
    }

    public void addLog(String log) {
        logArea.append(log + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String generateRandomClientId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
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
