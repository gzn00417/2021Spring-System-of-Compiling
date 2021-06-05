package cn.view;

import cn.MainApp;
import cn.model.lexical.tokenStr;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * created by meizhimin on 2021/4/14
 */
public class GrammarAnswer {
    @FXML
    private TreeView<String> tree;
    @FXML
    private TextArea error;

    // 显示树形结果和error信息
    public void showAnswer(TreeView<String> tree, String errorMessage){
        this.tree = tree;
        this.error.setText(errorMessage);
    }
}
