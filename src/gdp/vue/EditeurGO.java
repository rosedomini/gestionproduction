package gdp.vue;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import gdp.controleur.Observable;
import gdp.controleur.Observateur;
import gdp.modele.ActiviteGO;
import gdp.modele.ArticleFabrique;
import gdp.vue.crud.Editeur;

/**
 * @author Chris
 */

public class EditeurGO extends JPanel implements ActionListener,
		ListSelectionListener, Observateur, WindowListener {
    public static final long serialVersionUID = 1L;
    
    private ArticleFabrique articleFab;
    private ArrayList<ActiviteGO> gammeOperatoire;
    
	private JPanel westP = new JPanel(new BorderLayout());
	private JPanel emptyEditP = new JPanel();
	JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, westP, emptyEditP);
    
    DefaultListModel LMliste = new DefaultListModel();
    JList Lliste = new JList(LMliste);
    JScrollPane SPlist = new JScrollPane(Lliste);
    
    private JButton ajouter = new JButton("+");
	private JButton monter = new JButton("↑");
	private JButton descendre = new JButton("↓");
	private JButton supprimer = new JButton("-");
	
	private Window fenetre;

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		int i = Lliste.getSelectedIndex();
		if(cmd.equals("ajouter")){
        	ActiviteGO a = new ActiviteGO(articleFab);
        	a.ajouterObservateur(this);
        	articleFab.getGammeOperatoire().add(a);
        	articleFab.notifierObservateurs("maj");
        	articleFab.notifierObservateurs("gammeOperatoire");
        	LMliste.addElement(articleFab.getGammeOperatoire().size()+" : "+a);
        	Lliste.setSelectedIndex(Lliste.getLastVisibleIndex());
        	Editeur editeur = a.editeur(fenetre);
        	editeur.generer();
        	sp.setRightComponent(editeur);
        	fenetre.pack();
		} else if(cmd.equals("monter")){
			ActiviteGO a = articleFab.getGammeOperatoire().get(i);
			ActiviteGO a2 = articleFab.getGammeOperatoire().get(i-1);
			articleFab.getGammeOperatoire().set(i, a2);
			articleFab.getGammeOperatoire().set(i-1, a);
			LMliste.set(i, i+1+" : "+a2);
			LMliste.set(i-1, i+" : "+a);
        	Lliste.setSelectedIndex(i-1);
		} else if(cmd.equals("descendre")){
			ActiviteGO a = articleFab.getGammeOperatoire().get(i);
			ActiviteGO a2 = articleFab.getGammeOperatoire().get(i+1);
			articleFab.getGammeOperatoire().set(i, a2);
			articleFab.getGammeOperatoire().set(i+1, a);
			LMliste.set(i, i+1+" : "+a2);
			LMliste.set(i+1, i+2+" : "+a);
        	Lliste.setSelectedIndex(i+1);
		} else if(cmd.equals("supprimer")){
			LMliste.remove(i);
			articleFab.getGammeOperatoire().remove(i);
        	articleFab.notifierObservateurs("maj");
        	if(articleFab.getGammeOperatoire().isEmpty()){
            	articleFab.notifierObservateurs("gammeOperatoire");
        	}
        	sp.setRightComponent(emptyEditP);
		}
        majBoutons();
        
        fenetre.addWindowListener(this);
	}
	
	private void majBoutons(){
		int i = Lliste.getSelectedIndex();
        ajouter.setEnabled(true);
        monter.setEnabled(i > 0);
        descendre.setEnabled(i >= 0 && i < Lliste.getLastVisibleIndex());
        supprimer.setEnabled(i >= 0);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int i = Lliste.getSelectedIndex();
		if(i < 0){
        	sp.setRightComponent(emptyEditP);
		} else {
        	ActiviteGO a = articleFab.getGammeOperatoire().get(i);
        	a.ajouterObservateur(this);
        	Editeur editeur = a.editeur(fenetre);
        	editeur.generer();
        	sp.setRightComponent(editeur);
		}
		majBoutons();
	}

	@Override
	public void onNotification(Observable notifier, String notification) {
		int i = articleFab.getGammeOperatoire().indexOf(notifier);
		LMliste.set(i, i+1+" : "+notifier);
	}

	public EditeurGO(Window fenetre, ArticleFabrique articleFab) {
        super(new BorderLayout());
        this.articleFab = articleFab;
        this.fenetre = fenetre;
    	fenetre.setMinimumSize(new Dimension(900, 450));
    	fenetre.invalidate();
    	fenetre.pack();
    	fenetre.repaint();
        gammeOperatoire = articleFab.getGammeOperatoire();
        
        int i = 0;
        for(ActiviteGO ago: gammeOperatoire) {
        	LMliste.addElement(++i+" : "+ago);
        }
        
        add(sp, BorderLayout.CENTER);

		JPanel pButtons = new JPanel(new GridLayout(1,4));
        pButtons.setLayout(new FlowLayout());
        pButtons.add(ajouter);
        pButtons.add(monter);
        pButtons.add(descendre);
        pButtons.add(supprimer);
        westP.add(SPlist, BorderLayout.CENTER);
        westP.add(pButtons, BorderLayout.SOUTH);
        
        ajouter.addActionListener(this);
        ajouter.setActionCommand("ajouter");
        monter.addActionListener(this);
        monter.setActionCommand("monter");
        descendre.addActionListener(this);
        descendre.setActionCommand("descendre");
        supprimer.addActionListener(this);
        supprimer.setActionCommand("supprimer");
        
        Lliste.addListSelectionListener(this);
	}

	@Override
	public void windowOpened(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowClosing(WindowEvent arg0) {
		for(ActiviteGO a: articleFab.getGammeOperatoire()){
			a.supprimerObservateur(this);
		}
	}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowActivated(WindowEvent arg0) {}
}