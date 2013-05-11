package gdp.vue.crud;

import gdp.controleur.Application;
import gdp.controleur.EditeurRunnable;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

/**
 * JPanel permettant de modifier un objet Editable
 * @author Dominic ROSE
 */
public class Editeur extends JPanel {
    public static final long serialVersionUID = 1L;
    private ArrayList<Champ> champs = new ArrayList<Champ>();
    private String titre = "";
    private Window fenetre;

    public ArrayList<Champ> getChamps() {
		return champs;
	}

	public void setTitre(String titre) {
        this.titre = titre;
    }

    public Editeur(Window fenetre, String titre){
        super(new GridBagLayout());
        this.fenetre = fenetre;
        this.titre = titre;
    }
    
    /**
     * Ajoute un champs pour modifier un attribut d'un objet
     * @param libelle ex : "Poids"
     * @param valeur ex : "1000"
     * @param validation cf EditeurRunnable, ex : validation.run() met le poids à jour : 1000
     * @return champs de texte créé, associé à l'attribut et sa valeur
     */
    public Champ ajouterChamp(String libelle, String valeur, EditeurRunnable validation){
    	Champ champ = new Champ(fenetre, libelle, valeur, validation);
    	champs.add(champ);
    	return champ;
    }
    
    /**
     * Ajoute un champs pour modifier un attribut d'un objet
     * @param libelle ex : "Nomenclature"
     * @return la vue créée (JPanel vide à remplir) associée à ce champs
     */
    public JPanel ajouterChamp(String libelle){
    	Champ champ = new Champ(libelle);
    	champs.add(champ);
    	return (JPanel) champ.vue;
    }
    
    /**
     * Ajoute un bouton à l'éditeur
     * @param bouton
     * @return bouton
     */
    public JButton ajouterChamp(JButton bouton){
    	Champ champ = new Champ(bouton);
    	champs.add(champ);
    	return (JButton) champ.getVue();
    }
    
    /**
     * Génère la disposition finale des champs, boutons etc.
     */
    public void generer(){
    	setMinimumSize(new Dimension(580, 500));
        Application.setSousTitre(titre);
        JLabel titre = new JLabel(this.titre);
        titre.setFont(new Font("sansserif", Font.BOLD, 32));
        GridBagLayout bag = (GridBagLayout) getLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.ipadx = 8;
        c.ipady = 3;
        c.gridwidth = GridBagConstraints.REMAINDER;
        bag.setConstraints(titre, c);
        add(titre);
        for(Champ champ: champs){
            if(champ.getLabelNom() != null){
                c.weightx = 0.0;
                c.gridwidth = GridBagConstraints.RELATIVE;
                add(champ.getLabelNom());
                bag.setConstraints(champ.getLabelNom(), c);
            }
            c.weightx = 1.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            bag.setConstraints(champ.getVue(), c);
            add(champ.getVue());
        }
    }
    
    public static class Champ {
    	private String nom;
    	private JLabel labelNom;
    	private JComponent vue = new JPanel();
    	private JLabel lecteur;
    	private JTextField editeur;
    	private Window fenetre;
    	
		public Champ(String nom, JLabel lecteur, JTextField editeur) {
			super();
			this.nom = nom;
			this.lecteur = lecteur;
			this.editeur = editeur;
			vue.add(lecteur);
		}
		
		public Champ(String libelle){
			nom = libelle;
			labelNom = new JLabel(nom);
			//labelNom.setAlignmentY(JLabel.TOP_ALIGNMENT);
		}
		
		public Champ(Window fenetre, String libelle, String valeur, final EditeurRunnable validation){
			this.fenetre = fenetre;
			nom = libelle;
			labelNom = new JLabel(nom);
			lecteur = new JLabel(valeur);
			lecteur.setCursor(new Cursor(Cursor.HAND_CURSOR));
			final Champ self = this;
			
			vue.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mousePressed(MouseEvent arg0) {
					if(vue.getComponent(0) == lecteur){
						vue.removeAll();
						vue.add(editeur);
						vue.invalidate();
				    	self.fenetre.setMinimumSize(self.fenetre.getSize());
				    	self.fenetre.invalidate();
				    	self.fenetre.pack();
				    	self.fenetre.repaint();
						editeur.requestFocus();
						editeur.selectAll();
					}
				}
				
				@Override
				public void mouseExited(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseEntered(MouseEvent arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void mouseClicked(MouseEvent arg0) {
				}
			});
			
			editeur = new JTextField(valeur, 30);
			editeur.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					vue.removeAll();
					vue.add(lecteur);
					validation.valeur = editeur.getText();
					validation.editeur = editeur;
					try {
						validation.run();
					} catch(Error e){
						editeur.setText(lecteur.getText());
					}
					lecteur.setText(editeur.getText());
			    	self.fenetre.setMinimumSize(self.fenetre.getSize());
			    	self.fenetre.invalidate();
			    	self.fenetre.pack();
			    	self.fenetre.repaint();
				}
			});
			vue.add(lecteur);
			lecteur.setPreferredSize(editeur.getPreferredSize());
		}
		
		public Champ(JButton bouton){
			vue = bouton;
		}

		public String getNom() {
			return nom;
		}

		public void setNom(String nom) {
			this.nom = nom;
		}

		public JLabel getLabelNom() {
			return labelNom;
		}

		public void setLabelNom(JLabel labelNom) {
			this.labelNom = labelNom;
		}

		public JComponent getVue() {
			return vue;
		}

		public void setVue(JComponent vue) {
			this.vue = vue;
		}

		public JTextField getEditeur() {
			return editeur;
		}

		public void setEditeur(JTextField editeur) {
			this.editeur = editeur;
		}

		public JLabel getLecteur() {
			return lecteur;
		}
    }
}
