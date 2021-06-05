package cn.model.lexical;

import java.io.*;
import java.util.ArrayList;

public class Lexer {

    private String filepath;
    private cn.model.lexical.DFATableElement[] dfa;//dfa转换表
    private ArrayList<tokenStr> tokenStrs;//token序列
    private ArrayList<String> answerStrs;//返回的结果序列
    private ArrayList<String> errorStrs;//返回的错误信息序列

    public static String[] keywords = {"char", "long", "short", "float", "double", "const", "boolean", "void", "null", "false", "true", "enum", "int",
            "do", "while", "if", "else", "for", "then", "break", "continue", "class", "static", "final", "extends", "new", "return", "signed"
            , "struct", "union", "unsigned", "goto", "switch", "case", "default", "auto", "extern", "register", "sizeof", "typedef", "volatile","string"
            , "real", "proc", "record","call", "and", "or"
    };
    String[] tokens = {"char", "long", "short", "float", "double", "const", "boolean", "void", "null", "false", "true", "enum", "int",
            "do", "while", "if", "else", "for", "then", "break", "continue", "class", "static", "final", "extends", "new", "return", "signed"
            , "struct", "union", "unsigned", "goto", "switch", "case", "default", "auto", "extern", "register", "sizeof", "typedef", "volatile",
            ">", ">=", "<", "<=", "==", "!=", "|", "&", "||", "&&", "!", "^", "+", "-", "*", "/", "%", "++", "--", "+=", "-=", "*=", "/=",
            ",", "=", ";", "[", "]", "(", ")", "{", "}", ".", "\"", "'"};

    /**
     * Create the application.
     *
     * @throws Exception
     */
    public Lexer(String dfaFilePath) throws Exception {
        dfa = readDFATable.readDFAFromFile(new File(dfaFilePath));
        filepath = dfaFilePath;
        tokenStrs = new ArrayList<>();
        answerStrs = new ArrayList<>();
        errorStrs = new ArrayList<>();
        init();
    }

    public void init() throws IOException {
        String[][] result= readDFATable.getData(new File(filepath), 0);
        for(int i=1;i<result.length;i++) {
            for(int j=1;j<result[i].length-2;j++) {
                result[i][j]=result[i][j].replaceAll("-1", " ");
            }
        }
    }


    /**
     * 整个词法分析器中最核心的函数——对输入的String进行词法分析
     * @param input 待分析的String
     * @return  List<String>,包含2部分：
     * List.get(0)是识别出的token序列结果
     * List.get(1)是输入字符串中的错误信息提示
     */
    public  ArrayList<String> lexicalAnalysis(String input) {
        int level = 1;// 行号
        char[] charArray = input.toCharArray();
        String currentString = "";
        int currentState = 0;
        int lastState = 0;
        boolean readyToIncLever = false;
        for (char c : charArray) {
            if (c == '\n'&& currentState!=44) {
                readyToIncLever = true;
                continue;
            }
//            if(readyToIncLever){
//                level++;
//                readyToIncLever=false;
//            }
            if (c == ' ') {//遇到空格，读出已扫描的单词
                if (dfa[currentState].getState() == 9 || dfa[currentState].getState() == 10 || dfa[currentState].getState() == 42 || dfa[currentState].getState() == 44) {
                    //忽略注释和字符串中的空格
                    currentString = currentString + " ";
                    continue;
                }
                if (dfa[currentState].isFinish()) {//遇到终止状态
                    if (isKeyword(currentString)) {//当前单词是关键字
                        tokenStr tempStr = new tokenStr(level, null, currentString);
                        tokenStrs.add(tempStr);
                        String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+currentString+",_>\t关键字";
                        answerStrs.add(tmpAnswer);
                        System.out.println(tempStr.toString());
                    } else {//当前单词不是关键字
                        if (dfa[currentState].getType().equals("运算符")){
                            //关键字加入语法分析用token
                            tokenStr tempStr = new tokenStr(level, null, currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+currentString+",_>\t运算符";
                            answerStrs.add(tmpAnswer);
                            System.out.println(tempStr.toString());
                        }else if (dfa[currentState].getType().equals("界符")){
                            tokenStr tempStr = new tokenStr(level, null, currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+currentString+",_>\t界符";
                            answerStrs.add(tmpAnswer);
                            System.out.println(tempStr.toString());
                        }else if (dfa[currentState].getType().equals("字符常量")) {
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+"char"+","+currentString+">\t字符常量";
                            answerStrs.add(tmpAnswer);
                            //语法分析的文法中没有识别char的部分
                        } else if (dfa[currentState].getType().equals("注释")) {
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+currentString+",_>\t注释";
                            answerStrs.add(tmpAnswer);
                            //语法分析的文法中没有识别注释的部分
                        } else if (dfa[currentState].getType().equals("字符串常量")) {
                            tokenStr tempStr = new tokenStr(level, "string", currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+"String"+","+currentString+">\t字符串常量";
                            answerStrs.add(tmpAnswer);
                            System.out.println(tempStr.toString());
                        } else if (dfa[currentState].getType().equals("标识符")) {
                            tokenStr tempStr = new tokenStr(level, "id", currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+"id"+","+currentString+">\t标识符";
                            answerStrs.add(tmpAnswer);
                            System.out.println(tempStr.toString());
                        } else {
                            tokenStr tempStr = new tokenStr(level, "num", currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+"num"+","+currentString+">\t数字常量";
                            answerStrs.add(tmpAnswer);
                            System.out.println(tempStr.toString());
                        }
                    }
                    currentString = "";
                    currentState = 0;
                    if (readyToIncLever) {
                        level++;
                        readyToIncLever = false;
                    }
                    continue;
                }
            }

            // 当遇到\n且当前状态为“单行注释循环状态”时
            if(c == '\n'){
                readyToIncLever = true;
                lastState = 44;
                currentState = -1;
            }else{
                lastState = currentState;
                currentState = dfa[currentState].getNextState(c);
            }
            if (currentState < 0) {//读取字符后进入了非法状态，则判断前一状态
//                if (readyToIncLever) {
//                    level++;
//                    readyToIncLever = false;
//                }
                if (dfa[lastState].isFinish()) {//如果在读入字符进入非法状态前已经进入了终止状态，就把它提取出来
                    if (isKeyword(currentString)) {//关键字
                        tokenStr tempStr = new tokenStr(level, null, currentString);
                        tokenStrs.add(tempStr);
                        String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+currentString+",_>\t关键字";
                        answerStrs.add(tmpAnswer);
                        System.out.println(tempStr.toString());
                    } else {//不是关键字
                        if (dfa[lastState].getType().equals("运算符")) {
                            tokenStr tempStr = new tokenStr(level, null, currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+currentString+",_>\t运算符";
                            answerStrs.add(tmpAnswer);
                            System.out.println(tempStr.toString());
                        }else if(dfa[lastState].getType().equals("界符")){
                            tokenStr tempStr = new tokenStr(level, null, currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+currentString+",_>\t界符";
                            answerStrs.add(tmpAnswer);
                        } else if (dfa[lastState].getType().equals("字符常量")) {
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+"char"+","+currentString+">\t字符常量";
                            answerStrs.add(tmpAnswer);
                            //语法分析的文法中没有识别char的部分
                        } else if (dfa[lastState].getType().equals("注释")) {
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+currentString+",_>\t注释";
                            answerStrs.add(tmpAnswer);
                            //语法分析的文法中没有识别注释的部分
                        } else if (dfa[lastState].getType().equals("字符串常量")) {
                            tokenStr tempStr = new tokenStr(level, "string", currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+"String"+","+currentString+">\t字符串常量";
                            answerStrs.add(tmpAnswer);
                            System.out.println(tempStr.toString());
                        } else if (dfa[lastState].getType().equals("标识符")) {
                            tokenStr tempStr = new tokenStr(level, "id", currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+"id"+","+currentString+">\t标识符";
                            answerStrs.add(tmpAnswer);
                            System.out.println(tempStr.toString());
                        } else {
                            tokenStr tempStr = new tokenStr(level, "num", currentString);
                            tokenStrs.add(tempStr);
                            String tmpAnswer = "测试文件第"+level+"行\t"+currentString+"\t<"+"num"+","+currentString+">\t数字常量";
                            answerStrs.add(tmpAnswer);
                            System.out.println(tempStr.toString());
                        }
                    }
                } else {
                    currentString += c;
                    switch (lastState) {//下面的状态都是终止状态前的状态，如果此时读入了字符进入了非法状态，就认为是对应状态的错误输入
                        case 3:
                        case 31:
                            // TODO 添加错误信息
                            String tmpStr = "lexical error at line [Lexer.java 265]:" + "[测试文件第" + level + "行的字符串 <" + currentString + "> 发生浮点数格式错误!]\n";
                            errorStrs.add(tmpStr);
                            break;
                        case 5:
                        case 6:
                        case 33:
                        case 34:
                            // TODO 添加错误信息
                            String tmpStr2 = "lexical error at line [Lexer.java 274]:" + "[测试文件第" + level + "行的字符串 <" + currentString + "> 发生科学计数法格式错误!]\n";
                            errorStrs.add(tmpStr2);
                            // errorListModel.addRow(new Object[]{currentString, "第" + i + "字符", "错误的科学计数法格式"});
                            break;
                        case 40:
                            // TODO 添加错误信息
                            String tmpStr3 = "lexical error at line [Lexer.java 280]:" + "[测试文件第" + level + "行的字符串 <" + currentString + "> 发生字符常量格式错误!]\n";
                            errorStrs.add(tmpStr3);
                            // errorListModel.addRow(new Object[]{currentString, "第" + i + "字符", "错误的字符常量"});
                            break;
                        case 37:
                            // TODO 添加错误信息
                            String tmpStr4 = "lexical error at line [Lexer.java 286]:" + "[测试文件第" + level + "行的字符串 <" + currentString + "> 发生十六进制格式错误!]\n";
                            errorStrs.add(tmpStr4);
                            // errorListModel.addRow(new Object[]{currentString, "第" + i + "字符", "错误的十六进制格式"});
                            break;
                        case 42:
                            // TODO 添加错误信息
                            String tmpStr5 = "lexical error at line [Lexer.java 292]:" + "[测试文件第" + level + "行的字符串 <" + currentString + "> 发生字符串常量格式错误!]\n";
                            errorStrs.add(tmpStr5);
                            // errorListModel.addRow(new Object[]{currentString, "第" + i + "字符", "错误的字符串常量格式"});
                            break;
                        default:
                            if (c != ' ') {
                                // TODO 添加错误信息
                                String tmpStr6 = "lexical error at line [Lexer.java 299]:" + "[测试文件第" + level + "行的字符串 <" + currentString + "> 发生尚未定义的错误!]\n";
                                errorStrs.add(tmpStr6);
                                // errorListModel.addRow(new Object[]{currentString, "第" + i + "字符", "未定义错误"});
                            }
                            break;
                    }
                    currentString = "";
                    currentState = 0;
                    continue;
                }
                // 处理由于单行注释未被舍弃的\n
                if(c=='\n'){
                    currentString = "";
                    currentState = 0;
                    level++;
                    readyToIncLever=false;
                    continue;
                }
                currentString = "";
                currentString = currentString + c;//处理完了当前字符前面的单词，于是返回0状态，再次读取当前字符
                currentState = dfa[0].getNextState(c);
                if (currentState == -1) {//如果在0状态就扫描到非法字符
                    if (c != ' ') {
                        // TODO 添加错误信息
                        String tmpStr7 = "lexical error at line [Lexer.java 315]:" + "[测试文件第" + level + "行的字符 <" + currentString + "> 是非法字符!]\n";
                        errorStrs.add(tmpStr7);
                        // errorListModel.addRow(new Object[]{charArray[i], "第" + i + "字符", "非法字符"});
                    }
                    currentString = "";
                    currentState = 0;//要保证每次进入循环时的状态不为非法状态
                }
                if(readyToIncLever){
                    level++;
                    readyToIncLever = false;
                }
                continue;
            }
            currentString = currentString + c;//读取当前字符后没有进入非法状态，继续读入
        }
        if(currentString.length() != 0){
            // TODO 添加错误信息
            String tmpStr8 = "lexical error at line [Lexer.java 332]:"+"[测试文件第"+level+"行的字符串 <"+currentString+"> 是非法字符串!]\n";
            errorStrs.add(tmpStr8);
            // errorListModel.addRow(new Object[]{currentString, "第" + charArray.length + "字符", "非法字符串"});
        }

        StringBuilder sb = new StringBuilder();
        for (String x : answerStrs){
            sb.append(x).append("\n");
        }
        StringBuilder sb2 = new StringBuilder();
        for(String x: errorStrs){
            sb2.append(x);
        }
        ArrayList<String> result = new ArrayList<>();
        result.add(sb.toString());
        result.add(sb2.toString());
        return result;
    }

    /**
     * 从预定义的String[]中判断是否为关键词
     * @param word 待判断的字符串
     * @return  True if the word is keyword，False if not
     */
    public static boolean isKeyword(String word) {
        for (String keyword : keywords) {
            if (keyword.equals(word)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<tokenStr> getTokenStrs() {
        return this.tokenStrs;
    }
}
