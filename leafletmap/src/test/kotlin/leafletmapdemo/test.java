package leafletmapdemo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class test extends Application {

    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(test.class.getResource("/leafletmapdemo/demo.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}

