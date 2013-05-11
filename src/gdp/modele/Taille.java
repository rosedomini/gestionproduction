package gdp.modele;

import java.io.Serializable;

/**
 * @author Dominic ROSE
 */
public class Taille implements Serializable {
    public static final long serialVersionUID = 1L;
    private double largeur = 0;
    private double longueur = 0;
    private double hauteur = 0;

    public Taille() {}

    public Taille(double largeur, double longueur, double hauteur) {
        this.largeur = largeur;
        this.longueur = longueur;
        this.hauteur = hauteur;
    }

    public double getLargeur() {
        return largeur;
    }

    public void setLargeur(double largeur) {
        this.largeur = largeur > 0 ? largeur : 0;
    }

    public double getLongueur() {
        return longueur;
    }

    public void setLongueur(double longueur) {
        this.longueur = longueur > 0 ? longueur : 0;
    }

    public double getHauteur() {
        return hauteur;
    }

    public void setHauteur(double hauteur) {
        this.hauteur = hauteur > 0 ? hauteur : 0;
    }
}
