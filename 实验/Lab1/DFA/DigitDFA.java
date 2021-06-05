package Lexical.DFA;

import Lexical.Analysis.Main;

import static Lexical.Tool.Tool.isdigit;

public class DigitDFA {

    static int[] Status = {0, 1, 2, 3, 4, 5, 6};
    static int startStatus = 0;
    static int[] endStatus = {1, 3, 6};

    private static boolean isEnd(int nowStatus) {
        for (int i = 0; i < endStatus.length; i++) {
            if (nowStatus == endStatus[i])
                return true;
        }
        return false;
    }

    public static String getDigit(String input) {
        if (input == null)
            return null;
        StringBuilder digits = new StringBuilder();
        int pos = Main.pos;
        int nowStatus = startStatus;
        int lastStatus = startStatus;
        for (int i = 0; i < input.length() && nowStatus != -1; i++) {
            char x = input.charAt(i);
            digits.append(x);
            lastStatus = nowStatus;
            nowStatus = getNextStatus(nowStatus, x);
            pos++;
        }
        if (!isEnd(lastStatus)) {
            return "ERROR";
        }
        if (nowStatus == -1) {
            Main.pos = pos - 1;
            digits.deleteCharAt(digits.length() - 1);
        } else {
            Main.pos = pos;
        }
        return digits.toString();
    }

    public static int getNextStatus(int fromStatus, char x) {
        switch (fromStatus) {
            case 0:
                if (isdigit(x)) {
                    return 1;
                }
                return -1;
            case 1:
                if (isdigit(x)) {
                    return 1;
                } else if (x == '.') {
                    return 2;
                } else if (x == 'e' || x == 'E') {
                    return 4;
                }
                return -1;
            case 2:
                if (isdigit(x)) {
                    return 3;
                }
                return -1;
            case 3:
                if (isdigit(x)) {
                    return 3;
                } else if (x == 'e' || x == 'E') {
                    return 4;
                }
                return -1;
            case 4:
                if (isdigit(x)) {
                    return 6;
                }
                if (x == '+' || x == '-') {
                    return 5;
                }
                return -1;
            case 5:
                if (isdigit(x)) {
                    return 6;
                }
                return -1;
            case 6:
                if (isdigit(x)) {
                    return 6;
                }
                return -1;
        }
        return -1;
    }

}
