package syntax;

import java.io.*;
import java.util.*;

public class Grammar {
    public final static String epsilon = "ε";  // 空串
    public final static String end = "$";  // 结束符
    public final static String acc = "acc";
    public final static String start = "P'";
    public final static String reduce = "r";
    public final static String shift = "s";
    private final static String tablePath = "Table.txt";
    private final static String grammarPath = "grammar.txt";

    private final List<String> VN = new ArrayList<>();  // 非终结符集
    private final List<String> VT = new ArrayList<>();  // 终结符集
    private final List<Production> productions = new ArrayList<>();  // 产生式集
    private final Map<String, List<String>> firstMap = new HashMap<>();    //  每个符号的first集

    private final List<String> actionTitle = new ArrayList<>();
    private final List<String> gotoTitle = new ArrayList<>();
    private final String[][] actionTable;  // Action表，二维数组
    private final int[][] gotoTable;  // GoTo表，二维数组

    public Grammar() {
        this.initGrammar();
        // 调价表头
        this.actionTitle.addAll(this.VT); //遍历所有的终结符
        this.actionTitle.add(end);
        this.gotoTitle.addAll(this.VT);
        this.gotoTitle.addAll(this.VN);  //遍历所有的非终结符
        List<Goto> gotoList = new ArrayList<>();
        List<LRState> LRStates = this.statesInLRDFA(gotoList); // 所有DFA状态
        this.gotoTable = new int[LRStates.size()][gotoTitle.size() + actionTitle.size() - 1];
        this.actionTable = new String[LRStates.size()][actionTitle.size()];
        this.createAnalyzeTable(gotoList, LRStates);//填充语法分析表的相关内容
        this.saveTableToFile(LRStates.size());
    }

    //解析文法产生式
    private void initGrammar() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(grammarPath));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(""))
                    continue;
                String[] div = line.split("->");
                String left = div[0].trim();
                String[] right = div[1].split("\\|");//将合并书写的多个表达式解析成多个
                for (String r : right)
                    productions.add(new Production(left + "->" + r.trim()));
                if (!VN.contains(left)) // 添加到非终结符集
                    VN.add(left);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (Production p : productions) { // 计算终结符集
            List<String> rights = p.getRights();
            for (String s : rights) { //从右侧寻找终结符
                if (!VN.contains(s) && !s.equals(epsilon) && !VT.contains(s))
                    VT.add(s);
            }
        }

        //计算first集合
        for (String vt : VT) {  //将所有的终结符的first都设为本身
            firstMap.put(vt, new ArrayList<>());
            firstMap.get(vt).add(vt);
        }
        for (String vn : VN) //计算所有非终结符的first集合
            firstMap.put(vn, new ArrayList<>(this.getFirst(vn)));

        // 增加文法结束符号的first集
        List<String> endFirst = new ArrayList<>();
        endFirst.add(end);
        firstMap.put(end, endFirst);
    }

    // 求非终结符的first集
    private Set<String> getFirst(String vn) {
        Set<String> set = new HashSet<>();
        int size1, size2;
        do {
            size1 = set.size();
            for (Production d : productions) {
                if (!d.getLeft().equals(vn))
                    continue;
                String r0 = d.getRights().get(0);
                if (r0.equals(epsilon) || VT.contains(r0))  // 空符号或终结符，直接加入
                    set.add(r0);
                else if (VN.contains(r0) && !vn.equals(r0)) { // 非终结符，且除去类似于E->E*E这样的左递归，递归求解
                    for (String r : d.getRights()) {
                        Set<String> set2 = getFirst(r);
                        set.addAll(set2);
                        if (!set2.contains(epsilon))// 递归右部的所有符号的first集，直到不再产生空串
                            break;
                    }
                }
            }
            size2 = set.size();
        } while (size1 != size2);
        return set;
    }

    //递归地建立一个DFA(栈模拟非递归实现)
    private List<LRState> statesInLRDFA(List<Goto> gotoList) {
        List<LRState> LRStates = new ArrayList<>();
        Stack<StateInfoTemp> stateInfoTempStack = new Stack<>();
        List<LRItem> entryState = new ArrayList<>();
        entryState.add(new LRItem(productionsLeftIs(start).get(0), end, -1));
        stateInfoTempStack.push(new StateInfoTemp(-1, null, entryState));
        while (! stateInfoTempStack.isEmpty()) {
            StateInfoTemp info = stateInfoTempStack.pop();
            int lastState = info.lastState;
            String path = info.path;
            List<LRItem> stateItems = info.stateItems;
            LRState state = new LRState(LRStates.size());
            for (LRItem item : stateItems) {
                item.index++;
                state.addLRItem(item);
            }
            // 状态中加入增广的产生式
            for (int i = 0; i < state.items.size(); i++) { //遍历状态的所有产生式
                LRItem item = state.items.get(i);
                List<String> rights = item.production.getRights();
                if (item.index >= rights.size()) //item.index是点所在的位置，需要非规约
                    continue;
                String A = rights.get(item.index);  // 获取"."后面的符号
                if (!VN.contains(A)) // A不是非终结符，只有非终结符才能增广此状态
                    continue;
                Set<String> firstB = new HashSet<>();
                boolean flag = true; // 点后面只有一个符号（类似于“A->BB.C, #”的状态）或后面的符号都能产生空串
                for (int m = item.index + 1; m < rights.size(); m++) {
                    List<String> list1 = firstMap.get(rights.get(m)); //后面有多个符号的时候，需要看first集合
                    firstB.addAll(list1);
                    if (!list1.contains(epsilon)) {
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    firstB.add(item.forward);  //加入自身的展望符
                for (Production p : productionsLeftIs(A)) { //遍历所有的后继产生式，做相应的处理
                    for (String f : firstB) {
                        if (f.equals(epsilon))
                            continue;
                        if (p.getRights().get(0).equals(epsilon))
                            state.addLRItem(new LRItem(p, f, 1));
                        else
                            state.addLRItem(new LRItem(p, f, 0));
                    }
                }
            }
            boolean flag = false;
            for (int i = 0; i < LRStates.size(); i++) {
                if (LRStates.get(i).equals(state)) {
                    gotoList.add(new Goto(lastState, i, path));
                    flag = true;
                    break;
                }
            }
            if (flag)
                continue;
            LRStates.add(state); //状态构造完成，加入集合
            if (path != null)
                gotoList.add(new Goto(lastState, state.id, path));
            List<String> pathList = state.nextStateRead();
            for (String p : pathList) {
                List<LRItem> items = state.itemsInNextState(p); //直接通过路径传到下一个状态的情况
                stateInfoTempStack.push(new StateInfoTemp(state.id, p, items));//开始进行递归，建立用于分析的DFA
            }
        }
        return LRStates;
    }

    // 获取左部为v的产生式
    private List<Production> productionsLeftIs(String v) {
        List<Production> result = new ArrayList<>();
        for (Production p : productions) {
            if (p.getLeft().equals(v))
                result.add(p);
        }
        return result;
    }

    //填充语法分析表
    private void createAnalyzeTable(List<Goto> gotoList, List<LRState> LRStates) {
        //先全部填上空值
        for (int i = 0; i < gotoTable.length; i++)
            for (int j = 0; j < gotoTable[0].length; j++)
                gotoTable[i][j] = -1;
        for (int i = 0; i < actionTable.length; i++)
            for (int j = 0; j < actionTable[0].length; j++)
                actionTable[i][j] = "  ";
        //完善语法分析表的goto部分
        for (Goto g : gotoList) {
            int pathIndex = gotoTitle.indexOf(g.path);
            this.gotoTable[g.start][pathIndex] = g.end;
        }
        //完善语法分析表的action部分
        for (int i = 0; i < LRStates.size(); i++) {
            List<LRItem> items = LRStates.get(i).items;//获取dfa的单个状态
            for (LRItem item : items) {//对每一个进行分析
                List<String> rights = item.production.getRights();
                if (item.index == rights.size()) {
                    if (item.production.getLeft().equals(start))
                        actionTable[i][actionTitle.indexOf(end)] = acc;//设为接受
                    else {
                        int derivationIndex = productions.indexOf(item.production);
                        actionTable[i][actionTitle.indexOf(item.forward)] = reduce + derivationIndex;//设为规约
                    }
                } else {
                    String next = rights.get(item.index);//获取·后面的文法符号
                    if (VT.contains(next) && gotoTable[i][gotoTitle.indexOf(next)] != -1)
                        actionTable[i][actionTitle.indexOf(next)] = shift + gotoTable[i][gotoTitle.indexOf(next)];
                }
            }
        }
    }

    //输出分析表
    private void saveTableToFile(int statesSize) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(tablePath));
            StringBuilder colLine = new StringBuilder(formatLength(" "));
            for (String s : actionTitle) {
                if (s.length() <= 6)
                    colLine.append("\t");
                colLine.append(formatLength(s));
            }
            for (int j = actionTitle.size() - 1; j < gotoTitle.size(); j++)
                colLine.append("\t").append(formatLength(gotoTitle.get(j)));
            colLine.append("\n");
            bw.write(colLine.toString());
            for (int i = 0; i < statesSize; i++) {
                StringBuilder line = new StringBuilder(formatLength(String.valueOf(i)));
                int index = 0;
                while (index < actionTitle.size()) {
                    line.append("\t").append(formatLength(actionTable[i][index]));
                    index++;
                }
                index = actionTitle.size() - 1;
                while (index < gotoTitle.size()) {
                    line.append("\t");
                    if (gotoTable[i][index] == -1)
                        line.append(formatLength(" "));
                    else
                        line.append(formatLength(String.valueOf(gotoTable[i][index])));
                    index++;
                }
                line.append("\n");
                bw.write(line.toString());
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatLength(String str) {
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < 8 - sb.length(); i++)
            sb.append(" ");
        return sb.toString();
    }

    public String ACTION(int stateIndex, String vt) {
        int index = actionTitle.indexOf(vt);
        return actionTable[stateIndex][index];
    }

    public int GOTO(int stateIndex, String vn) {
        int index = gotoTitle.indexOf(vn);
        return gotoTable[stateIndex][index];
    }

    public List<Production> getProductions() {
        return productions;
    }
    /* =================================== 辅助类 ======================================== */
    private static class StateInfoTemp {
        public int lastState;
        public String path;
        public List<LRItem> stateItems;
        public StateInfoTemp(int lastState, String path, List<LRItem> stateItems) {
            this.lastState = lastState;
            this.path = path;
            this.stateItems =  stateItems;
        }
    }

    private static class Goto {
        //  当第x号DFA状态,输入S符号时,转移到第y号DFA状态,则:
        public int start; // 第x号DFA状态
        public int end; // 第y号DFA状态
        public String path; // S符号
        public Goto(int start, int end, String path) {
            this.start = start;
            this.end = end;
            this.path = path;
        }
    }

    /**
     * LR 项
     */
    private static class LRItem {
        public final Production production;  // 产生式
        public final String forward;  // 展望符
        public int index;  // 当前点所处位置

        public LRItem(Production production, String forward, int index) {
            this.production = production;
            this.forward = forward;
            this.index = index;
        }

        public LRItem(LRItem lrItem) {
            this.production = lrItem.production;
            this.forward = lrItem.forward;
            this.index = lrItem.index;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder(production.getLeft() + "->");
            int length = production.getRights().size();
            for (int i = 0; i < length; i++) {
                if (length == 1 && production.getRights().get(0).equals("ε")) {
                    result.append(" .");
                    break;
                } else {
                    result.append(" ");
                    if (i == index)//在index处额外输出一个点
                        result.append(".");
                    result.append(production.getRights().get(i));
                }
            }
            if (index == length && !production.getRights().get(0).equals("ε")) {
                result.append(".");
            }
            result.append(" ,");
            result.append(forward);
            return result.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (! (o instanceof LRItem))
                return false;
            LRItem item = (LRItem) o;
            if (! item.production.equals(production))
                return false;
            if (! item.forward.equals(forward))
                return false;
            return item.index == index;
        }
    }

    /**
     * LR DFA 状态
     */
    public static class LRState {
        public final int id; // 项目集编号,即DFA状态号
        public final List<LRItem> items = new ArrayList<>(); //LR项目的集合,每个元素表示一个产生式状态

        public LRState(int id) {
            this.id = id;
        }

        /**
         * 向状态中添加新的项目
         * 如果不包含item，则将其加入，否则什么也不做
         */
        public void addLRItem(LRItem item) {
            if (!items.contains(item))
                items.add(item);
        }

        /**
         * 获得状态转移的读入符号，即返回点后所有的符号
         */
        public List<String> nextStateRead() {
            List<String> result = new ArrayList<>();
            for (LRItem item : items) {
                if (item.production.getRights().size() == item.index)  // 规约状态
                    continue;
                String s = item.production.getRights().get(item.index);  // "."后面的符号
                if (!result.contains(s))
                    result.add(s);
            }
            return result;
        }

        /**
         * 返回此项目的后继项目集
         */
        public List<LRItem> itemsInNextState(String s) {
            List<LRItem> result = new ArrayList<>();
            for (LRItem item : items) {
                if (item.production.getRights().size() > item.index) { // 当前非规约状态
                    String s1 = item.production.getRights().get(item.index);
                    if (s1.equals(s))
                        result.add(new LRItem(item));
                }
            }
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (! (o instanceof LRState))
                return false;
            return ((LRState) o).items.equals(items);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                result.append(items.get(i));
                if (i < items.size() - 1) {
                    result.append("\n");
                }
            }
            return result.toString();
        }
    }

    public List<String> getVN() {
        return this.VN;
    }

    public List<String> getVT() {
        return this.VT;
    }
}