package fr.octonet;

import java.net.Socket;
import java.util.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.*;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.ConnectException;

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
        // Ne pas ajouter les clients distants à la liste visible
        // if (adminUI != null) {
        //     adminUI.addClientToList(clientName);
        // }
    }

    public boolean addRemoteServer(String serverAddress) {
        if (!remoteServers.contains(serverAddress)) {
            try {
                String[] parts = serverAddress.split(":");
                if (parts.length != 2) {
                    System.err.println("Format d'adresse invalide. Utilisez le format ip:port");
                    return false;
                }
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                
                // Créer un socket avec timeout
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 5000); // 5 secondes de timeout
                
                try {
                    // Créer une trame de routage avec nos informations
                    Trame_routage trame = new Trame_routage(
                        2,
                        serverAddress,
                        localIP + ":" + serveur.getPort(),
                        new ArrayList<>(Collections.singletonList(localIP + ":" + serveur.getPort())),
                        new ArrayList<>(),
                        new ArrayList<>(Collections.singletonList(new ArrayList<>(routingTable.keySet()))),
                        new ArrayList<>(Collections.singletonList(0))
                    );
                    
                    // Envoyer la trame de routage
                    serveur.sendTrameToServer(trame, serverAddress);
                    remoteServers.add(serverAddress);
                    System.out.println("Serveur distant ajouté: " + serverAddress);
                    return true;
                } finally {
                    socket.close();
                }
            } catch (NumberFormatException e) {
                System.err.println("Port invalide dans l'adresse du serveur");
                return false;
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

    public List<String> getRemoteServers() {
        return remoteServers;
    }

    public Serveur getServeur() {
        return serveur;
    }
}


