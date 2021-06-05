package cn.model.grammar;

/**
 * created by meizhimin on 2021/4/14
 */
public class Action {
    private ActionType type;
    private int operand;

    public Action(ActionType type, int operand) {
        this.type = type;
        this.operand = operand;
    }

    @Override
    public String toString() {
        return type + " " + (type == ActionType.ACCEPT ? "":operand);
    }

    public ActionType getType() {
        return type;
    }

    public int getOperand() {
        return operand;
    }
}
