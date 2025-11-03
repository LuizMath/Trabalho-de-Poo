module com.construmax {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.base;
    requires javafx.fxml;
    requires java.sql;
    requires spring.security.crypto;
    requires org.controlsfx.controls;
    requires transitive io.github.cdimascio.dotenv.java;
    opens com.construmax.Controllers to javafx.fxml;
    opens com.construmax to javafx.fxml;
    exports com.construmax;
}
