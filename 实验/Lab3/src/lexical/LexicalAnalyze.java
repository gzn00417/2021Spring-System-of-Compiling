package lexical;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexicalAnalyze {

    public static final String[] keywords = {"proc", "record", "integer", "real",
            "if", "else", "then", "while", "do", "call", "true", "false", "begin", "end"};
    public static final char[] operator = { '+', '-', '*', '%', '=', '<', '>', '!', '&', '|'};
    public static final char[] delimiter = { ',', ';', '[', ']', '.', '(', ')'};
    public static final Map<String, String> doubleOperatorMap = new HashMap<String, String>() {
        {
            put("!=", "!="); put("==", "==");
            put("<=", "<="); put(">=", ">=");
            put("&&", "&&"); put("||", "||");
            put("++", "++"); put("--", "--");
            put("+=", "+="); put("-=", "-=");
            put("*=", "*="); put("/=", "/=");
        }
    };
    public static final Map<String, String> delimiterMap = new HashMap<String, String>() {
        {
            put(".", ".");
            put(",", ","); put(";", ";");
            put("[", "["); put("]", "]");
            put("(", "("); put(")", ")");
            put("{", "{"); put("}", "}");
        }
    };

    private final String text;
    private final JTable jTable1;
    private final JTable jTable2;
    private int symbol_pos = 0;//记录符号表位置
    private final Map<String, Integer> symbol = new HashMap<>(); //符号表HashMap
    private final List<Token> tokenList = new ArrayList<>();

    public LexicalAnalyze(String text) {
        this.text = text;
        this.jTable1 = new JTable();
        this.jTable2 = new JTable();
    }

    public LexicalAnalyze(String text, JTable jTable1, JTable jTable2) {
        this.text = text;
        this.jTable1 = jTable1;
        this.jTable2 = jTable2;
    }

    public List<Token> getTokenList() {
        return tokenList;
    }

    public void analyze() {
        String[] texts = text.split("\n");
        symbol.clear();
        symbol_pos=0;
        for (int i=0; i<texts.length; i++) {
            String line = texts[i];
            if (line.equals("")) continue;

            char[] lineChars = line.toCharArray(); // 行字符数组
            for (int j=0; j<lineChars.length; j++) {
                char c = lineChars[j];
                if (isLettersOrUnderscores(c))  // 可能是关键字或者标识符
                    j = this.keywordOrIdentiferProcess(lineChars, j, i);
                else if (isOperator(c) || isDelimiter(c))  //识别运算符或界符
                    j = this.operatorOrDelimiterProcess(lineChars, j, i);
                else if (isDigit(c))  // 数字常量，识别无符号整数、浮点数
                    j = this.numberProcess(lineChars, j, i);
                else if (c == '"')  //识别字符串常量
                    j = this.stringProcess(lineChars, j, i);
                else if (c == '\'')
                    j = this.charProcess(lineChars, j, i);
                else if (c == '/') { // 识别注释
                    int[] res = this.commentProcess(texts, j, i);
                    i = res[0];
                    j = res[1];
                } // 识别注释
                else { //非法字符
                    if(c != ' ' && c != '\t' && c != '\0' && c != '\n' && c != '\r') {
                        DefaultTableModel tableModel2 = (DefaultTableModel) jTable2.getModel();
                        tableModel2.addRow(new Object[] {i+1, "存在非法字符 '" + c + "'"});
                        jTable2.invalidate();
                    }
                } //非法字符
            }
        }
    }

    private int keywordOrIdentiferProcess(char[] lineChars, int col, int row) {
        StringBuilder sb = new StringBuilder(); //初始化token
        char c = lineChars[col];
        while (col<lineChars.length-1 && (isLettersOrUnderscores(c) || isDigit(c))) {
            sb.append(c);
            c =lineChars[++col];
        }
        col--;
        String token = sb.toString();

        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        Token t;
        if (isKeyword(token)) // 关键字
            t = new Token(token, token, row+1);
        else { //标识符，不在符号表里的时候要先放入符号表
            if (symbol.isEmpty() || (!symbol.containsKey(token)))
                symbol.put(token, symbol_pos++);
            t = new Token("id", token, row+1);
        }
        tableModel.addRow(new Object[] {row + 1, token, t.toString()});
        tokenList.add(t);
        jTable1.invalidate();
        return col;
    }

    private int operatorOrDelimiterProcess(char[] lineChars, int col, int row) {
        StringBuilder sb = new StringBuilder(); //初始化token
        char c = lineChars[col];
        sb.append(c);
        if (isPlusEqu(c)) { //后面可以用一个"="
            c = lineChars[++col];
            if (c == '=') sb.append(c);
            else if (isPlusSame(lineChars[col - 1]) && c == lineChars[col - 1]) //后面可以用一个和自己一样的
                sb.append(c);
            else --col;
        }
        String token = sb.toString();

        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        Token t;
        if (token.length() == 1 && isDelimiter(token.charAt(0))) //判断是否为界符
            t = new Token(delimiterMap.get(token), token, row+1);
        else if (token.length() == 1) // 长度为1的运算符
            t = new Token(token, token, row+1);
        else t = new Token(doubleOperatorMap.get(token), token, row+1);
        tableModel.addRow(new Object[] {row + 1, token, t.toString()});
        tokenList.add(t);
        jTable1.invalidate();
        return col;
    }

    private int numberProcess(char[] lineChars, int col, int row) {
        StringBuilder sb = new StringBuilder();
        char c = lineChars[col];
        int state = 0; //初始化进入1状态
        boolean isFloat=false, isHexOrOctol=false, haveMistake = false;

        if((c == '0' && isDelimiter(lineChars[col+1])) || (c >= '1' && c <= '9')) { // 十进制无符号数或浮点数
            while ((c != '\r') && (isDigit(c) || c == '.' || c == 'e' || c == 'E'
                    || ((c == '-' || c == '+') && (lineChars[col-1]=='e' || lineChars[col-1]=='E')))) {
                if (c == '.' || c == 'e' || c == 'E')
                    isFloat = true;

                state = DFA.digitDFA(c, state);
                if (state == -1) {
                    haveMistake = true;
                    break;
                }
                sb.append(c);
                col++;//遍历符号先前移动
                if (col >= lineChars.length) break;
                c = lineChars[col];
            }
            if (state != 1 && state != 3 && state != 6)
                haveMistake = true;
        } else {
            isHexOrOctol = true;
            while (!isDelimiter(c)) {
                state = DFA.hexAndOctDFA(c, state);
                if (state == -1 || state == 5 || state == 6) {
                    haveMistake = true;
                    break;
                }

                sb.append(c);
                col++;//遍历符号先前移动
                if (col >= lineChars.length) break;
                c = lineChars[col];
            }
        }

        if ((!isOperator(c) && !isDelimiter(c)) && isDigit(c) || c== '.') //数字之后必须是运算符或者界符
            haveMistake = true;
        if (haveMistake) { //错误处理
            //一直到“可分割”的字符结束
            while (c != '\0' && c != ',' && c != ';' && c != ' ') {
                sb.append(c);
                col++;
                if(col >= lineChars.length) break;
                c = lineChars[col];
            }
            String message = " 确认无符号常数输入正确";
            if (isFloat) message = " 确认浮点数常数输入正确";
            DefaultTableModel tableModel2 = (DefaultTableModel) jTable2.getModel();
            tableModel2.addRow(new Object[] {row+1, sb + message});
            jTable2.invalidate();
        }
        else {
            DefaultTableModel tableModel1 = (DefaultTableModel) jTable1.getModel();
            Token t;
            if (isFloat) t = new Token("FLOAT", sb.toString(), row+1);
            else if (isHexOrOctol && state == 3) t = new Token("INT16", sb.toString(), row+1);
            else if (isHexOrOctol && state == 4) t = new Token("INT8", sb.toString(), row+1);
            else t = new Token("INT10", sb.toString(), row+1);
            tableModel1.addRow(new Object[] {row+1, sb.toString(), t.toString()});
            tokenList.add(t);
            jTable1.invalidate();
        }
        col--;
        return col;
    }

    private int stringProcess(char[] lineChars, int col, int row) {
        StringBuilder sb = new StringBuilder(); //初始化token
        char c = lineChars[col];
        boolean haveMistake = false;
        int state = 0;
        while (state != 3) {
            state = DFA.stringDFA(c, state);
            if (state == -1) {
                haveMistake = true;
                break;
            }
            sb.append(c);

            col++;
            if (col >= lineChars.length-1) {
                haveMistake = true;
                break;
            }
            c = lineChars[col];
            if (c == '\0') {
                haveMistake = true;
                break;
            }
        }
        if (haveMistake) {
            DefaultTableModel tableModel2 = (DefaultTableModel) jTable2.getModel();
            tableModel2.addRow(new Object[] {row+1, sb + " 字符串常量引号未封闭"});
            jTable2.invalidate();
            col--;
        }
        else {
            DefaultTableModel tableModel1 = (DefaultTableModel) jTable1.getModel();
            Token t = new Token("STR", sb.toString(), row+1);
            tableModel1.addRow(new Object[] {row+1, sb.toString(), t.toString()});
            tokenList.add(t);
            jTable1.invalidate();
        }
        return col;
    }

    private int charProcess(char[] lineChars, int col, int row) {
        char c = lineChars[col];
        StringBuilder sb = new StringBuilder();
        sb.append(c);
        boolean haveMistake = false;
        c = lineChars[++col];
        if (c == '\\') {
            sb.append(c);
            if (++col > lineChars.length-1) {
                haveMistake = true;
            } else c = lineChars[col];
        }
        sb.append(c);
        if (++col > lineChars.length-1) {
            haveMistake = true;
        } else c = lineChars[col];
        if (c == '\'')
            sb.append(c);
        else haveMistake = true;
        if (haveMistake) {
            DefaultTableModel tableModel2 = (DefaultTableModel) jTable2.getModel();
            tableModel2.addRow(new Object[] {row+1, sb + " 字符常量引号未封闭"});
            jTable2.invalidate();
            col--;
        } else {
            DefaultTableModel tableModel1 = (DefaultTableModel) jTable1.getModel();
            Token t = new Token("CHAR", sb.toString(), row+1);
            tableModel1.addRow(new Object[] {row+1, sb.toString(), t.toString()});
            tokenList.add(t);
            jTable1.invalidate();
        }
        return col;
    }

    private int[] commentProcess(String[] texts, int col, int row) {
        String line = texts[row];
        char[] lineChars = line.toCharArray(); // 行字符数组
        char c = lineChars[col];
        StringBuilder sb = new StringBuilder(); //初始化token
        sb.append(c);
        c = lineChars[++col];
        if (c != '*' && c != '/') { //不是多行注释及单行注释
            if (c == '=') { // /=
                sb.append(c);
                DefaultTableModel tableModel1 = (DefaultTableModel) jTable1.getModel();
                Token t = new Token(doubleOperatorMap.get(sb.toString()), sb.toString(), row+1);
                tableModel1.addRow(new Object[]{row + 1, sb.toString(), t.toString()});
                jTable1.invalidate();
            }
            else col--; // 指针回退 // /
        }
        else { // 注释可能是‘//’也可能是‘/*’
            boolean haveMistake = false;
            if (c == '*') { // 是/*的情况
                sb.append(c);
                int state = 2;
                while (state != 4) {
                    if (col < lineChars.length-1) c = lineChars[++col];
                    else {
                        row++;
                        if (row >= texts.length) {
                            haveMistake = true;
                            break;
                        }
                        line = texts[row];
                        if (line.equals("")) continue;
                        else {
                            lineChars = line.toCharArray();
                            col=0;
                            c = lineChars[col];
                        }
                    }

                    state = DFA.commentDFA(c, state);
                    if (state == -1) {
                        haveMistake = true;
                        break;
                    }
                    sb.append(c);
                }
            }
            else { //单行注释读取所有字符
                String tmpstr = line.substring(col);
                col += tmpstr.length();
                sb.append(tmpstr);
            }
            if(haveMistake) {
                DefaultTableModel tableModel2 = (DefaultTableModel) jTable2.getModel();
                tableModel2.addRow(new Object[] {row+1, "注释未封闭"});
                jTable2.invalidate();
                --col;
            }
            else {
                DefaultTableModel tableModel1 = (DefaultTableModel) jTable1.getModel();
                tableModel1.addRow(new Object[] {row+1, sb.toString(), "注释"});
                jTable1.invalidate();
            }
        }
        return new int[]{row, col};
    }

    /* =====================  静态判断函数  ====================*/
    public static boolean isKeyword(String token) {
        for (String keyword: keywords)
            if (token.equals(keyword)) return true;
        return false;
    }

    public static boolean isLettersOrUnderscores(char c) {
        return (c >= 'a' && c <='z') || (c>='A' && c<='Z') || c=='_';
    }

    public static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public static boolean isHexDigit(char c) {
        return (c >= '0' && c <= '9') || (c >='a' && c <= 'f') || (c >='A' && c <= 'F');
    }

    public static boolean isOperator(char c) {
        for (char o : operator)
            if (c == o) return true;
        return false;
    }

    public static boolean isDelimiter(char c) {
        for (char d: delimiter)
            if (c == d) return true;
        return false;
    }

    //运算符后可加等于
    public static boolean isPlusEqu(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '='
                || c == '>' || c == '<' || c == '&' || c == '|' || c == '^';
    }

    //可以连续两个运算符一样
    public static boolean isPlusSame(char c) {
        return c == '+' || c == '-' || c == '&' || c == '|';
    }

}
