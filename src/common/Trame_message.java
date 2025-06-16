package common;

public class Trame_message extends Trame{
	
	private static final long serialVersionUID = -484492464833561910L;
	private final String client_cible;
	private final String client_source;
	private final String du;
	
	public Trame_message(int type_message, String serveur_cible, String serveur_source, String client_cible, String client_source, String du) {
		super(type_message, serveur_cible, serveur_source);
		this.client_cible = client_cible;
		this.client_source = client_source;
		this.du = du;
	}

	public String getClient_cible() {
		return client_cible;
	}

	public String getClient_source() {
		return client_source;
	}

	public String getDu() {
		return du;
	}
	
	
}