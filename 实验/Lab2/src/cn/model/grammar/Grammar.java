package cn.model.grammar;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * created by meizhimin on 2021/4/14
 */
public class Grammar {
    // 成员变量,产生式集，终结符集，非终结符集
    private List<Production> productions;
    private List<String> terminals;
    private List<String> nonterminals;
    private Map<String, List<String>> firsts;
    private Map<String, List<String>> follows;

    public Grammar(){
        productions = new ArrayList<>();
        terminals = new ArrayList<>();
        nonterminals = new ArrayList<>();
        firsts = new HashMap<>();
        follows = new HashMap<>();

        setProductions();
        setNonTerminals();
        setTerminals();

        setFirst();
        setFollow();
        //        getSelect();

        //        Predict();

    }


    // 从文件中读取产生式
    public void setProductions(){
        try {
            File file = new File("res/语法规则.txt");
            RandomAccessFile randomfile = new RandomAccessFile(file, "r");
            String line;
            String left;
            String[] rights;
            while ((line=randomfile.readLine())!=null) {
                left = line.split("->")[0].trim();
                rights = line.split("->")[1].trim().split("\\|");
                for(int i=0;i<rights.length;i++){
                    Production production = new Production(left, rights[i].trim().split(" "));
                    productions.add(production);
                    //                    System.out.println(production);
                }
            }
            randomfile.close();
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }

    }

    // 从产生式中扫描出非终结符
    private void setNonTerminals() {
        for(Production production:productions){
            if(!nonterminals.contains(production.getLeft())){
                nonterminals.add(production.getLeft());
            }
        }
    }

    // 从产生式中扫描出终结符
    private void setTerminals() {
        String[] rights;
        for(Production production:productions){
            rights = production.getRights();
            for(int i=0;i<rights.length;i++){
                if(nonterminals.contains(rights[i]) || rights[i].equals("empty")){
                    continue;
                }else if(!terminals.contains(rights[i])){
                    terminals.add((rights[i]));
                }
            }
        }
    }

    private void setFirst() {
        List<String> first;
        //为每个终结符计算first集
        for(int i=0;i<terminals.size();i++){
            first = new ArrayList<>();
            first.add(terminals.get(i));
            firsts.put(terminals.get(i), first);
        }
        //为每个非终结符初始化first集
        for(int i=0;i<nonterminals.size();i++){
            first = new ArrayList<>();
            firsts.put(nonterminals.get(i), first);
        }
        boolean changed = false;
        while (true){
            changed = false;
            String left;
            String right = null;
            String[] rights;
            for(int i=0;i<productions.size();i++){
                left = productions.get(i).getLeft();
                rights = productions.get(i).getRights();
                int j;
                for(j=0;j<rights.length;j++){
                    right = rights[j];
                    if(!right.equals("empty")){
                        for(int k=0;k<firsts.get(right).size();k++){// A -> B C，First(A)中包含First(B)
                            if(!firsts.get(left).contains(firsts.get(right).get(k))){
                                firsts.get(left).add((firsts.get(right).get(k)));
                                changed = true;
                            }
                        }
                    }
                    if(canNull(right)){// 若B可空，继续循环将First(C)赋给First(A)
                        continue;
                    }else {// 若B不可空，跳出循环
                        break;
                    }
                }
                if(j == rights.length && canNull(right) && !firsts.get(left).contains("empty")){//如果产生式右端所有文法符号都可空
                    firsts.get(left).add("empty");
                }
            }
            if(changed == false){
                break;
            }
        }
    }

    private void setFollow() {
        //为所有非终结符初始化follow集
        List<String> follow;
        for(int i=0;i<nonterminals.size();i++){
            follow = new ArrayList<>();
            follows.put(nonterminals.get(i), follow);
        }
        follows.get("Program").add("$");//初始符号的follow集包含$
        boolean changed = false;
        //        boolean lastIsNonternimal = false;//标志产生式最后一个文法符号是否为非终结符
        while(true){
            changed = false;
            for(int i=0;i<productions.size();i++){
                String left = null;
                String right = null;
                String[] rights;
                rights = productions.get(i).getRights();
                for(int j=0;j<rights.length;j++){
                    right = rights[j];

                    if(nonterminals.contains(right)){
                        //                        lastIsNonternimal = true;
                        for(int k=j+1;k<rights.length;k++){ // A -> B C D，将First(C)赋给Follow(B)
                            for(int l=0;l<firsts.get(rights[k]).size();l++){
                                //将后一个元素的first集加入到前一个元素的follow集中
                                if(!follows.get(right).contains(firsts.get(rights[k]).get(l)) && !firsts.get(rights[k]).get(l).equals("empty")){
                                    follows.get(right).add(firsts.get(rights[k]).get(l));
                                    changed = true;
                                }
                            }
                            if (canNull(rights[k])){// 若C可空，继续循环将First(D)赋给Follow(B)
                                continue;
                            }else {//若C不可空，跳出循环
                                break;
                            }
                        }
                        //                        if(lastIsNonternimal)
                    }
                }

                for(int j=rights.length-1;j>=0;j--){
                    right = rights[j];
                    if(nonterminals.contains(right)){
                        left = productions.get(i).getLeft();
                        for (int p=0;p<follows.get(left).size();p++){
                            if (!follows.get(right).contains(follows.get(left).get(p))){
                                follows.get(right).add(follows.get(left).get(p));
                                changed = true;
                            }
                        }
                    }
                    if(canNull(right)){
                        continue;
                    }else {
                        break;
                    }
                }
            }
            if(changed == false){
                break;
            }
        }
    }

    public List<Production> getProductions() {
        return productions;
    }

    public int findProductionIndex(Production p){
        for(int i=0;i<productions.size();i++){
            if(productions.get(i).equals(p)){
                return i;
            }
        }
        return -1;
    }

    public List<String> getNonterminals(){
        return nonterminals;
    }

    public List<String> getTerminals(){
        return terminals;
    }

    public Map<String, List<String>> getFirsts() {
        return firsts;
    }

    public Map<String, List<String>> getFollows() {
        return follows;
    }

    public boolean containsNonterminal(String s){
        if(nonterminals.contains(s)){
            return true;
        }
        return false;
    }

    public HashSet<Production> getProdByNonterminal(String nonterminal){
        HashSet<Production> result = new HashSet<>();
        for(Production production:productions){
            if(production.getLeft().equals(nonterminal)){
                result.add(production);
            }
        }
        return result;
    }

    private void getSelect() {
        String left;
        String right;
        String[] rights;
        List<String> follow = new ArrayList<>();
        List<String> first = new ArrayList<>();

        for (int i=0;i<productions.size();i++){
            left = productions.get(i).getLeft();
            rights = productions.get(i).getRights();
            if(rights[0].equals("empty")){
                follow = follows.get(left);
                for(int j=0;j<follow.size();j++){
                    if(!productions.get(i).select.contains(follow.get(j))){
                        productions.get(i).select.add(follow.get(j));
                    }
                }
            }else {
                boolean allEmpty = true;//标志产生式右部是否都可空
                for (int j=0;j<rights.length;j++){
                    right = rights[j];
                    first = firsts.get(right);
                    for(int k=0;k<first.size();k++){
                        if(!productions.get(i).select.contains(first.get(k))){
                            productions.get(i).select.add(first.get(k));
                        }
                    }
                    if(canNull(right)){
                        continue;
                    }else {
                        allEmpty = false;
                        break;
                    }
                }
                if(allEmpty){
                    follow = follows.get(left);
                    for(int j=0;j<follow.size();j++){
                        if(!productions.get(i).select.contains(follow.get(j))){
                            productions.get(i).select.add(follow.get(j));
                        }
                    }
                }
            }
        }
    }

    private void Predict() {
        Production production;
        String[] rights;
        try {
            File file = new File("predictldy.txt");
            RandomAccessFile randomfile = new RandomAccessFile(file,"rw");
            for(int i=0;i<nonterminals.size();i++){
                StringBuilder line = new StringBuilder();
                String s = nonterminals.get(i);
                line.append(s + " {");
                for(String right:follows.get(s)){
                    line.append(right + " ");
                }
                line.append("}\n");
                randomfile.writeBytes(line.toString());
                //                System.out.println(line.toString());
            }
            randomfile.close();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    private boolean canNull(String symbol){
        if(symbol.equals("empty")){
            return true;
        }
        for(Production production:productions){
            if(production.getLeft().equals(symbol) && production.getRights()[0].equals("empty")){
                return true;
            }
        }
        return false;
    }

}
