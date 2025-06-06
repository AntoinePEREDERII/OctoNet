package fr.octonet;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Serveur {
    private int portListenCl;
    private int portListenSrv;
    private Map<String, String> tableDeRoutage;

    public Serveur(int portListenCl, int portListenSrv) {
        this.portListenCl = portListenCl;
        this.portListenSrv = portListenSrv;
        this.tableDeRoutage = new HashMap<>();
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
}
