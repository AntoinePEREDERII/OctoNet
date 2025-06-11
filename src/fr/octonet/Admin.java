package fr.octonet;

import java.net.Socket;
import java.util.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.*;

public class Admin {
    private final Map<String, Client> clients = new HashMap<>();
    private final List<String> remoteServers = new ArrayList<>();
    private final Map<String, String> routingTable = new ConcurrentHashMap<>();
    private Serveur serveur;
    private AdminUI adminUI;
    private String localIP;

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
        // Puis initialiser l'interface
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

    public void addClient(String clientName) {
        // Vérifier si le client existe déjà
        if (routingTable.containsKey(clientName)) {
            System.err.println("Le client " + clientName + " existe déjà");
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
        }
        
        System.out.println("Client local ajouté: " + clientName + " sur " + localAddress);
    }

    public void addRemoteClient(String clientName, String serverAddress) {
        routingTable.put(clientName, serverAddress);
        if (adminUI != null) {
            adminUI.addClientToList(clientName);
        }
    }

    public boolean addRemoteServer(String serverAddress) {
        if (!remoteServers.contains(serverAddress)) {
            try {
                String[] parts = serverAddress.split(":");
                String host = parts[0];
                try (Socket socket = new Socket(host, 12346)) {
                    remoteServers.add(serverAddress);
                    System.out.println("Serveur distant ajouté: " + serverAddress);
                    // Échanger les tables de routage (dummy data pour l'exemple)
                    Trame_routage trame = new Trame_routage(2, null, null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                    serveur.sendTrameToServer(trame, serverAddress);
                    return true;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la connexion au serveur distant: " + e.getMessage());
                return false;
            }
        }
        return false; // déjà présent
    }

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
                adminUI.addMessageToClientWindow(from,to,message);
            }
        }
    }

    public void sendMessageFromClientToClient(String from, String to, String message) {
        String nextHop = routingTable.get(to);
        if (nextHop == null) {
            if (adminUI != null) adminUI.addLog("Destination inconnue dans la table de routage pour " + to);
            return;
        }
        if (nextHop.equals(localIP + ":" + serveur.getPort())) {
            sendMessage(from, to, message);
            if (adminUI != null) adminUI.addLog("Message délivré localement à " + to);
        } else {
            Trame_message trame = new Trame_message(1, null, null, to, from, message);
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
}


