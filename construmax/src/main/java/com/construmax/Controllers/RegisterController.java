package com.construmax.Controllers;

import com.construmax.App;
import java.io.IOException;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

public class RegisterController {
    @FXML
    private Hyperlink hyperlink;
    @FXML
    private void switchToLogin () throws IOException {
        App.setRoot("login");
    }
    @FXML
    private void clickHyperLink () {
        hyperlink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    switchToLogin();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    @FXML
    public void initialize () {
        clickHyperLink();
    }
}
