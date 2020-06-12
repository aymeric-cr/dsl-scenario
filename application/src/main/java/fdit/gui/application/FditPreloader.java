package fdit.gui.application;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import static fdit.gui.Images.APPLCATION_ICON;
import static javafx.application.Preloader.StateChangeNotification.Type.BEFORE_START;

public class FditPreloader extends Preloader {

    private Scene scene;
    private Stage preLoaderStage;
    private FXMLpreloaderController fxmLpreloaderController;

    @Override
    public void init() throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("preloader.fxml"));
        final Parent root = fxmlLoader.load();
        scene = new Scene(root);
        fxmLpreloaderController = fxmlLoader.getController();
    }

    @Override
    public void start(Stage primaryStage) {
        this.preLoaderStage = primaryStage;
        this.preLoaderStage.getIcons().add(APPLCATION_ICON);
        this.preLoaderStage.initStyle(StageStyle.UNDECORATED);
        this.preLoaderStage.setScene(scene);
        this.preLoaderStage.show();
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof ProgressNotification) {
            this.fxmLpreloaderController.dataBaseReady();
        }
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        StateChangeNotification.Type type = info.getType();
        if (type == BEFORE_START) {
            this.preLoaderStage.hide();
        }
    }
}
