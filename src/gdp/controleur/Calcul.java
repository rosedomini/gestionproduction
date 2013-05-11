package gdp.controleur;

import java.util.HashMap;
import java.util.Map.Entry;

import gdp.modele.ActiviteGO;
import gdp.modele.Article;
import gdp.modele.ArticleFabrique;
import gdp.modele.Commande;

/*
 * @author dom, Florent
 */
public class Calcul{
    
    private static HashMap<Article, Integer> calculerBesoins(Article article, boolean fabrique){
    	HashMap<Article, Integer> besoins = new HashMap<Article, Integer>();
    	if(article instanceof ArticleFabrique){
    		calculerBesoins(article, 1, besoins, fabrique);
    	} else if(!fabrique) {
    		besoins.put(article, 1);
    	}
    	return besoins;
    }
	
    /**
     * fonction privée récursive pour calculerBesoins(Article, fabrique)
     * @param fabrique si true, on veut savoir quels articles sont fabriqués et combien. si false,
			on veut savoir les besoins en articles non fabriqués (achetés)
     */
	private static void calculerBesoins(Article article, int qte, HashMap<Article, Integer> besoins, boolean fabrique){
		
		if(article instanceof ArticleFabrique) {
			if(fabrique){
				if(besoins.containsKey(article)){
					besoins.put(article, besoins.get(article)+qte);
				} else {
					besoins.put(article, qte);
				}
			}
			for(Entry<Article, Integer> e: ((ArticleFabrique) article).getComposants().entrySet()){
				calculerBesoins(e.getKey(), e.getValue()*qte, besoins, fabrique);
			}
		}
		// les besoins intermédiaires ne comptent que des articles élémentaires (non ArticleFabrique)
		else if(!fabrique){
			if(besoins.containsKey(article)){
				besoins.put(article, besoins.get(article)+qte);
			} else {
				besoins.put(article, qte);
			}
		}
				
	}

    /**
     * fonction de calcul des besoins intermédiaires pour un article
     * @param article article élémentaire ou à fabriquer
     * @return liste des quantités d'articles élémentaires nécessaires 
     */
    public static HashMap<Article, Integer> calculerBesoinsIntermediaires(Article article){
    	return calculerBesoins(article, false);
    }
    
    /**
     * fonction de calcul des articles à fabriquer et le nombre de fabrications à effectuer
     * @param article
     * @return
     */
    public static HashMap<Article, Integer> calculerBesoinsFabrication(Article article){
    	return calculerBesoins(article, true);
    }
	
	/**
	 * Calculer les besoins de fabrication pour une commande d'articles
	 * Les stocks seront ignorés
	 * @param cmd commande de plusieurs quantités d'articles
	 * @return liste des quantités d'articles élémentaires nécessaires 
	 */
	public static HashMap<ArticleFabrique, Integer> calculerBesoinsFabrication(Commande cmd){
		HashMap<ArticleFabrique, Integer> besoins = new HashMap<ArticleFabrique, Integer>();
		for(Entry<Article, Integer> e: cmd.getQtes().entrySet()){
			HashMap<Article, Integer> b = calculerBesoinsFabrication(e.getKey());
			for(Entry<Article, Integer> e2: b.entrySet()){
				ArticleFabrique article = (ArticleFabrique) e2.getKey();
				int qte = e.getValue()*e2.getValue();
				if(besoins.containsKey(article)){
					besoins.put(article, besoins.get(article)+qte);
				} else {
					besoins.put(article, qte);
				}
			}
		}
		return besoins;
	}
	
	/**
	 * Calculer les besoins de fabrication pour une commande d'articles
	 * On ne contera ici que les articles dont la fabrication est nécessaire
	 * Les stocks sont donc pris en compte
	 * @param cmd commande de plusieurs quantités d'articles
	 * @return liste des quantités d'articles dont la fabrication est nécessaire pour la commande
	 */
	public static HashMap<ArticleFabrique, Integer> calculerBesoinsFabricationMoinsStocks(Commande cmd){
		HashMap<ArticleFabrique, Integer> besoins = calculerBesoinsFabrication(cmd);
		@SuppressWarnings("unchecked")
		HashMap<ArticleFabrique, Integer> besoins2 = (HashMap<ArticleFabrique, Integer>) besoins.clone();
		for(Entry<ArticleFabrique, Integer> e: besoins2.entrySet()){
			ArticleFabrique a = e.getKey();
			int qte = e.getValue();
			int qteEnStock = a.getQuantiteTotale();
			if(qteEnStock >= qte){
				besoins.remove(a);
			} else {
				besoins.put(a, qte-qteEnStock);
			}
		}
		return besoins;
	}
	
	public static HashMap<ArticleFabrique, Integer> calculerBesoinsFabrication(Commande cmd, boolean soustraireStocks){
		return soustraireStocks ? calculerBesoinsFabricationMoinsStocks(cmd) : calculerBesoinsFabrication(cmd);
	}
	
	public static double calculerDelaisFabrication(ArticleFabrique article, boolean stochastique){
		double d=0;
		for(ActiviteGO act: article.getGammeOperatoire()){
			d += act.getDuree(stochastique);
		}
		return d;
	}
	
	public static double calculerDelais(Commande cmd, boolean utiliserStocks, boolean stochastique){
		double d=0;
		for(Entry<ArticleFabrique, Integer> e: calculerBesoinsFabrication(cmd, utiliserStocks).entrySet()){
			ArticleFabrique a = e.getKey();
			d += calculerDelaisFabrication(a, stochastique);
		}
		return d;
	}
	
	public static double loiExpE(double theta, double nu){
		return nu+1/theta;
	}
	
	public static double loiExpESimulee(double theta, double nu, int nbIter){
		if(nbIter < 1) return loiExpE(theta, nu);
		
		double[] U = new double[nbIter];
		for(int a=0;a<nbIter;a++){
			U[a]=Math.random();
		}
		
		double esp=0;
		for(int j=0;j<nbIter;j++){
			double x= Math.log(Math.abs(1-U[j]))/(-theta);
			esp+=x;
		}

		return nu+esp/nbIter;
	}
}
