package gdp.vue;

import gdp.controleur.Calcul;
import gdp.modele.Article;

import java.awt.BorderLayout;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * @author dom
 */
public class VueBesoins extends JPanel {
    public static final long serialVersionUID = 1L;
    
    private DefaultTableModel besoinsTM = new DefaultTableModel();
    private JTable besoinsT = new JTable(besoinsTM);
	
    public VueBesoins(Article article) {
        super(new BorderLayout());
        
        JScrollPane tableau = new JScrollPane(besoinsT);
        besoinsT.setFillsViewportHeight(true);
        besoinsTM.addColumn("Code");
        besoinsTM.addColumn("Libellé");
        besoinsTM.addColumn("Quantité");
		DefaultTableCellRenderer custom = new DefaultTableCellRenderer();
		custom.setHorizontalAlignment(JLabel.CENTER);
		for (int i=0; i<besoinsT.getColumnCount(); i++)
			besoinsT.getColumnModel().getColumn(i).setCellRenderer(custom);
		
		this.add(new JScrollPane(tableau), BorderLayout.CENTER);
        
        for(Entry<Article,Integer> e: Calcul.calculerBesoinsIntermediaires(article).entrySet()){
        	Article a = e.getKey();
        	besoinsTM.addRow(new Object[]{a.getCodeString(), a.getLibelle(), e.getValue()});
        }
    }
}