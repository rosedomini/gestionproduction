package gdp.modele;

import gdp.controleur.Application;
import gdp.controleur.EditeurRunnable;
import gdp.controleur.Observateur;
import gdp.modele.crud.CRUD;
import gdp.vue.EditeurStocks;
import gdp.vue.crud.Editeur;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * Classe de gestion d'articles
 * @author Dom
 */
public class Article extends CRUD implements Comparable<Article> {
	// un Article sérialisé avec un serialVersionUID différent ne pourra pas être désérialisé
    public static final long serialVersionUID = 1L;
    
    // classement des articles par code
    public final static TreeMap<Integer,Article> articles = new TreeMap<Integer, Article>();
    
    /**
     * @param code le code (int) d'un article
     * @return l'article correspondant
     */
    public static Article fromCode(int code){
    	return articles.get(code);
    }
    
    /**
     * @param code le code d'un article (String) entre 00000000 et FFFFFFFF
     * @return article correspondant au code
     */
    public static Article fromCode(String code){
    	// conversion en entier plus décalage
    	long lcode = Long.valueOf(code, 16)+(long)Integer.MIN_VALUE;
    	if(lcode > (long)Integer.MAX_VALUE) lcode = (long)Integer.MAX_VALUE;
    	else if(lcode < (long)Integer.MIN_VALUE) lcode = (long)Integer.MIN_VALUE;
    	return articles.get((int)lcode);
    }
    
    // identifiant unique parmis ceux de tous les articles
    // Integer.MIN_VALUE correspond à l'entier négatif minimal
    // La valeur hexadécimale calculée sera en fait 0x0 pour Integer.MIN_VALUE
    // ce qui permet d'associer MAX_VALUE à 0xFFFFFFFF
    // ce qui permet d'utiliser efficacement les bits de l'entier code
    // On peut peut-être éviter ça avec un unsigned int
    protected int code = Integer.MIN_VALUE;
    
    // SansNom apparaît dans l'éditeur lors de la création d'un article
    protected String libelle = "SansNom";
    
    protected double poids;
    protected Taille encombrement = new Taille();
    
    // _stocks est une variable temporaire utilisée pour le chargement des données
    // on associe à un identifiant de magasin, les données de stockage de cet article
    protected TreeMap<Integer, StockArticle> _stocks = new TreeMap<Integer, StockArticle>();
    // liste générée d'après les identifiants des magasins
    protected TreeMap<Magasin, StockArticle> stocks = new TreeMap<Magasin, StockArticle>();
    
    protected String conditionnementStockage = "";
    protected String conditionnementTransport = "";

    /**
     * Le code de l'article (à ne pas confondre avec son identifiant dans la BDD)
     * @return code de l'article à 8 digits hexadécimaux (modifiable)
     * ex : 000000F5
     */
    public String getCodeString() {
        StringBuffer s = new StringBuffer();
        s.append(Long.toHexString((long)code-Integer.MIN_VALUE).toUpperCase());
        while(s.length() < 8){
            s.insert(0, '0');
        }
        return s.toString();
    }

    public int getQuantiteTotale() {
    	int t = 0;
    	for(StockArticle sa: stocks.values()){
    		t += sa.getQuantite();
    	}
    	return t;
	}

	/**
     * L'identifiant de l'article dans la BDD est unique
     * Son code est censé être unique mais peut ne pas l'être s'il n'a pas été fixé comme tel
     * On affiche donc les deux codes ainsi que le libellé de l'article
     */
    @Override
    public String toString(){
        return getCodeString()+" "+getLibelle();
    }

    /**
     * Génère un éditeur pour cet article
     */
    @Override
    public Editeur editeur(final Window fenetre) {
        final Editeur e = new Editeur(fenetre, "Article");
        final Article self = this;
        
        final JButton bSupprimer = new JButton("Supprimer");
        bSupprimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	boolean suppressionPossible = true;
            	for(Article article: Application.articles){
            		if(article instanceof ArticleFabrique){
            			ArticleFabrique af = (ArticleFabrique) article;
            			if(af.getComposants().containsKey(self)){
                            JOptionPane.showMessageDialog(fenetre,
                                    "Cet article ne peut pas être supprimé car " +
                                    "il est définit comme composant de l'article "+af,
                                    "Dépendance",
                                    JOptionPane.WARNING_MESSAGE);
                            suppressionPossible = false;
                            break;
            			}
            		}
            	}
            	if(suppressionPossible){
                	Object[] options = {"Oui", "Annuler"};
            		int n = JOptionPane.showOptionDialog(fenetre,
	            		"Supprimer l'article "+self+" ?",
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
        
        e.ajouterChamp("Code (hex)", getCodeString(), new EditeurRunnable(){
			@Override
			public void run() throws Error {
                try {
                	int oldCode = self.code;
                    long code = Long.valueOf(valeur, 16)+(long)Integer.MIN_VALUE;
                    if(code > (long)Integer.MAX_VALUE) code = (long)Integer.MAX_VALUE;
                    int icode = (int)code;
                    Article existant = Article.fromCode(icode);
                    if(existant != null){
                        JOptionPane.showMessageDialog(e,
                                "Ce code existe déjà pour l'article "+existant);
                    } else {
	                    setCode(icode);
	                    Article.articles.remove(oldCode);
	                    Article.articles.put(icode, self);
                    }
                    editeur.setText(getCodeString());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Code (hex entre 00000000 et FFFFFFFF) : valeur incorrecte");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        
        e.ajouterChamp("Libellé", libelle, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setLibelle(valeur);
			}
        });
        
        e.ajouterChamp("Poids (kg)", String.valueOf(poids), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try {
                    setPoids(Double.valueOf(valeur));
                    editeur.setText(String.valueOf(getPoids()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Poids : valeur incorrecte (réel)");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        
        e.ajouterChamp("Largeur (m)", String.valueOf(encombrement.getLargeur()), new EditeurRunnable(){
			@Override
			public void run() throws Error {
                try {
                    encombrement.setLargeur(Double.valueOf(valeur));
                	notifierObservateurs("maj");
                    editeur.setText(String.valueOf(encombrement.getLargeur()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Largeur : valeur incorrecte");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        
        e.ajouterChamp("Longueur (m)", String.valueOf(encombrement.getLongueur()), new EditeurRunnable(){
			@Override
			public void run() throws Error {
                try {
                    encombrement.setLongueur(Double.valueOf(valeur));
                	notifierObservateurs("maj");
                    editeur.setText(String.valueOf(encombrement.getLongueur()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Longeur : valeur incorrecte");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        
        e.ajouterChamp("Hauteur (m)", String.valueOf(encombrement.getHauteur()), new EditeurRunnable(){
			@Override
			public void run() throws Error {
                try {
                    encombrement.setHauteur(Double.valueOf(valeur));
                	notifierObservateurs("maj");
                    editeur.setText(String.valueOf(encombrement.getHauteur()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Hauteur : valeur incorrecte");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        
        e.ajouterChamp("Conditionnement de stockage", conditionnementStockage, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setConditionnementStockage(valeur);
			}
        });
        
        e.ajouterChamp("Conditionnement de transport", conditionnementTransport, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setConditionnementTransport(valeur);
			}
        });
        
		JPanel vueStocks = e.ajouterChamp("Stocks");
		final JButton modifier = new JButton("Consulter/Modifier les stocks");
		modifier.setPreferredSize(new Dimension(400,26));
		vueStocks.add(modifier);
		modifier.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog editeurStocks = 
						new JDialog(fenetre, JDialog.ModalityType.DOCUMENT_MODAL);
				editeurStocks.setLocation(200, 100);
				editeurStocks.setTitle(self+" - Stocks");
		        EditeurStocks estock = new EditeurStocks(self, editeurStocks);
		        estock.setOpaque(true);
				editeurStocks.setContentPane(estock);
				editeurStocks.pack();
				editeurStocks.setVisible(true);
			}
		});

        if(getClass().equals(Article.class)){
	        // transformation en article fabriqué
	        JButton boutonFabrique = new JButton("Définir une nomenclature");
	        boutonFabrique.setPreferredSize(new Dimension(400,26));
	        boutonFabrique.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					ArticleFabrique transformation = new ArticleFabrique(self);
					for(Observateur obs: self.observateurs){
						transformation.ajouterObservateur(obs);
					}
					Application.articles.changerObjet(transformation);
					notifierObservateurs("deleted");
					transformation.notifierObservateurs("created");
					transformation.modifierNomenclature();
				}
			});
	        JPanel nom = e.ajouterChamp("Nomenclature");
	        nom.add(boutonFabrique);
       }
       return e;
    }

    @Override
    public void operationsPostChargement(int vague) {
        if(vague == 1){
            for(Entry<Integer, StockArticle> entry: _stocks.entrySet()){
                stocks.put(Application.magasins.getObjet(entry.getKey()), entry.getValue());
            }
            _stocks.clear();
            for(Entry<Magasin, StockArticle> entry: stocks.entrySet()){
				StockArticle sa = entry.getValue();
				sa.setArticle(this);
				sa.setMagasin(entry.getKey());
            }
            Article.articles.put(code, this);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(code);
        out.writeUTF(libelle);
        out.writeDouble(poids);
        out.writeObject(encombrement);
        _stocks.clear();
        for(Map.Entry<Magasin, StockArticle> entry: stocks.entrySet()){
            _stocks.put(entry.getKey().getIdCRUD(), entry.getValue());
        }
        out.writeObject(_stocks);
        _stocks.clear();
        out.writeUTF(conditionnementStockage);
        out.writeUTF(conditionnementTransport);
    }

    @SuppressWarnings("unchecked")
	@Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        code = in.readInt();
        libelle = in.readUTF();
        poids = in.readDouble();
        encombrement = (Taille) in.readObject();
        encombrement = new Taille();
        _stocks = (TreeMap<Integer, StockArticle>) in.readObject();
        conditionnementStockage = in.readUTF();
        conditionnementTransport = in.readUTF();
    }

    /**
     * utilisé lors de la désérialisation
     */
    public Article() {}

    public Article(ArticleFabrique article){
    	idCRUD = article.idCRUD;
    	statutCRUD = article.statutCRUD;
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
    
    @Override
    public void apresCreation(){
    	if(Article.articles.get(code) != null){
    		code = Article.articles.lastKey()+1;
    	}
    	articles.put(code, this);
    }

    /**
     * @param magasin Magasin où on cherche la quantité disponible
     * @return cette quantité
     */
    public int quantiteMagasin(Magasin magasin) {
        if(stocks.containsKey(magasin)){
            return stocks.get(magasin).getQuantite();
        }
        else {
            return 0;
        }
    }

	public int compareTo(Article arg0) {
		int i = libelle.compareTo(arg0.libelle);
		if(i == 0) return Integer.valueOf(idCRUD).compareTo(arg0.idCRUD);
		return i;
	}

    public int getCode() {
    	return code;
    }

    public void setCode(int code) {
        this.code = code;
        notifierObservateurs("toString");
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
        notifierObservateurs("toString");
    }

    public double getPoids() {
        return poids;
    }
    
    public void setPoids(double poids) {
        this.poids = poids > 0 ? poids : 0;
        notifierObservateurs("maj");
    }

    public Taille getEncombrement() {
        return encombrement;
    }

    public String getConditionnementStockage() {
        return conditionnementStockage;
    }

    public void setConditionnementStockage(String conditionnementStockage) {
        this.conditionnementStockage = conditionnementStockage;
        notifierObservateurs("maj");
    }

    public String getConditionnementTransport() {
        return conditionnementTransport;
    }

    public void setConditionnementTransport(String conditionnementTransport) {
        this.conditionnementTransport = conditionnementTransport;
        notifierObservateurs("maj");
    }

    public TreeMap<Magasin, StockArticle> getStocks() {
    	return stocks;
	}
}