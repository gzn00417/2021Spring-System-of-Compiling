package semantic;

public class Array {
    // e.g. array(2,array(2,integer))
    private final int length;  // 长度：2
    private final Array type;  // 数组类型：array(2,integer)
    private final String baseType;  // 基本类型：integer

    public Array(int length, String baseType, Array type) {
        this.length = length;
        this.type = type;
        this.baseType = baseType;
    }

    public int getLength() {
        return length;
    }

    public Array getType() {
        return type;
    }

    public String getBaseType() {
        return baseType;
    }
}
