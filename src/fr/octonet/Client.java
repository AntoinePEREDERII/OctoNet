// Classe représentant un client dans le réseau
// Un client peut envoyer et recevoir des messages
// Il est géré par un serveur local et peut communiquer avec d'autres clients
package fr.octonet;

import java.io.*;
import java.net.*;
import java.util.*;

import common.Trame;
import common.Trame_message;

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
        // Décompresser le message et vérifier la parité
        String decompressedMessage = decompressLZ78(message);
        boolean isCorrupted = checkParity(message);
        
        if (isCorrupted) {
            System.out.println("⚠️ ATTENTION: Les données reçues sont potentiellement corrompues!");
        }
        
        System.out.println("Message reçu de " + from + ": " + decompressedMessage);
    }

    // Envoie un message à un client spécifique
    public void sendMessage(String destClient, String message) {
        // Compresser le message avec LZ78
        String compressedMessage = compressLZ78(message);
        // Ajouter le bit de parité
        boolean parityBit = calculateParity(compressedMessage);
        String messageWithParity = compressedMessage + (parityBit ? "1" : "0");
        
        // type_message=1 pour message, serveur_cible et serveur_source peuvent être null ou this.name si besoin
        Trame_message trame = new Trame_message(1, null, null, destClient, this.name, messageWithParity);
        sendTrame(trame);
    }

    // Envoie une trame au serveur
    public void sendTrame(Trame trame) {
        try {
            // Ajouter un délai avant la tentative de connexion
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

    // Implémentation de la compression LZ78
    private String compressLZ78(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        List<String> dictionary = new ArrayList<>();
        dictionary.add(""); // Index 0 est une chaîne vide
        StringBuilder compressed = new StringBuilder();
        String current = "";
        
        for (int i = 0; i < input.length(); i++) {
            current += input.charAt(i);
            int index = dictionary.indexOf(current);
            
            if (index == -1) {
                // Ajouter au dictionnaire
                String prefix = current.substring(0, current.length() - 1);
                int prefixIndex = dictionary.indexOf(prefix);
                char nextChar = current.charAt(current.length() - 1);
                
                dictionary.add(current);
                compressed.append(prefixIndex).append(nextChar);
                current = "";
            }
        }
        
        // Gérer le dernier caractère si nécessaire
        if (!current.isEmpty()) {
            int index = dictionary.indexOf(current);
            compressed.append(index);
        }
        
        return compressed.toString();
    }

    // Implémentation de la décompression LZ78
    private String decompressLZ78(String compressed) {
        if (compressed == null || compressed.isEmpty()) {
            return "";
        }

        List<String> dictionary = new ArrayList<>();
        dictionary.add(""); // Index 0 est une chaîne vide
        
        StringBuilder decompressed = new StringBuilder();
        int i = 0;
        
        while (i < compressed.length()) {
            // Lire l'index
            StringBuilder indexStr = new StringBuilder();
            while (i < compressed.length() && Character.isDigit(compressed.charAt(i))) {
                indexStr.append(compressed.charAt(i));
                i++;
            }
            
            int index = Integer.parseInt(indexStr.toString());
            String entry = dictionary.get(index);
            
            // Lire le caractère suivant s'il existe
            if (i < compressed.length()) {
                char nextChar = compressed.charAt(i);
                entry += nextChar;
                i++;
            }
            
            dictionary.add(entry);
            decompressed.append(entry);
        }
        
        return decompressed.toString();
    }

    // Calcule le bit de parité pour une chaîne
    private boolean calculateParity(String data) {
        int ones = 0;
        for (char c : data.toCharArray()) {
            ones += Integer.bitCount(c);
        }
        return ones % 2 == 1;
    }

    // Vérifie si les données sont corrompues en utilisant le bit de parité
    private boolean checkParity(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        
        // Le dernier caractère est le bit de parité
        String message = data.substring(0, data.length() - 1);
        boolean receivedParity = data.charAt(data.length() - 1) == '1';
        boolean calculatedParity = calculateParity(message);
        
        return receivedParity != calculatedParity;
    }
}
