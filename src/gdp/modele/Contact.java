package gdp.modele;

import gdp.controleur.Application;
import gdp.controleur.EditeurRunnable;
import gdp.modele.crud.CRUD;
import gdp.vue.crud.Editeur;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JButton;
import javax.swing.JOptionPane;


public class Contact extends CRUD implements ActionListener {
	private static final long serialVersionUID = 1L;
	private String nom = "Indéfini";
	private String prenom = "Indéfini";
	private String telTravail = "Indéfini";
	private String telPersonnel = "Indéfini";
	private String courriel = "Indéfinie";
	private String adresse = "Indéfinie";
	private String ville = "Indéfinie";

	public Contact(){}

	@Override
	public Editeur editeur(Window fenetre) {
        final Editeur e = new Editeur(fenetre, "Contact");
        final JButton bSupprimer = new JButton("Supprimer");
        bSupprimer.setActionCommand("supprimer");
        bSupprimer.addActionListener(this);
        e.ajouterChamp(bSupprimer);
        
        e.ajouterChamp("Nom", nom, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setNom(valeur);
			}
        });
        e.ajouterChamp("Prénom", prenom, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setPrenom(valeur);
			}
        });
        e.ajouterChamp("Tél. travail", telTravail, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setTelTravail(valeur);
			}
        });
        e.ajouterChamp("Tél. personnel", telPersonnel, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setTelPersonnel(valeur);
			}
        });
        e.ajouterChamp("Addresse e-mail", courriel, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setCourriel(valeur);
			}
        });
        e.ajouterChamp("Ville", ville, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setVille(valeur);
			}
        });
        e.ajouterChamp("Adresse", adresse, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setAdresse(valeur);
			}
        });
		return e;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("supprimer")){
        	Object[] options = {"Oui", "Annuler"};
    		int n = JOptionPane.showOptionDialog(Application.fenetre,
        		"Supprimer les informations sur "+this+" ?",
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

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeUTF(nom);
        out.writeUTF(prenom);
        out.writeUTF(telTravail);
        out.writeUTF(telPersonnel);
        out.writeUTF(courriel);
        out.writeUTF(adresse);
        out.writeUTF(ville);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
        super.readExternal(in);
        nom = in.readUTF();
        prenom = in.readUTF();
        telTravail = in.readUTF();
        telPersonnel = in.readUTF();
        courriel = in.readUTF();
        adresse = in.readUTF();
        ville = in.readUTF();
	}
	
	@Override
	public void operationsPostChargement(int vague) {
	}
	
	@Override
	public String toString() {
		return super.toString()+" "+nom;
	}
	
	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
		notifierObservateurs("toString");
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
		notifierObservateurs("maj");
	}

	public String getTelTravail() {
		return telTravail;
	}

	public void setTelTravail(String telTravail) {
		this.telTravail = telTravail;
		notifierObservateurs("maj");
	}

	public String getTelPersonnel() {
		return telPersonnel;
	}

	public void setTelPersonnel(String telPersonnel) {
		this.telPersonnel = telPersonnel;
		notifierObservateurs("maj");
	}

	public String getCourriel() {
		return courriel;
	}

	public void setCourriel(String courriel) {
		this.courriel = courriel;
		notifierObservateurs("maj");
	}

	public String getVille() {
		return ville;
	}

	public void setVille(String ville) {
		this.ville = ville;
		notifierObservateurs("maj");
	}

	public String getAdresse() {
		return adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
		notifierObservateurs("maj");
	}
}