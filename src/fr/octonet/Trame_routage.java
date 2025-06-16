// Classe représentant une trame de routage entre serveurs
// Utilisée pour échanger les tables de routage entre les serveurs
// Permet de maintenir à jour la connaissance du réseau (serveurs, clients, distances)
package fr.octonet;

import java.net.Inet4Address;
import java.util.ArrayList;

public class Trame_routage extends Trame{
	
	private ArrayList<String> serveurs;
	private ArrayList<Inet4Address> passerelles;
	private ArrayList<ArrayList<String>> clients_serveurs;	
	private ArrayList<Integer> distance;
	
	Trame_routage(int type_message, String serveur_cible,
			String serveur_source, ArrayList<String> serveurs, ArrayList<Inet4Address> passerelles, ArrayList<ArrayList<String>> clients_serveurs, ArrayList<Integer> distance) {
		
		super(type_message, serveur_cible, serveur_source);
		this.setServeurs(serveurs);
		this.setPasserelles(passerelles);
		this.setClients_serveurs(clients_serveurs);
		this.setDistance(distance);
	}

	public ArrayList<String> getServeurs() {
		return serveurs;
	}

	public void setServeurs(ArrayList<String> serveurs) {
		this.serveurs = serveurs;
	}

	public ArrayList<Inet4Address> getPasserelles() {
		return passerelles;
	}

	public void setPasserelles(ArrayList<Inet4Address> passerelles) {
		this.passerelles = passerelles;
	}

	public ArrayList<ArrayList<String>> getClients_serveurs() {
		return clients_serveurs;
	}

	public void setClients_serveurs(ArrayList<ArrayList<String>> clients_serveurs) {
		this.clients_serveurs = clients_serveurs;
	}

	public void setDistance(ArrayList<Integer> distance) {
		this.distance = distance;
	}
	
	public ArrayList<Integer> getDistance(){
		return distance;
	}
}

	
