package fdit.gui.application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FXMLpreloaderController {
    public Label preloadTitle;
    public Label preloadLoadTitle;
    public Label preloadVersion;
    public Label preloadLoad1;

    @FXML
    private void initialize() {
        preloadTitle.setText("FDIT");
        preloadLoadTitle.setText("Initialise application");
        preloadLoad1.setText("Creating data base...");
        preloadVersion.setText("Version: 4.0");
    }

    public void dataBaseReady() {
        preloadLoad1.setText("Database Created.");
    }

}
