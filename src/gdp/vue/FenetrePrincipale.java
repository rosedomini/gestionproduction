package gdp.vue;

import gdp.controleur.Application;
import gdp.modele.*;
import gdp.modele.PosteCharge.Machine;
import gdp.modele.PosteCharge.Operateur;
import gdp.modele.crud.CRUD;

import javax.swing.*;


import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

/**
 * @author Dominic ROSE, Minh Huy NGUYEN
 */
public class FenetrePrincipale extends JFrame {
    public static final long serialVersionUID = 1L;
    private JTabbedPane onglets = new JTabbedPane(SwingConstants.TOP);
    private HashMap<Class<? extends CRUD>, ChoixEditeur> choixEditeurs = 
    		new HashMap<Class<? extends CRUD>, ChoixEditeur>();
    final private static Dimension initSize = new Dimension(970, 600);
    
    public void afficherAccueil() {
        setContentPane(onglets);
        onglets.setPreferredSize(initSize);
        for(ChoixEditeur choixEditeur: choixEditeurs.values()){
        	choixEditeur.actualiserListe();
        }
        rafraichir();
    }
    
    public void afficherEditeur(CRUD objet){
    	choixEditeurs.get(objet.getTopClass()).afficherEditeur(objet);
    }
    
    public HashMap<Class<? extends CRUD>, ChoixEditeur> getChoixEditeurs() {
		return choixEditeurs;
	}

	public void cacherEditeur(Class<? extends CRUD> classe){
        choixEditeurs.get(classe).cacherEditeur();
    }
    
	public void actualiserListeEditeur(Class<? extends CRUD> classe) {
		choixEditeurs.get(classe).actualiserListe();
	}

	public void actualiserListeEditeur(CRUD objet) {
		choixEditeurs.get(objet.getTopClass()).actualiserListe(objet);
	}

    public void rafraichir() {
    	setMinimumSize(getSize());
    	invalidate();
        pack();
        repaint();
        //setMinimumSize(new Dimension(800, 600));
    }

    public FenetrePrincipale(String titre){
        super(titre);
        JMenuBar menuBar = new JMenuBar();
        JMenu fichier = new JMenu("Fichier");
        fichier.setMnemonic(KeyEvent.VK_F);
        JMenuItem enregistrer = new JMenuItem("Enregistrer");
        enregistrer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Application.enregistrerDonnees();
			}
		});
        fichier.add(enregistrer);
        enregistrer.setMnemonic(KeyEvent.VK_E);
        JMenuItem quitter = new JMenuItem("Quitter");
        quitter.setMnemonic(KeyEvent.VK_Q);
        quitter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Application.quitter();
            }
        });
        fichier.add(quitter);
        menuBar.add(fichier);
        JMenu aide = new JMenu("Aide");
        aide.setMnemonic(KeyEvent.VK_A);
        JMenuItem apropos = new JMenuItem("A Propos");
        apropos.setMnemonic(KeyEvent.VK_F1);
        apropos.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e){
        		JOptionPane.showMessageDialog(null,
        				"Application Génie Logiciel 2\n"
        		+"Gestion de production\n"
        		+"Version 1.0\n"
        		+"Réalisation: Equipe One Top, EISTI",
        				"A Propos: Application GL2",
        				JOptionPane.INFORMATION_MESSAGE);
        	}
        });
        aide.add(apropos);
        menuBar.add(aide);
        setJMenuBar(menuBar);

        setLocationByPlatform(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}

            @Override
            public void windowClosing(WindowEvent e) {
                Application.quitter();
            }

            @Override
            public void windowClosed(WindowEvent e) {}

            @Override
            public void windowIconified(WindowEvent e) {}

            @Override
            public void windowDeiconified(WindowEvent e) {}

            @Override
            public void windowActivated(WindowEvent e) {}

            @Override
            public void windowDeactivated(WindowEvent e) {}
        });

        ajouterOnglet("Articles", Article.class);
        ajouterOnglet("Commandes", Commande.class);
        ajouterOnglet("Magasins", Magasin.class);
        ajouterOnglet("Postes de charge", PosteCharge.class);
        ajouterOnglet("Machines", Machine.class);
        ajouterOnglet("Opérateurs", Operateur.class);
        ajouterOnglet("Contacts", Contact.class);

        getContentPane().setPreferredSize(initSize);
        rafraichir();
        setVisible(true);
    }
    
    private void ajouterOnglet(String nom, Class<? extends CRUD> classe){
        ChoixEditeur choixEditeur = new ChoixEditeur(this, classe);
        choixEditeurs.put(classe, choixEditeur);
        onglets.addTab(nom, choixEditeur);
    }
}
