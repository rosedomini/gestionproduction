package gdp.vue.crud;


import gdp.controleur.Observable;
import gdp.controleur.Observateur;
import gdp.modele.crud.CRUD;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Permet de modifier la liste des stocks d'un article
 * @author dom
 *
 */
public class EditeurListe<E> extends JPanel implements Observable, Observateur, ActionListener, ListSelectionListener{
    public static final long serialVersionUID = 1L;
	
    public transient final Set<Observateur> observateurs = new HashSet<Observateur>();
    
    private ListeCRUD liste;
	private DefaultListModel LM = new DefaultListModel();
	private JList liste2 = new JList(LM);
	private JSplitPane sp;
	private E selection;
	private List<E> liste2O;
	private JButton boutonAjouter = new JButton("Ajouter");
	private JButton boutonRetirer = new JButton("Retirer");

    public EditeurListe(Window fenetre, Class<? extends CRUD> classe, List<E> liste){
    	super(new BorderLayout());
    	liste2O = liste;
    	for(E o: liste){
    		LM.addElement(o);
    	}
    	fenetre.setPreferredSize(new Dimension(500, 400));
    	this.liste = new ListeCRUD(fenetre, classe, false);
    	this.liste.ajouterObservateur(this);
    	JPanel pListe = new JPanel(new BorderLayout());
    	pListe.add(this.liste, BorderLayout.CENTER);
    	boutonAjouter.addActionListener(this);
    	boutonAjouter.setActionCommand("ajouter");
    	pListe.add(boutonAjouter, BorderLayout.SOUTH);
    	JPanel pListe2 = new JPanel(new BorderLayout());
    	pListe2.add(liste2, BorderLayout.CENTER);
    	boutonRetirer.addActionListener(this);
    	boutonRetirer.setActionCommand("retirer");
    	pListe2.add(boutonRetirer, BorderLayout.SOUTH);
        JScrollPane spListe = new JScrollPane(pListe);
        JScrollPane spListe2 = new JScrollPane(pListe2);
    	sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, spListe, spListe2);
        sp.setDividerLocation(230);
        Dimension minimumSize = new Dimension(150, 200);
        spListe.setMinimumSize(minimumSize);
        spListe2.setMinimumSize(minimumSize);
        sp.setOneTouchExpandable(true);
    	add(sp, BorderLayout.CENTER);
    	liste2.addListSelectionListener(this);
    	boutonAjouter.setEnabled(false);
    	boutonRetirer.setEnabled(false);
    	this.liste.actualiser();
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("ajouter")){
			notifierObservateurs("ajout");
			LM.addElement(selection);
			liste2O.add(selection);
	    	boutonAjouter.setEnabled(false);
		} else if(cmd.equals("retirer")){
			notifierObservateurs("retrait");
			Object o = LM.getElementAt(liste2.getSelectedIndex());
			liste2O.remove(o);
			LM.remove(liste2.getSelectedIndex());
	    	boutonAjouter.setEnabled(o.equals(selection));
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		int index = liste2.getSelectedIndex();
		if(index < 0){
	    	boutonRetirer.setEnabled(false);
		} else {
	    	boutonRetirer.setEnabled(true);
		}
	}
	
	public void ajouter(E objet){
		LM.addElement(objet);
		liste2O.add(objet);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onNotification(Observable notifier, String notification) {
		if(notification.equals("valueChanged")){
			this.selection = (E) liste.getSelectedObject();
	    	boutonAjouter.setEnabled(!liste2O.contains(selection));
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
}
