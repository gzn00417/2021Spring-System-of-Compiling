package cn.model.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * created by meizhimin on 2021/4/14
 * 产生式类
 */
public class Production {
    String left;
    String[] right;

    // 初始化select集
    ArrayList<String> select = new ArrayList<String>();
    public Production(String left, String[] right){
        this.left = left;
        this.right = right;
    }

    public String[] getRights(){
        return right;
    }

    public String getLeft(){
        return left;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Production that = (Production) o;
        return Objects.equals(left, that.left) &&
                Arrays.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(left);
        result = 31 * result + Arrays.hashCode(right);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(left + "->");
        for(int i=0;i<right.length;i++){
            // 空格“ ”视为2个产生式之间的间隔
            sb.append(right[i] + " ");
        }
        return sb.toString();
    }
}
