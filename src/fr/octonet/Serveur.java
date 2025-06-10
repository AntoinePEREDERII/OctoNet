package fr.octonet;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Serveur {
    private int portListenCl;
    private int portListenSrv;
    private Admin admin;
    private List<Socket> remoteServerSockets = new ArrayList<>();
    private List<Socket> clientSockets = new ArrayList<>();

    public Serveur(Admin admin) {
        this.admin = admin;
        this.portListenCl = 12345; // Port fixe pour les clients
        this.portListenSrv = 12346; // Port fixe pour les serveurs
    }

    public int getPort() {
        return portListenCl;
    }

    public void start() {
        new Thread(this::listenCl).start();
        new Thread(this::listenSrv).start();
    }

    public void listenCl() {
        try (ServerSocket serverSocket = new ServerSocket(portListenCl)) {
            System.out.println("Serveur démarré. En attente de connexion client sur le port " + portListenCl);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientSockets.add(clientSocket);
                System.out.println("Nouveau client connecté: " + clientSocket.getInetAddress());
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            if (e.getMessage().contains("Address already in use")) {
                System.err.println("Le port " + portListenCl + " est déjà utilisé. Veuillez attendre quelques secondes ou redémarrer l'application.");
            } else {
                e.printStackTrace();
            }
        }
    }

    public void listenSrv() {
        try (ServerSocket serverSocket = new ServerSocket(portListenSrv)) {
            System.out.println("Serveur démarré. En attente de connexion serveur sur le port " + portListenSrv);

            while (true) {
                Socket serverSocketConnection = serverSocket.accept();
                remoteServerSockets.add(serverSocketConnection);
                System.out.println("Nouveau serveur distant connecté: " + serverSocketConnection.getInetAddress());
                new Thread(() -> handleServer(serverSocketConnection)).start();
            }
        } catch (IOException e) {
            if (e.getMessage().contains("Address already in use")) {
                System.err.println("Le port " + portListenSrv + " est déjà utilisé. Veuillez attendre quelques secondes ou redémarrer l'application.");
            } else {
                e.printStackTrace();
            }
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            while (true) {
                Trame trame = (Trame) in.readObject();
                System.out.println("Trame reçue du client: " + trame);
                
                if ("CLIENT".equals(trame.getType())) {
                    String destClient = trame.getClientNameDest();
                    String message = (String) trame.getData();
                    String fromClient = trame.getClientNameSrc();
                    // Vérifie si le client destinataire est local
                    if (admin.getRoutingTable().get(destClient).equals(admin.getLocalIP() + ":" + getPort())) {
                        admin.sendMessage(fromClient, destClient, message);
                    } else {
                        // Sinon, route à nouveau (multi-sauts possible)
                        String nextHop = admin.getRoutingTable().get(destClient);
                        if (nextHop != null && !nextHop.equals(trame.getServerIpDest())) {
                            sendTrameToServer(trame, nextHop);
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur de connexion client: " + e.getMessage());
            clientSockets.remove(clientSocket);
        }
    }

    private void handleServer(Socket serverSocket) {
        try (ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream())) {
            while (true) {
                Trame trame = (Trame) in.readObject();
                System.out.println("Trame reçue du serveur: " + trame);
                
                if (trame.getType().equals("CLIENT")) {
                    String sourceClient = trame.getClientNameSrc();
                    String destClient = trame.getClientNameDest();
                    String message = (String) trame.getData();
                    admin.sendMessage(sourceClient, destClient, message);
                } else if (trame.getType().equals("ROUTING_TABLE")) {
                    // Mise à jour de la table de routage avec les clients distants
                    Map<String, String> remoteTable = trame.getRoutingTable();
                    for (Map.Entry<String, String> entry : remoteTable.entrySet()) {
                        if (!entry.getValue().equals("local")) {
                            admin.addRemoteClient(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur de connexion serveur: " + e.getMessage());
            remoteServerSockets.remove(serverSocket);
        }
    }

    public void connectToRemoteServer(String address) {
        try {
            String[] parts = address.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            Socket socket = new Socket(host, port);
            remoteServerSockets.add(socket);

            // Création d'une trame contenant la table de routage locale
            Trame trameRouting = new Trame();
            trameRouting.setType("ROUTING_TABLE");
            trameRouting.setRoutingTable(new HashMap<>(admin.getRoutingTable()));
            trameRouting.setServerIpDest(address);

            // Envoi de la trame au serveur distant
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(trameRouting);
            out.flush();

            // Démarrer un thread pour écouter les réponses du serveur distant
            new Thread(() -> handleServer(socket)).start();

        } catch (Exception e) {
            System.err.println("Erreur de connexion au serveur distant " + address + ": " + e.getMessage());
        }
    }

    public void sendTrameToServer(Trame trame, String serverAddress) {
        try {
            String[] parts = serverAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                out.writeObject(trame);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de la trame au serveur distant : " + e.getMessage());
        }
    }

    private void handleTrame(Trame trame, Socket socket) {
        switch (trame.getType()) {
            case "CLIENT":
                handleClientTrame(trame);
                break;
            case "ROUTING":
                handleRoutingTrame(trame);
                break;
            default:
                System.err.println("Type de trame inconnu: " + trame.getType());
        }
    }

    private void handleClientTrame(Trame trame) {
        String clientName = trame.getClientNameDest();
        String message = trame.getData();
        String from = trame.getClientNameSrc();
        admin.sendMessage(from, clientName, message);
    }

    private void handleRoutingTrame(Trame trame) {
        String routingData = trame.getData().toString();
        admin.updateRoutingTable(routingData);
        System.out.println("Table de routage mise à jour");
    }

    public static void main(String[] args) {
        Admin admin = new Admin();
        // Remplir la table de routage pour l'exemple
        admin.getRoutingTable().put("clientB", "192.168.1.2:9081");

        Serveur serveur = new Serveur(admin);
        serveur.start();

        // Attendre un moment que les serveurs écoutent
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Tester la connexion à un serveur distant
        serveur.connectToRemoteServer("192.168.1.2:9081");

        // Création et envoi d'une trame client
        Trame trameClient = new Trame();
        trameClient.setType("CLIENT");
        trameClient.setClientNameSrc("clientA");
        trameClient.setClientNameDest("clientB");
        trameClient.setServerIpDest("192.168.1.2:9081"); // serveur cible
        trameClient.setData("Coucou !");
    }
}
