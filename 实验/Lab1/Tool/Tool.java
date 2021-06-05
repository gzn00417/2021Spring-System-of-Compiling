package Lexical.Tool;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Tool {

    public static List<String> ReadFile(String filename) throws IOException {
        FileInputStream inputStream = new FileInputStream(filename);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> list=new ArrayList<>();
        String str=null;
        while ((str=bufferedReader.readLine())!=null){
            list.add(str);
        }
        inputStream.close();
        bufferedReader.close();
        return list;
    }

    public static void WriteFile(String filename,List<String> WriteList) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename)) ;
        for (int i = 0; i <WriteList.size() ; i++) {
            bw.write(WriteList.get(i));
            bw.newLine();
        }
        bw.flush();
        bw.close();
    }

    public static boolean isdigit(char x){
        return x<='9'&&x>='0';
    }

    public static boolean isletter(char x){
        return x<='z'&&x>='a'||x<='Z'&&x>='A';
    }

    public static boolean isunderline(char x) {
        return x == '_';
    }
}
