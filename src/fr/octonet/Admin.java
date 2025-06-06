package fr.octonet;

import java.util.*;

public class Admin {
    private int nbClient = 0;
    private int portListenCl = 9080;
    private int portListenSrv = 9081;
    private int portClient = 9082;

    private final List<Client> clients = new ArrayList<>();
    private final List<String> remoteServers = new ArrayList<>();
    private final Map<String, String> routingTable = new HashMap<>(); // clé: nom, valeur: next hop

    private Serveur serveur;

    public Client newClient() {
        int newPort = portClient + nbClient;
        Client client = new Client("localhost", newPort);
        clients.add(client);
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
        return clients;
    }

    public void sendMessageToClient(String clientName, String message) {
        for (Client c : clients) {
            if (c.getName().equals(clientName)) {
                c.sendMessage(message);
                break;
            }
        }
    }

    public void startSrv() {
        serveur = new Serveur(portListenCl, portListenSrv, this);
        new Thread(serveur::listenCl).start();
        new Thread(serveur::listenSrv).start();
    }

    public Map<String, String> getRoutingTable() {
        return routingTable;
    }

    public void sendMessageFromClientToClient(String clientSrc, String clientDest, String message) {
        Trame trame = new Trame();
        trame.setType("CLIENT");
        trame.setClientNameSrc(clientSrc);
        trame.setClientNameDest(clientDest);
        trame.setData(message);

        String nextHop = routingTable.get(clientDest);
        if (nextHop == null) {
            System.out.println("Destination inconnue dans la table de routage.");
            return;
        }
        if (nextHop.equals("local")) {
            sendMessageToClient(clientDest, message);
        } else {
            // Envoi via le serveur distant
            if (serveur != null) {
                trame.setServerIpDest(nextHop);
                serveur.sendTrameToServer(trame, nextHop);
            }
        }
    }
}
