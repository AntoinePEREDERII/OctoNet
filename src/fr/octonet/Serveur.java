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

    private void handleServer(Socket socket) {
        try {
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            while (true) {
                Trame trame = (Trame) in.readObject();
                handleTrame(trame, socket);
            }
        } catch (EOFException e) {
            System.out.println("Connexion au serveur distant fermée");
        } catch (Exception e) {
            System.err.println("Erreur lors de la communication avec le serveur distant: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }

    public void connectToRemoteServer(String serverAddress) {
        try {
            String[] parts = serverAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
                Trame trame = new Trame();
                trame.setType("ROUTING");
                trame.setData(serializeRoutingTable(admin.getRoutingTable()));
                out.writeObject(trame);
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion au serveur distant: " + e.getMessage());
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
            System.err.println("Erreur lors de l'envoi de la trame au serveur: " + e.getMessage());
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
        String routingData = trame.getData();
        admin.updateRoutingTable(routingData);
        System.out.println("Table de routage mise à jour");
    }

    private String serializeRoutingTable(Map<String, String> table) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : table.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return sb.toString();
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
