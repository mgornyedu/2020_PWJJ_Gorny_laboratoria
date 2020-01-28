package WorkManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {

    private Controller controller;
    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("WorkManagerView.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        primaryStage.setTitle("WorkManager");
        primaryStage.setMaximized(true);
        primaryStage.setScene(new Scene(root, 1000, 775));
        primaryStage.show();
    }

    @Override
    public void stop(){

    }
    public static void main(String[] args) {
        launch(args);
    }
}
