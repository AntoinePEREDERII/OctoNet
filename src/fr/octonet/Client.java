package fr.octonet;

import java.io.*;
import java.net.Socket;

public class Client {
    private String serverAddress;
    private int serverPort;
    private String name;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.name = "Client_" + System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public void sendMessage(String destClient, String message) {
        Trame trame = new Trame(message);
        trame.setClientNameSrc(this.name);
        trame.setClientNameDest(destClient);
        sendTrame(trame);
    }

    public void sendMessage(String message) {
        // Pour la compatibilité avec l'ancien code
        sendMessage(null, message);
    }

    public void sendTrame(Trame trame) {
        try {
            // Ajouter un délai avant la tentative de connexion
            Thread.sleep(100);
            
            Socket socket = new Socket(serverAddress, serverPort);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(trame);
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Erreur de connexion pour le client " + name + ": " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Trame receiveTrame() {
        try {
            // Ajouter un délai avant la tentative de connexion
            Thread.sleep(100);
            
            Socket socket = new Socket(serverAddress, serverPort);
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            Trame trame = (Trame) in.readObject();
            in.close();
            socket.close();
            return trame;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur de réception pour le client " + name + ": " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
