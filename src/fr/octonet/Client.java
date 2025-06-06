package fr.octonet;

import java.io.*;
import java.net.Socket;

public class Client {
    private String serverAddress;
    private int serverPort;

    public Client(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void sendTrame(Trame trame) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.writeObject(trame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Trame receiveTrame() {
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            return (Trame) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
