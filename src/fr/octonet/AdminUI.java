// Interface graphique pour l'administrateur du réseau
// Permet de :
// - Voir la liste des clients connectés
// - Ajouter des serveurs distants
// - Envoyer la table de routage
// - Créer de nouveaux clients
// - Envoyer des messages entre clients
// - Voir les logs et la table de routage
// - Gérer les fenêtres des clients
package fr.octonet;

import javax.swing.*;

import common.Trame_routage;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
    private JTextArea routingTableArea;

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
        JPanel controlPanel = new JPanel(new GridLayout(13, 1, 5, 5));

        serverAddressField = new JTextField();
        controlPanel.add(new JLabel("Adresse du Serveur distant (IP uniquement):"));
        controlPanel.add(serverAddressField);

        JButton addServerButton = new JButton("Ajouter Serveur");
        controlPanel.add(addServerButton);

        JButton sendRoutingTableButton = new JButton("Envoyer Table de Routage");
        controlPanel.add(sendRoutingTableButton);

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
        messageField.addActionListener(event -> {
            String clientSrc = clientSrcField.getText();
            String clientDest = clientDestField.getText();
            String message = messageField.getText();
            if (!clientSrc.isEmpty() && !clientDest.isEmpty() && !message.isEmpty()) {
                admin.sendMessageFromClientToClient(clientSrc, clientDest, message);
                addLog("Demande d'envoi de " + clientSrc + " à " + clientDest + " : " + message);
                messageField.setText("");
            } else {
                JOptionPane.showMessageDialog(AdminUI.this,
                        "Veuillez remplir tous les champs (source, destination, message).");
            }
        });
        controlPanel.add(messageField);

        JButton sendMessageButton = new JButton("Envoyer Message");
        controlPanel.add(sendMessageButton);

        JScrollPane controlScrollPane = new JScrollPane(controlPanel);
        mainPanel.add(controlScrollPane, BorderLayout.CENTER);

        // Ajout d'un panneau pour la table de routage
        routingTableArea = new JTextArea(10, 20);
        routingTableArea.setEditable(false);
        JScrollPane routingScrollPane = new JScrollPane(routingTableArea);
        routingScrollPane.setPreferredSize(new Dimension(220, 0));
        mainPanel.add(routingScrollPane, BorderLayout.EAST);

        // Zone de log dans un panneau séparé
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Logs"));
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(400, 120));
        logPanel.add(logScrollPane, BorderLayout.CENTER);
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        // Action pour ajouter un serveur distant
        addServerButton.addActionListener(event -> {
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
                        updateRoutingTable();
                        addServerButton.setEnabled(true); // Réactive le bouton
                    });
                }).start();
            }
        });

        // Action pour envoyer la table de routage
        sendRoutingTableButton.addActionListener(event -> {
            for (String serverAddress : admin.getRemoteServers()) {
                // Créer une trame de routage avec uniquement nos clients locaux
                ArrayList<String> localClients = new ArrayList<>();
                for (Map.Entry<String, String> entry : admin.getRoutingTable().entrySet()) {
                    if (entry.getValue().equals(admin.getLocalIP() + ":" + admin.getPortSrv())) {
                        localClients.add(entry.getKey());
                    }
                }
                
                Trame_routage trame = new Trame_routage(
                    2,
                    serverAddress,
                    admin.getLocalIP() + ":" + admin.getPortSrv(),
                    new ArrayList<>(Collections.singletonList(admin.getLocalIP() + ":" + admin.getPortSrv())),
                    new ArrayList<>(Collections.singletonList(localClients)),
                    new ArrayList<>(Collections.singletonList(0))
                );
                
                admin.getServeur().sendTrameToServer(trame, serverAddress);
                addLog("Table de routage envoyée à " + serverAddress);
            }
        });

        // Action pour ajouter un client
        addClientButton.addActionListener(event -> {
            String clientName = generateRandomClientId();
            admin.addClient(clientName);
            clientSrcField.setText(clientName);
            updateRoutingTable();
        });

        // Action pour envoyer un message d'un client à un autre
        sendMessageButton.addActionListener(event -> {
            String clientSrc = clientSrcField.getText();
            String clientDest = clientDestField.getText();
            String message = messageField.getText();
            if (!clientSrc.isEmpty() && !clientDest.isEmpty() && !message.isEmpty()) {
                admin.sendMessageFromClientToClient(clientSrc, clientDest, message);
                addLog("Demande d'envoi de " + clientSrc + " à " + clientDest + " : " + message);
                messageField.setText("");
            } else {
                JOptionPane.showMessageDialog(AdminUI.this,
                        "Veuillez remplir tous les champs (source, destination, message).");
            }
        });

        add(mainPanel);
    }

    // Ajoute un client à la liste et crée sa fenêtre
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

    // Met à jour l'affichage de la table de routage
    public void updateRoutingTable() {
        SwingUtilities.invokeLater(() -> {
            if (routingTableArea != null) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : admin.getRoutingTable().entrySet()) {
                    sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
                }
                routingTableArea.setText(sb.toString());
            }
        });
    }

    // Ajoute un message dans la fenêtre d'un client
    public void addMessageToClientWindow(String from, String clientName, String message) {
        ClientWindow window = clientWindows.get(clientName);
        if (window != null) {
            window.receiveMessage(from,clientName, message);
        }
    }

    // Ajoute un message dans la zone de log
    public void addLog(String log) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(log + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    // Génère un ID aléatoire pour un nouveau client
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

    // Supprime un client de la liste
    public void removeClientFromList(String clientName) {
        clientListModel.removeElement(clientName);
    }
}
