package cn.view;

import cn.MainApp;
import cn.model.grammar.Grammar;
import cn.model.grammar.SLRParser;
import cn.model.grammar.Syntax_rule;
import cn.model.lexical.Lexer;
import cn.model.lexical.tokenStr;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

/**
 * created by meizhimin on 2021/4/13
 */
public class GrammarView {
    @FXML
    private TextArea fileContents;
    private String fileStr;
    private Grammar grammar;

    public GrammarView(){
        grammar = new Grammar();
        fileStr="";
    }

    @FXML
    private void chooseFile() {
        // TODO 修改“不选文件就无法退出”
        File selectedFile = null;
        while(selectedFile == null){
            Stage mainStage = null;
            // 构建一个文件选择器实例
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("请选择待分析文件");
            // 设置初始路径
            fileChooser.setInitialDirectory(new File("res"));
            // 设置过滤器
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            selectedFile = fileChooser.showOpenDialog(mainStage);
            if(selectedFile == null){
                // Nothing selected.
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("No File Selected");
                alert.setContentText("Please select a file");
                alert.showAndWait();
            }
        }
        // 获取文件的绝对路径
       // String path = selectedFile.getPath();
        String path = "./res/grammar_test_false.txt";
        System.out.println(path);
//        path=path.replaceAll("\\\\", "/");
        File file = new File(path);
        String fileStr = MainApp.txt2String(file);
        this.fileStr = fileStr;
        // 加行号
        String[] lines = fileStr.split("\\r?\\n");
        for(int i=0;i<lines.length;i++){
            lines[i]=(i+1)+":\t"+lines[i];
        }
        // 合成String
        StringBuilder sb1 = new StringBuilder();
        String allStr="";
        for(int i=0;i<lines.length-1;i++){
            allStr=sb1.append(lines[i]).append("\n").toString();
        }
        // 显示file内容以及行号
        fileContents.setText(allStr);
    }

    @FXML
    private void showRule() {
        SLRParser parser = new SLRParser(grammar);
        Syntax_rule window = new Syntax_rule(grammar, parser.getGotoTable(), parser.getActionTable());
        window.setVisible(true);
    }

    @FXML
    private void showResult(){
        try{
            // 利用Lexer获取token序列再传入SLRParser进行分析
            String str1 = this.fileStr+" ";
            String str2 = str1.replaceAll("\r","");  //去掉\r符号
            Lexer lexer = new Lexer("res/dfa.xls");
            lexer.lexicalAnalysis(str2);
            ArrayList<tokenStr> tokenStrs = lexer.getTokenStrs();
//            SLRParser parser = new SLRParser(grammar);
//            parser.parse(lexer.getTokenStrs());
            SLRParser parser = new SLRParser(grammar);
            //TODO
            String errorMessage = parser.parse(tokenStrs);
            TreeItem<String> rootItem =parser.constructTree();
            TreeView<String> tree = new TreeView<>(rootItem);
            // load FXML
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/GrammarAnswer.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            GrammarAnswer grammarAnswer = loader.getController();
//            TreeItem<String> rootItem = new TreeItem<>("Program");
            grammarAnswer.showAnswer(tree,errorMessage);
            // 手动从Page中置换TreeView
            SplitPane splitPane = (SplitPane)page.getChildren().get(0);
            AnchorPane anchorPane = (AnchorPane)splitPane.getItems().get(0);
            StackPane stackPane = (StackPane)anchorPane.getChildren().get(0);
            stackPane.getChildren().set(0,tree);
            // Create the alert Stage.
            Stage alertStage = new Stage();
            alertStage.setTitle("语法分析结果");
            alertStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            alertStage.setScene(scene);
            // Show the alert and wait until the user closes it
            alertStage.showAndWait();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
