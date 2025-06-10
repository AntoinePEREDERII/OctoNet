package fr.octonet;

import java.util.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.io.*;
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
        this.serveur = new Serveur(this);
        this.adminUI = new AdminUI(this);
        new Thread(() -> serveur.start()).start();
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
        Client client = new Client(clientName);
        clients.put(clientName, client);
        routingTable.put(clientName, localIP + ":" + serveur.getPort());
        if (adminUI != null) {
            adminUI.addClientToList(clientName);
        }
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
                int port = Integer.parseInt(parts[1]);
                try (Socket socket = new Socket(host, port)) {
                    remoteServers.add(serverAddress);
                    System.out.println("Serveur distant ajouté: " + serverAddress);
                    return true;
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de la connexion au serveur distant: " + e.getMessage());
                return false;
            }
        }
        return false; // déjà présent
    }

    public void sendMessage(String from, String to, String message) {
        Client client = clients.get(to);
        if (client != null) {
            client.receiveMessage(from, message);
            if (adminUI != null) {
                adminUI.addMessageToClientWindow(to, "De " + from + " : " + message);
            }
        }
    }

    public Map<String, String> getRoutingTable() {
        return routingTable;
    }

    public int getPortListenCl() {
        return serveur.getPort();
    }

    public void setAdminUI(AdminUI adminUI) {
        this.adminUI = adminUI;
    }
}


