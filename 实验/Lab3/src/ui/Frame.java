package ui;

import lexical.LexicalAnalyze;
import lexical.Token;
import semantic.SemanticAnalyze;
import syntax.Grammar;
import syntax.SyntaxAnalyze;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class Frame extends JFrame {
    private final Grammar grammar = new Grammar();

    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JScrollPane jScrollPane4;
    private JScrollPane jScrollPane5;
    private JScrollPane jScrollPane6;
//    private JTable lexTable;
    private JTable errTable;
//    private JTable synTable;
    private JTable semTable; //四元式及三地址指令表
    private JTable symbolTable; //符号表
    private JButton jButton1;
    private JButton jButton2;
    private JTextArea jTextArea;

    public Frame(){
        initFrame();
    }

    private void initFrame() {
        jScrollPane1 = new JScrollPane();
        jScrollPane2 = new JScrollPane();
        jScrollPane3 = new JScrollPane();
        jScrollPane4 = new JScrollPane();
        jScrollPane5 = new JScrollPane();
        jScrollPane6 = new JScrollPane();
//        lexTable = new JTable();
        errTable = new JTable();
//        synTable = new JTable();
        semTable = new JTable();
        symbolTable = new JTable();
        jButton1 = new JButton("打开文件");
        jButton2 = new JButton("语义分析");
        jTextArea = new JTextArea();

        getContentPane().setForeground(Color.WHITE);
        setTitle("Semantic Analysis by ZhuoningGuo");    //设置显示窗口标题
        setSize(1000,860);    //设置窗口显示尺寸
        setResizable(false);    //窗口大小不可变
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);    //置窗口是否可以关闭
        getContentPane().setLayout(null);//设置为绝对定位
        // source code text area
        jScrollPane1.setBounds(10, 10, 440, 600);
        jScrollPane1.setRowHeaderView(new LineNumberHeaderView());
        getContentPane().add(jScrollPane1);
        jScrollPane1.setViewportView(jTextArea);
        jTextArea.setBackground(new Color(238, 238, 238));
        // error table
        jScrollPane2.setBounds(10, 620, 440, 190);
        getContentPane().add(jScrollPane2);
        errTable.setModel(new DefaultTableModel(new Object[][] { }, new String[] {
                "Line Number","错误信息" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        });
        jScrollPane2.setViewportView(errTable);
        // token table
//        jScrollPane3.setBounds(360, 10, 340, 800);
//        getContentPane().add(jScrollPane3);
//        lexTable.setModel(new DefaultTableModel(new Object[][] { }, new String[] {
//                "Line Number", "Symbol", "Token"}) {
//            public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
//        });
//        jScrollPane3.setViewportView(lexTable);
//        // syntax analyze tree table
//        jScrollPane4.setBounds(710, 10, 340, 800);
//        getContentPane().add(jScrollPane4);
//        synTable.setModel(new DefaultTableModel(new Object[][] { }, new String[] {"Tree" }) {
//            public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
//        });
//        jScrollPane4.setViewportView(synTable);
        // semantic analyze result
        jScrollPane5.setBounds(500, 10, 400, 500);
        getContentPane().add(jScrollPane5);
        semTable.setModel(new DefaultTableModel(new Object[][] { }, new String[] {"标号", "四元式", "三地址指令" }) {
            public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        });
        jScrollPane5.setViewportView(semTable);
        // symbol table
        jScrollPane6.setBounds(500, 520, 400, 250);
        getContentPane().add(jScrollPane6);
        symbolTable.setModel(new DefaultTableModel(new Object[][] { }, new String[] {"表号", "符号", "类型", "offset"}) {
            public boolean isCellEditable(int rowIndex, int columnIndex) { return false; }
        });
        jScrollPane6.setViewportView(symbolTable);

        jButton1.addActionListener(this::jButton1ActionPerformed);
        jButton1.setBounds(540,775, 100, 30);
        getContentPane().add(jButton1);

        jButton2.addActionListener(this::jButton2ActionPerformed);
        jButton2.setBounds(680, 775, 100, 30);
        getContentPane().add(jButton2);

        setVisible(true);    //设置窗口是否可见
    }

    /**
     * open the file
     */
    private void jButton1ActionPerformed(ActionEvent evt) {
        FileDialog fileDialog;
        File file; //An abstract representation of file and directory pathnames.
        fileDialog = new FileDialog((Frame) null, "Open", FileDialog.LOAD);
        fileDialog.setVisible(true);

        try {
            jTextArea.setText(""); //将textArea清空
            file = new File(fileDialog.getDirectory(), fileDialog.getFile());
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String aline;
            while ((aline = bufferedReader.readLine()) != null)
                jTextArea.append(aline + "\r\n");
            fileReader.close();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * analysis
     */
    private void jButton2ActionPerformed(ActionEvent evt) {
        String program = jTextArea.getText();
        // 清除原有行
//        DefaultTableModel tableModel1 = (DefaultTableModel) lexTable.getModel();
//        tableModel1.setRowCount(0);
//        lexTable.invalidate();
//        DefaultTableModel tableModel2 = (DefaultTableModel) errTable.getModel();
//        tableModel2.setRowCount(0);
//        errTable.invalidate();
//        DefaultTableModel tableModel3 = (DefaultTableModel) synTable.getModel();
//        tableModel3.setRowCount(0);
//        synTable.invalidate();
        DefaultTableModel tableModel4 = (DefaultTableModel) semTable.getModel();
        tableModel4.setRowCount(0);
        semTable.invalidate();
        DefaultTableModel tableModel5 = (DefaultTableModel) symbolTable.getModel();
        tableModel5.setRowCount(0);
        symbolTable.invalidate();

        LexicalAnalyze lexicalAnalyze = new LexicalAnalyze(program, new JTable(), errTable);
        lexicalAnalyze.analyze();
        SyntaxAnalyze syntaxAnalyze = new SyntaxAnalyze(grammar, lexicalAnalyze.getTokenList(), errTable, new JTable());
        Token root = syntaxAnalyze.analyze();
        SemanticAnalyze semanticAnalyze = new SemanticAnalyze(grammar, root, errTable, semTable, symbolTable);
        semanticAnalyze.analyze();
    }
}

class LineNumberHeaderView extends JComponent {

    /**
     * JAVA TextArea行数显示插件
     */
    private static final long serialVersionUID = 1L;
    private final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    public final Color DEFAULT_BACKGROUD = new Color(228, 228, 228);
    public final Color DEFAULT_FOREGROUD = Color.BLACK;
    public final int nHEIGHT = Integer.MAX_VALUE - 1000000;
    public final int MARGIN = 5;
    private int lineHeight;
    private int fontLineHeight;
    private int currentRowWidth;
    private FontMetrics fontMetrics;

    public LineNumberHeaderView() {
        setFont(DEFAULT_FONT);
        setForeground(DEFAULT_FOREGROUD);
        setBackground(DEFAULT_BACKGROUD);
        setPreferredSize(9999);
    }

    public void setPreferredSize(int row) {
        int width = fontMetrics.stringWidth(String.valueOf(row));
        if (currentRowWidth < width) {
            currentRowWidth = width;
            setPreferredSize(new Dimension(2 * MARGIN + width + 1, nHEIGHT));
        }
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
        fontMetrics = getFontMetrics(getFont());
        fontLineHeight = fontMetrics.getHeight();
    }

    public int getLineHeight() {
        if (lineHeight == 0) {
            return fontLineHeight;
        }
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        if (lineHeight > 0) {
            this.lineHeight = lineHeight;
        }
    }

    public int getStartOffset() {
        return 4;
    }

    @Override
    protected void paintComponent(Graphics g) {
        int nlineHeight = getLineHeight();
        int startOffset = getStartOffset();
        Rectangle drawHere = g.getClipBounds();
        g.setColor(getBackground());
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);
        g.setColor(getForeground());
        int startLineNum = (drawHere.y / nlineHeight) + 1;
        int endLineNum = startLineNum + (drawHere.height / nlineHeight);
        int start = (drawHere.y / nlineHeight) * nlineHeight + nlineHeight - startOffset;
        for (int i = startLineNum; i <= endLineNum; ++i) {
            String lineNum = String.valueOf(i);
            int width = fontMetrics.stringWidth(lineNum);
            g.drawString(lineNum + " ", MARGIN + currentRowWidth - width - 1, start);
            start += nlineHeight;
        }
        setPreferredSize(endLineNum);
    }
}