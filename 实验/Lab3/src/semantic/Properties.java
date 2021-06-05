package semantic;

import java.util.List;

public class Properties {
    private String name;  // 变量或者函数的name
    private String type;  // 节点类型
    private String offset;  // 数组类型的属性
    private int width;  // 类型大小属性
    private Array array;  // 数组类型属性
    private String addr;  // 表达式类型的属性
    private int quad;  // 回填用到的属性,指令位置
    private List<Integer> trueList;
    private List<Integer> falseList;
    private List<Integer> nextList;

    public Properties(String type, int width) {
        this(type, width, null, null, null, null, 0);
    }

    public Properties(Array array, String type, int width) {
        this(type, width, array, null, null, null, 0);
    }

    public Properties(String addr, String type) {
        this(type, 0, null, addr, null, null, 0);
    }

    public Properties(String addr) {
        this(null, 0, null, addr, null, null, 0);
    }

    public Properties(String name, String type, String offset) {
        this(type, 0, null, null, name, offset, 0);
    }

    public Properties(int quad) {
        this(null, 0, null, null, null, null, quad);
    }

    public Properties(String type, int width, Array array, String addr, String name, String offset, int quad) {
        this.type = type;
        this.addr = addr;
        this.name = name;
        this.offset = offset;
        this.quad = quad;
        this.width = width;
        this.array = array;
    }

    public Properties(List<Integer> trueList, List<Integer> falseList) {
        this.trueList = trueList;
        this.falseList = falseList;
    }

    public Properties(List<Integer> nextList) {
        this.nextList = nextList;
    }

    public String getName() {
        return name;
    }

    public Array getArray() {
        return array;
    }

    public String getAddr() {
        return addr;
    }

    public String getType() {
        return type;
    }

    public String getOffset() {
        return offset;
    }

    public int getWidth() {
        return width;
    }

    public int getQuad() {
        return quad;
    }

    public List<Integer> getNext() {
        return nextList;
    }

    public List<Integer> getTrue() {
        return trueList;
    }

    public List<Integer> getFalse() {
        return falseList;
    }
}