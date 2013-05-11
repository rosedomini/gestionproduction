package gdp.vue;

import gdp.controleur.Application;
import gdp.modele.Article;
import gdp.modele.Magasin;
import gdp.modele.StockArticle;
import gdp.modele.crud.CRUD;
import gdp.vue.crud.Editeur;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Permet de modifier la liste des stocks d'un article
 * @author dom
 *
 */
public class EditeurStocks extends JPanel implements ActionListener {
    public static final long serialVersionUID = 1L;
    
    private static String CREATE_COMMAND = "create"; // créer un stock
    private static String DEL_COMMAND = "delete"; // supprimer un stock
    
    // tous les magasins
    private DefaultListModel magLM = new DefaultListModel();
    private JList magL = new JList(magLM);
    private List<Magasin> mags = new ArrayList<Magasin>();
    
    // article dont on veut modifier les stocks
    private Article article;
    private Magasin mag;
    
	private JPanel listP = new JPanel(new BorderLayout());
	private JPanel emptyEditP = new JPanel();
	JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listP, emptyEditP);
	
	private JButton createButton = new JButton("Créer un stock");
    private JButton delButton = new JButton("Supprimer");
    private JDialog fenetre;

    public EditeurStocks(Article article, JDialog fenetre) {
    	super(new BorderLayout());
    	this.fenetre = fenetre;
    	fenetre.setMinimumSize(new Dimension(800, 600));
    	this.article = article;

        sp.setOneTouchExpandable(true);
    	add(sp, BorderLayout.CENTER);
    	
        createButton.setEnabled(false);
        createButton.setActionCommand(CREATE_COMMAND);
        createButton.addActionListener(this);
        
        delButton.setEnabled(false);
        delButton.setActionCommand(DEL_COMMAND);
        delButton.addActionListener(this);
    	
        JScrollPane spML = new JScrollPane(magL);
        listP.add(spML, BorderLayout.CENTER);
        JPanel bP = new JPanel(new GridLayout(2,1));
        bP.add(createButton);
        bP.add(delButton); 
        listP.add(bP, BorderLayout.SOUTH);
        
        // tous les magasins
    	mags.addAll(Application.magasins.values());
    	// vont dans la liste
    	for(CRUD m: mags){
    		if(article.getStocks().containsKey(m)){
        		magLM.addElement(m.toString()+" ("+article.getStocks().get(m).getQuantite()+")");
    		} else {
    			magLM.addElement(m.toString());
    		}
    	}

    	final EditeurStocks self = this;
    	magL.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				self.mag = (Magasin) mags.get(magL.getSelectedIndex());
				if(self.getArticle().getStocks().containsKey(self.mag)){
		        	StockArticle sa = self.getArticle().getStocks().get(mag);
		        	Editeur editeur = sa.editeur(self.fenetre);
		        	editeur.generer();
		        	sp.setRightComponent(editeur);
			        createButton.setEnabled(false);
			        delButton.setEnabled(true);
				} else {
			        createButton.setEnabled(true);
			        delButton.setEnabled(false);
		        	sp.setRightComponent(emptyEditP);
				}
			}
		});
    }
    
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (CREATE_COMMAND.equals(command)) {
        	StockArticle sa = new StockArticle(article, mag);
        	Editeur editeur = sa.editeur(fenetre);
        	editeur.generer();
        	sp.setRightComponent(editeur);
        	fenetre.pack();
        	magLM.setElementAt(mag.toString()+" (0)", magL.getSelectedIndex());
        	article.notifierObservateurs("maj");
        	article.getStocks().put(mag, sa);
	        createButton.setEnabled(false);
	        delButton.setEnabled(true);
        } else if (DEL_COMMAND.equals(command)) {
        	article.notifierObservateurs("maj");
        	article.getStocks().remove(mag);
        	magLM.setElementAt(mag.toString(), magL.getSelectedIndex());
        	sp.setRightComponent(emptyEditP);
	        createButton.setEnabled(true);
	        delButton.setEnabled(false);
        }
    }

	public Article getArticle() {
		return article;
	}
}
