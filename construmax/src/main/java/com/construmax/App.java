package com.construmax;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import com.construmax.Database.DatabaseConnection;

public class App extends Application {
    private static Stage primaryStage;
    private static Scene scene;
    private static Dotenv dotenv = Dotenv.configure().directory("construmax/.env").load();

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.setTitle("Construmax");
        primaryStage.getIcons().add(new Image(
            getClass().getResourceAsStream("/com/construmax/Icon/tools.png")
        ));
        scene = new Scene(loadFXML("login"), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }
    public static void main(String[] args) {
        DatabaseConnection.init(dotenv);
        launch(args);
    }
}
