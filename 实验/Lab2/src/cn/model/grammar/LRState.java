package cn.model.grammar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * created by meizhimin on 2021/4/14
 * LR自动机中的状态
 */
public class LRState {
    private LinkedHashSet<LRItem> items;
    private HashMap<String, LRState> transition;

    public LRState(Grammar grammar,HashSet<LRItem> coreItems) {
        items = new LinkedHashSet<>(coreItems);
        transition = new HashMap<>();
        closure(grammar);
    }

    private void closure(Grammar grammar) {
        boolean changed = false;
        do {
            changed = false;
            HashSet<LRItem> temp = new HashSet<>();
            for(LRItem item:items){
                if(item.getCurrentTerminal() != null && grammar.containsNonterminal(item.getCurrentTerminal())) {
                    HashSet<Production> productions = grammar.getProdByNonterminal(item.getCurrentTerminal());
                    temp.addAll(createLRItems(productions));
                }
            }
            if(!items.containsAll(temp)){
                items.addAll(temp);
                changed = true;
            }
        }while (changed);
    }

    public HashSet<LRItem> createLRItems(HashSet<Production> productions){
        HashSet<LRItem> LRItems = new HashSet<>();
        for(Production production:productions){
            LRItems.add(new LRItem(production));
        }
        return LRItems;
    }

    public void addTransition(String s,LRState state){
        transition.put(s, state);
    }

    public HashSet<LRItem> getItems(){
        return items;
    }

    public HashMap<String, LRState> getTransition() {
        return transition;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(LRItem item:items){
            sb.append(item.toString() + "\n");
        }
        return sb.toString();
    }
}
