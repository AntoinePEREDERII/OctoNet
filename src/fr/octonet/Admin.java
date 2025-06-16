// Classe principale gérant l'administration du réseau
// Coordonne les interactions entre le serveur, les clients et l'interface utilisateur
// Gère la table de routage et les connexions avec les serveurs distants
// Permet d'ajouter/supprimer des clients et d'envoyer des messages
// Détecte automatiquement l'adresse IP locale
package fr.octonet;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.SwingUtilities;

import java.net.Socket;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.*;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import common.Trame;
import common.Trame_message;
import common.Trame_routage;

public class Admin {
    private final Map<String, Client> clients = new HashMap<>();
    private final List<String> remoteServers = new ArrayList<>();
    private final Map<String, String> routingTable = new ConcurrentHashMap<>();
    private Serveur serveur;
    private AdminUI adminUI;
    private String localIP;

    // Initialise le serveur et récupère l'IP locale
    public Admin() {
        try {
            // Obtenir l'adresse IP réelle de la machine
            this.localIP = getLocalIP();
        } catch (Exception e) {
            this.localIP = "127.0.0.1";
            System.err.println("Erreur lors de la récupération de l'adresse IP: " + e.getMessage());
        }
        // Initialiser le serveur d'abord
        this.serveur = new Serveur(this);
        new Thread(() -> serveur.start()).start();
    }

    public void initializeUI() {
        // Initialiser l'interface après la création de l'instance Admin
        this.adminUI = new AdminUI(this);
        // Configurer l'interface dans le serveur
        this.serveur.setAdminUI(this.adminUI);
    }

    public String getLocalIP() {
        try {
            // Essayer d'obtenir l'adresse IP non-loopback
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // Ignorer les interfaces de bouclage et les interfaces désactivées
                if (iface.isLoopback() || !iface.isUp()) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // Vérifier si c'est une adresse IPv4
                    if (addr.getHostAddress().indexOf(':') == -1) {
                        return addr.getHostAddress();
                    }
                }
            }
            // Si aucune adresse IP n'est trouvée, retourner l'adresse de bouclage
            return "127.0.0.1";
        } catch (Exception e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }

    // Ajoute un nouveau client local
    public void addClient(String clientName) {
        // Vérifier si le client existe déjà
        if (routingTable.containsKey(clientName)) {
            System.err.println("Le client " + clientName + " existe déjà");
            if (adminUI != null) adminUI.addLog("Erreur: Le client " + clientName + " existe déjà");
            return;
        }

        // Créer le client local
        Client client = new Client(clientName);
        clients.put(clientName, client);
        
        // Ajouter le client à la table de routage avec l'adresse locale
        String localAddress = localIP + ":" + serveur.getPort();
        routingTable.put(clientName, localAddress);
        
        if (adminUI != null) {
            adminUI.addClientToList(clientName);
            adminUI.addLog("Client local ajouté: " + clientName + " sur " + localAddress);
        }
        
        System.out.println("Client local ajouté: " + clientName + " sur " + localAddress);
    }

    // Ajoute un client distant à la table de routage
    public void addRemoteClient(String clientName, String serverAddress) {
        System.out.println("Ajout du client distant " + clientName + " via " + serverAddress);
        routingTable.put(clientName, serverAddress);
        if (adminUI != null) {
            SwingUtilities.invokeLater(() -> {
                adminUI.updateRoutingTable();
                adminUI.addLog("Client distant ajouté: " + clientName + " via " + serverAddress);
            });
        }
    }

    // Tente de se connecter à un serveur distant
    public boolean addRemoteServer(String host) {
        if (!remoteServers.contains(host)) {
            try {
                
                // Utiliser le portsrv pour la connexion au serveur distant
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host,  this.getPortSrv()), 5000); // 5 secondes de timeout
                
                try {
                    // Ne pas envoyer la table de routage automatiquement
                    // Elle sera envoyée via le bouton dédié
                    remoteServers.add(host + ":" + this.getPortSrv()); // Stocker avec le portsrv
                    System.out.println("Serveur distant ajouté: " + host + ":" + this.getPortSrv());
                    if (adminUI != null) {
                        adminUI.addLog("Serveur distant ajouté: " + host + ":" + this.getPortSrv());
                    }
                    return true;
                } finally {
                    socket.close();
                }
            } catch (SocketTimeoutException e) {
                System.err.println("Timeout lors de la connexion au serveur distant");
                return false;
            } catch (ConnectException e) {
                System.err.println("Connexion refusée - Le serveur distant n'est pas accessible");
                return false;
            } catch (Exception e) {
                System.err.println("Erreur lors de la connexion au serveur distant: " + e.getMessage());
                return false;
            }
        }
        System.out.println("Serveur déja présent");
        return false; // déjà présent
    }

    // Met à jour la table de routage avec les infos reçues
    public void updateRoutingTable(String serializedTable) {
        String[] entries = serializedTable.split(";");
        for (String entry : entries) {
            if (!entry.isEmpty()) {
                String[] parts = entry.split("=");
                if (parts.length == 2) {
                    String clientName = parts[0];
                    String serverAddress = parts[1];
                    
                    // Ne pas ajouter le client s'il est déjà géré localement
                    if (!routingTable.containsKey(clientName) || 
                        !routingTable.get(clientName).equals(localIP + ":" + serveur.getPort())) {
                        // Ajouter le client avec l'adresse du serveur distant
                        routingTable.put(clientName, serverAddress);
                        if (adminUI != null) {
                            adminUI.addClientToList(clientName);
                        }
                        System.out.println("Client distant ajouté: " + clientName + " via " + serverAddress);
                    }
                }
            }
        }
    }

    public void sendMessage(String from, String to, String message) {
        Client client = clients.get(to);
        if (client != null) {
            client.receiveMessage(from, message);
            if (adminUI != null) {
                adminUI.addMessageToClientWindow(from, to, message);
                adminUI.addLog("Message reçu de " + from + ": " + message);
            }
        }
    }

    // Envoie un message entre deux clients
    public void sendMessageFromClientToClient(String from, String to, String message) {
        String nextHop = routingTable.get(to);
        if (nextHop == null) {
            if (adminUI != null) adminUI.addLog("Destination inconnue dans la table de routage pour " + to);
            return;
        }

        // Si le destinataire est local
        if (nextHop.equals(localIP + ":" + serveur.getPort())) {
            sendMessage(from, to, message);
            if (adminUI != null) adminUI.addLog("Message délivré localement à " + to);
        } else {
            // Pour un client distant, envoyer via le serveur distant
            Trame_message trame = new Trame_message(1, nextHop, localIP + ":" + serveur.getPort(), to, from, message);
            serveur.sendTrameToServer(trame, nextHop);
            if (adminUI != null) adminUI.addLog("Message routé vers " + nextHop + " pour " + to);
        }
    }

    public Map<String, String> getRoutingTable() {
        return routingTable;
    }

    public int getPortSrv() {
        return serveur.getPort();
    }

    public void setAdminUI(AdminUI adminUI) {
        this.adminUI = adminUI;
    }

    public AdminUI getAdminUI() {
        return adminUI;
    }

    public List<String> getRemoteServers() {
        return remoteServers;
    }

    public Serveur getServeur() {
        return serveur;
    }

    // Supprime un client du réseau
    public void removeClient(String clientName) {
        // Supprimer le client de la table de routage
        routingTable.remove(clientName);
        
        // Supprimer le client des clients locaux
        clients.remove(clientName);
        
        // Mettre à jour l'interface
        if (adminUI != null) {
            adminUI.removeClientFromList(clientName);
            adminUI.addLog("Client supprimé: " + clientName);
            adminUI.updateRoutingTable();
        }
        
        System.out.println("Client supprimé: " + clientName);
    }
}


