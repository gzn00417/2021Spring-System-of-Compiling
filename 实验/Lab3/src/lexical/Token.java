package lexical;

import java.util.ArrayList;
import java.util.List;

public class Token {
    private String tokenName;
    private final String attributeValue;
    private int lineNumber;
    private final List<Token> children;

    public Token(String tokenName, String attributeValue, int lineNumber){
        this.tokenName = tokenName;
        this.attributeValue = attributeValue;
        this.lineNumber = lineNumber;
        this.children = new ArrayList<>();
    }

    public void setTokenName(String newName) {
        this.tokenName = newName;
    }

    public String getTokenName() {
        return tokenName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void addChildren(Token token) {
        children.add(token);
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public List<Token> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "<" + tokenName + ", " + attributeValue + ">";
    }

    @Override
    public int hashCode() {
        return (tokenName + attributeValue).hashCode();
    }
}
