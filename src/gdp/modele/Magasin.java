package gdp.modele;

import gdp.controleur.Application;
import gdp.controleur.EditeurRunnable;
import gdp.modele.crud.CRUD;
import gdp.vue.crud.Editeur;

import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * @author dom
 */
public class Magasin extends CRUD implements Comparable<Magasin> {
    public static final long serialVersionUID = 1L;
    private String libelle = "SansNom";
    private String emplacement = "Indéfini";
    private List<String> allees = new ArrayList<String>();

	@Override
    public Editeur editeur(final Window fenetre) {
        final Editeur e = new Editeur(fenetre, "Magasin");
        final Magasin self = this;
        final JButton bSupprimer = new JButton("Supprimer");
        bSupprimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	boolean suppressionPossible = true;
            	for(Article a: Application.articles){
            		if(a.getStocks().containsKey(self)){
                        JOptionPane.showMessageDialog(fenetre,
                                "Ce magasin ne peut pas être supprimé car " +
                                "il contient un stock de l'article "+a,
                                "Dépendance",
                                JOptionPane.WARNING_MESSAGE);
                        suppressionPossible = false;
                        break;
            		}
            	}
            	if(suppressionPossible){
	            	Object[] options = {"Oui", "Annuler"};
	        		int n = JOptionPane.showOptionDialog(fenetre,
	            		"Supprimer le magasin "+self.toString()+" ?",
	            		"Quitter",
	            		JOptionPane.YES_NO_CANCEL_OPTION,
	            		JOptionPane.QUESTION_MESSAGE,
	            		null,
	            		options,
	            		options[1]
	            	);
	        		if(n == 0){
	        			notifierObservateurs("deleted");
	        		}
            	}
            }
        });
        e.ajouterChamp(bSupprimer);
        
        e.ajouterChamp("Libellé", libelle, new EditeurRunnable() {
			@Override
			public void run() throws Error {
                setLibelle(valeur);
			}
		});
        
        e.ajouterChamp("Emplacement", emplacement, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setEmplacement(valeur);
			}
        });
        
        final JPanel editeurAllees = e.ajouterChamp("Allées");
        final JTextField nouvelleAllee = new JTextField(20);
        final JButton ajouterAllee = new JButton("+");
		ActionListener ajouterAAL = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String t = nouvelleAllee.getText();
				if(t.isEmpty()) return;
				if(allees.contains(t) || t.equals("Indéfinie")){
                    JOptionPane.showMessageDialog(Application.fenetre,
                            "Cette allée existe déjà.",
                            "Valeur existante",
                            JOptionPane.WARNING_MESSAGE);
				} else {
					allees.add(t);
					nouvelleAllee.selectAll();
					editeurActualiserAllees(editeurAllees, nouvelleAllee, ajouterAllee);
					nouvelleAllee.requestFocus();
				}
			}
		};
        nouvelleAllee.addActionListener(ajouterAAL);
        ajouterAllee.addActionListener(ajouterAAL);
        editeurActualiserAllees(editeurAllees, nouvelleAllee, ajouterAllee);
        return e;
    }
	
	private void editeurActualiserAllees(final JPanel editeurAllees,
		final JTextField nouvelleAllee, final JButton ajouterAllee){
		editeurAllees.removeAll();
		final Magasin self = this;
        editeurAllees.setLayout(new GridLayout(allees.size()+1, 2));
        for(final String allee: allees){
            editeurAllees.add(new JLabel(allee));
            JButton remove = new JButton("-");
            remove.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					for(Article article: Application.articles){
						if(article.getStocks().containsKey(self)){
							StockArticle sa = article.getStocks().get(self);
							if(sa.getAllee() == allee){
								sa.setAllee("Indéfinie");
							}
						}
					}
					self.getAllees().remove(allee);
					self.editeurActualiserAllees(editeurAllees, nouvelleAllee, ajouterAllee);
				}
			});
            editeurAllees.add(remove);
        }
        editeurAllees.add(nouvelleAllee);
        editeurAllees.add(ajouterAllee);
        notifierObservateurs("maj");
	}

    @Override
    public void operationsPostChargement(int vague) {}

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
    	super.writeExternal(out);
    	out.writeUTF(libelle);
    	out.writeUTF(emplacement);
    	out.writeObject(allees);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	libelle = in.readUTF();
    	emplacement = in.readUTF();
    	allees = (ArrayList<String>) in.readObject();
    }

    public Magasin() {}

	@Override
	public String toString() {
		return super.toString()+" "+libelle;
	}

	public int compareTo(Magasin arg0) {
		int i = libelle.compareTo(arg0.libelle);
		if(i == 0) return Integer.valueOf(idCRUD).compareTo(arg0.idCRUD);
		return i;
	}

    public String getLibelle() {
		return libelle;
	}

	public void setLibelle(String libelle) {
		this.libelle = libelle;
    	notifierObservateurs("toString");
	}

	public String getEmplacement() {
		return emplacement;
	}

	public void setEmplacement(String emplacement) {
		this.emplacement = emplacement;
    	notifierObservateurs("maj");
	}

	public List<String> getAllees() {
		return allees;
	}
}
