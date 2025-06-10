package fr.octonet;

import java.io.Serializable;

public class Trame implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type; // "CLIENT", "SERVER", "ROUTING_TABLE", etc.
    private String serverIpDest; // IP:port du serveur destinataire
    private String clientNameSrc; // nom du client source (si concerné)
    private String clientNameDest; // nom du client destinataire (si concerné)
    private String data; // message, ou autre

    public Trame() {
        this.type = "DEFAULT";
    }

    public Trame(String message) {
        this.type = "CLIENT";
        this.data = message;
    }

    // Getters et setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getServerIpDest() { return serverIpDest; }
    public void setServerIpDest(String serverIpDest) { this.serverIpDest = serverIpDest; }

    public String getClientNameSrc() { return clientNameSrc; }
    public void setClientNameSrc(String clientNameSrc) { this.clientNameSrc = clientNameSrc; }

    public String getClientNameDest() { return clientNameDest; }
    public void setClientNameDest(String clientNameDest) { this.clientNameDest = clientNameDest; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    @Override
    public String toString() {
        return "Trame{" +
                "type='" + type + '\'' +
                ", serverIpDest='" + serverIpDest + '\'' +
                ", clientNameSrc='" + clientNameSrc + '\'' +
                ", clientNameDest='" + clientNameDest + '\'' +
                ", data=" + data +
                '}';
    }
}
