package gdp.modele;

import gdp.controleur.Editable;
import gdp.controleur.EditeurRunnable;
import gdp.controleur.Observateur;
import gdp.vue.crud.Editeur;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author dom, Florent
 */
public class StockArticle implements Serializable, Editable, Comparable<StockArticle>, ActionListener {
    public static final long serialVersionUID = 1L;
	
    public transient final Set<Observateur> observateurs = new HashSet<Observateur>();
    
    private int stockMin = 0;
    private int stockSecurite = 100;
    private int quantite = 0;
    private int stockMax = Integer.MAX_VALUE;
    private String allee = "Indéfinie"; // doit appartenir au magasin
    private Article article = null;
    private Magasin magasin = null;
    
    public StockArticle(){}
    
    public StockArticle(Article article, Magasin magasin){
    	this.article = article;
    	this.magasin = magasin;
    }
    
    public boolean isSecure(){
    	return quantite >= stockSecurite;
    }

	@Override
	public Editeur editeur(Window fenetre) {
        final Editeur e = new Editeur(fenetre, "Stock d'articles");
        JPanel articleP = e.ajouterChamp("Article");
        articleP.add(new JLabel(article.toString()));
        JPanel magasinP = e.ajouterChamp("Magasin");
        magasinP.add(new JLabel(magasin.toString()));
        e.ajouterChamp("Quantité en stock", String.valueOf(getQuantite()), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try {
                    if(!setQuantite(Integer.valueOf(valeur))){
                    	JOptionPane.showMessageDialog(e,
                                "Valeur incorrecte.");
                    }
                    if(!isSecure()){
                    	JOptionPane.showMessageDialog(e,
                                "Quantité critique !");
                    }
                    editeur.setText(String.valueOf(getQuantite()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Valeur incorrecte");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        e.ajouterChamp("Stock minimal", String.valueOf(getStockMin()), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try {
                    if(!setStockMin(Integer.valueOf(valeur))){
                    	JOptionPane.showMessageDialog(e,
                                "Valeur incorrecte.");
                    }
                    editeur.setText(String.valueOf(getStockMin()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Valeur incorrecte");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        e.ajouterChamp("Stock de sécurité", String.valueOf(getStockSecurite()), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try {
                    if(!setStockSecurite(Integer.valueOf(valeur))){
                    	JOptionPane.showMessageDialog(e,
                                "Valeur incorrecte.");
                    }
                    editeur.setText(String.valueOf(getStockSecurite()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Valeur incorrecte");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        e.ajouterChamp("Stock maximal", String.valueOf(getStockMax()), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try {
                    setStockMax(Integer.valueOf(valeur));
                    editeur.setText(String.valueOf(getStockMax()));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Valeur incorrecte");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        JPanel alleeP = e.ajouterChamp("Allée");
        if(magasin.getAllees().isEmpty()){
            alleeP.add(new JLabel(allee));
        } else {
        	List<String> alleesM = new ArrayList<String>();
        	alleesM.addAll(magasin.getAllees());
        	alleesM.add("Indéfinie");
        	JComboBox allees = new JComboBox(alleesM.toArray());
        	
        	alleeP.add(allees);
        	allees.setActionCommand("allee");
        	allees.addActionListener(this);
        	allees.setSelectedIndex(alleesM.indexOf(allee));
        }
		return e;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("allee")){
			JComboBox cb = (JComboBox) e.getSource();
		    String cbAllee = (String) cb.getSelectedItem();
		    allee = cbAllee;
		}
	}

	@Override
	public int compareTo(StockArticle arg0) {
		return Integer.valueOf(quantite).compareTo(arg0.quantite);
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

    public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	public Magasin getMagasin() {
		return magasin;
	}

	public void setMagasin(Magasin magasin) {
		this.magasin = magasin;
	}

	public int getStockMin() {
        return stockMin;
    }

    public boolean setStockMin(int stockMin) {
        this.stockMin = Math.min(stockMin, Math.min(quantite, stockSecurite));
    	article.notifierObservateurs("maj");
    	return this.stockMin == stockMin;
    }

    public int getStockSecurite() {
        return stockSecurite;
    }

    public boolean setStockSecurite(int stockSecurite) {
    	if(stockSecurite < stockMin) this.stockSecurite = stockMin;
    	else if(stockSecurite > stockMax) this.stockSecurite = stockMax;
    	else this.stockSecurite = stockSecurite;
    	article.notifierObservateurs("maj");
    	return this.stockSecurite == stockSecurite;
    }

    public int getQuantite() {
        return quantite;
    }

    public boolean setQuantite(int quantite) {
    	if(quantite < stockMin) this.quantite = stockMin;
    	else if(quantite > stockMax) this.quantite = stockMax;
    	else this.quantite = quantite;
    	article.notifierObservateurs("maj");
    	return this.quantite == quantite;
    }

    public int getStockMax() {
        return stockMax;
    }

    public boolean setStockMax(int stockMax) {
        this.stockMax = Math.max(stockMax, Math.min(quantite, stockSecurite));
    	article.notifierObservateurs("maj");
    	return this.stockMax == stockMax;
    }

    public String getAllee() {
        return allee;
    }

    public void setAllee(String allee) {
        this.allee = allee;
    	article.notifierObservateurs("maj");
    }
}
