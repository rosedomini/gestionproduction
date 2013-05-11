package gdp.modele.crud;

import gdp.controleur.Editable;
import gdp.controleur.Observateur;
import gdp.modele.StatutCRUD;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;


/**
 * Cread Read Update Delete
 * Permet la gestion des objets du modèle à créer, modifier via un éditeur, sauvegarder etc.
 * @author dom
 */
public abstract class CRUD implements Externalizable, Editable {
    public static final long serialVersionUID = 1L;
	
    public transient final Set<Observateur> observateurs = new HashSet<Observateur>();
    
    // identifiant propre aux instances de getTopClass(this.class)
    protected int idCRUD;
    
    // statut qui permet de savoir si l'objet en RAM est à jour dans le disque dur ou non
    // , s'il est à supprimer ou non, etc.
    protected StatutCRUD statutCRUD;
    
    /**
     * ex: getTopClass(ArticleFabrique.class).equals(Article.class)
     * ex: getTopClass(Article.class).equals(Article.class)
     * Ceci est utile notamment parce qu'un article a un identifiant unique qu'il
     * qu'il soit ArticleFabrique ou non
     * On cherche une classe qui hérite directement de CRUD
     * @param classe classe qui hérite de CRUD
     * @return
     */
	@SuppressWarnings("unchecked")
	public static Class<? extends CRUD> getTopClass(Class<? extends CRUD> classe){
    	while(classe.getSuperclass() != CRUD.class){
    		classe = (Class<? extends CRUD>) classe.getSuperclass();
    	}
        return (Class<? extends CRUD>)classe;
    }

    public int getIdCRUD() {
        return idCRUD;
    }
    
	public Class<? extends CRUD> getTopClass(){
    	return CRUD.getTopClass(this.getClass());
    }

    public void setIdCRUD(int idCRUD) {
        this.idCRUD = idCRUD;
    }

    public StatutCRUD getStatutCRUD() {
        return statutCRUD;
    }

    public void setStatutCRUD(StatutCRUD statutCRUD) {
    	this.statutCRUD = statutCRUD;
    }

    /**
     * demande un nouvel identifiant unique à la BDD
     * @return
     */
    public int genererId() {
        idCRUD = BDD.getBDD(getTopClass()).genererId();
        return 0;
    }

    /**
     * Calcul des données à calculer
     * 
     * la vague 1 passe sur tous les objets après la désérialisation de tous les objets
     * la vague n+1 passe après la vague n
     * @param vague
     */
    public abstract void operationsPostChargement(int vague);
    
    public void apresCreation(){}

    /**
     * un début de sérialisation pour les objets CRUD
     * à réimplémenter dans les sous-classes
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(idCRUD);
        out.writeObject(statutCRUD);
    }

    /**
     * un début de désérialisation pour les objets CRUD
     * à réimplémenter dans les sous-classes
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        idCRUD = in.readInt();
        statutCRUD = (StatutCRUD) in.readObject();
    }

	@Override
	public String toString() {
		return String.valueOf(idCRUD);
	}

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
		// évite java.util.ConcurrentModificationException
		Observateur[] obs = {};
		obs = observateurs.toArray(obs);
    	for(int i=0; i<obs.length; i++){
    		obs[i].onNotification(this, notification);
    	}
	}
}