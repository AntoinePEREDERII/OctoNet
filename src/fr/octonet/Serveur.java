package fr.octonet;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Serveur {
    private int portListenCl;
    private int portListenSrv;
    private Admin admin;
    private List<Socket> remoteServerSockets = new ArrayList<>();

    public Serveur(int portListenCl, int portListenSrv, Admin admin) {
        this.portListenCl = portListenCl;
        this.portListenSrv = portListenSrv;
        this.admin = admin;
    }

    public void listenCl() {
        try (ServerSocket serverSocket = new ServerSocket(portListenCl)) {
            System.out.println("Serveur démarré. En attente de connexion client...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenSrv() {
        try (ServerSocket serverSocket = new ServerSocket(portListenSrv)) {
            System.out.println("Serveur démarré. En attente de connexion serveur...");

            while (true) {
                Socket serverSocketConnection = serverSocket.accept();
                new Thread(() -> handleServer(serverSocketConnection)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            Trame trame = (Trame) in.readObject();
            System.out.println("Trame reçue du client: " + trame);
            // Traiter la trame et envoyer une réponse si nécessaire
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleServer(Socket serverSocket) {
        try (ObjectInputStream in = new ObjectInputStream(serverSocket.getInputStream())) {
            Trame trame = (Trame) in.readObject();
            System.out.println("Trame reçue du serveur: " + trame);
            // Traiter la trame et envoyer une réponse si nécessaire
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
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
            trameRouting.setServerIpDest(address); // adresse du serveur distant
            // Pas besoin de clientNameSrc/clientNameDest ici

            // Envoi de la trame au serveur distant
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(trameRouting);
            out.flush();

            // (Optionnel) Attendre la table de routage du serveur distant en retour
            // ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            // Trame trameRecu = (Trame) in.readObject();
            // Traiter la trame reçue si besoin

        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
