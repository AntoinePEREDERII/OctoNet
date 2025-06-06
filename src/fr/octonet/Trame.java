package fr.octonet;

import java.util.Arrays;

public class Trame {
    private String adresseDest;
    private String adresseSource;
    private byte synAckDataError;
    private byte[] dataUser;
    private byte bitDeParite;

    public Trame(String adresseDest, String adresseSource, byte synAckDataError, byte[] dataUser, byte bitDeParite) {
        this.adresseDest = adresseDest;
        this.adresseSource = adresseSource;
        this.synAckDataError = synAckDataError;
        this.dataUser = dataUser;
        this.bitDeParite = bitDeParite;
    }

    // Getters et Setters
    public String getAdresseDest() { return adresseDest; }
    public void setAdresseDest(String adresseDest) { this.adresseDest = adresseDest; }
    public String getAdresseSource() { return adresseSource; }
    public void setAdresseSource(String adresseSource) { this.adresseSource = adresseSource; }
    public byte getSynAckDataError() { return synAckDataError; }
    public void setSynAckDataError(byte synAckDataError) { this.synAckDataError = synAckDataError; }
    public byte[] getDataUser() { return dataUser; }
    public void setDataUser(byte[] dataUser) { this.dataUser = dataUser; }
    public byte getBitDeParite() { return bitDeParite; }
    public void setBitDeParite(byte bitDeParite) { this.bitDeParite = bitDeParite; }

    @Override
    public String toString() {
        return "Trame{" +
                "adresseDest='" + adresseDest + '\'' +
                ", adresseSource='" + adresseSource + '\'' +
                ", synAckDataError=" + synAckDataError +
                ", dataUser=" + Arrays.toString(dataUser) +
                ", bitDeParite=" + bitDeParite +
                '}';
    }
}
