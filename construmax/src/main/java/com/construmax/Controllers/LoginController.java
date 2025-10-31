package com.construmax.Controllers;

import java.io.IOException;

import com.construmax.App;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.event.EventHandler;

public class LoginController {
    @FXML
    private Hyperlink hyperlink;
    @FXML
    private void switchToRegister () throws IOException {
        App.setRoot("register");
    }
    @FXML 
    private void clickHyperLink () {
        hyperlink.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    switchToRegister();
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
