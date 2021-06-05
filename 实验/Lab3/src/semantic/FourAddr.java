package semantic;

public class FourAddr {
    private final String op;  // 操作符
    private final String param1;  // 参数一
    private final String param2;  // 参数二
    private String toAddr;  // 地址

    public FourAddr(String op, String param1, String param2, String toAddr) {
        this.op = op;
        this.param1 = param1;
        this.param2 = param2;
        this.toAddr = toAddr;
    }

    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
    }

    @Override
    public String toString() {
        return "(" + op + ", " + param1 + ", " + param2 + ", " + toAddr + ")";
    }
}