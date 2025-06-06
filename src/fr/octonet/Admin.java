package fr.octonet;

public class Admin {
    private int nbClient = 0;
    private int portListenCl = 9080;
    private int portListenSrv = 9081;
    private int portClient = 9082;

    public void newClient() {
        int newPort = portClient + nbClient;
        Client client = new Client("localhost", newPort);
        // Stocker le client dans une liste ou une structure de données appropriée
        nbClient++;
    }

    public void startSrv() {
        Serveur serveur = new Serveur(portListenCl, portListenSrv);
        new Thread(serveur::listenCl).start();
        new Thread(serveur::listenSrv).start();
    }
}
