package common;

import java.net.Inet4Address;
import java.util.ArrayList;

public class Trame_routage extends Trame {
	
	private static final long serialVersionUID = -484492464833561911L;
	private final ArrayList<String> serveurs;
	private ArrayList<Inet4Address> passerelles;
	private final ArrayList<ArrayList<String>> clients_serveurs;	
	private final ArrayList<Integer> distances;
	
	public Trame_routage(int type, String serveur_cible, String serveur_source, 
						ArrayList<String> serveurs, ArrayList<ArrayList<String>> clients_serveurs, 
						ArrayList<Integer> distances) {
		super(type, serveur_cible, serveur_source);
		this.serveurs = serveurs;
		this.clients_serveurs = clients_serveurs;
		this.distances = distances;
	}

	public ArrayList<String> getServeurs() {
		return serveurs;
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

	public ArrayList<Integer> getDistance(){
		return distances;
	}
}

	