package com.construmax.Controllers;

import java.io.IOException;
import java.sql.Connection;

import com.construmax.App;
import com.construmax.DAO.UserDAO;
import com.construmax.Database.DatabaseConnection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.event.EventHandler;

public class LoginController {
    private static Connection connection;
    @FXML
    private Hyperlink hyperlink;
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
                UserDAO userDAO = new UserDAO(connection);
                userDAO.authenticateUser(email.getText());
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
        connection = DatabaseConnection.getConnection();
        clickHyperLink();
        clickSubmitButton();
    }
}
