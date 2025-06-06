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

    public Serveur(int portListenCl, int portListenSrv, Admin admin) {
        this.portListenCl = portListenCl;
        this.portListenSrv = portListenSrv;
        this.admin = admin;
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
                if (admin.adminUI != null) admin.adminUI.addLog("Trame reçue du client: " + trame);

                // Traiter la trame
                if (trame.getType().equals("CLIENT")) {
                    String sourceClient = trame.getClientNameSrc();
                    String destClient = trame.getClientNameDest();
                    String message = (String) trame.getData();

                    if (destClient != null && !destClient.equals(sourceClient)) {
                        // Envoyer uniquement si le destinataire est différent de l'expéditeur
                        admin.sendMessageToClient(destClient, message);
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
                if (admin.adminUI != null) admin.adminUI.addLog("Trame reçue du serveur: " + trame);

                if ("ROUTING_TABLE".equals(trame.getType())) {
                    // Fusionner la table reçue avec la table locale
                    Map<String, String> receivedTable = trame.getRoutingTable();
                    for (Map.Entry<String, String> entry : receivedTable.entrySet()) {
                        // Si le client n'est pas local, ajoute/maj la route
                        if (!admin.getRoutingTable().containsKey(entry.getKey())) {
                            admin.getRoutingTable().put(entry.getKey(), trame.getServerIpDest());
                        }
                    }
                } else if ("CLIENT".equals(trame.getType())) {
                    String destClient = trame.getClientNameDest();
                    String message = (String) trame.getData();
                    // Si le client est local, délivrer
                    if ("local".equals(admin.getRoutingTable().get(destClient))) {
                        admin.sendMessageToClient(destClient, message, trame.getClientNameSrc());
                        if (admin.adminUI != null) admin.adminUI.addLog("Message délivré à " + destClient + " : " + message);
                    } else {
                        // Sinon, router vers le bon serveur
                        String nextHop = admin.getRoutingTable().get(destClient);
                        if (nextHop != null && !nextHop.equals(trame.getServerIpDest())) {
                            sendTrameToServer(trame, nextHop);
                            if (admin.adminUI != null) admin.adminUI.addLog("Routage de la trame vers " + nextHop + " : " + trame);
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

        } catch (Exception e) {
            System.err.println("Erreur de connexion au serveur distant " + address + ": " + e.getMessage());
        }
    }

    public void sendTrameToServer(Trame trame, String serverAddress) {
        try {
            String[] parts = serverAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            Socket socket = new Socket(host, port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(trame);
            out.flush();
            out.close();
            socket.close();
        } catch (Exception e) {
            System.err.println("Erreur d'envoi au serveur " + serverAddress + ": " + e.getMessage());
        }
    }

    // Ajoutez ici la logique de routage et de diffusion des tables de routage

    public static void main(String[] args) {
        Admin admin = new Admin();
        // Remplir la table de routage pour l'exemple
        admin.getRoutingTable().put("clientB", "192.168.1.2:9081");

        Serveur serveur = new Serveur(8080, 9090, admin);
        new Thread(serveur::listenCl).start();
        new Thread(serveur::listenSrv).start();

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
