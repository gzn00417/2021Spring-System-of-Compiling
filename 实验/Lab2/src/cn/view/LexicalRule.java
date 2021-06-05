package cn.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;


/**
 * created by meizhimin on 2021/4/10
 */
public class LexicalRule {
    @FXML
    private TextArea ruleArea;

    public void showRule(String str){
        ruleArea.setText(str);
    }
}
