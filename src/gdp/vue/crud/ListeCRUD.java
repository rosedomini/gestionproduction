package gdp.vue.crud;


import gdp.controleur.Observable;
import gdp.controleur.Observateur;
import gdp.modele.crud.BDD;
import gdp.modele.crud.CRUD;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class ListeCRUD extends JPanel
		implements ListSelectionListener, ActionListener, KeyListener, Observable, Observateur {
	private static final long serialVersionUID = 1L;
	
    public transient final Set<Observateur> observateurs = new HashSet<Observateur>();
    
    private DefaultListModel dlm = new DefaultListModel();
    private JList liste = new JList(dlm);
    private ArrayList<Integer> objets = new ArrayList<Integer>();
    private JTextField champRecherche = new JTextField();
    private Window fenetre;
    private Class<? extends CRUD> classe;
    private CRUD selectedObject;

	public ListeCRUD(Window fenetre, Class<? extends CRUD> classe, boolean boutonCreer){
		super(new BorderLayout());
		this.classe = classe;
		this.fenetre = fenetre;
        liste.addListSelectionListener(this);
        champRecherche.addKeyListener(this);
        add(champRecherche, BorderLayout.NORTH);
        add(new JScrollPane(liste), BorderLayout.CENTER);
        if(boutonCreer){
	        JButton bCreation = new JButton("Créer");
	        bCreation.addActionListener(this);
        	add(bCreation, BorderLayout.SOUTH);
        }
	}

    public void actualiser(CRUD objet){
        dlm.setElementAt(objet, objets.indexOf(objet.getIdCRUD()));
    }
	
	public void actualiser(){
    	String val = champRecherche.getText().toLowerCase();
    	
    	dlm.clear();
    	objets.clear();
    	for(CRUD objet: BDD.getBDD(classe)){
    		if(objet.toString().toLowerCase().contains(val)){
        		dlm.addElement(objet);
        		objets.add(objet.getIdCRUD());
        		objet.ajouterObservateur(this);
    		} else {
    			objet.supprimerObservateur(this);
    		}
    	}
    	fenetre.setMinimumSize(getSize());
    	fenetre.invalidate();
    	fenetre.pack();
    	fenetre.repaint();
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		CRUD nouvelObjet = BDD.getBDD(classe).creerObjet();
		nouvelObjet.ajouterObservateur(this);
        objets.add(nouvelObjet.getIdCRUD());
        dlm.addElement(nouvelObjet);
        liste.setSelectedIndex(liste.getLastVisibleIndex());
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
    	int index = liste.getSelectedIndex();
    	if(index != -1){
    		selectedObject = BDD.getBDD(classe).objets.get(objets.get(index));
    		notifierObservateurs("valueChanged");
    	}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		actualiser();
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void onNotification(Observable notifier, String notification) {
		if(notification.equals("gammeOperatoire")){
    		notifierObservateurs("valueChanged");
		} else if(classe.isInstance(notifier)){
			CRUD o = (CRUD) notifier;
			if(notification.equals("toString")){
				actualiser(o);
			} else if(notification.equals("deleted")){
				dlm.removeElement(o);
				objets.remove(Integer.valueOf(o.getIdCRUD()));
				o.supprimerObservateur(this);
			} else if(notification.equals("created")){
				actualiser();
				liste.setSelectedIndex(objets.indexOf(o.getIdCRUD()));
			}
		}
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
		for(Observateur observateur: observateurs){
			observateur.onNotification(this, notification);
		}
	}

	public CRUD getSelectedObject() {
		return selectedObject;
	}
}
