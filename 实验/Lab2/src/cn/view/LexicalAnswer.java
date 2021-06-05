package cn.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

/**
 * created by meizhimin on 2021/4/8
 */
public class LexicalAnswer {
    @FXML
    private TextArea answer;

    @FXML
    private TextArea error;

    public void initArea(String answerMessage,String errorMessage){
        this.answer.setText(answerMessage);
        this.error.setText(errorMessage);
    }

}
