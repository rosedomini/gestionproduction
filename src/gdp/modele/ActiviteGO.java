package gdp.modele;


import gdp.controleur.Application;
import gdp.controleur.Calcul;
import gdp.controleur.Editable;
import gdp.controleur.EditeurRunnable;
import gdp.controleur.Observable;
import gdp.controleur.Observateur;
import gdp.modele.crud.CRUD;
import gdp.vue.crud.Editeur;
import gdp.vue.crud.Editeur.Champ;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * @author ONE TOP
 */
public class ActiviteGO implements Serializable, Editable, ActionListener, Observable {
	public static final long serialVersionUID = 1L;
	
    public transient Set<Observateur> observateurs = new HashSet<Observateur>();
	
	/**
	 * Nombre d'itérations utilisé lors du calcul de l'estimation de la durée stochastique de l'activité
	 * Un nombre < 1 entraîne un calcul de la valeur réelle de l'espérance mathématique
	 */
	private static final int nIterDureeStochastique = 10000 /*-1*//*1*/;

	private static final String[] symboles = {"O", "↨", "♦", "[]", "**"};
	private static final String[] typeActivite = {
		"(O) Opération (usinage, assemblage,...)",
		"(↨) Flux/mouvement (marche, transport manutention...)",
		"(♦) Contrôle",
		"([]) Attente (séchage, maturation, refroidissement…)",
		"(**) Stockage"
	};

	private TypeActiviteGO type = TypeActiviteGO.OPERATION;
    private String natureTravail = "À préciser";
    private String description = "Aucune";
    private double dureeDeterministe = 0; //s
    private double theta = 0.0015;
    private double nu = 0;
    private int posteCharge; // identifiant du poste de charge
    
    private transient ArticleFabrique article = null;
    
	private transient JComboBox uniteCB;
	private transient JTextField dureeDTF;
	private transient JLabel dureeDL;
	private transient JTextField thetaTF;
	private transient JLabel thetaL;
	private transient JTextField nuTF;
	private transient JLabel nuL;
	private transient JTextArea descriptionTA;
	private transient JLabel simulationL;

	public ActiviteGO(){}
	
	public ActiviteGO(ArticleFabrique article){
		setArticle(article);
	}

    public String getSymbole() {
    	return symboles[type.ordinal()];
    }

    public double getDuree(boolean stochastique){
    	return stochastique ? getDureeStochastique() : getDureeDeterministe(); 
    }
	
	private double getDureeStochastique() {
		return Calcul.loiExpESimulee(theta, nu, nIterDureeStochastique);
	}

	@Override
	public String toString() {
		return "("+getSymbole()+") "+natureTravail+" "+getDureeDeterministeString();
	}

	private String getDureeDeterministeString() {
		return ActiviteGO.dureeToString(dureeDeterministe);
	}
	
	public static String dureeToString(double duree){
		int s = (int)duree;
		double r = duree - (double)s;
		StringBuffer d = new StringBuffer();
		if(s >= 86400){
			d.append(s/86400+"j ");
			s %= 86400;
		}
		if(s >= 3600){
			d.append(s/3600+"h ");
			s %= 3600;
		}
		if(s >= 60){
			d.append(s/60+"min ");
			s %= 60;
		}
		r += duree%60;
		r = ((double)Math.round(r*1000))/1000;
		if(r > 0 || d.toString().isEmpty()){
			d.append(r+"s");
		}
		return d.toString();
	}

	@Override
	public Editeur editeur(Window fenetre) {
		uniteCB = new JComboBox(new Object[]{"s", "min", "h", "j"});
		descriptionTA = new JTextArea();
		final Editeur e = new Editeur(fenetre, "Activité");
        JPanel typeP = e.ajouterChamp("Type");
    	JComboBox types = new JComboBox(typeActivite);
    	types.setActionCommand("type");
    	types.addActionListener(this);
    	types.setSelectedIndex(type.ordinal());
    	typeP.add(types);
    	
        e.ajouterChamp("Nature du travail", natureTravail, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setNatureTravail(valeur);
			}
        });
        
        JPanel p0 = e.ajouterChamp("Description");
        p0.setLayout(new BorderLayout());
		JScrollPane sp = new JScrollPane(descriptionTA);
		sp.setMinimumSize(new Dimension(0, 150));
        p0.add(sp, BorderLayout.CENTER);
        descriptionTA.setText(description);
        descriptionTA.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent arg0) {}
			@Override
			public void keyReleased(KeyEvent arg0) {
				setDescription(descriptionTA.getText());}
			@Override
			public void keyPressed(KeyEvent arg0) {}
		});
    	
    	JPanel p1 = e.ajouterChamp("Unité de temps");
        p1.setLayout(new FlowLayout());
		p1.add(uniteCB);
		uniteCB.setActionCommand("unité durée");
		uniteCB.addActionListener(this);

        Champ c = e.ajouterChamp("Durée (env. déterministe)", String.valueOf(dureeDeterministe), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try {
            		double t = Double.valueOf(valeur);
    				if(uniteCB.getSelectedItem().equals("min")){
    					t *= 60;
    				} else if(uniteCB.getSelectedItem().equals("h")){
    					t *= 3600;
    				} else if(uniteCB.getSelectedItem().equals("j")){
    					t *= 86400;
    				}
    				setDureeDeterministe(t);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Durée : valeur incorrecte (double)");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        dureeDTF = c.getEditeur();
    	dureeDL = c.getLecteur();

        Champ c2 = e.ajouterChamp("Θ (durée ~ loi exp.)", String.valueOf(theta), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try {
            		double t = Double.valueOf(valeur);
    				if(uniteCB.getSelectedItem().equals("min")){
    					t *= 60;
    				} else if(uniteCB.getSelectedItem().equals("h")){
    					t *= 3600;
    				} else if(uniteCB.getSelectedItem().equals("j")){
    					t *= 86400;
    				}
    				setTheta(t);
    				editeur.setText(String.valueOf(theta));
    				if(t <= 0) t = 0.1;
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "Θ : valeur incorrecte (réel strictement positif)");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        thetaTF = c2.getEditeur();
    	thetaL = c2.getLecteur();

        Champ c3 = e.ajouterChamp("ν (durée ~ loi exp.)", String.valueOf(nu), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try {
            		double t = Double.valueOf(valeur);
    				if(uniteCB.getSelectedItem().equals("min")){
    					t *= 60;
    				} else if(uniteCB.getSelectedItem().equals("h")){
    					t *= 3600;
    				} else if(uniteCB.getSelectedItem().equals("j")){
    					t *= 86400;
    				}
    				setNu(t);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(e,
                            "ν : valeur incorrecte (réel)");
                    editeur.requestFocusInWindow();
                    throw new Error();
                }
			}
        });
        nuTF = c3.getEditeur();
    	nuL = c3.getLecteur();
    	
    	JPanel p5 = e.ajouterChamp("Durée estimée");
    	JButton bCalcul = new JButton("Calculer");
    	simulationL = new JLabel();
    	p5.add(bCalcul);
    	p5.add(simulationL);
    	bCalcul.setActionCommand("calculer");
    	bCalcul.addActionListener(this);
    	
    	JComboBox CBposteCharge = new JComboBox(Application.posteCharges.values().toArray());
    	JPanel p4 = e.ajouterChamp("Poste de charge utilisé");
    	p4.add(CBposteCharge);
    	CBposteCharge.setSelectedItem(Application.posteCharges.getObjet(posteCharge));
    	CBposteCharge.setActionCommand("poste de charge");
    	CBposteCharge.addActionListener(this);
    	
		return e;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals("type")){
			JComboBox cb = (JComboBox) e.getSource();
		    setType(TypeActiviteGO.values()[cb.getSelectedIndex()]);
		} else if(cmd.equals("unité durée")){
			JComboBox cb = (JComboBox) e.getSource();
		    String u = (String) cb.getSelectedItem();
		    double coef = 1.0;
		    if(u.equals("min")) coef /= 60;
			else if(u.equals("h")) coef /= 3600;
			else if(u.equals("j")) coef /= 84600;
			dureeDTF.setText(String.valueOf(dureeDeterministe*coef));
			dureeDL.setText(dureeDTF.getText());
			thetaTF.setText(String.valueOf(theta*coef));
			thetaL.setText(thetaTF.getText());
			nuTF.setText(String.valueOf(nu*coef));
			nuL.setText(nuTF.getText());
		} else if(cmd.equals("poste de charge")){
			JComboBox cb = (JComboBox) e.getSource();
		    setPosteCharge(((CRUD) cb.getSelectedItem()).getIdCRUD());
		} else if(cmd.equals("calculer")){
			simulationL.setText(ActiviteGO.dureeToString(
					Calcul.loiExpESimulee(theta, nu, nIterDureeStochastique)));
		}
	}

	public TypeActiviteGO getType() {
		return type;
	}

	public void setType(TypeActiviteGO type) {
		this.type = type;
    	notifierObservateurs("toString");
	}

	public String getNatureTravail() {
		return natureTravail;
	}

	public void setNatureTravail(String natureTravail) {
		this.natureTravail = natureTravail;
    	notifierObservateurs("toString");
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
    	notifierObservateurs("maj");
	}

	public double getDureeDeterministe() {
		return dureeDeterministe;
	}

	public void setDureeDeterministe(double dureeDeterministe) {
		this.dureeDeterministe = dureeDeterministe;
    	notifierObservateurs("toString");
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		this.theta = theta <= 0 ? 1E-6 : theta;
    	notifierObservateurs("maj");
	}

	public double getNu() {
		return nu;
	}

	public void setNu(double nu) {
		this.nu = nu;
    	notifierObservateurs("maj");
	}

	public ArticleFabrique getArticle() {
		return article;
	}

	public void setArticle(ArticleFabrique article) {
		this.article = article;
		ajouterObservateur(article);
    	notifierObservateurs("article");
	}
	
	public int getPosteCharge() {
		return posteCharge;
	}

	public void setPosteCharge(int posteCharge) {
		this.posteCharge = posteCharge;
    	notifierObservateurs("posteCharge");
	}

	@Override
	public void ajouterObservateur(Observateur observateur) {
		if(observateurs == null){
			observateurs = new HashSet<Observateur>();
		}
		observateurs.add(observateur);
	}
	
	@Override
	public void supprimerObservateur(Observateur observateur) {
		if(observateurs != null){
			observateurs.remove(observateur);
		}
	}
	
	@Override
	public void notifierObservateurs(String notification) {
		if(observateurs != null){
			for(Observateur observateur: observateurs){
				observateur.onNotification(this, notification);
			}
		}
	}
}