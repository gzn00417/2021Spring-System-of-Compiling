package Lexical.Analysis;

import Lexical.Tool.SymbolTable;
import Lexical.Tool.Tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static Lexical.Tool.Tool.isdigit;
import static Lexical.Tool.Tool.isletter;
import static Lexical.Tool.Tool.isunderline;

public class Main {

    public static int pos = 0;
    public static boolean annotation = false;
    public static List<String> WriteList=new ArrayList<>();

    public static void analysis(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            pos = 0;
            lineAnalysis(line,i+1);
        }
    }

    private static void lineAnalysis(String line,int LineNumber) {
        if (line == null || line.equals("")) {
            return;
        }
        while (pos < line.length()) {
            char x = line.charAt(pos);
            classifyType(x, line,LineNumber);
        }
    }

    private static void classifyType(char x, String line, int lineNumber) {
        if (x==' '||x=='\t'){
            Main.pos++;
            return;
        }
        Token.handleAnnotation(line,lineNumber);
        if (Main.pos>=line.length()){
            return;
        }
        if (isletter(x) || isunderline(x)) {
            Token.getWordToken(line,lineNumber);
        } else if (isdigit(x)) {
            Token.getDigitsToken(line,lineNumber);
        } else {
            Token.getOthers(line,lineNumber);
        }
    }

    public static void LexicalAnalysis() throws IOException {
        List<String> lines = Tool.ReadFile("test.txt");
        analysis(lines);
        if (annotation){
            System.err.println("注解未完");
        }
        Tool.WriteFile("LexicalResult.txt",WriteList);
        SymbolTable.WriteFile("SymbolTable.txt");
    }

    public static void main(String[] args) throws IOException {
        List<String> lines = Tool.ReadFile("test2.txt");
        analysis(lines);
//        System.out.println("~~~"+WriteList);
        if (annotation){
            System.err.println("注解未完");
        }
//        System.out.println("~~~"+WriteList);
        Tool.WriteFile("LexicalResult.txt",WriteList);
        SymbolTable.WriteFile("SymbolTable.txt");
    }
}
