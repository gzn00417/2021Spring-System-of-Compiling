package cn.model.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * created by meizhimin on 2021/4/14
 */
public class SLRParser extends LRParser{

    private ArrayList<LRState> states;

    public SLRParser(Grammar grammar) {
        super(grammar);
        createStates();
        createActionTable();
        createGoToTable();

        //查看冲突状态用的
        for(int i=0;i<states.size();i++){
            System.out.println("state:" + i);
            System.out.println(states.get(i) + "\n");
        }
    }

    /*
    从初始状态开始，创建所有的SLR自动机状态
     */
    protected void createStates(){
        states = new ArrayList<>();
        HashSet<LRItem> start = new HashSet<>();
        start.add(new LRItem(grammar.getProductions().get(0)));
        LRState startState = new LRState(grammar, start);// 初始状态0
        states.add(startState);
        /*
        以如下初始状态举例：
        S' -> .S
        S -> .+SS
        S -> .*SS
        S -> .a
         */
        for(int i=0;i<states.size();i++){// states的size随着状态的添加在变化
            HashSet<String> stringAfterDot = new HashSet<>();// 右部某位置标有圆点的产生式称为相应文法的一个LR(0)项目
            for(LRItem item:states.get(i).getItems()){
                if(item.getCurrentTerminal() != null){
                    stringAfterDot.add(item.getCurrentTerminal());// 收集所有项目中圆点右边的文法符号，用于添加下一状态
                }
            }
            /*
            此时stringAfterDot中有{S, +, * a}
             */
            for(String str:stringAfterDot){
                HashSet<LRItem> nextStateItems = new HashSet<>();
                for(LRItem item:states.get(i).getItems()){
                    if(item.getCurrentTerminal() != null && item.getCurrentTerminal().equals(str)){// 根据圆点后的文法符号找到对应产生式，如根据S可找到 S' -> .S
                        LRItem temp = new LRItem(item);
                        temp.moveDot();// 生成S' -> .S 的后继项目 S' -> S.
                        nextStateItems.add(temp);
                    }
                }
                LRState nextState = new LRState(grammar, nextStateItems);
                boolean isExist = false;
                for(int j=0;j<states.size();j++){
                    if(states.get(j).getItems().equals(nextState.getItems())){// 若新状态已存在，直接添加转移
                        isExist = true;
                        states.get(i).addTransition(str, states.get(j));
                    }
                }
                if(!isExist){// 若新状态还不存在，先添加状态再添加转移
                    states.add(nextState);
                    states.get(i).addTransition(str,nextState);
                }
            }
        }
    }

    @Override
    protected void createGoToTable() {
        gotoTable = new HashMap[states.size()];
        for(int i=0;i<gotoTable.length;i++){// 为每个状态初始化goto表
            gotoTable[i] = new HashMap<>();
        }
        for(int i=0;i<gotoTable.length;i++){
            for(String s:states.get(i).getTransition().keySet()){// 转换表中有对应终结符的转换和非终结符的转换
                if(grammar.getNonterminals().contains(s)){// goto表是对应非终结符输入的状态转换表
                    gotoTable[i].put(s, states.indexOf(states.get(i).getTransition().get(s)));
                }
            }
        }
    }

    @Override
    protected void createActionTable() {
        int n = states.size();
        actionTable = new HashMap[n];
        for(int i=0;i<n;i++){//初始化
            actionTable[i] = new HashMap<>();
        }
        for(int i=0;i<n;i++){
            for(String s:states.get(i).getTransition().keySet()){// 转换表中有对应终结符的转换和非终结符的转换
                if(grammar.getTerminals().contains(s)){// action表是对应终结符输入的状态转换表
                    actionTable[i].put(s, new Action(ActionType.SHIFT, states.indexOf(states.get(i).getTransition().get(s))));// 添加移入动作
                }
            }
        }
        for(int i=0;i<n;i++){
            for(LRItem item:states.get(i).getItems()){
                if(item.getDot() == item.getRights().length){// 检查所有的归约状态
                    if(item.getLeft().equals("Program")){// 若归约为初始文法符号，此时输入$即成功接受
                        actionTable[i].put("$", new Action(ActionType.ACCEPT, 0));
                    }else {//若是正常的归约
                        List<String> follow = grammar.getFollows().get(item.getLeft());// SLR文法只对Follow集中的符号归约
                        Production production = new Production(item.getLeft(),item.getRights());
                        int index = grammar.findProductionIndex(production);//找到对应产生式的标号
                        Action action = new Action(ActionType.REDUCE, index);//添加归约动作
                        for(String str:follow){
                            if(actionTable[i].get(str) != null && !str.equals("else")){
                                if(!str.equals("and") && !str.equals("or")){
                                    System.out.println("it has a REDUCE-" + actionTable[i].get(str).getType() + " confilct in state " + i + " for input " + str);
                                }
                                //                                return;
                            }else {
                                actionTable[i].put(str, action);
                            }
                        }
                    }
                }
            }
        }
    }

    public HashMap<String, Integer>[] getGotoTable(){
        return gotoTable;
    }

    public HashMap<String, Action>[] getActionTable(){
        return actionTable;
    }

}
