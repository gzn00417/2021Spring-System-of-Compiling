package cn.model.lexical;

import java.util.HashMap;
import java.util.Map;

//DFA转换表的一行
public class DFATableElement {
    private int state;//当前状态
    private Map<String[], Integer> transfer;//转换函数
    private String type;//当前状态代表的token类别
    private boolean isFinish;//是否为终止状态

    public DFATableElement(int state, String type, boolean isFinish) {
        this.state = state;
        this.type = type;
        this.isFinish = isFinish;
        this.transfer = new HashMap<>();
    }

    /**
     * 获取当前状态
     * @return 返回当前所处状态
     */
    public int getState() {
        return state;
    }

    /**
     * 获取单词类型
     * @return 当前状态所表示的单词类型
     */
    public String getType() {
        return type;
    }

    /**
     * 判断当前状态是否为结束状态
     * @return True if 当前状态是结束状态，否则返回False
     */
    public boolean isFinish() {
        return this.isFinish;
    }

    /**
     * 将对应输入及其下一状态添加到该行DFA转换表中
     * @param input 当前的输入
     * @param nextState 下一状态
     */
    public void addTransferElement(String[] input, int nextState){
        transfer.put(input, nextState);
    }

    /**
     * 根据输入获取下一状态
     * @param input 当前输入
     * @return 依据转换表跳转到的下一个状态
     */
    public int getNextState(char input){
        for(String[] key:transfer.keySet()){
            for(String s:key){
                if(s.indexOf(input)!=-1){
                    return transfer.get(key);
                }
            }
        }
        if(this.state==9||this.state==10)
        {
        	this.isFinish=false;
        	return 9;
        }
        return -1;
    }

    //debug用，打印出来的不规范
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(state + ":");
        for(String[] key:transfer.keySet()){
            for(String s:key){
                sb.append(" " + s);
            }
            sb.append(": " + transfer.get(key));
        }
        sb.append(" isFinish=" + isFinish + " type=" + type);
        return sb.toString();
    }
}
