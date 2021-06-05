package semantic;

import lexical.Token;
import syntax.Grammar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.*;

public class SemanticAnalyze {
    private final Token root;
    private final Map<Token, Properties> propertiesMap = new HashMap<>();
    private final Grammar grammar;
    private final JTable errTable;
    private final JTable semTable;
    private final JTable symbolTable;

    private final List<Stack<Symbol>> table = new ArrayList<>(); // 符号表
    private final List<String> three = new ArrayList<>(); // 三地址指令序列
    private final List<FourAddr> four = new ArrayList<>(); // 四元式指令序列

    private String t; // 类型
    private int w; // 大小
    private int offset; // 偏移量
    private int tempVarCount = 0; // 新建变量计数
    private final int initial = 1; // 记录第一条指令的位置

    private final List<String> paramQueue = new ArrayList<>(); // 过程调用参数队列
    private final Stack<Integer> tblptr = new Stack<>(); // 符号表指针栈
    private final Stack<Integer> off = new Stack<>(); // 符号表偏移大小栈

    public SemanticAnalyze(Grammar grammar, Token root, JTable errTable, JTable semTable, JTable symbolTable) {
        this.root = root;
        this.grammar = grammar;
        this.errTable = errTable;
        this.semTable = semTable;
        this.symbolTable = symbolTable;
    }

    public void analyze() {
        this.dfs(root);

        DefaultTableModel tableModel1 = (DefaultTableModel) semTable.getModel();
        for (int i = 0; i < three.size(); i++)
            tableModel1.addRow(new Object[]{i + 1, four.get(i).toString(), three.get(i)});
        DefaultTableModel tableModel2 = (DefaultTableModel) symbolTable.getModel();
        for (int i = 0; i < table.size(); i++) {
            for (int j = 0; j < table.get(i).size(); j++) {
                tableModel2.addRow(new Object[]{i, table.get(i).get(j).getName(),
                        table.get(i).get(j).getType(), table.get(i).get(j).getOffset()});
            }
        }
    }

    /**
     * 深搜遍历语法树
     */
    private void dfs(Token root) {
        List<String> VN = grammar.getVN();
        for (int i = 0; i < root.getChildren().size(); i++) {
            Token tn = root.getChildren().get(i);
            if (VN.contains(tn.getTokenName())) { // 非终结符
                dfs(tn); // 递归遍历孩子节点
                findSemantic(tn); // 查找相应的语义动作函数
            }
        }
    }

    /**
     * 向符号表中增加元素
     *
     * @param i      第i个符号表
     * @param name   元素名字
     * @param type   元素类型
     * @param offset 偏移量
     */
    private void enter(int i, String name, String type, int offset) {
        if (table.size() == 0) {
            table.add(new Stack<>());
        }
        Symbol s = new Symbol(name, type, offset);
        table.get(i).push(s);
    }

    /**
     * 查找符号表，查看变量是否存在
     *
     * @return 该名字在符号表中的位置
     */
    private int[] lookup(String id) {
        int[] a = new int[2];
        for (int i = 0; i < table.size(); i++) {
            for (int j = 0; j < table.get(i).size(); j++) {
                if (table.get(i).get(j).getName().equals(id)) {
                    a[0] = i;
                    a[1] = j;
                    return a;
                }
            }
        }
        a[0] = -1;
        a[1] = -1;
        return a;
    }

    /**
     * 新建一个变量
     *
     * @return 新建变量名
     */
    private String newTempVar() {
        return "t" + (++tempVarCount);
    }

    /**
     * 回填地址
     *
     * @param list 需要回填的指令序列
     * @param quad 回填的地址
     */
    private void backPatch(List<Integer> list, int quad) {
        for (Integer integer : list) {
            int x = integer - initial;
            three.set(x, three.get(x) + quad);
            four.get(x).setToAddr(String.valueOf(quad));
        }
    }

    /**
     * 合并列表
     *
     * @param a 列表
     * @param b 列表
     * @return a与b合并后的列表
     */
    private List<Integer> merge(List<Integer> a, List<Integer> b) {
        List<Integer> merge = new ArrayList<>();
        merge.addAll(a);
        merge.addAll(b);
        return merge;
    }

    /**
     * 返回下一条指令地址
     *
     * @return 下一条指令地址
     */
    private int nextquad() {
        return three.size() + initial;
    }

    /**
     * 新建包含i的列表并返回
     *
     * @param i 待加入元素
     * @return 列表
     */
    private List<Integer> makeList(int i) {
        List<Integer> a1 = new ArrayList<>();
        a1.add(i);
        return a1;
    }

    /**
     * 新增一个符号表
     */
    private void makeTable() {
        table.add(new Stack<>());
    }

    // P -> proc id ; M0 begin D S end
    // {addwidth(top(tblptr),top(offset));pop(tblptr);pop(offset)}
    private void P_SDT_1() {
        tblptr.pop();
        off.pop();
    }

    // S -> S1 M S2 {backpatch(S1.nextlist,M.quad); S.nextlist=S2.nextlist;}
    private void S_SDT_1(Token S) {
        Token S1 = S.getChildren().get(0);
        Token M = S.getChildren().get(1);
        Token S2 = S.getChildren().get(2);
        this.backPatch(propertiesMap.get(S1).getNext(), propertiesMap.get(M).getQuad());

        Properties properties = new Properties(propertiesMap.get(S2).getNext());
        propertiesMap.put(S, properties);
    }

    /* ================================ 声明语句的翻译 ====================================== */
    // D -> T id ; {enter(top(tblptr),id.name,T.type,top(offset));
    // top(offset) = top(offset)+T.width}
    private void D_SDT_2(Token D) {
        Token T = D.getChildren().get(0);
        String id = D.getChildren().get(1).getAttributeValue(); // id
        int[] i = this.lookup(id);
        if (i[0] == -1) { // 如果没有找到，在符号表里添加一项
            this.enter(tblptr.peek(), id, propertiesMap.get(T).getType(), off.peek());
            int s = off.pop();
            off.push(s + propertiesMap.get(T).getWidth());
            offset = offset + propertiesMap.get(T).getWidth();// 更新offset
        } else { // 否则报错
            DefaultTableModel tableModel = (DefaultTableModel) errTable.getModel();
            tableModel.addRow(new Object[]{D.getChildren().get(1).getLineNumber(),
                    "Semantic Error: 变量" + id + "重复声明"});
        }
    }

    // D -> T * id ; {enter(top(tblptr),id.name,T.type,top(offset));
    // top(offset) = top(offset)+T.width}
    private void D_SDT_3(Token D) {
        Token T = D.getChildren().get(0);
        String id = D.getChildren().get(2).getAttributeValue();
        int[] i = this.lookup(id);
        if (i[0] == -1) { // 如果没有找到，在符号表里添加一项
            this.enter(tblptr.peek(), id, propertiesMap.get(T).getType() + " pointer", off.peek());
            int s = off.pop();
            off.push(s + 8);
            offset = offset + 8;// 更新offset
        } else { // 否则报错
            DefaultTableModel tableModel = (DefaultTableModel) errTable.getModel();
            tableModel.addRow(new Object[]{D.getChildren().get(1).getLineNumber(),
                    "Semantic Error: 变量" + id + "重复声明"});
        }
    }

    // T -> X C {T.type=C.type; T.width=C.width;}
    private void T_SDT_1(Token T) {
        Token C = T.getChildren().get(1);
        Properties a1 = new Properties(propertiesMap.get(C).getType(), propertiesMap.get(C).getWidth());
        propertiesMap.put(T, a1);// 在T节点上附加相应属性
    }

    // T -> record begin N2 D end {T.type=record(top(tblptr));
    // T.width=top(offset); pop(tblptr); pop(offset)}
    private void T_SDT_2(Token T) {
        Properties a1 = new Properties("record", off.pop());
        tblptr.pop();
        propertiesMap.put(T, a1);
    }

    // X -> integer {X.type=integer; X.width=4;t=integer,w=4}
    private void X_SDT_1(Token X) {
        t = "integer";
        w = 4;
        Properties a1 = new Properties("integer", 4);
        propertiesMap.put(X, a1);
    }

    // X -> real {X.type=real; X.width=8;t=real;w=8}
    private void X_SDT_2(Token X) {
        t = "real";
        w = 8;
        Properties a1 = new Properties("real", 8);
        propertiesMap.put(X, a1);
    }

    // C -> [ num ] C1 {C.type=array(num.val,C1.type); C.width=num.val*C1.width;}
    private void C_SDT_1(Token C) {
        int num = Integer.parseInt(C.getChildren().get(1).getAttributeValue());
        Token C1 = C.getChildren().get(3);

        String baseType = propertiesMap.get(C1).getType();
        Array type = null;
        if (baseType.startsWith("array")) {
            type = propertiesMap.get(C1).getArray();
            baseType = "array";
        }
        Array a2 = new Array(num, baseType, type);
        Properties a1 = new Properties(a2, arrayString(a2), num * propertiesMap.get(C1).getWidth());
        propertiesMap.put(C, a1);
    }

    // C -> ε {C.type=t; C.width=w;}
    private void C_SDT_2(Token C) {
        Properties a1 = new Properties(t, w);
        propertiesMap.put(C, a1);
    }

    /* =================================== 简单赋值语句的翻译 =============================== */
    // S -> id = E ; {p=lookup(id.lexeme); if p==nil then error;
    // gencode(p'='E.addr); S.nextlist=null}
    private void S_SDT_2(Token S) {
        String id = S.getChildren().get(0).getAttributeValue();
        Token E = S.getChildren().get(2);
        int[] i = this.lookup(id);
        if (i[0] == -1) {
            DefaultTableModel tableModel = (DefaultTableModel) errTable.getModel();
            tableModel.addRow(new Object[]{S.getChildren().get(0).getLineNumber(),
                    "Semantic Error: 变量" + id + "引用前未声明"});

            this.enter(tblptr.peek(), id, "integer", offset);
            offset = offset + 4;
        }
        String code = id + " = " + propertiesMap.get(E).getAddr();
        three.add(code);
        four.add(new FourAddr("=", propertiesMap.get(E).getAddr(), "-", id));

        Properties a1 = new Properties(new ArrayList<>());
        propertiesMap.put(S, a1);
    }

    // S -> L = E ; {gencode(L.array'['L.offset']''='E.addr); S.nextlist=null}
    private void S_SDT_3(Token S) {
        Token L = S.getChildren().get(0);
        Token E = S.getChildren().get(2);
        String code = propertiesMap.get(L).getName() + "[" + propertiesMap.get(L).getOffset() + "] = "
                + propertiesMap.get(E).getAddr();
        three.add(code);
        four.add(new FourAddr("[]=", propertiesMap.get(E).getAddr(), "-",
                propertiesMap.get(L).getName() + "[" + propertiesMap.get(L).getOffset() + "]"));

        Properties a1 = new Properties(new ArrayList<>());
        propertiesMap.put(S, a1);
    }

    // E -> E1 + E2 {E.addr=newtemp(); gencode(E.addr'='E1.addr'+'E2.addr);}
    private void E_SDT_1(Token E) {
        Token E1 = E.getChildren().get(0);
        Token E2 = E.getChildren().get(2);
        String tempVar1 = newTempVar();
        String E1_Type = propertiesMap.get(E1).getType();
        String E2_Type = propertiesMap.get(E2).getType();
        if ((E1_Type.equals("integer") && E2_Type.equals("integer"))
                || (E1_Type.equals("real") && E2_Type.equals("real"))) {
            Properties a1 = new Properties(tempVar1, E1_Type);
            propertiesMap.put(E, a1);

            String code = tempVar1 + " = " + propertiesMap.get(E1).getAddr() + "+" + propertiesMap.get(E2).getAddr();
            three.add(code);
            four.add(new FourAddr("+", propertiesMap.get(E1).getAddr(), propertiesMap.get(E2).getAddr(), tempVar1));
        }
        if ((E1_Type.equals("real") && E2_Type.equals("integer"))) {
            String tempVar2 = newTempVar();
            Properties a1 = new Properties(tempVar2, "real");
            propertiesMap.put(E, a1);
            String code1 = tempVar1 + " = int2real " + propertiesMap.get(E2).getAddr();
            String code2 = tempVar2 + " = " + propertiesMap.get(E1).getAddr() + "+" + tempVar1;
            three.add(code1);
            three.add(code2);
            four.add(new FourAddr("=", "int2real" + propertiesMap.get(E2).getAddr(), "-", tempVar1));
            four.add(new FourAddr("+", propertiesMap.get(E1).getAddr(), tempVar1, tempVar2));
        }
        if ((E1_Type.equals("integer") && E2_Type.equals("real"))) {
            String tempVar2 = newTempVar();
            Properties a1 = new Properties(tempVar2, "real");
            propertiesMap.put(E, a1);

            String code1 = tempVar1 + " = int2real " + propertiesMap.get(E1).getAddr();
            String code2 = tempVar2 + " = " + tempVar1 + "+" + propertiesMap.get(E2).getAddr();
            three.add(code1);
            three.add(code2);
            four.add(new FourAddr("=", "int2real" + propertiesMap.get(E1).getAddr(), "-", tempVar1));
            four.add(new FourAddr("+", tempVar1, propertiesMap.get(E2).getAddr(), tempVar2));
        }
        if (E1_Type.contains("array")) {
            String tempVar2 = newTempVar();
            Properties a1 = new Properties(tempVar2, "integer");
            propertiesMap.put(E, a1);

            int x = typeWidth(propertiesMap.get(E1).getType());
            String code1 = tempVar1 + " = " + x;
            String code2 = tempVar2 + " = " + tempVar1 + "+" + propertiesMap.get(E2).getAddr();
            three.add(code1);
            three.add(code2);
            four.add(new FourAddr("=", String.valueOf(x), "-", tempVar1));
            four.add(new FourAddr("+", tempVar1, propertiesMap.get(E2).getAddr(), tempVar2));

            DefaultTableModel tableModel = (DefaultTableModel) errTable.getModel();
            tableModel.addRow(new Object[]{E.getChildren().get(0).getLineNumber(),
                    "Semantic Error: 整型变量与数组变量相加减"});
        }
        if (E2_Type.contains("array")) {
            String tempVar2 = newTempVar();
            Properties a1 = new Properties(tempVar2, "integer");
            propertiesMap.put(E, a1);

            int x = typeWidth(propertiesMap.get(E2).getType());
            String code1 = tempVar1 + " = " + x;
            String code2 = tempVar2 + " = " + propertiesMap.get(E1).getAddr() + "+" + tempVar1;
            three.add(code1);
            three.add(code2);
            four.add(new FourAddr("=", String.valueOf(x), "-", tempVar1));
            four.add(new FourAddr("+", propertiesMap.get(E1).getAddr(), tempVar1, tempVar2));

            DefaultTableModel tableModel = (DefaultTableModel) errTable.getModel();
            tableModel.addRow(new Object[]{E.getChildren().get(1).getLineNumber(),
                    "Semantic Error: 整型变量与数组变量相加减"});
        }
    }

    // E -> E1 {E.addr=E1.addr}
    private void E_SDT_2_3(Token E) {
        Token E1 = E.getChildren().get(0);
        Properties a1 = new Properties(propertiesMap.get(E1).getAddr(), propertiesMap.get(E1).getType());
        propertiesMap.put(E, a1);
    }

    // E -> E1 * E2 {E.addr=newtemp(); gencode(E.addr'='E1.addr'*'E2.addr);}
    private void E_SDT_4(Token E) {
        Token E1 = E.getChildren().get(0);
        Token E2 = E.getChildren().get(2);
        String tempVar = this.newTempVar();
        Properties a1 = new Properties(tempVar);
        propertiesMap.put(E, a1);

        String code = tempVar + " = " + propertiesMap.get(E1).getAddr() + "*" + propertiesMap.get(E2).getAddr();
        three.add(code);
        four.add(new FourAddr("*", propertiesMap.get(E1).getAddr(), propertiesMap.get(E2).getAddr(), tempVar));
    }

    // E -> ( E1 ) {E.addr=E1.addr}
    private void E_SDT_5(Token E) {
        Token E1 = E.getChildren().get(1);
        Properties a1 = new Properties(propertiesMap.get(E1).getAddr(), propertiesMap.get(E1).getType());
        propertiesMap.put(E, a1);
    }

    // E -> - E1 {E.addr=newtemp(); gencode(E.addr'=''uminus'E1.addr);}
    private void E_SDT_6(Token E) {
        Token E1 = E.getChildren().get(1); // E1
        String tempVar = newTempVar();
        Properties a1 = new Properties(tempVar, propertiesMap.get(E1).getType());
        propertiesMap.put(E, a1);

        String code = tempVar + " = -" + propertiesMap.get(E1).getAddr();
        three.add(code);
        four.add(new FourAddr("=", "-" + propertiesMap.get(E1).getAddr(), "-", tempVar));
    }

    // E -> id {E.addr=lookup(id.lexeme); if E.addr==null then error;}
    private void E_SDT_7(Token E) {
        String id = E.getChildren().get(0).getAttributeValue();
        int[] i = this.lookup(id);
        String type;
        if (i[0] == -1) {
            DefaultTableModel tableModel = (DefaultTableModel) errTable.getModel();
            tableModel.addRow(new Object[]{E.getChildren().get(0).getLineNumber(),
                    "Semantic Error: 变量" + id + "引用前未声明"});
            this.enter(tblptr.peek(), id, "integer", offset);
            offset = offset + 4;
            type = "integer";
        } else type = table.get(i[0]).get(i[1]).getType();
        Properties a1 = new Properties(id, type);
        propertiesMap.put(E, a1);
    }

    // E -> num {E.addr=lookup(num.lexeme); if E.addr==null then error}
    private void E_SDT_8(Token E) {
        String num = E.getChildren().get(0).getAttributeValue(); // num
        Properties a1 = new Properties(num, "integer");
        propertiesMap.put(E, a1);
    }

    // E -> L {E.addr=newtemp(); gencode(E.addr'='L.array'['L.offset']');}
    private void E_SDT_9(Token E) {
        Token L = E.getChildren().get(0);
        String tempVar = newTempVar();
        Properties a1 = new Properties(tempVar, "integer");
        propertiesMap.put(E, a1);

        String code = tempVar + " = " + propertiesMap.get(L).getName() + "[" + propertiesMap.get(L).getOffset() + "] ";
        three.add(code);
        four.add(new FourAddr("=[]",
                propertiesMap.get(L).getName() + "[" + propertiesMap.get(L).getOffset() + "]",
                "-", tempVar));
    }

    // L -> id [ E ] {L.array=lookup(id.lexeme); if L.array==nil then error;
    // L.type=L.array.type.elem; L.offset=newtemp();
    // gencode(L.offset'='E.addr'*'L.type.width);}
    private void L_SDT_1(Token L) {
        String id = L.getChildren().get(0).getAttributeValue();
        Token E = L.getChildren().get(2);
        String tempVar = newTempVar();
        int[] i = lookup(id);
        if (i[0] == -1) {
            DefaultTableModel tableModel = (DefaultTableModel) errTable.getModel();
            tableModel.addRow(new Object[]{L.getChildren().get(0).getLineNumber(),
                    "Semantic Error: 数组变量" + id + "引用前未声明"});
            Properties a1 = new Properties(id, "array(1,integer)", tempVar);
            propertiesMap.put(L, a1);

            String code = tempVar + " = " + 4;
            four.add(new FourAddr("=", String.valueOf(4), "-", tempVar));
            three.add(code);
            return;
        }
        if (!table.get(i[0]).get(i[1]).getType().contains("array")) {
            DefaultTableModel tableModel = (DefaultTableModel) errTable.getModel();
            tableModel.addRow(new Object[]{E.getChildren().get(0).getLineNumber(),
                    "Semantic Error: 非数组变量" + id + "数组访问"});
        }
        Properties a1 = new Properties(id, elemType(table.get(i[0]).get(i[1]).getType()), tempVar);
        propertiesMap.put(L, a1);

        String code;
        String s = elemType(table.get(i[0]).get(i[1]).getType());
        if (s.contains("array")) {
            code = tempVar + " = " + propertiesMap.get(E).getAddr() + "*" + typeWidth(s);
            four.add(new FourAddr("*", propertiesMap.get(E).getAddr(), String.valueOf(typeWidth(s)), tempVar));
        } else {
            code = tempVar + " = " + propertiesMap.get(E).getAddr();
            four.add(new FourAddr("=", propertiesMap.get(E).getAddr(), "-", tempVar));
        }
        three.add(code);
    }

    // L -> L1 [ E ] {L.array=L1.array; L.type=L1.type.elem; t=newtemp();
    // gencode(t'='E.addr'*'L.type.width); L.offset=newtemp();
    // gencode(L.offset'='L1.offset'+'t);}
    private void L_SDT_2(Token L) {
        Token L1 = L.getChildren().get(0);
        Token E = L.getChildren().get(2);
        String tempVar1 = newTempVar();
        String tempVar2 = newTempVar();
        Properties a1 = new Properties(propertiesMap.get(L1).getName(),
                elemType(propertiesMap.get(L1).getType()), tempVar2);
        propertiesMap.put(L, a1);

        String code1;
        String s = elemType(propertiesMap.get(L1).getType());
        if (s.contains("array")) {
            code1 = tempVar1 + " = " + propertiesMap.get(E).getAddr() + "*" + typeWidth(s);
            four.add(new FourAddr("*", propertiesMap.get(E).getAddr(), String.valueOf(typeWidth(s)), tempVar1));
        } else {
            code1 = tempVar1 + " = " + propertiesMap.get(E).getAddr() + "*" + w;
            four.add(new FourAddr("*", propertiesMap.get(E).getAddr(), String.valueOf(w), tempVar1));
        }
        three.add(code1);

        String code2 = tempVar2 + " = " + propertiesMap.get(L1).getOffset() + "+" + tempVar1;
        three.add(code2);
        four.add(new FourAddr("+", propertiesMap.get(L1).getOffset(), tempVar1, tempVar2));
    }

    // B -> B1 or M B2 {backpatch(B1.falselist,M.quad);
    // B.truelist=merge(B1.truelist,B2.truelist);
    // B.falselist=B2.falselist}
    private void B_SDT_1(Token B) {
        Token B1 = B.getChildren().get(0);
        Token M = B.getChildren().get(2);
        Token B2 = B.getChildren().get(3);
        this.backPatch(propertiesMap.get(B1).getFalse(), propertiesMap.get(M).getQuad());
        List<Integer> trueList = this.merge(propertiesMap.get(B1).getTrue(), propertiesMap.get(B2).getTrue());
        Properties a1 = new Properties(trueList, propertiesMap.get(B2).getFalse());
        propertiesMap.put(B, a1);
    }

    // B -> B1 {B.truelist=B1.truelist; B.falselist=B1.falselist}
    private void B_SDT_2_3(Token B) {
        Token B1 = B.getChildren().get(0);
        Properties a1 = new Properties(propertiesMap.get(B1).getTrue(), propertiesMap.get(B1).getFalse());
        propertiesMap.put(B, a1);
    }

    // B -> B1 and M B2 {backpatch(B1.truelist M.quad); B.truelist=B2.truelist;
    // B.falselist=merge(B1.falselist, B2.falselist)}
    private void B_SDT_4(Token B) {
        Token B1 = B.getChildren().get(0);
        Token M = B.getChildren().get(2);
        Token B2 = B.getChildren().get(3);
        this.backPatch(propertiesMap.get(B1).getTrue(), propertiesMap.get(M).getQuad());
        List<Integer> falseList = this.merge(propertiesMap.get(B1).getFalse(), propertiesMap.get(B2).getFalse());
        Properties a1 = new Properties(propertiesMap.get(B2).getTrue(), falseList);
        propertiesMap.put(B, a1);
    }

    // B -> not B1 {B.truelist=B1.falselist; B.falselist=B1.truelist}
    private void B_SDT_5(Token B) {
        Token B1 = B.getChildren().get(1);
        Properties a1 = new Properties(propertiesMap.get(B1).getFalse(), propertiesMap.get(B1).getTrue());
        propertiesMap.put(B, a1);
    }

    // B -> ( B1 ) {B.truelist := B1.truelist; B.falselist := B1.falselist}
    private void B_SDT_6(Token B) {
        Token B1 = B.getChildren().get(1); // B1
        Properties a1 = new Properties(propertiesMap.get(B1).getTrue(), propertiesMap.get(B1).getFalse());
        propertiesMap.put(B, a1);
    }

    // B -> E1 R E2 {B.truelist=makelist(nextquad); B.falselist=
    // makelist(nextquad+1);
    // gencode('if' E1.addr relop.op E2.addr 'goto –'); gencode('goto –')}
    private void B_SDT_7(Token B) {
        Token E1 = B.getChildren().get(0);
        Token R = B.getChildren().get(1);
        Token E2 = B.getChildren().get(2);
        Properties a1 = new Properties(this.makeList(this.nextquad()), this.makeList(this.nextquad() + 1));
        propertiesMap.put(B, a1);

        String code1 = "if " + propertiesMap.get(E1).getAddr() + propertiesMap.get(R).getName()
                + propertiesMap.get(E2).getAddr() + " goto ";
        three.add(code1);
        four.add(new FourAddr("j" + propertiesMap.get(R).getName(), propertiesMap.get(E1).getAddr(),
                propertiesMap.get(E2).getAddr(), "-"));
        String code2 = "goto ";
        three.add(code2);
        four.add(new FourAddr("j", "-", "-", "-"));
    }

    // B -> true {B.truelist=makelist(nextquad); gencode('goto –')}
    private void B_SDT_8(Token B) {
        Properties a1 = new Properties(this.makeList(this.nextquad()), null);
        propertiesMap.put(B, a1);

        String code = "goto ";
        three.add(code);
        four.add(new FourAddr("j", "-", "-", "-"));
    }

    // B -> false {B.falselist=makelist(nextquad); gencode('goto –')}
    private void B_SDT_9(Token B) {
        Properties a1 = new Properties(null, makeList(nextquad()));
        propertiesMap.put(B, a1);

        String code = "goto ";
        three.add(code);
        four.add(new FourAddr("j", "-", "-", "-"));
    }

    // R -> < | <= | == | != | > | >= {R.name=op}
    private void R_SDT(Token R) {
        String op = R.getChildren().get(0).getAttributeValue();
        Properties a1 = new Properties(op, null, null);
        propertiesMap.put(R, a1);
    }

    // S -> S1 {S.nextlist=S1.nextlist}
    private void S_SDT_4_5_6(Token S) {
        Token S1 = S.getChildren().get(0);
        Properties a1 = new Properties(propertiesMap.get(S1).getNext());
        propertiesMap.put(S, a1);
    }

    // S -> if B then M1 S1 N else M2 S2
    // {backpatch(B.truelist, M1.quad); backpatch(B.falselist,M2.quad);
    // S.nextlist=merge(S1.nextlist,merge(N.nextlist, S2.nextlist))}
    private void S_SDT_7_8(Token S) {
        Token B = S.getChildren().get(1);
        Token M1 = S.getChildren().get(3);
        Token S1 = S.getChildren().get(4);
        Token N = S.getChildren().get(5);
        Token M2 = S.getChildren().get(7);
        Token S2 = S.getChildren().get(8);
        this.backPatch(propertiesMap.get(B).getTrue(), propertiesMap.get(M1).getQuad());
        this.backPatch(propertiesMap.get(B).getFalse(), propertiesMap.get(M2).getQuad());
        List<Integer> nextList = this.merge(propertiesMap.get(N).getNext(), propertiesMap.get(S2).getNext());
        nextList = this.merge(propertiesMap.get(S1).getNext(), nextList);
        Properties a1 = new Properties(nextList);
        propertiesMap.put(S, a1);
    }

    // S -> while M1 B do M2 S1 {backpatch(S1.nextlist, M1.quad);
    // backpatch(B.truelist,M2.quad); S.nextlist=B.falselist;
    // gencode('goto'M1.quad)}
    private void S_SDT_9(Token S) {
        Token M1 = S.getChildren().get(1);
        Token B = S.getChildren().get(2);
        Token M2 = S.getChildren().get(4);
        Token S1 = S.getChildren().get(5);
        this.backPatch(propertiesMap.get(S1).getNext(), propertiesMap.get(M1).getQuad());
        this.backPatch(propertiesMap.get(B).getTrue(), propertiesMap.get(M2).getQuad());
        Properties a1 = new Properties(propertiesMap.get(B).getFalse());
        propertiesMap.put(S, a1);

        String code = "goto " + propertiesMap.get(M1).getQuad();
        three.add(code);
        four.add(new FourAddr("j", "-", "-",
                String.valueOf(propertiesMap.get(M1).getQuad())));
    }

    // S -> if B then M S1 {backpatch(B.truelist,M.quad);
    // S.nextlist=merge(B.falselist,S1.nextlist)}
    private void S_SDT_10(Token S) {
        Token B = S.getChildren().get(1);
        Token M = S.getChildren().get(3);
        Token S1 = S.getChildren().get(4);
        this.backPatch(propertiesMap.get(B).getTrue(), propertiesMap.get(M).getQuad());
        Properties a1 = new Properties(this.merge(propertiesMap.get(B).getFalse(),
                propertiesMap.get(S1).getNext()));
        propertiesMap.put(S, a1);
    }

    // S -> begin S1 end {S.nextlist=S1.nextlist}
    private void S_SDT_11_12_13(Token S) {
        Token S1 = S.getChildren().get(1);
        Properties a1 = new Properties(propertiesMap.get(S1).getNext());
        propertiesMap.put(S, a1);
    }

    // S -> S1 ; M S2 {backpatch(S1.nextlist, M.quad); S.nextlist=S2.nextlist}
    private void S_SDT_14(Token S) {
        Token S1 = S.getChildren().get(0);
        Token M = S.getChildren().get(2);
        Token S2 = S.getChildren().get(3);
        this.backPatch(propertiesMap.get(S1).getNext(), propertiesMap.get(M).getQuad());
        Properties a1 = new Properties(propertiesMap.get(S2).getNext());
        propertiesMap.put(S, a1);
    }

    // {t := mktable(nil); push(t, tblptr); push(0, offset)}
    // M0 -> ε {offset=0;}
    private void N0_SDT() {
        this.makeTable();
        int size = table.size() - 1;
        tblptr.push(size);
        off.push(0);
        offset = 0;
    }

    // M -> ε {M.quad=nextquad}
    private void M_SDT(Token M) {
        Properties a1 = new Properties(this.nextquad());
        propertiesMap.put(M, a1);
    }

    // N -> ε {N.nextlist=makelist(nextquad); gencode('goto –')}
    private void N_SDT(Token N) {
        Properties a1 = new Properties(this.makeList(this.nextquad()));
        propertiesMap.put(N, a1);
        String code = "goto ";
        three.add(code);
        four.add(new FourAddr("j", "-", "-", "-"));
    }

    // S -> call id ( EL ) {n=0; for queue中的每个t do {gencode('param't); n=n+1}
    // gencode('call'id.addr','n);}
    private void S_SDT_15(Token S) {
        String id = S.getChildren().get(1).getAttributeValue();
        int[] index = this.lookup(id);
        if (!table.get(index[0]).get(index[1]).getType().equals("function")) {
            DefaultTableModel tableModel = (DefaultTableModel) errTable.getModel();
            tableModel.addRow(new Object[]{S.getChildren().get(0).getLineNumber(),
                    "Semantic Error: " + id + "不是过程，不能被call"});
            Properties a1 = new Properties(new ArrayList<>());
            propertiesMap.put(S, a1);
            return;
        }
        for (String s : paramQueue) {
            String code = "param " + s;
            three.add(code);
            four.add(new FourAddr("param", "-", "-", s));
        }
        int size = paramQueue.size();
        String code = "call " + id + " " + size;
        three.add(code);
        four.add(new FourAddr("call", String.valueOf(size), "-", id));

        Properties a1 = new Properties(new ArrayList<>());
        propertiesMap.put(S, a1);
    }

    // EL -> EL , E {将E.addr添加到queue的队尾}
    private void EL_SDT_1(Token EL) {
        Token E = EL.getChildren().get(2);
        paramQueue.add(propertiesMap.get(E).getAddr());
    }

    // EL -> E {初始化queue,然后将E.addr加入到queue的队尾}
    private void EL_SDT_2(Token EL) {
        Token E = EL.getChildren().get(0);
        paramQueue.clear();
        paramQueue.add(propertiesMap.get(E).getAddr());
    }

    // D -> proc id; N1 D S {t=top(tblptr); addwidth(t, top(offset));
    // pop(tblptr); pop(offset); enterproc(top(tblptr), id.name,t)}
    private void D_SDT_4(Token D) {
        String id = D.getChildren().get(1).getAttributeValue();
        int t = tblptr.peek();
        tblptr.pop();
        off.pop();
        this.enter(tblptr.peek(), id, "function", t);
    }

    // N1 -> ε {t:= mktable(top(tblptr)); push(t, tblptr); push(0, offset)}
    private void N1_SDT() {
        this.makeTable();
        int size = table.size() - 1;
        tblptr.push(size);
        off.push(0);
    }

    // N2 -> ε {t:= mktable(nil); push(t, tblptr); push(0, offset)}
    private void N2_SDT() {
        this.makeTable();
        int size = table.size() - 1;
        tblptr.push(size);
        off.push(0);
    }

    private void findSemantic(Token tree) {
        String s = treeToPro(tree);
        switch (s) {
            case "P -> proc id ; M0 begin D S end":
                P_SDT_1();
                break;
            case "S -> S M S":
                S_SDT_1(tree);
                break;
            case "D -> T id ;":
                D_SDT_2(tree);
                break;
            case "D -> T * id ;":
                D_SDT_3(tree);
                break;
            case "T -> X C":
                T_SDT_1(tree);
                break;
            case "T -> record begin N2 D end":
                T_SDT_2(tree);
                break;
            case "X -> integer":
                X_SDT_1(tree);
                break;
            case "X -> real":
                X_SDT_2(tree);
                break;
            case "C -> [ num ] C":
                C_SDT_1(tree);
                break;
            case "C ->":
                C_SDT_2(tree);
                break;
            case "S -> id = E ;":
                S_SDT_2(tree);
                break;
            case "S -> L = E ;":
                S_SDT_3(tree);
                break;
            case "E -> E + E1":
                E_SDT_1(tree);
                break;
            case "E -> E1":
            case "E1 -> E2":
                E_SDT_2_3(tree);
                break;
            case "E1 -> E1 * E2":
                E_SDT_4(tree);
                break;
            case "E2 -> ( E )":
                E_SDT_5(tree);
                break;
            case "E2 -> - E":
                E_SDT_6(tree);
                break;
            case "E2 -> id":
                E_SDT_7(tree);
                break;
            case "E2 -> num":
                E_SDT_8(tree);
                break;
            case "E2 -> L":
                E_SDT_9(tree);
                break;
            case "L -> id [ E ]":
                L_SDT_1(tree);
                break;
            case "L -> L [ E ]":
                L_SDT_2(tree);
                break;
            case "B -> B or M B1":
                B_SDT_1(tree);
                break;
            case "B -> B1":
            case "B1 -> B2":
                B_SDT_2_3(tree);
                break;
            case "B1 -> B1 and M B2":
                B_SDT_4(tree);
                break;
            case "B2 -> not B":
                B_SDT_5(tree);
                break;
            case "B2 -> ( B )":
                B_SDT_6(tree);
                break;
            case "B2 -> E R E":
                B_SDT_7(tree);
                break;
            case "B2 -> true":
                B_SDT_8(tree);
                break;
            case "B2 -> false":
                B_SDT_9(tree);
                break;
            case "R -> <":
            case "R -> <=":
            case "R -> ==":
            case "R -> !=":
            case "R -> >":
            case "R -> >=":
                R_SDT(tree);
                break;
            case "S -> S1":
            case "S -> S2":
            case "S3 -> S":
                S_SDT_4_5_6(tree);
                break;
            case "S1 -> if B then M S1 N else M S1":
            case "S2 -> if B then M S1 N else M S2":
                S_SDT_7_8(tree);
                break;
            case "S1 -> while M B do M S0":
                S_SDT_9(tree);
                break;
            case "S2 -> if B then M S0":
                S_SDT_10(tree);
                break;
            case "S0 -> begin S3 end":
            case "S1 -> begin S3 end":
            case "S2 -> begin S3 end":
                S_SDT_11_12_13(tree);
                break;
            case "S3 -> S3 ; M S":
                S_SDT_14(tree);
                break;
            case "M0 ->":
                N0_SDT();
                break;
            case "M ->":
                M_SDT(tree);
                break;
            case "N ->":
                N_SDT(tree);
                break;
            case "S -> call id ( EL ) ;":
                S_SDT_15(tree);
                break;
            case "EL -> EL , E":
                EL_SDT_1(tree);
                break;
            case "EL -> E":
                EL_SDT_2(tree);
                break;
            case "D -> proc id ; N1 begin D S end":
                D_SDT_4(tree);
                break;
            case "N1 ->":
                N1_SDT();
                break;
            case "N2 ->":
                N2_SDT();
                break;
        }
    }


    /* ============================================================================== */
    public static String arrayString(Array a) {
        String b = a.getBaseType();
        if (!b.equals("array"))
            return "array" + "(" + a.getLength() + "," + b + ")";
        return "array" + "(" + a.getLength() + "," + arrayString(a.getType()) + ")";
    }

    // "array(3,array(5,array(8,int)))" ---> "array(5,array(8,int))"
    public static String elemType(String s) {
        if (!s.contains("array"))
            return "integer";
        int i;
        int len = s.length();
        for (i = 0; i < len; i++) {
            if (s.charAt(i) == ',')
                break;
        }
        return s.substring(i + 1, len - 1);
    }

    //  "array(3,array(5,array(8,int)))" ---> 3
    public static int typeWidth(String s) {
        if (!s.contains("array"))
            return 4;
        int i, j = 0;
        int len = s.length();
        for (i = 0; i < len; i++) {
            if (s.charAt(i) == '(')
                j = i;
            if (s.charAt(i) == ',')
                break;
        }
        return Integer.parseInt(s.substring(j + 1, i));
    }

    public static String treeToPro(Token tree) {
        StringBuilder result = new StringBuilder(tree.getTokenName() + " ->");
        for (Token c : tree.getChildren()) {
            result.append(" ");
            result.append(c.getTokenName());
        }
        return result.toString().trim();
    }
}
