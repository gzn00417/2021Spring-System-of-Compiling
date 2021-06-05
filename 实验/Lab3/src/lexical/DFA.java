package lexical;

public class DFA {

    public static int stringDFA(char c, int state) {
        if (state == 0 && c == '\"')
            return 1;
        if (state == 1)
            if (c == '\\')
                return 2;
            else if (c == '"') return 3;
            else return 1;
        if (state == 2) return 1;
        return -1;
    }

    public static int digitDFA(char c, int state) {
        if (state == 0 && LexicalAnalyze.isDigit(c))
            return 1;
        if (state == 1)
            if (LexicalAnalyze.isDigit(c))
                return 1;
            else if (c == '.') return 2;
            else if (c == 'e' || c == 'E') return 4;
        if (state == 2)
            if (LexicalAnalyze.isDigit(c))
                return 3;
        if (state == 3)
            if (LexicalAnalyze.isDigit(c))
                return 3;
            else if (c == 'e' || c == 'E') return 4;
        if (state == 4)
            if (c == '-' || c == '+' )
                return 5;
            if (LexicalAnalyze.isDigit(c)) return 6;
        if (state == 5)
            if (LexicalAnalyze.isDigit(c)) return 6;
        return -1;
    }

    public static int hexAndOctDFA(char c, int state) {
        if (state == 0 && c == '0') return 1;
        if (state == 1)
            if (c == 'x' || c == 'X') return 2;
            else if (c>='0' && c <= '7') return 4;
            else return -1;
        if (state == 2)
            if (LexicalAnalyze.isHexDigit(c)) return 3;
            else return 5; // 十六进制错误
        if (state == 3)
            if (LexicalAnalyze.isHexDigit(c)) return 3;
            else return 5;
        if (state == 4)
            if (c>='0' && c <= '7') return 4;
            else return 6; // 八进制错误
        return -1;
    }

    public static int commentDFA(char c, int state) {
        if (state == 0 && c == '/')
            return 1;
        if (state == 1 && c == '*')
            return 2;
        if (state == 2)
            if (c == '*') return 3;
            else return 2;
        if (state == 3)
            if (c == '/') return 4;
            else if (c == '*') return 3;
            else return 2;
        else return -1;
    }
}
