package Lexical.Analysis;

import Lexical.DFA.DigitDFA;
import Lexical.DFA.WordDFA;
import Lexical.Tool.SymbolTable;

public class Token {

    public static void getWordToken(String line, int lineNumber){
        String s= WordDFA.getWord(line.substring(Main.pos));
        if (s.equals("ERROR")){
            String output="Line:"+lineNumber+":单词识别发生错误  "+s;
            System.err.println(output);
        }
        //关键字
        else if (Table.contains(s)){
            String output=s+"  "+"< "+Table.getPosition(s)+" , "+"_"+" >"+"   Line:"+lineNumber;
            Main.WriteList.add("< "+Table.getPosition(s)+" , "+"_"+" > "+lineNumber);
            System.out.println(output);
        }
        //标识符
        else{
            int pos=SymbolTable.addSymbolTable(s);
            String output=s+"  "+"< "+0+" , "+pos+" >"+"   Line:"+lineNumber;
            Main.WriteList.add("< "+0+" , "+pos+" > "+lineNumber);
            System.out.println(output);
        }
    }

    public static void getDigitsToken(String line, int lineNumber){
        String d= DigitDFA.getDigit(line.substring(Main.pos));
        if (d.equals("ERROR")){
            String output="Line:"+lineNumber+":数字识别发生错误  "+d;
            System.err.println(output);
        }
        else{
            if (d.contains(".")||d.contains("E")){
                SymbolTable.constFloat.add(d);
                String output=d+"  "+"< "+2+" , "+d+" >"+"   Line:"+lineNumber;
                Main.WriteList.add("< "+2+" , "+d+" > "+lineNumber);
                System.out.println(output);
            }else{
                SymbolTable.constInt.add(d);
                String output=d+"  "+"< "+1+" , "+d+" >"+"   Line:"+lineNumber;
                Main.WriteList.add("< "+1+" , "+d+" > "+lineNumber);
                System.out.println(output);
            }
        }
    }

    public static void handleAnnotation(String line, int lineNumber){
        if (!Main.annotation){
            char x=line.charAt(Main.pos);
            if (x=='/'){
                if (Main.pos+1<line.length()){
                    char y=line.charAt(Main.pos+1);
                    if (y=='*'){
                        Main.annotation=true;
                        Main.pos+=2;
                    }
                }
            }
            else {
                return;
            }
        }
        if (Main.annotation){
            while (Main.pos<line.length()){
                char x=line.charAt(Main.pos);
                Main.pos++;
                if (Main.pos<line.length()) {
                    char y = line.charAt(Main.pos);
                    if (x=='*'&&y=='/'){
                        Main.annotation=false;
                        Main.pos++;
                        break;
                    }
                }
            }
        }
    }

    public static void getOthers(String line, int lineNumber){
        char x=line.charAt(Main.pos);
        if (Main.pos+1<line.length()){
            char y=line.charAt(Main.pos+1);
            String temp=String.valueOf(x)+String.valueOf(y);
            if (Table.contains(temp)){
                String output=temp+"  "+"< "+Table.getPosition(temp)+" , "+"_"+" >"+"   Line:"+lineNumber;
                Main.WriteList.add("< "+Table.getPosition(temp)+" , "+"_"+" > "+lineNumber);
                System.out.println(output);
                Main.pos+=2;
                return;
            }
        }
        if (Table.contains(x)){
            int tpos=Main.pos;
            if (x=='"'){
                Main.pos++;
                StringBuilder string=new StringBuilder();
                string.append(x);
                while (Main.pos<line.length()){
                    char y=line.charAt(Main.pos);
                    string.append(y);
                    if (y=='"'){
                        String output=string+"  "+"< "+3+" , "+string+" >"+"   Line:"+lineNumber;
                        Main.WriteList.add("< "+3+" , "+string+" > "+lineNumber);
                        System.out.println(output);
                        Main.pos++;
                        return;
                    }
                    Main.pos++;
                }
                if (Main.pos==line.length()){
                    String output="Line:"+lineNumber+":字符串未封闭  "+line.substring(tpos);
                    System.err.println(output);
                    return;
                }
            }
            String output=x+"  "+"< "+Table.getPosition(x)+" , "+"_"+" >"+"   Line:"+lineNumber;;
            System.out.println(output);
            Main.WriteList.add("< "+Table.getPosition(x)+" , "+"_"+" > "+lineNumber);
            Main.pos++;
        }
        else {
            String output="Line:"+lineNumber+":发现非法字符  "+line.charAt(Main.pos);
            Main.pos++;
            System.err.println(output);
        }
    }
}
