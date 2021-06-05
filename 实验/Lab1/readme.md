1.DFA类
①public static String getDigit(String input) 
识别数字的DFA

②public static String getWord(String input) 
识别单词的DFA

2.Tool类
①public static boolean isunderline(char x)
判断字符是否是下划线

②public static boolean isletter(char x)
判断字符是否是字母

③public static boolean isdigit(char x)
判断字符是否是数字

3.Analysis包
①private static void lineAnalysis(String line,int LineNumber) 
按行进行词法分析

②private static void classifyType(char x, String line, int lineNumber)
判断单词的类别

③ public static void handleAnnotation(String line, int lineNumber)
处理注释的函数
