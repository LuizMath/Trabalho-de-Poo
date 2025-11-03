package com.construmax.Controllers;

import java.io.IOException;

import com.construmax.App;
import com.construmax.DAO.UserDAO;
import com.construmax.Database.DatabaseConnection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.event.EventHandler;

public class LoginController {
    @FXML
    private Hyperlink hyperlink;
    @FXML
    private PasswordField password;
    @FXML
    private Button submit;
    @FXML
    private TextField email;
    @FXML
    private void switchToRegister () throws IOException {
        App.setRoot("register");
    }
    @FXML
    private void clickSubmitButton () {
        submit.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                UserDAO userDAO = new UserDAO(DatabaseConnection.getConnection());
                userDAO.authenticateUser(email.getText(), password.getText());
            }
        });
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
        clickSubmitButton();
    }
}
