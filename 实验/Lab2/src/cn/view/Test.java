package cn.view;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * created by meizhimin on 2021/4/15
 */
public class Test {
    @FXML
    private  TreeView<String> tree;

    public Test(){
    }

    public TreeView<String> showTree(TreeItem<String> rootItem){
        tree = new TreeView<>(rootItem);
        return tree;
    }
}
