package fr.octonet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AdminUI extends JFrame {
    private DefaultListModel<String> clientListModel;
    private JList<String> clientJList;
    private JTextField serverAddressField;
    private JTextField messageField;

    public AdminUI() {
        setTitle("Admin Interface");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Panel pour les clients
        clientListModel = new DefaultListModel<>();
        clientJList = new JList<>(clientListModel);
        mainPanel.add(new JScrollPane(clientJList), BorderLayout.WEST);

        // Panel pour les serveurs distants et les messages
        JPanel controlPanel = new JPanel(new GridLayout(5, 1));

        serverAddressField = new JTextField();
        controlPanel.add(new JLabel("Adresse du Serveur:"));
        controlPanel.add(serverAddressField);

        JButton addServerButton = new JButton("Ajouter Serveur");
        controlPanel.add(addServerButton);

        messageField = new JTextField();
        controlPanel.add(new JLabel("Message:"));
        controlPanel.add(messageField);

        JButton sendMessageButton = new JButton("Envoyer Message");
        controlPanel.add(sendMessageButton);

        mainPanel.add(controlPanel, BorderLayout.CENTER);

        // Ajouter des actions aux boutons
        addServerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String serverAddress = serverAddressField.getText();
                if (!serverAddress.isEmpty()) {
                    // Ajouter la logique pour ajouter un serveur distant
                    JOptionPane.showMessageDialog(AdminUI.this,
                            "Serveur ajouté: " + serverAddress);
                    serverAddressField.setText("");
                }
            }
        });

        sendMessageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                String selectedClient = clientJList.getSelectedValue();
                if (selectedClient != null && !message.isEmpty()) {
                    // Ajouter la logique pour envoyer un message au client sélectionné
                    JOptionPane.showMessageDialog(AdminUI.this,
                            "Message envoyé à " + selectedClient + ": " + message);
                    messageField.setText("");
                } else {
                    JOptionPane.showMessageDialog(AdminUI.this,
                            "Veuillez sélectionner un client et entrer un message.");
                }
            }
        });

        add(mainPanel);
    }

    public void addClient(String client) {
        clientListModel.addElement(client);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                AdminUI adminUI = new AdminUI();
                adminUI.setVisible(true);

                // Exemple d'ajout de clients
                adminUI.addClient("Client 1");
                adminUI.addClient("Client 2");
            }
        });
    }
}
