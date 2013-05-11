package gdp.vue;

import gdp.controleur.Application;
import gdp.modele.Article;
import gdp.modele.Commande;
import gdp.modele.crud.CRUD;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;


/**
 * Permet de modifier la liste des composants d'un article (et non tout l'arbre de sa nomenclature)
 * @author dom
 *
 */
public class EditeurNomenclatureCommande extends JPanel implements ActionListener {
    public static final long serialVersionUID = 1L;
    
    private static String ADD_COMMAND = "add"; // ajouter un composant
    private static String REMOVE_COMMAND = "remove"; // supprimer un composant
    private static String CLEAR_COMMAND = "clear"; // vider la liste des composants
    
    // tous les articles
    private DefaultListModel articlesLM = new DefaultListModel();
    private JList articlesL = new JList(articlesLM);
    private List<Article> articles = new ArrayList<Article>();
    
    // tous les composants
    private DefaultTableModel compoTM = new DefaultTableModel();
    private JTable compoT = new JTable(compoTM); // Article ; Quantité
    private ArrayList<Article> composantsT = new ArrayList<Article>();
    private HashMap<Article, Integer> composants;
    
    // article dont on veut modifier la liste de composants
    private Commande cmd;

    public EditeurNomenclatureCommande(Commande _cmd) {
        super(new BorderLayout());
    	
        JScrollPane spCompo = new JScrollPane(compoT);
        compoT.setFillsViewportHeight(true);
        compoTM.addColumn("Article");
        compoTM.addColumn("Quantité");
		DefaultTableCellRenderer custom = new DefaultTableCellRenderer();
		custom.setHorizontalAlignment(JLabel.CENTER);
		for (int i=0; i<compoT.getColumnCount(); i++)
			compoT.getColumnModel().getColumn(i).setCellRenderer(custom);

        JButton addButton = new JButton(">>");
        addButton.setActionCommand(ADD_COMMAND);
        addButton.addActionListener(this);
        
        JButton removeButton = new JButton("<<");
        removeButton.setActionCommand(REMOVE_COMMAND);
        removeButton.addActionListener(this);
        
        JButton clearButton = new JButton("Vider");
        clearButton.setActionCommand(CLEAR_COMMAND);
        clearButton.addActionListener(this);

        add(spCompo, BorderLayout.CENTER);
    	
        JScrollPane spAL = new JScrollPane(articlesL);
        add(spAL, BorderLayout.WEST);

        JPanel panel = new JPanel(new GridLayout(0,3));
        panel.add(addButton);
        panel.add(removeButton); 
        panel.add(clearButton);
        add(panel, BorderLayout.SOUTH);

    	this.cmd = _cmd;
    	composants = cmd.getQtes();
        
        // tous les articles
    	articles.addAll(Application.articles.values());
    	// sauf ceux déjà contenus dans la commande
    	articles.removeAll(composants.keySet());
    	// vont dans la liste de gauche
    	for(CRUD a: articles){
    		articlesLM.addElement(a.toString());
    	}
    	

    	// les composants et leurs quantités vont dans le tableau à droite
    	for(Entry<Article, Integer> e: composants.entrySet()){
    		Article a = e.getKey();
    		int quantite = e.getValue();
            compoTM.addRow(new Object[]{a.toString(), String.valueOf(quantite)});
            composantsT.add(a);
    	}
    	
    	compoTM.addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent arg0) {
				cmd.notifierObservateurs("maj");
				int l = compoTM.getRowCount();
				for(int i=0; i<l; i++){
					String s = (String) compoTM.getValueAt(i, 1);
					composants.put(
							(Article) composantsT.get(i), Integer.parseInt(s));
				}
			}
		});
    }
    
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if (ADD_COMMAND.equals(command)) {
        	int i = articlesL.getSelectedIndex();
        	if(i == -1){
                JOptionPane.showMessageDialog(this,
                        "Aucun article selectionné",
                        "",
                        JOptionPane.WARNING_MESSAGE);
        	} else {
	        	Article a = (Article) articles.get(i);
        		articles.remove(i);
        		articlesLM.remove(i);
	            composantsT.add(a);
	            composants.put(a, 1);
	            cmd.notifierObservateurs("maj");
	            compoTM.addRow(new Object[]{a.toString(), "1"});
        	}
        } else if (REMOVE_COMMAND.equals(command)) {
        	int i = compoT.getSelectedRow();
        	if(i == -1){
                JOptionPane.showMessageDialog(this,
                        "Aucun article de la liste des composants n'est selectionné",
                        "",
                        JOptionPane.WARNING_MESSAGE);
        	} else {
        		compoTM.removeRow(i);
        		Article a = composantsT.get(i);
        		composantsT.remove(i);
        		articles.add(a);
        		articlesLM.addElement(a.toString());
        		composants.remove(a);
	            cmd.notifierObservateurs("maj");
        	}
        } else if (CLEAR_COMMAND.equals(command)) {
            cmd.notifierObservateurs("maj");
        	articlesLM.clear();
        	articles.clear();
        	articles.addAll(Application.articles.values());
        	for(CRUD a: articles){
        		articlesLM.addElement(a.toString());
        	}
            int l = compoTM.getRowCount();
        	for(int i=0; i < l; i++){
        		compoTM.removeRow(0);
        		composants.remove(composantsT.get(0));
        		composantsT.remove(0);
        	}
        	composantsT.clear();
        	composants.clear();
        }
    }
}
