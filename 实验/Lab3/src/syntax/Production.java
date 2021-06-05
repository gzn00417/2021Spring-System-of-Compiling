package syntax;

import java.util.ArrayList;
import java.util.List;

/**
 * 产生式
 */
public class Production {
    private final String left;  // 产生式左部
    private final List<String> rights;  //产生式右部

    //构造函数要求输入产生式字符串
    public Production(String s) {
        rights = new ArrayList<>();
        String[] div = s.split("->");
        this.left = div[0].trim();
        String[] v = div[1].split(" ");
        for (String value : v) {
            if (!value.trim().equals("")) {
                rights.add(value.trim());
            }
        }
    }

    public String getLeft() {
        return left;
    }

    public List<String> getRights() {
        return rights;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(left + " -> ");
        for (String r : rights) {
            result.append(r);
            result.append(" ");
        }
        return result.toString().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (! (o instanceof Production))
            return false;
        Production d = (Production) o;
        return this.toString().equals(d.toString());
    }
}
