package fr.octonet;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AdminUI extends JFrame {
    private DefaultListModel<String> clientListModel;
    private JList<String> clientJList;
    private JTextField serverAddressField;
    private JTextField clientSrcField;
    private JTextField clientDestField;
    private JTextField messageField;
    private Admin admin;

    public AdminUI(Admin admin) {
        this.admin = admin;

        setTitle("Admin Interface");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel pour les clients
        clientListModel = new DefaultListModel<>();
        clientJList = new JList<>(clientListModel);
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
            clientListModel.addElement(client.getName());
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

    // Méthode pour afficher la table de routage
    private void updateRoutingTable(JTextArea area) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : admin.getRoutingTable().entrySet()) {
            sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        area.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Admin admin = new Admin();
            admin.startSrv();
            AdminUI adminUI = new AdminUI(admin);
            adminUI.setVisible(true);
        });
    }
}
