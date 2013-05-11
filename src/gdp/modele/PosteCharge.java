package gdp.modele;

import gdp.controleur.Application;
import gdp.controleur.EditeurRunnable;
import gdp.controleur.Observable;
import gdp.controleur.Observateur;
import gdp.modele.crud.BDD;
import gdp.modele.crud.CRUD;
import gdp.vue.crud.Editeur;
import gdp.vue.crud.EditeurListe;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * 
 * @author dom
 */
public class PosteCharge extends CRUD implements Observateur {
	private static final long serialVersionUID = 1L;
	private String designation = "Indéfini";
	private List<Integer> _machines = new ArrayList<Integer>();
	private List<Machine> machines = new ArrayList<Machine>();
	private List<Integer> _operateurs = new ArrayList<Integer>();
	private List<Operateur> operateurs = new ArrayList<Operateur>();
	
	public PosteCharge(){}
	
	@Override
	public Editeur editeur(final Window fenetre) {
        final Editeur e = new Editeur(fenetre, "Poste de charge");
        final PosteCharge self = this;
        final JButton bSupprimer = new JButton("Supprimer");
        bSupprimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	for(Article article: Application.articles){
            		if(article instanceof ArticleFabrique){
            			ArticleFabrique af = (ArticleFabrique) article;
            			for(ActiviteGO ac: af.getGammeOperatoire()){
            				if(ac.getPosteCharge() == getIdCRUD()){
                                JOptionPane.showMessageDialog(fenetre,
                                        "Ce poste de charge ne peut pas être supprimé car " +
                                        "il est est affecté à l'activité :\n"+ac+" de l'article "+af,
                                        "Dépendance",
                                        JOptionPane.WARNING_MESSAGE);
                                return;
            				}
            			}
            		}
            	}
            	Object[] options = {"Oui", "Annuler"};
        		int n = JOptionPane.showOptionDialog(fenetre,
            		"Supprimer le poste de charge "+self+" ?",
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
        });
        e.ajouterChamp(bSupprimer);
        
        e.ajouterChamp("Désignation", designation, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setDesignation(valeur);
			}
        });
        
		JPanel vueMachines = e.ajouterChamp("Machines");
		final JButton modifierMachines = new JButton("Consulter/Modifier les machines");
		modifierMachines.setPreferredSize(new Dimension(284,26));
		vueMachines.add(modifierMachines);
		modifierMachines.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog editeurMachines = new JDialog(fenetre, JDialog.ModalityType.DOCUMENT_MODAL);
				editeurMachines.setLocation(200, 100);
				editeurMachines.setTitle("Poste de charge "+self+" - Machines");
		        EditeurListe<Machine> eMachines = 
		        		new EditeurListe<Machine>(editeurMachines, Machine.class, machines);
		        eMachines.ajouterObservateur(self);
		        eMachines.setOpaque(true);
				editeurMachines.setContentPane(eMachines);
				editeurMachines.pack();
				editeurMachines.setVisible(true);
			}
		});
        
		JPanel vueOperateurs = e.ajouterChamp("Opérateurs");
		final JButton modifierOperateurs = new JButton("Consulter/Modifier les opérateurs");
		modifierOperateurs.setPreferredSize(new Dimension(284,26));
		vueOperateurs.add(modifierOperateurs);
		modifierOperateurs.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog editeurOperateurs = new JDialog(fenetre, JDialog.ModalityType.DOCUMENT_MODAL);
				editeurOperateurs.setLocation(200, 100);
				editeurOperateurs.setTitle("Poste de charge "+self+" - Opérateurs");
		        EditeurListe<Operateur> eOperateurs = 
		        		new EditeurListe<Operateur>(editeurOperateurs, Operateur.class, operateurs);
		        eOperateurs.ajouterObservateur(self);
		        eOperateurs.setOpaque(true);
				editeurOperateurs.setContentPane(eOperateurs);
				editeurOperateurs.pack();
				editeurOperateurs.setVisible(true);
			}
		});
		return e;
	}

	@Override
	public void operationsPostChargement(int vague) {
        if(vague == 1){
            for(int mid: _machines){
                machines.add(Application.machines.getObjet(mid));
            }
            _machines.clear();
            for(int mid: _operateurs){
            	operateurs.add(Application.operateurs.getObjet(mid));
            }
            _operateurs.clear();
        }
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeUTF(designation);
        _machines.clear();
        for(Machine m: machines){
            _machines.add(m.getIdCRUD());
        }
        out.writeObject(_machines);
        _machines.clear();
        _operateurs.clear();
        for(Operateur o: operateurs){
            _operateurs.add(o.getIdCRUD());
        }
        out.writeObject(_operateurs);
        _operateurs.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
        super.readExternal(in);
        designation = in.readUTF();
        _machines = (ArrayList<Integer>) in.readObject();
        _operateurs = (ArrayList<Integer>) in.readObject();
	}
	
	@Override
	public String toString() {
		return super.toString()+" "+designation;
	}

	public String getDesignation() {
		return designation;
	}
	
	public void setDesignation(String designation) {
		this.designation = designation;
		notifierObservateurs("toString");
	}
	
	public List<Machine> getMachines() {
		return machines;
	}
	
	public void setMachines(List<Machine> machines) {
		this.machines = machines;
		notifierObservateurs("maj");
	}
	
	public List<Operateur> getOperateurs() {
		return operateurs;
	}
	
	public void setOperateurs(List<Operateur> operateurs) {
		this.operateurs = operateurs;
		notifierObservateurs("maj");
	}
	
	public static class Machine extends CRUD{
		private static final long serialVersionUID = 1L;
		private String designation = "Indéfini";
		
		public Machine(){}
		
		@Override
		public Editeur editeur(final Window fenetre) {
	        final Editeur e = new Editeur(fenetre, "Machine");
	        final Machine self = this;
	        final JButton bSupprimer = new JButton("Supprimer");
	        bSupprimer.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
		            boolean suppressionPossible = true;
	            	for(PosteCharge p: Application.posteCharges){
            			if(p.getMachines().contains(self)){
                            JOptionPane.showMessageDialog(fenetre,
                                    "Cette machine ne peut pas être supprimée car " +
                                    "elle appartient au poste de charge "+p,
                                    "Dépendance",
                                    JOptionPane.WARNING_MESSAGE);
                            suppressionPossible = false;
                            break;
            			}
            		}
	            	if(suppressionPossible){
		            	Object[] options = {"Oui", "Annuler"};
		        		int n = JOptionPane.showOptionDialog(fenetre,
		            		"Supprimer la machine "+self+" ?",
		            		"Quitter",
		            		JOptionPane.CANCEL_OPTION,
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
	        
	        e.ajouterChamp("Désignation", designation, new EditeurRunnable(){
				@Override
				public void run() throws Error {
	                setDesignation(valeur);
				}
	        });
			return e;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
	        super.writeExternal(out);
	        out.writeUTF(designation);
		}
		
		@Override
		public void readExternal(ObjectInput in) throws IOException,
				ClassNotFoundException {
	        super.readExternal(in);
	        designation = in.readUTF();
		}
		
		@Override
		public void operationsPostChargement(int vague) {
		}
		
		@Override
		public String toString() {
			return super.toString()+" "+designation;
		}

		public String getDesignation() {
			return designation;
		}
		public void setDesignation(String designation) {
			this.designation = designation;
			notifierObservateurs("toString");
		}
	}

	@Override
	public void onNotification(Observable notifier, String notification) {
		notifierObservateurs("maj");
	}
	
	public static class Operateur extends CRUD{
		private static final long serialVersionUID = 1L;
	    public static final BDD<Operateur> BDD = new BDD<Operateur>(Operateur.class);
		private String qualification = "Indéfini";
		private String nom = "SansNom";
		
		public Operateur(){}
		
		@Override
		public Editeur editeur(final Window fenetre) {
	        final Editeur e = new Editeur(fenetre, "Opérateur");
	        final Operateur self = this;
	        final JButton bSupprimer = new JButton("Supprimer");
	        bSupprimer.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
		            boolean suppressionPossible = true;
	            	for(PosteCharge p: Application.posteCharges){
            			if(p.getOperateurs().contains(self)){
                            JOptionPane.showMessageDialog(fenetre,
                                    "Cette catégorie d'opérateurs ne peut pas être supprimée car " +
                                    "elle appartient au poste de charge "+p,
                                    "Dépendance",
                                    JOptionPane.WARNING_MESSAGE);
                            suppressionPossible = false;
                            break;
            			}
            		}
	            	if(suppressionPossible){
		            	Object[] options = {"Oui", "Annuler"};
		        		int n = JOptionPane.showOptionDialog(fenetre,
		            		"Supprimer l'opérateur "+self+" ?",
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
	        
	        e.ajouterChamp("Qualification", qualification, new EditeurRunnable(){
				@Override
				public void run() throws Error {
	                setQualification(valeur);
				}
	        });
	        
	        e.ajouterChamp("Nom", nom, new EditeurRunnable(){
				@Override
				public void run() throws Error {
	                setNom(valeur);
				}
	        });
			return e;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
	        super.writeExternal(out);
	        out.writeUTF(qualification);
	        out.writeUTF(nom);
		}
		
		@Override
		public void readExternal(ObjectInput in) throws IOException,
				ClassNotFoundException {
	        super.readExternal(in);
	        qualification = in.readUTF();
	        nom = in.readUTF();
		}
		
		@Override
		public void operationsPostChargement(int vague) {
		}
		
		@Override
		public String toString() {
			return super.toString()+" "+nom;
		}

		public String getQualification() {
			return qualification;
		}
		
		public void setQualification(String qualification) {
			this.qualification = qualification;
			notifierObservateurs("maj");
		}
		
		public String getNom() {
			return nom;
		}
		
		public void setNom(String nom) {
			this.nom = nom;
			notifierObservateurs("toString");
		}
	}
}
