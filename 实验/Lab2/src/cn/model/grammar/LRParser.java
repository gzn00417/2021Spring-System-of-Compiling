package cn.model.grammar;

import cn.model.lexical.Lexer;
import cn.model.lexical.tokenStr;
import javafx.scene.control.TreeItem;
import java.util.*;

/**
 * created by meizhimin on 2021/4/14
 */
public abstract class LRParser {
    protected HashMap<String, Integer>[] gotoTable;
    protected HashMap<String, Action>[] actionTable;
    protected Grammar grammar;
    protected ArrayList<Production> reductSeq;
    private StringBuilder action_state;
    private StringBuilder errorMessage;
    private ArrayList<tokenStr> tokenStrs;


    public LRParser(Grammar grammar) {
        action_state=new StringBuilder();
        errorMessage=new StringBuilder();
        this.grammar = grammar;
        reductSeq = new ArrayList<>();
    }

    protected abstract void createGoToTable();

    protected abstract void createActionTable();

    public TreeItem<String> constructTree() {
        for (Production production : reductSeq) {
            System.out.println(production);
        }
        Stack<tokenStr> tokenStack = new Stack<>();
        for (tokenStr token : tokenStrs) {
            tokenStack.push(token);
            //          System.out.println(token);
        }
        Stack<TreeItem<String>> treeStack = new Stack<>();
        int index = 0;
        TreeItem<String> root = new TreeItem<> (reductSeq.get(index).getLeft());
        root.setExpanded(true);
        treeStack.push(root);
        while (!treeStack.empty()) {
            TreeItem<String> father = treeStack.pop();
            father.setExpanded(true);
            if (father.getValue().equals("empty")) {
                continue;
            }
            if (!tokenStack.empty() && father.getValue().equals(tokenStack.peek().getCatagory())) {
                tokenStr currentToken = tokenStack.pop();
                StringBuilder content = new StringBuilder();
                content.append(father.getValue());
                if (!currentToken.getValue().equals(currentToken.getCatagory())) {
                    content.append(" :" + currentToken.getValue());
                }
                content.append(" (" + currentToken.getLevel() + ")");
                father.setValue(content.toString());
                // I'm god of algorithm
                while (father.previousSibling() == null && father != root) {
                    TreeItem<String> grandFather = (TreeItem<String>) father.getParent();
                    grandFather.setExpanded(true);
                    grandFather.setValue(grandFather.getValue() + " (" + currentToken.getLevel() + ")");
                    father = grandFather;
                }

                continue;
            }
            if (!father.getValue().equals(reductSeq.get(index).getLeft())) {
                continue;
            }
            for (String right : reductSeq.get(index).getRights()) {
                TreeItem<String> son = new TreeItem<String>(right);
                son.setExpanded(true);
                //DefaultMutableTreeNode son = new DefaultMutableTreeNode(right);
                father.getChildren().add(son);
                treeStack.push(son);
            }
            index++;
        }
        return root;
    }

    public String parse(ArrayList<tokenStr> inputs) {
        tokenStrs = new ArrayList<>(inputs);
        inputs.add(new tokenStr(0, "$", ""));
        int index = 0;

        Stack<String> symbolStack = new Stack<>();// 文法符号栈
        Stack<String> stateStack = new Stack<>();// 状态栈
        Stack<Action> actionStack = new Stack<>();// 记录做过的动作，错误处理用
        stateStack.add("0");// 初始状态
        while (index < inputs.size()) {
            int state = Integer.valueOf(stateStack.peek());
            String nextInput = "";
            Action action;
//            if(state == 7 && inputs.get(index).getValue().equals("*")){
//                index ++;
//                continue;
//            }else{
//                nextInput = inputs.get(index).getCatagory();
//                action = actionTable[state].get(nextInput);// 获取对应动作
//            }
            nextInput = inputs.get(index).getCatagory();
            action = actionTable[state].get(nextInput);// 获取对应动作
            action_state.append("state=" + state + " input=" + nextInput + "\n");
            if (action != null) {
                action_state.append("action=" + action.getType() + "\n\n");
            } else {
                action_state.append("action=" + "null" + "\n\n");
            }
            if (action == null) {// 错误处理
                System.out.println("错误输入:" + nextInput + "  当前状态:" + state);

                if (inputs.get(index).getCatagory().equals("}")) {
                    errorMessage.append("测试文件第"+inputs.get(index).getLevel()+"行的输入"+nextInput+"是单独的右括号！\n");
                   // errorModel.addRow(new Object[]{inputs.get(index).getLevel(), nextInput, "单独的右括号"});
                    inputs.remove(index);
                    tokenStrs.remove(index);
                } else if(Lexer.isKeyword(inputs.get(index).getCatagory())){
                    errorMessage.append("测试文件第"+inputs.get(index).getLevel()+"行的输入"+nextInput+"是处于错误位置的关键字！\n");
                    // errorModel.addRow(new Object[]{inputs.get(index).getLevel(), nextInput, "语法错误"});
                } else if(inputs.get(index).getCatagory().equals("id")){
                    errorMessage.append("测试文件第"+inputs.get(index).getLevel()+"行的输入"+nextInput+"是尚未定义的id！\n");
                   // errorModel.addRow(new Object[]{inputs.get(index).getLevel(), nextInput, "语法错误"});
                } else if(inputs.get(index).getCatagory().equals("num")) {
                    errorMessage.append("测试文件第" + inputs.get(index).getLevel() + "行的输入" + nextInput + "是处于非法位置的数字常量\n");
                } else if(inputs.get(index).getCatagory().equals("string const")) {
                    errorMessage.append("测试文件第" + inputs.get(index).getLevel() + "行的输入" + nextInput + "是处于非法位置的字符串常量\n");
                } else {
                    errorMessage.append("测试文件第" + inputs.get(index).getLevel() + "行的输入" + nextInput + "有语法错误！\n");
                }
                while (gotoTable[Integer.valueOf(stateStack.peek())].size() == 0) {
                    stateStack.pop();
                    symbolStack.pop();
                    if (actionStack.pop().getType() == ActionType.SHIFT) {
                        System.out.println("删除栈内:" + inputs.remove(index - 1));
                        tokenStrs.remove(index - 1);
                        index--;
                    }
                }
                state = Integer.valueOf(stateStack.peek());
                String A = (String) gotoTable[state].keySet().toArray()[0];
                System.out.println("goto表中存在跳转的状态:" + state + "  对应的跳转非终结符:" + A);
                while (true) {
                    nextInput = inputs.get(index).getCatagory();
                    System.out.println("恐慌模式输入：" + nextInput);
                    if (!grammar.getFollows().get(A).contains(nextInput)) {
                        //index++;
                        System.out.println("删除栈外:" + inputs.remove(index));
                        tokenStrs.remove(index);
                        continue;
                    }
                    stateStack.push(gotoTable[state].get(A) + "");
                    symbolStack.push(A);
                    actionStack.push(new Action(ActionType.REDUCE, 0));
                    break;
                }
                continue;
            } else if (action.getType() == ActionType.SHIFT) {// 移入动作
                symbolStack.push(nextInput);// 文法符号栈压入输入符号
                stateStack.push(action.getOperand() + "");// 状态栈压入下一状态
                actionStack.push(action);
                index++;
            } else if (action.getType() == ActionType.REDUCE) {// 归约动作
                int prodIndex = action.getOperand();// 归约应用的产生式标号
                Production production = grammar.getProductions().get(prodIndex);
                System.out.println(production);
                reductSeq.add(production);
                String left = production.getLeft();
                int popLength = production.getRights()[0].equals("empty") ? 0 : production.getRights().length;

                for (int i = 0; i < popLength; i++) {// 对状态栈和文法符号栈进行弹出
                    stateStack.pop();
                    symbolStack.pop();
                }
                symbolStack.push(left);// 压入产生式左部符号
                int nowState = Integer.parseInt(stateStack.peek());
                int nextState = gotoTable[nowState].get(left);// 获取下一状态
                stateStack.push(nextState + "");
                actionStack.push(action);
            } else if (action.getType() == ActionType.ACCEPT) {
                reductSeq.add(grammar.getProductions().get(0));
                Collections.reverse(reductSeq);
                return errorMessage.toString();
            }
        }
        return errorMessage.toString();
    }
}
