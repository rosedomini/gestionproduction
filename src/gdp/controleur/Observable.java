package gdp.controleur;

public interface Observable {
    public void ajouterObservateur(Observateur observateur);
    public void supprimerObservateur(Observateur observateur);
    public void notifierObservateurs(String notification);
}

/*
	Pour les classes qui implémentent l'interface
	
    public final Set<Observateur> observateurs = new HashSet<Observateur>();
	
	@Override
	public void ajouterObservateur(Observateur observateur) {
		observateurs.add(observateur);
	}
	
	@Override
	public void supprimerObservateur(Observateur observateur) {
		observateurs.remove(observateur);
	}
	
	@Override
	public void notifierObservateurs(String notification) {
		for(Observateur observateur: observateurs){
			observateur.onNotification(this, notification);
		}
	}
	
	Ou
	
    public Set<Observateur> observateurs = new HashSet<Observateur>();

	@Override
	public void ajouterObservateur(Observateur observateur) {
		if(observateurs == null){
			observateurs = new HashSet<Observateur>();
		}
		observateurs.add(observateur);
	}
	
	@Override
	public void supprimerObservateur(Observateur observateur) {
		if(observateurs != null){
			observateurs.remove(observateur);
		}
	}
	
	@Override
	public void notifierObservateurs(String notification) {
		if(observateurs != null){
			for(Observateur observateur: observateurs){
				observateur.onNotification(this, notification);
			}
		}
	}
*/