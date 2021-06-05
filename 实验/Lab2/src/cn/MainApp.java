package cn;

import cn.view.sample;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainApp extends Application {
    private Stage primaryStage;
    private AnchorPane rootLayout;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("编译原理实验");
        initRootLayout();
    }

    /**
     * 通过加载View/sample.fxml来初始化界面
     */
    private void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/sample.fxml"));
            rootLayout = (AnchorPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            // Give the controller access to the main app.
            sample controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLexicalView() {
        try {
            // Load the fxml file and create a new stage for the popup alert.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/LexicalView.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the alert Stage.
            Stage alertStage = new Stage();
            alertStage.setTitle("词法分析模块");
            alertStage.initModality(Modality.WINDOW_MODAL);
            alertStage.initOwner(primaryStage);
            Scene scene = new Scene(page);
            alertStage.setScene(scene);
            // Show the alert and wait until the user closes it
            alertStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 静态方法：完成txt文件内容到String的格式转换
     * @param file txt文件，作为词法分析的测试文件
     * @return 一个map，key为txt文件内容的String形式，Value为文件内容的行数
     */
    public static String txt2String(File file){
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result.append(s).append(System.lineSeparator());
            }
            br.close();
            result.append(' ');
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Returns the main stage.
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
