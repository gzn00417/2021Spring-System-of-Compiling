package cn.view;

/**
 * created by meizhimin on 2021/4/15
 */
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Tree extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)    {
        primaryStage.setTitle("Tree View Sample");
        TreeItem<String> rootItem = new TreeItem<> ("Inbox");
        rootItem.setExpanded(true);
//        for (int i = 1; i < 6; i++) {
//            TreeItem<String> item = new TreeItem<> ("Message" + i);
//            rootItem.getChildren().add(item);
//        }
        TreeView<String> tree = new TreeView<> (rootItem);
        StackPane root = new StackPane();
        root.getChildren().add(tree);
        primaryStage.setScene(new Scene(root, 300, 250));
        primaryStage.show();
    }
}
