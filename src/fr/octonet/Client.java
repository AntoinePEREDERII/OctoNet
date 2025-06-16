// Classe représentant un client dans le réseau
// Un client peut envoyer et recevoir des messages
// Il est géré par un serveur local et peut communiquer avec d'autres clients
// Note: Certaines méthodes sont commentées car non utilisées dans cette version
package fr.octonet;

import java.io.*;
import java.net.*;
import java.util.*;

import common.Trame;
import common.Trame_message;

public class Client {
    private String serverAddress;
    private int serverPort;
    private String name;

    public Client(String name) {
        this.name = name;
    }
    //inutile
    /* 
    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.name = "Client_" + System.currentTimeMillis();
    }
*/
    public String getName() {
        return name;
    }

    // Reçoit un message d'un autre client
    public void receiveMessage(String from, String message) {
        // Cette méthode est appelée quand un message est reçu
        System.out.println("Message reçu de " + from + ": " + message);
    }

    // Envoie un message à un client spécifique
    public void sendMessage(String destClient, String message) {
        // type_message=1 pour message, serveur_cible et serveur_source peuvent être null ou this.name si besoin
        Trame_message trame = new Trame_message(1, null, null, destClient, this.name, message);
        sendTrame(trame);
    }
/* 
    public void sendMessage(String message) {
        // Pour la compatibilité avec l'ancien code
        sendMessage(null, message);
    }
*/
    // Envoie une trame au serveur
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
/* 
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
        */
}
