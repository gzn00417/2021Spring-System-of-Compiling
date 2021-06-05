package cn.view;

import cn.MainApp;
import cn.model.lexical.Lexer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * created by zhuoning guo on 2021/4/8
 */
public class LexicalView {
    @FXML
    private TextArea fileContents;
    private String fileStr;
    public LexicalView(){
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
        String path = selectedFile.getPath();
        System.out.println(path);
        path=path.replaceAll("\\\\", "/");
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

    // TODO 显示规则
    @FXML
    private void showRule() {
        try {
            // Load the fxml file and create a new stage for the popup alert.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/LexicalRule.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            LexicalRule controller = loader.getController();
            File file = new File("res/词法规则.txt");
            String ruleStr = MainApp.txt2String(file);
            controller.showRule(ruleStr);
            // Create the alert Stage.
            Stage alertStage = new Stage();
            alertStage.setTitle("词法分析规则");
            alertStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            alertStage.setScene(scene);
            // Show the alert and wait until the user closes it
            alertStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO 分析结果
    @FXML
    private void showResult() {
        try {
            // Load the fxml file and create a new stage for the popup alert.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/LexicalAnswer.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            LexicalAnswer lexicalAnswer = loader.getController();

            String str1 = this.fileStr+" ";
            String str2 = str1.replaceAll("\r","");  //去掉\r符号
            Lexer lexer = new Lexer("res/dfa.xls");
            List<String> result = lexer.lexicalAnalysis(str2);
            String answer = result.get(0);
            String errorMessage = result.get(1);

            lexicalAnswer.initArea(answer,errorMessage);

            // Create the alert Stage.
            Stage alertStage = new Stage();
            alertStage.setTitle("分析结果");
            alertStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            alertStage.setScene(scene);
            // Show the alert and wait until the user closes it
            alertStage.showAndWait();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
