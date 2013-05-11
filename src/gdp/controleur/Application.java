package gdp.controleur;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;


import gdp.modele.Article;
import gdp.modele.Commande;
import gdp.modele.Contact;
import gdp.modele.Magasin;
import gdp.modele.PosteCharge;
import gdp.modele.PosteCharge.Machine;
import gdp.modele.PosteCharge.Operateur;
import gdp.modele.crud.BDD;
import gdp.vue.FenetrePrincipale;

/**
 * Classe exécutable de cet outil de gestion de production
 * @author Dominic Rose
 */
public class Application {
	public static final String nom = "Gestionnaire de production";
	public static final String repertoireDonnees = "data";
    public static final FenetrePrincipale fenetre =
    		new FenetrePrincipale(nom);
    public static final BDD<Article> articles = new BDD<Article>(Article.class);
    public static final BDD<Magasin> magasins = new BDD<Magasin>(Magasin.class);
    public static final BDD<Commande> commandes = new BDD<Commande>(Commande.class);
    public static final BDD<PosteCharge> posteCharges = new BDD<PosteCharge>(PosteCharge.class);
    public static final BDD<Machine> machines = new BDD<Machine>(Machine.class);
    public static final BDD<Operateur> operateurs = new BDD<Operateur>(Operateur.class);
    public static final BDD<Contact> contacts = new BDD<Contact>(Contact.class);

    public static void main(String[] args){
        BDD.ajouterBDD(articles);
        BDD.ajouterBDD(magasins);
        BDD.ajouterBDD(commandes);
        BDD.ajouterBDD(posteCharges);
        BDD.ajouterBDD(machines);
        BDD.ajouterBDD(operateurs);
        BDD.ajouterBDD(contacts);
        chargerDonnees();
        fenetre.afficherAccueil();
    }

    public static void setSousTitre(String titre) {
        fenetre.setTitle(titre+" - "+nom);
    }
    
    /**
     * Propose de quitter l'application avec ou sans enregistrer les données
     */
    public static void quitter(){
    	// s'il y a des modifications à enregistrer
    	if(BDD.isMiseAJour()){
        	Object[] options = {"Oui", "Non", "Annuler"};
    		int n = JOptionPane.showOptionDialog(
    				fenetre,
    				"Enregistrer les modifications avant de quitter ?",
    				"Quitter",
    				JOptionPane.YES_NO_CANCEL_OPTION,
    				JOptionPane.QUESTION_MESSAGE,
    				null,
    				options,
    				options[2]
    		);
    		if(n == 2 || n == JOptionPane.CLOSED_OPTION) return; // Annulé : on ne fait rien
    		if(n == 0) enregistrerDonnees(); // Oui : on enregistre les données
    	}
    	
    	// fermeture normale de l'application
	    System.exit(0);
    }

    public static void chargerDonnees(){
    	// création du répertoire de données si non existant pour éviter les erreurs
    	File data = new File(repertoireDonnees);
    	data.mkdir();
    	
    	// fichier caché (avec Linux) permettant de détecter la présence 
    	// d'une autre instance du programme (marche avec windows)
    	// permettant d'éviter de manipuler la base de données avec deux fenêtres
    	File lock = new File(repertoireDonnees+"/.lock");
    	
    	// si le fichier de verrou existe mais ne peut pas être supprimé,
    	// c'est qu'il est utilisé (marche avec Windows)
    	if(lock.exists() && !lock.delete()){
            JOptionPane.showMessageDialog(fenetre,
                    "Vérifier que l'application n'est pas déjà ouverte.",
                    "Base de données verrouillée",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(1);
    	}
    	try {
    		lock.createNewFile();
		} catch (IOException e) {
            JOptionPane.showMessageDialog(fenetre,
                    e.getMessage());
            e.printStackTrace();
            System.exit(1);
		}
		try {
			// simule l'utilisation du fichier de verrou
			// c'est supposé rendre impossible sa suppression
			new FileReader(lock);
		} catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(fenetre,
                    e.getMessage());
            e.printStackTrace();
		}
		
    	BDD.charger(repertoireDonnees);
    }
    
    public static void enregistrerDonnees(){
    	BDD.sauvegarder(fenetre, repertoireDonnees);
    }
}
