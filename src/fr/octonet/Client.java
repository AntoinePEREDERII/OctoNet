// Classe représentant un client dans le réseau
// Un client peut envoyer et recevoir des messages
// Il est géré par un serveur local et peut communiquer avec d'autres clients
package fr.octonet;

import java.io.*;
import java.net.*;
import java.util.*;

import common.Trame;
import common.Trame_message;
import common.CompressionUtil;

public class Client {
    private String name;

    public Client(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Reçoit un message d'un autre client
    public void receiveMessage(String from, String message) {
        if (message == null || message.isEmpty()) {
            System.out.println("Message vide reçu de " + from);
            return;
        }

        try {
            // Extraire le message et le bit de parité
            String messageWithoutParity = message.substring(0, message.length() - 1);
            boolean receivedParity = message.charAt(message.length() - 1) == '1';
            
            // Vérifier la parité
            boolean isCorrupted = CompressionUtil.isDataCorrupted(message, receivedParity);
            if (isCorrupted) {
                System.out.println("⚠️ ATTENTION: Les données reçues sont potentiellement corrompues!");
                return;
            }

            // Décompresser le message
            System.out.println("Message reçu (avec parité): " + message);
            System.out.println("Message sans parité: " + messageWithoutParity);
            String decompressedMessage = CompressionUtil.decompressLZ78(messageWithoutParity);
            System.out.println("Message décompressé: " + decompressedMessage);
            System.out.println("Message reçu de " + from + ": " + decompressedMessage);
        } catch (Exception e) {
            System.err.println("Erreur lors du traitement du message de " + from + ": " + e.getMessage());
        }
    }

    // Envoie un message à un client spécifique
    public void sendMessage(String destClient, String message) {
        if (message == null || message.isEmpty()) {
            System.out.println("Impossible d'envoyer un message vide");
            return;
        }

        try {
            // Forcer la compression pour tous les messages
            System.out.println("\n=== Envoi de message ===");
            System.out.println("Message original: " + message);
            String compressedMessage = CompressionUtil.compressLZ78(message);
            System.out.println("Message compressé: " + compressedMessage);
            
            // Ajouter le bit de parité
            boolean parityBit = CompressionUtil.calculateParity(compressedMessage);
            String messageWithParity = compressedMessage + (parityBit ? "1" : "0");
            System.out.println("Message avec parité: " + messageWithParity);
            
            // type_message=1 pour message, serveur_cible et serveur_source peuvent être null ou this.name si besoin
            Trame_message trame = new Trame_message(1, null, null, destClient, this.name, messageWithParity);
            sendTrame(trame);
            System.out.println("=== Message envoyé ===\n");
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du message: " + e.getMessage());
        }
    }

    // Envoie une trame au serveur
    public void sendTrame(Trame trame) {
        try {
            // Ajouter un délai avant l'envoi
            Thread.sleep(100);
            
            Socket socket = new Socket("localhost", 12345);
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
