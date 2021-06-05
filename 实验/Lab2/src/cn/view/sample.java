package cn.view;

import cn.MainApp;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class sample {
    private MainApp mainApp;

    public sample(){
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    // TODO 词法分析
    @FXML
    private void handleLexical() {
        mainApp.showLexicalView();
    }

    // TODO 语法分析
    @FXML
    private void handleGrammar() {
        try {
            // Load the fxml file and create a new stage for the popup alert.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/GrammarView.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            // Create the alert Stage.
            Stage alertStage = new Stage();
            alertStage.setTitle("语法分析模块");
            alertStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            alertStage.setScene(scene);
            // Show the alert and wait until the user closes it
            alertStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // TODO 语义分析
    @FXML
    private void handleSemantic() {

    }
}
