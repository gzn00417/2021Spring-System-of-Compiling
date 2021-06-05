package cn.model.grammar;

import com.sun.deploy.util.StringUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * created by meizhimin on 2021/5/3
 */
public class Syntax_rule extends JFrame {
    private static final long serialVersionUID = -1827909598853481778L;

    private Grammar grammar;
    private HashMap<String, Integer>[] gotoTable;
    private HashMap<String, Action>[] actionTable;

    //GUI相关
    private DefaultTableModel firstFollowModel;
    private DefaultTableModel actionModel;
    private DefaultTableModel gotoModel;
    private JPanel panel;

    public Syntax_rule(Grammar grammar, HashMap<String, Integer>[] gotoTable, HashMap<String, Action>[] actionTable) {
        this.grammar = grammar;
        this.gotoTable = gotoTable;
        this.actionTable = actionTable;
        init();
    }

    public void init(){
        panel = (JPanel)getContentPane();
        setTitle("语法规则");
        setBounds(100, 100,1500,900);
        Dimension screensize   =   Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize=getSize();
        setLocation((screensize.width-frameSize.width)/2,(screensize.height-frameSize.height)/2);
        getContentPane().setLayout(null);

        // first和follow表
        firstFollowModel = new DefaultTableModel(
                new Object[][] {},
                new String[] {
                        "产生式","FIRST集","FOLLOW集"
                }
        );

        JTable firstFollowTable = new JTable(){
            public boolean getScrollableTracksViewportWidth()
            {
                return getPreferredSize().width < getParent().getWidth();
            }
        };
        firstFollowTable.setFillsViewportHeight(true);
        firstFollowTable.setModel(firstFollowModel);
        firstFollowTable.setBackground(new Color(220, 245, 240));
        firstFollowTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane firstFollowSP = new JScrollPane();
        firstFollowSP.setViewportView(firstFollowTable);
        firstFollowSP.setBounds(10, 77, 480, 765);
        panel.add(firstFollowSP);

        JLabel firstFollowLabel = new JLabel("first集和follow集");
        firstFollowLabel.setFont(new Font("宋体", Font.BOLD, 20));

        firstFollowLabel.setBounds(160, 30, 285, 47);
        panel.add(firstFollowLabel);
        firstFollowTable.setRowHeight(18);
        firstFollowTable.setFont(new Font("楷体", Font.PLAIN, 18));
        addFirstFollow();

        for (int column = 0; column < firstFollowTable.getColumnCount(); column++)
        {
            TableColumn tableColumn = firstFollowTable.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();

            for (int row = 0; row < firstFollowTable.getRowCount(); row++)
            {
                TableCellRenderer cellRenderer = firstFollowTable.getCellRenderer(row, column);
                Component c = firstFollowTable.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + firstFollowTable.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);

                //  We've exceeded the maximum width, no need to check other rows

                if (preferredWidth >= maxWidth)
                {
                    preferredWidth = maxWidth;
                    break;
                }
            }

            tableColumn.setPreferredWidth( preferredWidth );
        }

        // action表
        List<String> actionCols = new ArrayList<>();
        actionCols.add("状态");
        actionCols.add("$");
        actionCols.addAll(grammar.getTerminals());

        actionModel = new DefaultTableModel(
                new Object[][] {},
                actionCols.toArray(new String[]{})
        );

        JTable actionJTable = new JTable();
        actionJTable.setFillsViewportHeight(true);
        actionJTable.setModel(actionModel);
        actionJTable.setBackground(new Color(220, 245, 240));
        actionJTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane actionSP = new JScrollPane();
        actionSP.setViewportView(actionJTable);
        actionSP.setBounds(500, 77, 480, 765);
        panel.add(actionSP);

        JLabel actionLabel = new JLabel("action表");
        actionLabel.setFont(new Font("宋体", Font.BOLD, 20));
        actionLabel.setBounds(680, 30, 285, 47);
        panel.add(actionLabel);
        addAction();

        // goto表
        List<String> gotoCols = new ArrayList<>();
        gotoCols.add("状态");
        gotoCols.addAll(grammar.getNonterminals());
        gotoModel = new DefaultTableModel(
                new Object[][] {},
                gotoCols.toArray(new String[]{})
        );

        JTable gotoJTable = new JTable();
        gotoJTable.setFillsViewportHeight(true);
        gotoJTable.setModel(gotoModel);
        gotoJTable.setBackground(new Color(220, 245, 240));
        gotoJTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane gotoSP = new JScrollPane();
        gotoSP.setViewportView(gotoJTable);
        gotoSP.setBounds(990, 77, 480, 765);
        panel.add(gotoSP);

        JLabel gotoLabel = new JLabel("goto表");
        gotoLabel.setFont(new Font("宋体", Font.BOLD, 20));
        gotoLabel.setBounds(1170, 30, 285, 47);
        panel.add(gotoLabel);
        addGoto();
    }

    public void addFirstFollow(){
        List<String> nonterminals = grammar.getNonterminals();
        List<Production> productions = grammar.getProductions();
        for(String s:nonterminals){
            Object[] row = new Object[3];

            // add production
            StringBuilder productionSB = new StringBuilder();
            productionSB.append(s + "->");
            for(Production production:productions){
                if(production.getLeft().equals(s)){
                    for(int i=0;i<production.getRights().length;i++){
                        productionSB.append(production.getRights()[i]);
                        if(i!=production.getRights().length-1){
                            productionSB.append(" ");
                        }
                    }
                    productionSB.append("|");
                }
            }
            productionSB.deleteCharAt(productionSB.length()-1);
            row[0] = productionSB.toString();

            // add first
            StringBuilder firstSB = new StringBuilder();
            firstSB.append("FIRST(" + s + ")={ ");
            firstSB.append(StringUtils.join(grammar.getFirsts().get(s), " "));
            firstSB.append(" }");
            row[1] = firstSB.toString();

            //add follow
            StringBuilder followSB = new StringBuilder();
            followSB.append("FOLLOW(" + s + ")={ ");
            followSB.append(StringUtils.join(grammar.getFollows().get(s), " "));
            followSB.append(" }");
            row[2] = followSB.toString();
            firstFollowModel.addRow(row);
        }
    }
    public void addAction(){
        List<String> terminals = new ArrayList<>();
        terminals.add("$");
        terminals.addAll(grammar.getTerminals());
        int cols = terminals.size() + 1;
        for(int i=0;i<actionTable.length;i++){
            Object[] row = new Object[cols];
            row[0] = i;
            for(int j=1;j<cols;j++){
                if(actionTable[i].get(terminals.get(j-1)) == null){
                    row[j] = " ";
                    continue;
                }
                Action action = actionTable[i].get(terminals.get(j-1));
                switch (action.getType()){
                    case SHIFT:
                        row[j] = "s" + action.getOperand();
                        break;
                    case REDUCE:
                        row[j] = "r" + action.getOperand();
                        break;
                    case ACCEPT:
                        row[j] = "acc";
                        break;
                }
            }
            actionModel.addRow(row);
        }
    }

    public void addGoto(){
        List<String> nonterminals = grammar.getNonterminals();
        int cols = nonterminals.size() + 1;
        for(int i=0;i<gotoTable.length;i++){
            Object[] row= new Object[cols];
            row[0] = i;
            for(int j=1;j<cols;j++){
                row[j] = gotoTable[i].get(nonterminals.get(j-1)) != null?gotoTable[i].get(nonterminals.get(j-1)):" ";
            }
            gotoModel.addRow(row);
        }
    }
}
