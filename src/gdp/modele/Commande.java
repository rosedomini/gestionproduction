
package gdp.modele;

import gdp.controleur.Application;
import gdp.controleur.Calcul;
import gdp.controleur.EditeurRunnable;
import gdp.modele.crud.CRUD;
import gdp.vue.EditeurNomenclatureCommande;
import gdp.vue.VueBesoins;
import gdp.vue.crud.Editeur;

import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * classe Commande
 *
 * Spécialise la commande dans le cadre d'un entreprise
 *
 * @author Minh Huy NGUYEN
 * @version 1.0 (29/03/2012)
 */
public class Commande extends CRUD {
	public static final long serialVersionUID = 1L;
	private String nomCom = "";
	private long numCom = 0;
	private double montant;
	private String DateOrdreCom = "jj/mm/aaaa";
	private String DateLiv = "jj/mm/aaaa";
	private String commentaires = "";
    private String client = "";
	private HashMap<Integer, Integer> _qtes = new HashMap<Integer, Integer>();
    private HashMap<Article, Integer> qtes = new HashMap<Article, Integer>();

	private ArrayList<Article> articles = new ArrayList<Article>();
    private final JComboBox cb = new JComboBox();

	public int quantiteArt(Article article) {
        if(qtes.containsKey(article)){
            return qtes.get(article);
        }
        else {
            return 0;
        }
    }
    
    public Commande() {}
	
	@Override
	public String toString(){
		return super.toString()+" "+nomCom+" de "+client+" N°"+numCom;
	}

	@Override
	public Editeur editeur(final Window fenetre) {
		final Editeur e = new Editeur(fenetre, "Commande");
		final Commande self = this;
        final JButton bSupprimer = new JButton("Supprimer");
        bSupprimer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	Object[] options = {"Oui", "Annuler"};
        		int n = JOptionPane.showOptionDialog(Application.fenetre,
            		"Supprimer la commande "+self.toString()+" ?",
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
   	 	
        e.ajouterChamp("Nom de la commande", nomCom, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setNomCom(valeur);
			}
        });
        
        e.ajouterChamp("Numéro de commande", String.valueOf(numCom), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try{
	                setNumCom(Long.valueOf(valeur));
	                editeur.setText(String.valueOf(getNumCom()));
            	}catch (NumberFormatException ex){
            		JOptionPane.showMessageDialog(e,
                            "Numéro de commande : valeur incorrecte (entier)");
                    editeur.requestFocusInWindow();
                    throw new Error();
            	}
			}
        });
        
        e.ajouterChamp("Client", client, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setClient(valeur);
			}
        });
        
        e.ajouterChamp("Date de Commande: ", DateOrdreCom, new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	setDateOrdreCom(valeur);
			}
        });
        
        e.ajouterChamp("Date de Livraison: ", DateLiv, new EditeurRunnable(){
     			@Override
     			public void run() throws Error {
                 	setDateLiv(valeur);
     			}
        });
        
        e.ajouterChamp("Montant total", String.valueOf(montant), new EditeurRunnable(){
			@Override
			public void run() throws Error {
            	try{
	                setMontant(Integer.valueOf(valeur));
	                editeur.setText(String.valueOf(getMontant()));
            	}catch (NumberFormatException ex){
            		JOptionPane.showMessageDialog(e,
                            "Montant : valeur incorrecte (entier)");
                    editeur.requestFocusInWindow();
                    throw new Error();
            	}
			}
        });
        
        e.ajouterChamp("Commentaires", commentaires, new EditeurRunnable(){
			@Override
			public void run() throws Error {
                setCommentaires(valeur);
			}
        });

        JPanel p = e.ajouterChamp("Liste des articles");
        JButton boutonArt = new JButton("Consulter/Modifier");
        boutonArt.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JDialog editeurNom = new JDialog(fenetre, JDialog.ModalityType.DOCUMENT_MODAL);
				editeurNom.setTitle("Composants de "+self);
		        EditeurNomenclatureCommande nome = new EditeurNomenclatureCommande(self);
				nome.setOpaque(true);
				editeurNom.setContentPane(nome);
				editeurNom.pack();
				editeurNom.setVisible(true);
			}
		});
        p.add(boutonArt);
        
        JPanel p1 = e.ajouterChamp("Besoins en composants élémentaires");
		majCB();
		p1.add(cb);
		JButton boutonAff = new JButton("Afficher");
		p1.add(boutonAff);
		boutonAff.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for(Article a: qtes.keySet()){
					if(a.toString().equals((String) cb.getSelectedItem())){
						JDialog vue = new JDialog(Application.fenetre, JDialog.ModalityType.DOCUMENT_MODAL);
						vue.setLocation(200,100);
						vue.setTitle(a+" - besoins en composants élémentaires");
						VueBesoins vueB = new VueBesoins(a);
						vueB.setOpaque(true);
						vue.setContentPane(vueB);
						vue.pack();
						vue.setVisible(true);
						break;
					}
				}
			}
		});

		JPanel p2 = e.ajouterChamp("Délais d'obtention (env. déterministe)");
		p2.setLayout(new GridLayout(3,1));
		JButton affDet = new JButton("Calculer");
		p2.add(affDet);
		final JLabel detL = new JLabel("En utilisant les stocks : ");
		final JLabel detL2 = new JLabel("Sans utiliser les stocks : ");
		p2.add(detL);
		p2.add(detL2);
		affDet.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				detL.setText("En utilisant les stocks : "+
						ActiviteGO.dureeToString(Calcul.calculerDelais(self, true, false)));
				detL2.setText("Sans utiliser les stocks : "+
						ActiviteGO.dureeToString(Calcul.calculerDelais(self, false, false)));
			}
		});

		JPanel p3 = e.ajouterChamp("Délais d'obtention (env. stochastique)");
		p3.setLayout(new GridLayout(3,1));
		JButton affSto = new JButton("Calculer");
		p3.add(affSto);
		final JLabel stoL = new JLabel("En utilisant les stocks : ");
		final JLabel stoL2 = new JLabel("Sans utiliser les stocks : ");
		p3.add(stoL);
		p3.add(stoL2);
		affSto.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				stoL.setText("En utilisant les stocks : "+
						ActiviteGO.dureeToString(Calcul.calculerDelais(self, true, true)));
				stoL2.setText("Sans utiliser les stocks : "+
						ActiviteGO.dureeToString(Calcul.calculerDelais(self, false, true)));
			}
		});
		
        return e;
	}
	
	private void majCB(){
		cb.removeAllItems();
		for(Article a: qtes.keySet()){
			cb.addItem(a.toString());
		}
	}

	@Override
	public void operationsPostChargement(int vague) {
		 if(vague == 1){
	            for(Integer id: _qtes.keySet()){
	                qtes.put(Application.articles.getObjet(id), _qtes.get(id));
	            }
	            _qtes.clear();
	        }
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		   super.writeExternal(out);
	        out.writeUTF(nomCom);
	        out.writeLong(numCom);
	        out.writeUTF(DateOrdreCom);
	        out.writeUTF(DateLiv);
	        out.writeDouble(montant);
	        out.writeUTF(commentaires);
	        out.writeUTF(client);
	        _qtes.clear();
	        for(Entry<Article, Integer> entry: qtes.entrySet()){
	            _qtes.put(entry.getKey().getIdCRUD(), entry.getValue());
	        }
	        out.writeObject(_qtes);
	        _qtes.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		 super.readExternal(in);
		 nomCom = in.readUTF();
		 numCom = in.readLong();
		 DateOrdreCom = in.readUTF();
		 DateLiv = in.readUTF();
		 montant = in.readDouble();
		 commentaires = in.readUTF();
		 client = in.readUTF();
		 _qtes = (HashMap<Integer, Integer>) in.readObject();
	}
	
	public String getClient() {
		return client;
	}
	
	public void setClient(String contacts) {
		this.client = contacts;
		notifierObservateurs("toString");
	}
	
	public HashMap<Article, Integer> getQtes() {
		return qtes;
	}
    
	public String getNomCom(){
		return nomCom;
	}
	
	public void setNomCom(String nomCom){
		this.nomCom = nomCom;
		notifierObservateurs("toString");
	}
	
	public long getNumCom(){
		return numCom;
	}
	
	public void setNumCom(long numCom){
		this.numCom=numCom;
		notifierObservateurs("toString");
	}
	
	public ArrayList<Article> getArticles(){
		return articles;
	}
	
	public Article getIndexArticle(int index){
		return articles.get(index);
	}
	
	public double getMontant(){
		return montant;
	}
	
	public void setMontant(double montant){
		this.montant = montant;
		notifierObservateurs("maj");
	}
	
	public String getDateOrdreCom(){
		return DateOrdreCom;
	}
	
	public void setDateOrdreCom(String DateOrdreCom){
		this.DateOrdreCom = DateOrdreCom;
		notifierObservateurs("maj");
	}
	
	public String getDateLiv(){
		return DateLiv;
	}
	
	public void setDateLiv(String DateLiv){
		this.DateLiv = DateLiv;
		notifierObservateurs("maj");
	}
	
	public String getCommentaires(){
		return commentaires;
	}
	
	public void setCommentaires(String commentaires){
		this.commentaires = commentaires;
		notifierObservateurs("maj");
	}
}