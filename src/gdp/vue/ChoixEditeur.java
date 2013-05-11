package gdp.vue;

import gdp.controleur.Application;
import gdp.controleur.Editable;
import gdp.controleur.Observable;
import gdp.controleur.Observateur;
import gdp.modele.crud.BDD;
import gdp.modele.crud.CRUD;
import gdp.vue.crud.Editeur;
import gdp.vue.crud.ListeCRUD;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;


/**
 * JPanel contenant une liste d'objets à gauche et un éditeur à droite
 * @author dom
 *
 */
public class ChoixEditeur extends JPanel implements Observateur{
	private static final long serialVersionUID = 1L;
    private ListeCRUD pListe;
    private JPanel pEditeur = new JPanel(new FlowLayout());
    private Window fenetre;
    private Editeur editeur;

    public void actualiserListe(){
    	pListe.actualiser();
    }

    public void actualiserListe(CRUD objet){
        pListe.actualiser(objet);
    }
    
    public ChoixEditeur(Window fenetre, Class<? extends CRUD> classe){
    	super(new BorderLayout());
    	//this.classe = classe;
    	this.fenetre = fenetre;
    	pListe = new ListeCRUD(fenetre, classe, true);
    	pListe.ajouterObservateur(this);
        JScrollPane spEditeur = new JScrollPane(pEditeur);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pListe, spEditeur);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(230);
        Dimension minimumSize = new Dimension(100, 50);
        pListe.setMinimumSize(minimumSize);
        spEditeur.setMinimumSize(minimumSize);
        add(splitPane, BorderLayout.CENTER);
    }
    
    public void cacherEditeur(){
        pEditeur.removeAll();
    }
    
    public void afficherEditeur(Editable objet) {
        editeur = objet.editeur(Application.fenetre);
        editeur.generer();
        pEditeur.removeAll();
        pEditeur.add(editeur);
    	fenetre.setMinimumSize(getSize());
    	fenetre.invalidate();
    	fenetre.pack();
    	fenetre.repaint();
    }

	public Editeur getEditeur() {
		return editeur;
	}

	@Override
	public void onNotification(Observable notifier, String notification) {
		if(notification.equals("valueChanged")){
			CRUD o = pListe.getSelectedObject();
			afficherEditeur(BDD.getBDD(o.getTopClass()).objets.get(o.getIdCRUD()));
		}
	}
}
