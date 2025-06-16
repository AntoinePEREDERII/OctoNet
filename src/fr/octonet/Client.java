// Classe représentant un client dans le réseau
// Un client peut envoyer et recevoir des messages
// Il est géré par un serveur local et peut communiquer avec d'autres clients
// Note: Certaines méthodes sont commentées car non utilisées dans cette version
package fr.octonet;

import java.io.*;
import java.net.*;

import common.Trame;
import common.Trame_message;

public class Client {
    private String serverAddress;
    private int serverPort;
    private String name;

    public Client(String name) {
        this.name = name;
    }

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
}
