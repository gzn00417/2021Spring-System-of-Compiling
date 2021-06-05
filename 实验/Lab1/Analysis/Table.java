package Lexical.Analysis;

import Lexical.Tool.Tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Table {

    public static List<String> table=new ArrayList<>();
    public static List<String> keywords=new ArrayList<>();

    static {
        try {
            table = Tool.ReadFile("src/lexical/table.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).charAt(0) != '_') {
                keywords.add(table.get(i));
            }
        }
    }

    public static boolean contains(String s){
        if (s==null){
            return false;
        }
        for (int i = 0; i <keywords.size() ; i++) {
            String inTable=keywords.get(i);
            if (inTable.equals(s)){
                return true;
            }
        }
        return false;
    }

    public static boolean contains(char c){
        for (int i = 0; i <keywords.size() ; i++) {
            String inTable=keywords.get(i);
            if (inTable.equals(String.valueOf(c))){
                return true;
            }
        }
        return false;
    }

    public static int getPosition(String s){
        return table.indexOf(s);
    }

    public static int getPosition(char c){
        return table.indexOf(String.valueOf(c));
    }


}
