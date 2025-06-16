// Classe gérant le serveur de communication
// Gère les connexions avec les clients et les autres serveurs
// Traite les trames de message et de routage
// Maintient la table de routage à jour en communiquant avec les autres serveurs
// Utilise deux ports : un pour les clients (9091) et un pour les serveurs (9081)
package fr.octonet;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.SwingUtilities;

import common.Trame;
import common.Trame_message;
import common.Trame_routage;

public class Serveur {
    private final Admin admin;
    private final int portServeur;
    private final Set<Socket> remoteServerSockets = new HashSet<>();
    private ServerSocket serverSocketServeur;
    private AdminUI adminUI;

    public Serveur(Admin admin) {
        this.admin = admin;
        this.portServeur = 9081;
    }

    public void setAdminUI(AdminUI adminUI) {
        this.adminUI = adminUI;
    }

    // Démarre les serveurs pour clients et serveurs distants
    public void start() {
        // Démarrer le serveur pour les serveurs distants
        new Thread(() -> {
            try {
                serverSocketServeur = new ServerSocket(portServeur);
                System.out.println("Serveur démarré. En attente de connexion serveur sur le port " + portServeur);
                while (true) {
                    Socket serverSocket = serverSocketServeur.accept();
                    remoteServerSockets.add(serverSocket);
                    System.out.println("Nouveau serveur distant connecté: " + serverSocket.getRemoteSocketAddress());
                    if (adminUI != null) adminUI.addLog("Nouveau serveur distant connecté: " + serverSocket.getRemoteSocketAddress());
                    new Thread(() -> handleServer(serverSocket)).start();
                }
            } catch (IOException e) {
                if (e.getMessage().contains("Address already in use")) {
                    System.err.println("Le port " + portServeur + " est déjà utilisé. Veuillez attendre quelques secondes ou redémarrer l'application.");
                } else {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Gère la communication avec un serveur distant
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
                remoteServerSockets.remove(socket);
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }

    // Traite les différents types de trames reçues
    private void handleTrame(Trame trame, Socket socket) {
        if (trame instanceof Trame_message) {
            handleClientTrame((Trame_message) trame);
        } else if (trame instanceof Trame_routage) {
            handleRoutingTrame((Trame_routage) trame);
        } else {
            System.err.println("Type de trame inconnu: " + trame.getClass().getSimpleName());
        }
    }

    // Traite une trame de message entre clients
    private void handleClientTrame(Trame_message trame) {
        String clientName = trame.getClient_cible();
        String message = trame.getDu();
        String from = trame.getClient_source();
        String nextHop = admin.getRoutingTable().get(clientName);
        
        if (nextHop == null) {
            if (admin.getAdminUI() != null) admin.getAdminUI().addLog("Destination inconnue dans la table de routage pour " + clientName);
            return;
        }

        // Si le client est local
        if (nextHop.equals(admin.getLocalIP() + ":" + getPort())) {
            admin.sendMessage(from, clientName, message);
            if (admin.getAdminUI() != null) admin.getAdminUI().addLog("Message délivré localement à " + clientName);
        } else {
            // Pour un client distant, envoyer via le serveur distant
            Trame_message forwardTrame = new Trame_message(
                1,
                nextHop,
                admin.getLocalIP() + ":" + getPort(),
                clientName,
                from,
                message
            );
            sendTrameToServer(forwardTrame, nextHop);
            if (admin.getAdminUI() != null) admin.getAdminUI().addLog("Message routé vers " + nextHop + " pour " + clientName);
        }
    }

    // Traite une trame de routage entre serveurs
    private void handleRoutingTrame(Trame_routage trame) {
        System.out.println("Table de routage reçue");
        if (adminUI != null) adminUI.addLog("Table de routage reçue");
        
        // Récupérer l'adresse du serveur source
        String sourceServer = trame.getServeur_source();
        if (adminUI != null) adminUI.addLog("Table reçue du serveur : " + sourceServer);

        // Vérifier si le serveur source est déjà dans notre table de routage
        boolean serverAlreadyKnown = false;
        for (Map.Entry<String, String> entry : admin.getRoutingTable().entrySet()) {
            if (entry.getValue().equals(sourceServer)) {
                serverAlreadyKnown = true;
                break;
            }
        }

        // Envoyer notre table de routage seulement si le serveur source n'est pas déjà connu
        if (!serverAlreadyKnown) {
            if (admin.getAdminUI() != null) {
                admin.getAdminUI().addLog("Envoi automatique de notre table de routage à " + sourceServer);
            }
            
            // Créer une trame de routage avec uniquement nos clients locaux
            ArrayList<String> localClients = new ArrayList<>();
            for (Map.Entry<String, String> entry : admin.getRoutingTable().entrySet()) {
                if (entry.getValue().equals(admin.getLocalIP() + ":" + getPort())) {
                    localClients.add(entry.getKey());
                }
            }
            
            Trame_routage responseTrame = new Trame_routage(
                2,
                sourceServer,
                admin.getLocalIP() + ":" + getPort(),
                new ArrayList<String>(Collections.singletonList(admin.getLocalIP() + ":" + getPort())),
                new ArrayList<ArrayList<String>>(Collections.singletonList(localClients)),
                new ArrayList<Integer>(Collections.singletonList(0))
            );
            
            sendTrameToServer(responseTrame, sourceServer);
        } else {
            if (admin.getAdminUI() != null) {
                admin.getAdminUI().addLog("Serveur " + sourceServer + " déjà connu, pas d'envoi de table de routage");
            }
        }
        
        // Mettre à jour la table de routage avec les informations reçues
        ArrayList<String> serveurs = trame.getServeurs();
        ArrayList<ArrayList<String>> clients_serveurs = trame.getClients_serveurs();
        
        if (serveurs != null && clients_serveurs != null) {
            for (int i = 0; i < serveurs.size(); i++) {
                String serverAddress = serveurs.get(i);
                ArrayList<String> clients = clients_serveurs.get(i);
                
                // Ajouter chaque client avec l'adresse du serveur correspondant
                for (String client : clients) {
                    // Ne pas ajouter si le client est déjà géré localement
                    if (!admin.getRoutingTable().containsKey(client) || 
                        !admin.getRoutingTable().get(client).equals(admin.getLocalIP() + ":" + getPort())) {
                        admin.addRemoteClient(client, serverAddress);
                    }
                }
            }
        }
        
        // Mettre à jour l'affichage de la table de routage sur le thread EDT
        if (admin.getAdminUI() != null) {
            SwingUtilities.invokeLater(() -> {
                adminUI.updateRoutingTable();
            });
        }
    }

    // Envoie une trame à un serveur distant
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

    public int getPort() {
        return portServeur;
    }

    public static void main(String[] args) {
    }
}
