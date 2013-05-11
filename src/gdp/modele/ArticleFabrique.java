package gdp.modele;

import gdp.controleur.Application;
import gdp.controleur.Observable;
import gdp.controleur.Observateur;
import gdp.vue.EditeurNomenclature;
import gdp.vue.VueBesoins;
import gdp.vue.EditeurGO;
import gdp.vue.VueNomenclature;
import gdp.vue.crud.Editeur;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.*;


/**
 * Articles pour lesquels on veut définir une nomenclature et éventuellement une gamme opératoire
 * @author Dom, Chris
 */

public class ArticleFabrique extends Article implements Observateur {
	public static final long serialVersionUID = 1L;
    
    private String referenceClasse = "";
    private HashMap<Integer, Integer> _composants = new HashMap<Integer, Integer>();
    private HashMap<Article, Integer> composants = new HashMap<Article, Integer>();
    private ArrayList<ActiviteGO> gammeOperatoire = new ArrayList<ActiviteGO>();
    
    private ActionListener modifierNomenclature;

	public ArticleFabrique(){}
    
    public ArticleFabrique(Article article){
    	idCRUD = article.getIdCRUD();
    	statutCRUD = article.getStatutCRUD();
    	code = article.code;
    	libelle = article.libelle;
    	poids = article.poids;
    	encombrement = article.encombrement;
    	_stocks = article._stocks;
    	stocks = article.stocks;
    	conditionnementStockage = article.conditionnementStockage;
    	conditionnementTransport = article.conditionnementTransport;
        for(Entry<Magasin, StockArticle> entry: stocks.entrySet()){
			StockArticle sa = entry.getValue();
			sa.setArticle(this);
			sa.setMagasin(entry.getKey());
        }
        for(Article a: Application.articles){
        	if(a instanceof ArticleFabrique){
        		HashMap<Article, Integer> cp = ((ArticleFabrique) a).getComposants();
        		if(cp.containsKey(article)){
	        		int qte = cp.get(article);
	        		cp.remove(article);
	        		cp.put(this, qte);
        		}
        	}
        }
    }
    
    public void modifierNomenclature() {
    	modifierNomenclature.actionPerformed(null);
	}

	@Override
	public Editeur editeur(final Window fenetre) {
		final ArticleFabrique self = this;
		Editeur e = super.editeur(fenetre);
		
		JPanel nom = e.ajouterChamp("Nomenclature");
		final JButton voirNom = new JButton("Voir");
		voirNom.setPreferredSize(new Dimension(130,26));
		final JButton modifierNom = new JButton("Modifier");
		modifierNom.setPreferredSize(new Dimension(130,26));
		final JButton supprimerNom = new JButton("Supprimer");
		supprimerNom.setPreferredSize(new Dimension(130,26));
		nom.add(voirNom);
		nom.add(modifierNom);
		nom.add(supprimerNom);
		supprimerNom.setVisible(self.getGammeOperatoire().isEmpty());
		
		voirNom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog vueNom = new JDialog(Application.fenetre, JDialog.ModalityType.DOCUMENT_MODAL);
				vueNom.setLocation(200,100);
				vueNom.setTitle("Nomenclature de "+self);
				VueNomenclature vueEdit = new VueNomenclature(self, 1);
				vueEdit.setOpaque(true);
				vueNom.setContentPane(vueEdit);
				vueNom.pack();
				vueNom.setVisible(true);
			}
		});
		
		modifierNomenclature = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog editeurNom = new JDialog(fenetre, JDialog.ModalityType.DOCUMENT_MODAL);
				editeurNom.setTitle("Composants de "+self);
		        EditeurNomenclature nome = new EditeurNomenclature(self);
				nome.setOpaque(true);
				editeurNom.setLocation(200, 100);
				editeurNom.setContentPane(nome);
				editeurNom.pack();
				editeurNom.setVisible(true);
			}
		};
		modifierNom.addActionListener(modifierNomenclature);
		supprimerNom.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				Object[] options = {"Ok", "Annuler"};
        		int n = JOptionPane.showOptionDialog(fenetre,
            		"Êtes-vous sûr de vouloir supprimer la nomenclature de "+self+" ?",
            		"Supprimer",
            		JOptionPane.YES_NO_CANCEL_OPTION,
            		JOptionPane.QUESTION_MESSAGE,
            		null,
            		options,
            		options[1]
            	);
        		if(n == 0){
        			Article transformation = new Article(self);
        			Application.articles.changerObjet(transformation);
					for(Observateur obs: self.observateurs){
						transformation.ajouterObservateur(obs);
					}
					notifierObservateurs("deleted");
					transformation.notifierObservateurs("created");
        		}
			}
		});
		
		JPanel go = e.ajouterChamp("Gamme opératoire");
		final JButton editerGamme = new JButton("Consulter/Modifier");
		editerGamme.setPreferredSize(new Dimension(265,26));
		final JButton supprimerGO = new JButton("Supprimer");
		supprimerGO.setPreferredSize(new Dimension(130,26));
		supprimerGO.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] options = {"Ok", "Annuler"};
        		int n = JOptionPane.showOptionDialog(Application.fenetre,
            		"Êtes-vous sûr de vouloir supprimer la gamme opératoire de "+self+" ?",
            		"Supprimer",
            		JOptionPane.YES_NO_CANCEL_OPTION,
            		JOptionPane.QUESTION_MESSAGE,
            		null,
            		options,
            		options[1]
            	);
        		if(n == 0){
        			getGammeOperatoire().clear();
        			supprimerGO.setVisible(false);
        			supprimerNom.setVisible(true);
        			notifierObservateurs("gammeOperatoire");
        		}
			}
		});
		go.add(editerGamme);
		go.add(supprimerGO);
		supprimerGO.setVisible(!self.getGammeOperatoire().isEmpty());
		
		editerGamme.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog DGamme = new JDialog(fenetre, JDialog.ModalityType.DOCUMENT_MODAL);
				DGamme.setLocation(200,100);
				DGamme.setTitle("Gamme opératoire de "+self);
				EditeurGO EGamme = new EditeurGO(DGamme, self);
				EGamme.setOpaque(true);
				DGamme.setContentPane(EGamme);
				DGamme.pack();
				DGamme.setVisible(true);
			}
		});
		
		JPanel besoins = e.ajouterChamp("Besoins en composants élémentaires");
		final JButton voirBesoins = new JButton("Voir");
		besoins.add(voirBesoins);
		voirBesoins.setPreferredSize(new Dimension(400, 26));
		voirBesoins.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JDialog vue = new JDialog(Application.fenetre, JDialog.ModalityType.DOCUMENT_MODAL);
				vue.setLocation(200,100);
				vue.setTitle(self+" - Besoins en composants élémentaires");
				VueBesoins vueB = new VueBesoins(self);
				vueB.setOpaque(true);
				vue.setContentPane(vueB);
				vue.pack();
				vue.setVisible(true);
			}
		});
		
		return e;
	}

	@Override
	public void operationsPostChargement(int vague) {
		super.operationsPostChargement(vague);
        if(vague == 1){
            for(Map.Entry<Integer, Integer> entry: _composants.entrySet()){
            	composants.put(Application.articles.objets.get(entry.getKey()), entry.getValue());
            }
            _composants.clear();
            for(ActiviteGO a: gammeOperatoire){
            	a.setArticle(this);
            }
        }
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(referenceClasse);
        _composants.clear();
        for(Map.Entry<Article,Integer> entry: composants.entrySet()){
            _composants.put(entry.getKey().getIdCRUD(), entry.getValue());
        }
        out.writeObject(_composants);
        _composants.clear();
        out.writeObject(gammeOperatoire);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		super.readExternal(in);
		referenceClasse = in.readUTF();
        _composants = (HashMap<Integer, Integer>) in.readObject();
		gammeOperatoire = (ArrayList<ActiviteGO>) in.readObject();
	}

	@Override
	public void onNotification(Observable notifier, String notification) {
		if(notification.equals("gammeOperatoire")){
			notifierObservateurs(notification);
		}
	}

    public HashMap<Article, Integer> getComposants() {
		return composants;
	}
    
	public ArrayList<ActiviteGO> getGammeOperatoire() {
		return gammeOperatoire;
	}
}