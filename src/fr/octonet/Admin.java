package fr.octonet;

import java.util.*;

public class Admin {
    private int nbClient = 0;
    private int portListenCl = 9090;
    private int portListenSrv = 9091;
    private int portClient = 9092;
    private String localIP = "127.0.0.1"; // Adresse IP locale

    private final Map<String, Client> clients = new HashMap<>();
    private final List<String> remoteServers = new ArrayList<>();
    private final Map<String, String> routingTable = new HashMap<>(); // clé: nom, valeur: next hop

    private Serveur serveur;
    private AdminUI adminUI;

    public Client newClient() {
        int newPort = portClient + nbClient;
        Client client = new Client("localhost", portListenCl); // Les clients se connectent au port d'écoute client
        clients.put(client.getName(), client);
        nbClient++;
        routingTable.put(client.getName(), "local"); // Routage local
        return client;
    }

    public void addRemoteServer(String address) {
        remoteServers.add(address);
        routingTable.put(address, address); // Routage vers serveur distant
        if (serveur != null) {
            serveur.connectToRemoteServer(address);
        }
    }

    public List<Client> getClients() {
        return new ArrayList<>(clients.values());
    }

    public void sendMessageToClient(String clientName, String message) {
        Client client = clients.get(clientName);
        if (client != null) {
            try {
                client.sendMessage(clientName, message);  // Le client envoie à lui-même
                System.out.println("Message envoyé à " + clientName + ": " + message);
                
                // Afficher le message dans la fenêtre du client
                if (adminUI != null) {
                    adminUI.addMessageToClientWindow(clientName, message);
                }
                // Affichage dans la fenêtre du client destinataire (si UI)
                if (AdminUI.clientWindows != null && AdminUI.clientWindows.containsKey(clientName)) {
                    AdminUI.clientWindows.get(clientName).addMessage("Reçu: " + message);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi du message à " + clientName + ": " + e.getMessage());
            }
        } else {
            System.err.println("Client " + clientName + " non trouvé");
        }
    }

    public void startSrv() {
        serveur = new Serveur(portListenCl, portListenSrv, this);
        new Thread(serveur::listenCl).start();
        new Thread(serveur::listenSrv).start();
        
        // Attendre que le serveur démarre
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Map<String, String> getRoutingTable() {
        return routingTable;
    }

    public void sendMessageFromClientToClient(String clientSrc, String clientDest, String message) {
        Client sourceClient = clients.get(clientSrc);
        if (sourceClient != null) {
            try {
                sourceClient.sendMessage(clientDest, message);
                System.out.println("Message envoyé de " + clientSrc + " à " + clientDest + ": " + message);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
            }
        } else {
            System.err.println("Client source " + clientSrc + " non trouvé");
        }
    }

    public void newClient(String name) {
        try {
            // Attendre que le serveur soit prêt
            Thread.sleep(1000);
            
            // Créer un nouveau client
            Client client = new Client(localIP, portListenCl);
            clients.put(name, client);
            
            // Pas besoin de thread ou de start, le client est prêt à envoyer des messages
            System.out.println("Nouveau client créé : " + name);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du client " + name + ": " + e.getMessage());
        }
    }

    public void sendMessageToClient(String source, String destination, String message) {
        try {
            Client sourceClient = clients.get(source);
            Client destClient = clients.get(destination);
            
            if (sourceClient == null || destClient == null) {
                System.err.println("Client source ou destination non trouvé");
                return;
            }
            
            // Envoyer le message
            sourceClient.sendMessage(source, message);
            System.out.println("Message envoyé de " + source + " à " + destination);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
        }
    }

    public void setAdminUI(AdminUI ui) {
        this.adminUI = ui;
    }

    // Ajoute ce setter si pas déjà présent
    public String getLocalIP() {
        return localIP;
    }
    public int getPortListenCl() {
        return portListenCl;
    }
}
