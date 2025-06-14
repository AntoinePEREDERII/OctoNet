// Classe abstraite de base pour toutes les trames du réseau
// Une trame est l'unité de base de communication entre les serveurs
// Elle contient les informations essentielles comme le type de message et les adresses source/destination
package fr.octonet;

import java.io.Serializable;

public abstract class Trame implements Serializable {
	
	private static final long serialVersionUID = -484492464833561910L; //Nécessaire de tous avoir le même UID pour bien passer de notre objet aux bits envoyés et inversement.
	private int type_message; 
	private String serveur_cible;
	private String serveur_source;
	
	Trame(int type_message, String serveur_cible, String serveur_source){
		this.type_message = type_message;
		this.serveur_cible = serveur_cible;
		this.serveur_source = serveur_source;
	}
	
	public int getType_message() {
		return type_message;
	}
	public void setType_message(int type_message) {
		this.type_message = type_message;
	}
	public String getServeur_cible() {
		return serveur_cible;
	}
	public void setServeur_cible(String serveur_cible) {
		this.serveur_cible = serveur_cible;
	}
	public String getServeur_source() {
		return serveur_source;
	}
	public void setServeur_source(String serveur_source) {
		this.serveur_source = serveur_source;
	}
	
}