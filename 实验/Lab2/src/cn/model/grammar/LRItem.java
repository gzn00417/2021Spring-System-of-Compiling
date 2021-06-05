package cn.model.grammar;

import java.util.Arrays;
import java.util.Objects;

/**
 * created by meizhimin on 2021/4/14
 */
public class LRItem extends Production{
    protected int dot;

    public LRItem(String left, String[] right, int dot) {
        super(left, right);
        this.dot = dot;
    }

    public LRItem(Production p){
        super(p.getLeft(),p.getRights());
        int finished = 0;
        if (p.getRights().length == 1 && p.getRights()[0].equals("empty")) {
            finished = 1;//如果产生式右侧为空，将·放在位置1
        }
        dot = finished;
    }

    public LRItem(LRItem item){
        super(item.getLeft(), item.getRights());
        this.dot = item.getDot();
    }

    public int getDot() {
        return dot;
    }

    public boolean moveDot(){
        if (dot >= right.length) {
            return false;
        }
        dot++;
        return true;
    }

    public String getCurrentTerminal() {
        if(dot == right.length){
            return null;
        }
        return right[dot];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LRItem item = (LRItem) o;
        if(!this.left.equals(item.left)){
            return false;
        }
        if(this.dot != item.dot){
            return false;
        }
        if(!Arrays.equals(this.right, item.right)){
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.dot;
        hash = 89 * hash + Objects.hashCode(this.left);
        hash = 89 * hash + Arrays.deepHashCode(this.right);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(left + " -> ");
        for (int i = 0; i < right.length; i++) {
            if (i == dot) {
                str.append(".");
            }
            str.append(right[i]);
            if(i != right.length - 1){
                str.append(" ");
            }
        }
        if (right.length == dot) {
            str.append(".");
        }
        return str.toString();
    }

}

