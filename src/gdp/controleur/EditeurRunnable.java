package gdp.controleur;

import javax.swing.JTextField;

/**
 * La méthode run peut ici utiliser les champs valeur et editeur.
 * Cette méthode permettra de modifier un attribut d'un objet d'après la valeur entrée
 * par l'utilisateur dans un éditeur.
 * @author dom
 *
 */
public abstract class EditeurRunnable implements Runnable {
	public String valeur;
	public JTextField editeur;
	
	public abstract void run() throws Error;
}
