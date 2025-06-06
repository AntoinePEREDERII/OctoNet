package fr.octonet;

public class Main {
    public static void main(String[] args) {
        Admin admin = new Admin();
        admin.startSrv();
        admin.newClient();
    }
}
