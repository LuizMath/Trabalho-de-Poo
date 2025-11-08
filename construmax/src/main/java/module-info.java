module com.construmax {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.base;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires kernel;
    requires io;
    requires forms;
    requires layout;
    requires html2pdf;
    requires spring.security.crypto;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome6;
    requires transitive io.github.cdimascio.dotenv.java;
    opens com.construmax.Controllers to javafx.fxml;
    opens com.construmax to javafx.fxml;
    opens com.construmax.Model to javafx.base;
    exports com.construmax;
}
