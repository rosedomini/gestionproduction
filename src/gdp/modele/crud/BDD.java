package gdp.modele.crud;

import gdp.controleur.Observable;
import gdp.controleur.Observateur;
import gdp.modele.StatutCRUD;

import java.awt.Window;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

/**
 * @author Dominic ROSE
 */
public class BDD<E extends CRUD> implements Iterable<E>, Observateur {
	private static boolean miseAJour = false;
    private static int nombreVaguesOPC = 3;
    private static Map<Class<? extends CRUD>, BDD<? extends CRUD>> listeBDD
            = new HashMap<Class<? extends CRUD>, BDD<? extends CRUD>>();

    public static void charger(String repertoire) {
        File repertoireF = new File(repertoire);
        repertoireF.mkdir();
        
        for(BDD<? extends CRUD> base: listeBDD.values()){
        	base.preCharger(repertoire);
        }
        for(int vague = 1; vague <= nombreVaguesOPC; vague++){
            for(BDD<? extends CRUD> base: listeBDD.values()){
            	base.operationsPostChargement(vague);
            }
        }
    }

    public static void sauvegarder(Window fenetre, String repertoire) {
        miseAJour = false;
        
        File repertoireF = new File(repertoire);
        repertoireF.mkdir();

        for(BDD<? extends CRUD> base: listeBDD.values()){
        	base.mettreAJour(fenetre, repertoire);
        }
    }
    
	public static BDD<? extends CRUD> getBDD(Class<? extends CRUD> classe) {
		return listeBDD.get(classe);
	}

	public static void ajouterBDD(BDD<? extends CRUD> base) {
		listeBDD.put(base.classe, base);
	}

	public static boolean isMiseAJour() {
		return miseAJour;
	}

	public static void setMiseAJour(boolean miseAJour) {
		BDD.miseAJour = miseAJour;
	}

    private TreeMap<Integer, E> objetsASupprimer = new TreeMap<Integer, E>();
    
	
	public TreeMap<Integer, E> objets = new TreeMap<Integer, E>();
	private Class<? extends CRUD> classe;
	
	public BDD(Class<? extends CRUD> classe){
		this.classe = classe;
	}
	
    public void changerObjet(E objet){
    	miseAJour = true;
    	E ancien = objets.get(objet.getIdCRUD());
    	ancien.supprimerObservateur(this);
    	ancien.setStatutCRUD(StatutCRUD.SUPPRIME);
    	objets.put(objet.getIdCRUD(), objet);
    	objet.ajouterObservateur(this);
    	objet.setStatutCRUD(StatutCRUD.A_METTRE_A_JOUR);
    }

	@SuppressWarnings("unchecked")
	public E creerObjet() {
		miseAJour = true;
        E nouvelObjet = null;
		try {
			nouvelObjet = (E) classe.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
        nouvelObjet.genererId();
        nouvelObjet.setStatutCRUD(StatutCRUD.A_METTRE_A_JOUR);
        ajouter(nouvelObjet);
        nouvelObjet.apresCreation();
        nouvelObjet.notifierObservateurs("created");
        return nouvelObjet;
	}
	
	@SuppressWarnings("unchecked")
	public void preCharger(String repertoire) {
		String repertoireClasse = repertoire+"/"+classe.toString().substring(6);//.replace(".", "/");
        File repertoireClasseF = new File(repertoireClasse);
        repertoireClasseF.mkdir();
        
        FileInputStream fichier;
        ObjectInputStream ois;
        String fichiers[] = repertoireClasseF.list();
        if(fichiers != null){
            for(int i = 0; i < fichiers.length; i++){
                if(fichiers[i].endsWith(".ser")){
                    try {
                        fichier = new FileInputStream(repertoireClasse+"/"+fichiers[i]);
                        ois = new ObjectInputStream(fichier);
                        E objet = (E) classe.cast(ois.readObject());
                        ajouter(objet);
                        ois.close();
                        fichier.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
	}

	private void operationsPostChargement(int vague) {
        for (E objet: objets.values()) {
            objet.operationsPostChargement(vague);
        }
	}
	
	public void mettreAJour(Window fenetre, String repertoire) {
        FileOutputStream fichier;
        ObjectOutputStream oos;
        
        File repertoireClasse = new File(repertoire+"/"+classe);
        repertoireClasse.mkdir();
        for(E objet: this){
            if(objet.getStatutCRUD() == StatutCRUD.A_METTRE_A_JOUR){
                try {
                	objet.setStatutCRUD(StatutCRUD.A_JOUR);
                    fichier = new FileOutputStream(repertoire+"/"+classe+"/"+objet.getIdCRUD()+".ser");
                    oos = new ObjectOutputStream(fichier);
                    oos.writeObject(objet);
                    oos.flush();
                    oos.close();
                    fichier.close();
                }
                catch (IOException e) {
                    JOptionPane.showMessageDialog(fenetre,
                            e.getMessage(),
                            "Mise à jour de \""+objet+"\"",
                            JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    objet.setStatutCRUD(StatutCRUD.A_METTRE_A_JOUR);
                    miseAJour = true;
                }
            }
        }
        LinkedHashSet<E> oAS = new LinkedHashSet<E>();
        oAS.addAll(objetsASupprimer.values());
        for(E objet: oAS){
            File f = new File(repertoire+"/"+classe+"/"+objet.getIdCRUD()+".ser");
            if(f.exists() && !f.delete()){
                JOptionPane.showMessageDialog(fenetre,
                		"Le fichier n'a pas pu être supprimé : "+f,
                        "Suppression de \""+objet+"\"",
                        JOptionPane.WARNING_MESSAGE);
                miseAJour = true;
            } else {
            	objet.setStatutCRUD(StatutCRUD.SUPPRIME);
            	objetsASupprimer.remove(objet.getIdCRUD());
            }
        }
    }
	
    public int genererId() {
        int max = 0;
        for(int id: objets.keySet()){
            if(id > max){
            	max = id;
            }
        }
        return max + 1;
    }

    private void ajouter(E objet) {
        objets.put(objet.getIdCRUD(), objet);
        objet.ajouterObservateur(this);
    }
    
	@Override
	public Iterator<E> iterator() {
		return objets.values().iterator();
	}
	
	public Collection<E> values(){
		return objets.values();
	}
	
	public E getObjet(int idCRUD){
		return objets.get(idCRUD);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onNotification(Observable notifier, String notification) {
		E objet = (E) notifier;
		setMiseAJour(true);
		if(notification.equals("deleted")){
	    	objets.remove(objet.getIdCRUD());
	    	objetsASupprimer.put(objet.getIdCRUD(), objet);
	    	objet.supprimerObservateur(this);
	    	objet.setStatutCRUD(StatutCRUD.A_SUPPRIMER);
		} else {
			objet.setStatutCRUD(StatutCRUD.A_METTRE_A_JOUR);
		}
	}
}
