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

        Stack<String> symbolStack = new Stack<>();// ???????????????
        Stack<String> stateStack = new Stack<>();// ?????????
        Stack<Action> actionStack = new Stack<>();// ???????????????????????????????????????
        stateStack.add("0");// ????????????
        while (index < inputs.size()) {
            int state = Integer.valueOf(stateStack.peek());
            String nextInput = "";
            Action action;
//            if(state == 7 && inputs.get(index).getValue().equals("*")){
//                index ++;
//                continue;
//            }else{
//                nextInput = inputs.get(index).getCatagory();
//                action = actionTable[state].get(nextInput);// ??????????????????
//            }
            nextInput = inputs.get(index).getCatagory();
            action = actionTable[state].get(nextInput);// ??????????????????
            action_state.append("state=" + state + " input=" + nextInput + "\n");
            if (action != null) {
                action_state.append("action=" + action.getType() + "\n\n");
            } else {
                action_state.append("action=" + "null" + "\n\n");
            }
            if (action == null) {// ????????????
                System.out.println("????????????:" + nextInput + "  ????????????:" + state);

                if (inputs.get(index).getCatagory().equals("}")) {
                    errorMessage.append("???????????????"+inputs.get(index).getLevel()+"????????????"+nextInput+"????????????????????????\n");
                   // errorModel.addRow(new Object[]{inputs.get(index).getLevel(), nextInput, "??????????????????"});
                    inputs.remove(index);
                    tokenStrs.remove(index);
                } else if(Lexer.isKeyword(inputs.get(index).getCatagory())){
                    errorMessage.append("???????????????"+inputs.get(index).getLevel()+"????????????"+nextInput+"????????????????????????????????????\n");
                    // errorModel.addRow(new Object[]{inputs.get(index).getLevel(), nextInput, "????????????"});
                } else if(inputs.get(index).getCatagory().equals("id")){
                    errorMessage.append("???????????????"+inputs.get(index).getLevel()+"????????????"+nextInput+"??????????????????id???\n");
                   // errorModel.addRow(new Object[]{inputs.get(index).getLevel(), nextInput, "????????????"});
                } else if(inputs.get(index).getCatagory().equals("num")) {
                    errorMessage.append("???????????????" + inputs.get(index).getLevel() + "????????????" + nextInput + "????????????????????????????????????\n");
                } else if(inputs.get(index).getCatagory().equals("string const")) {
                    errorMessage.append("???????????????" + inputs.get(index).getLevel() + "????????????" + nextInput + "???????????????????????????????????????\n");
                } else {
                    errorMessage.append("???????????????" + inputs.get(index).getLevel() + "????????????" + nextInput + "??????????????????\n");
                }
                while (gotoTable[Integer.valueOf(stateStack.peek())].size() == 0) {
                    stateStack.pop();
                    symbolStack.pop();
                    if (actionStack.pop().getType() == ActionType.SHIFT) {
                        System.out.println("????????????:" + inputs.remove(index - 1));
                        tokenStrs.remove(index - 1);
                        index--;
                    }
                }
                state = Integer.valueOf(stateStack.peek());
                String A = (String) gotoTable[state].keySet().toArray()[0];
                System.out.println("goto???????????????????????????:" + state + "  ???????????????????????????:" + A);
                while (true) {
                    nextInput = inputs.get(index).getCatagory();
                    System.out.println("?????????????????????" + nextInput);
                    if (!grammar.getFollows().get(A).contains(nextInput)) {
                        //index++;
                        System.out.println("????????????:" + inputs.remove(index));
                        tokenStrs.remove(index);
                        continue;
                    }
                    stateStack.push(gotoTable[state].get(A) + "");
                    symbolStack.push(A);
                    actionStack.push(new Action(ActionType.REDUCE, 0));
                    break;
                }
                continue;
            } else if (action.getType() == ActionType.SHIFT) {// ????????????
                symbolStack.push(nextInput);// ?????????????????????????????????
                stateStack.push(action.getOperand() + "");// ???????????????????????????
                actionStack.push(action);
                index++;
            } else if (action.getType() == ActionType.REDUCE) {// ????????????
                int prodIndex = action.getOperand();// ??????????????????????????????
                Production production = grammar.getProductions().get(prodIndex);
                System.out.println(production);
                reductSeq.add(production);
                String left = production.getLeft();
                int popLength = production.getRights()[0].equals("empty") ? 0 : production.getRights().length;

                for (int i = 0; i < popLength; i++) {// ??????????????????????????????????????????
                    stateStack.pop();
                    symbolStack.pop();
                }
                symbolStack.push(left);// ???????????????????????????
                int nowState = Integer.parseInt(stateStack.peek());
                int nextState = gotoTable[nowState].get(left);// ??????????????????
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
