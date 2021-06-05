package Lexical.Tool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {

    private static List<String> symbolTable=new ArrayList<>();
    private static Map<String,String> symbolTypeTable=new HashMap<>();
    public static List<String> constInt=new ArrayList<>();
    public static int intConstNum=0;
    public static List<String> constFloat=new ArrayList<>();
    public static int floatConstNum=0;


    public static int addSymbolTable(String s){
        if (symbolTable.contains(s)){
            return symbolTable.indexOf(s);
        }
        else{
            symbolTable.add(s);
            return symbolTable.size()-1;
        }
    }

    public static int getPosition(String s){
        if (symbolTable.contains(s)){
            return symbolTable.indexOf(s);
        }
        return -1;
    }

    public static String getType(String id){
        return symbolTypeTable.get(id);
    }

    public static boolean hasType(String id){
        return symbolTypeTable.containsKey(id);
    }

    public static void addIDtype(String id,String type){
        symbolTypeTable.put(id,type);
    }

    public static String getString(int x){
        return symbolTable.get(x);
    }

    public static void WriteFile(String filename) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename)) ;
        for (int i = 0; i <symbolTable.size() ; i++) {
            bw.write(i+"\t\t"+symbolTable.get(i));
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }

}
