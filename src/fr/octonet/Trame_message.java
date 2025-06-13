// Classe représentant une trame de message entre clients
// Utilisée pour transporter les messages entre les clients à travers le réseau
// Contient les informations sur l'expéditeur, le destinataire et le contenu du message
package fr.octonet;

public class Trame_message extends Trame{
	
	private String client_cible;
	private String client_source;
	private String du;
	
	Trame_message(int type_message, String serveur_cible, String serveur_source, String client_cible, String client_source, String du) {
		super(type_message, serveur_cible, serveur_source);
		this.setClient_cible(client_cible);
		this.setClient_source(client_source);
		this.setDu(du);
	}

	public String getClient_cible() {
		return client_cible;
	}

	public void setClient_cible(String client_cible) {
		this.client_cible = client_cible;
	}

	public String getClient_source() {
		return client_source;
	}

	public void setClient_source(String client_source) {
		this.client_source = client_source;
	}

	public String getDu() {
		return du;
	}

	public void setDu(String du) {
		this.du = du;
	}
	
	
}