package semantic;

public class Symbol {
    private final String name;  // 符号名
    private final String type;  // 符号类型
    private final int offset;  // 偏移量

    public Symbol(String name, String type, int offset) {
        this.name = name;
        this.type = type;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getOffset() {
        return offset;
    }

    public String toString() {
        return "(" + name + ", " + type + ", " + offset + ")";
    }
}
