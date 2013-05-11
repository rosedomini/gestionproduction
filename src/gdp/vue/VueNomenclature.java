package gdp.vue;

import gdp.modele.Article;
import gdp.modele.ArticleFabrique;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.util.Map.Entry;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * @author Chris
 */

public class VueNomenclature extends JPanel {
    public static final long serialVersionUID = 1L;
    
    private DefaultTableModel TMcompo = new DefaultTableModel();
    private JTable Tcompo = new JTable(TMcompo);
    private int niveauMax;
    private ArticleFabrique articleFab;
    
	private JButton change = new JButton("Afficher plus de niveaux");
	private JButton retablir = new JButton("Rétablir");
	
	public void afficherNomenclature(Article article, int niveau, int qte){
		afficherLigneNomenclature(article, niveau, qte);
		if(article instanceof ArticleFabrique && niveau < niveauMax) {
			ArticleFabrique af = (ArticleFabrique) article;
			for(Entry<Article, Integer> e: af.getComposants().entrySet()){
				Article composant = e.getKey();
				int qteC = e.getValue();
				afficherNomenclature(composant, niveau+1, qteC);
			}
		}
	}
	
	private void afficherLigneNomenclature(Article article, int niveau, int qte){
		StringBuffer n = new StringBuffer();
		for (int i = 0; i < niveau; i++)
			n.append(".");
		TMcompo.addRow(new Object[]{n.toString()+niveau, article.getCodeString(), article.getLibelle(), qte});
	}
	
    public VueNomenclature(ArticleFabrique articleFab, int niveauMax) {
        super(new BorderLayout());
        this.articleFab = articleFab;
        this.niveauMax = niveauMax;
        
        JScrollPane tableau = new JScrollPane(Tcompo);
        Tcompo.setFillsViewportHeight(true);
        TMcompo.addColumn("Niveau");
        TMcompo.addColumn("Code");
        TMcompo.addColumn("Libellé");
        TMcompo.addColumn("Quantité");
		DefaultTableCellRenderer custom = new DefaultTableCellRenderer();
		custom.setHorizontalAlignment(JLabel.CENTER);
		for (int i=1; i<Tcompo.getColumnCount(); i++)
			Tcompo.getColumnModel().getColumn(i).setCellRenderer(custom);

		JPanel pan = new JPanel();
		pan.add(change);
		pan.add(retablir);
		
		final VueNomenclature self = this;
		change.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viderTable();
				self.niveauMax++;
				afficherNomenclature(self.articleFab, 0, 1);
			}
		});
		
		retablir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				viderTable();
				self.niveauMax = 1;
				afficherNomenclature(self.articleFab, 0, 1);
			}
		});
		
		this.add(new JScrollPane(tableau), BorderLayout.CENTER);
		this.add(pan, BorderLayout.SOUTH);
        
        afficherNomenclature(articleFab, 0, 1);
    }
    
    private void viderTable(){
        int l = TMcompo.getRowCount();
    	for(int i=0; i < l; i++){
    		TMcompo.removeRow(0);
    	}
    }
}