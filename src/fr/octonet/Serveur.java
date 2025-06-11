package fr.octonet;

import java.io.*;
import java.net.*;
import java.util.*;
import fr.octonet.Trame;
import fr.octonet.Trame_message;
import fr.octonet.Trame_routage;

public class Serveur {
    private final Admin admin;
    private final int portClient;
    private final int portServeur;
    private final Set<Socket> clientSockets = new HashSet<>();
    private final Set<Socket> remoteServerSockets = new HashSet<>();
    private ServerSocket serverSocketClient;
    private ServerSocket serverSocketServeur;
    private AdminUI adminUI; // Référence à l'interface admin

    public Serveur(Admin admin) {
        this.admin = admin;
        this.portClient = 9091;
        this.portServeur = 9090;
    }

    public void setAdminUI(AdminUI adminUI) {
        this.adminUI = adminUI;
    }

    public void start() {
        // Démarrer le serveur pour les clients
        new Thread(() -> {
            try {
                serverSocketClient = new ServerSocket(portClient);
                System.out.println("Serveur démarré. En attente de connexion client sur le port " + portClient);
                while (true) {
                    Socket clientSocket = serverSocketClient.accept();
                    clientSockets.add(clientSocket);
                    System.out.println("Nouveau client connecté: " + clientSocket.getInetAddress());
                    if (adminUI != null) adminUI.addLog("Nouveau client connecté: " + clientSocket.getInetAddress());
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            } catch (IOException e) {
                if (e.getMessage().contains("Address already in use")) {
                    System.err.println("Le port " + portClient + " est déjà utilisé. Veuillez attendre quelques secondes ou redémarrer l'application.");
                } else {
                    e.printStackTrace();
                }
            }
        }).start();

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

    private void handleClient(Socket clientSocket) {
        try {
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            while (true) {
                Trame trame = (Trame) in.readObject();
                handleTrame(trame, clientSocket);
            }
        } catch (EOFException e) {
            System.out.println("Client déconnecté");
        } catch (Exception e) {
            System.err.println("Erreur client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                clientSockets.remove(clientSocket);
            } catch (IOException e) {
                System.err.println("Erreur fermeture client: " + e.getMessage());
            }
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
                remoteServerSockets.remove(socket);
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
            }
        }
    }

    private void handleTrame(Trame trame, Socket socket) {
        if (trame instanceof Trame_message) {
            handleClientTrame((Trame_message) trame);
        } else if (trame instanceof Trame_routage) {
            handleRoutingTrame((Trame_routage) trame);
        } else {
            System.err.println("Type de trame inconnu: " + trame.getClass().getSimpleName());
        }
    }

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

    private void handleRoutingTrame(Trame_routage trame) {
        System.out.println("Table de routage reçue");
        if (adminUI != null) adminUI.addLog("Table de routage reçue");
        
        // Mettre à jour la table de routage avec les informations reçues
        ArrayList<String> serveurs = trame.getServeurs();
        ArrayList<ArrayList<String>> clients_serveurs = trame.getClients_serveurs();
        
        if (serveurs != null && clients_serveurs != null) {
            for (int i = 0; i < serveurs.size(); i++) {
                String serverAddress = serveurs.get(i);
                ArrayList<String> clients = clients_serveurs.get(i);
                
                // Ajouter chaque client avec l'adresse du serveur correspondant
                for (String client : clients) {
                    admin.addRemoteClient(client, serverAddress);
                }
            }
        }
        
        // Ne pas envoyer de réponse automatiquement pour éviter les boucles infinies
        // La réponse sera envoyée uniquement via le bouton dédié
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

    public int getPort() {
        return portServeur;
    }

    public void connectToRemoteServer(String serverAddress) {
        try {
            String[] parts = serverAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);
            try (Socket socket = new Socket(host, port);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la connexion au serveur distant: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
    }
}
