package gdp.controleur;

import gdp.vue.crud.Editeur;

import java.awt.Window;

/**
 * @author Dominic ROSE
 */
public interface Editable extends Observable {
	public Editeur editeur(Window fenetre);
}
