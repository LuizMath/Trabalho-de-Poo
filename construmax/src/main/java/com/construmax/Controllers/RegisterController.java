package com.construmax.Controllers;

import com.construmax.App;
import com.construmax.DAO.UserDAO;
import com.construmax.Database.DatabaseConnection;
import com.construmax.Model.User;

import java.io.IOException;
import java.sql.Connection;

import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    User user = new User();
    private static Connection connection;
    @FXML
    private Hyperlink hyperlink;
    @FXML
    private TextField name;
    @FXML
    private TextField phone;
    @FXML 
    private TextField email;
    @FXML
    private PasswordField password;
    @FXML
    private TextField CPF;
    @FXML
    private Button submit;
    @FXML
    private void switchToLogin () throws IOException {
        App.setRoot("login");
    }
    @FXML 
    private void getName () {
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            user.setName(newValue);
        });
    }
    @FXML
    private void getPhone () {
        phone.textProperty().addListener((observable, oldValue, newValue) -> {
            user.setPhone(newValue);
        });
    }
    @FXML
    private void getEmail() {
        email.textProperty().addListener((observable, oldValue, newValue) -> {
            user.setEmail(newValue);
        });
    }
    @FXML
    private void getCPF () {
        CPF.textProperty().addListener((observable, oldValue, newValue) -> {
            user.setCPF(newValue);
        });
    }
    @FXML
    private void getPassword () {
        password.textProperty().addListener((observable, oldValue, newValue) -> {
            user.setPassword(newValue);
        });
    }
    @FXML
    private void clickSubmitButton () {
        submit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserDAO userDAO = new UserDAO(connection);
                userDAO.insertUser(user);
            }
        });
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
        connection = DatabaseConnection.getConnection();
        getName();
        getCPF();
        getEmail();
        getPassword();
        getPhone();
        clickHyperLink();
        clickSubmitButton();
    }
}
