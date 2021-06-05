package syntax;

import java.util.*;

import lexical.Token;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class SyntaxAnalyze {
    private final List<Token> tokenList;  // 从词法分析器获得的所有token
    private final Grammar table;  //分析文法，构造的语法分析表
    private final List<Production> productions;
    private int indent = 0;//当前输出缩进值
    private final JTable jTable2;
    private final JTable jTable3;

    public SyntaxAnalyze(Grammar grammar, List<Token> tokenList, JTable jTable2, JTable jTable3) {
        this.jTable2 = jTable2;
        this.jTable3 = jTable3;
        this.tokenList = tokenList;
        this.replaceTokenName2GrammarSymbol();
        int last = tokenList.get(tokenList.size() - 1).getLineNumber() + 1;
        this.tokenList.add(new Token(Grammar.end, Grammar.end, last));    //最后一行加入一个$

        this.table = grammar;
        this.productions = table.getProductions();
    }

    private void printTree(Token root) {
        String output = "";
        for (int i = 0; i < indent; i++)
            output += " ";
        if (root.getTokenName().equals("id"))
            output += "id: " + root.getAttributeValue() + " (" + root.getLineNumber() + ")";
        else if (root.getTokenName().equals("num"))
            output += "digit: " + root.getAttributeValue() + " (" + root.getLineNumber() + ")";
        else
            output += root.getTokenName() + " (" + root.getLineNumber() + ")";

        DefaultTableModel tableModel = (DefaultTableModel) jTable3.getModel();
        tableModel.addRow(new Object[]{output});

        indent = indent + 4;
        List<Token> children = root.getChildren();
        Collections.reverse(children);
        for (Token child : children)
            printTree(child);
        indent = indent - 4;
    }

    /**
     * 替换Token的种别码为对应的文法单词
     */
    private void replaceTokenName2GrammarSymbol() {
        for (Token t: this.tokenList) {
            String tokenName = t.getTokenName();
            if (tokenName.equals("INT8") || tokenName.equals("INT10") || tokenName.equals("INT16"))
                t.setTokenName("num");
        }
    }

    public Token analyze() {
        int index = 0; // 语法分析进行到的位置
        Stack<Integer> stateStack = new Stack<>();  // 状态栈，用于存储相应的DFA状态号
        stateStack.push(0);  // 初始为0状态
        Stack<Token> tokenStack = new Stack<>();
        tokenStack.push(new Token(Grammar.end, Grammar.end, -1)); //顺序是种别码，值，行号
        while (true) {
            Token token = tokenList.get(index++);
            String value = token.getTokenName();
            int state = stateStack.lastElement();
            String action = table.ACTION(state, value);    //查action表
            if (action.startsWith(Grammar.shift)) {  //移入动作
                int newState = Integer.parseInt(action.substring(1));
                stateStack.push(newState);
                tokenStack.push(token);
            } else if (action.startsWith(Grammar.reduce)) { // 规约动作
                Production p = productions.get(Integer.parseInt(action.substring(1)));
                //查找对应的产生式，产生式类型由左部和右部构成
                System.out.println(p);
                int r = p.getRights().size();
                index--;
                Token temp = new Token(p.getLeft(), null, token.getLineNumber());
                if (!p.getRights().get(0).equals(Grammar.epsilon)) {
                    for (int i = 0; i < r; i++) {
                        stateStack.pop();
                        Token child = tokenStack.pop();
                        if (child.getLineNumber() < temp.getLineNumber())
                            temp.setLineNumber(child.getLineNumber());
                        temp.addChildren(child);
                    }
                }
                int s = table.GOTO(stateStack.lastElement(), p.getLeft());
                stateStack.push(s);
                tokenStack.push(temp);
            } else if (action.equals(Grammar.acc)) {
                System.out.println("Accepted");
                break;
            } else {
                DefaultTableModel tableModel = (DefaultTableModel) jTable2.getModel();
                tableModel.addRow(new Object[]{tokenList.get(index - 1).getLineNumber(),
                        "Syntax error: " + tokenList.get(index - 1) + " found an error"});
                while (action.startsWith(Grammar.reduce)) {
//                    index = index - 1;
                    Token token1 = tokenList.get(index+1);
                    tokenList.remove(token1);
                    index = index - 1;
                    String value1 = token1.getTokenName();
                    stateStack.pop();
                    tokenStack.pop();
                    if (value1.equals("")) {
                        tableModel.addRow(new Object[]{tokenList.get(index - 1).getLineNumber(),
                                "Syntax error: " + tokenList.get(index - 1) + " found an error"});
                        continue;
                    }
                    int state1 = stateStack.lastElement();
                    action = table.ACTION(state1, value1);
                }
            }
        }
        this.printTree(tokenStack.get(1)); //从语法树的根节点开始输出
        return tokenStack.get(1);
    }
}
