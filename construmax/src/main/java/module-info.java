module com.construmax {
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.base;
    requires javafx.fxml;
    opens com.construmax.Controllers to javafx.fxml;
    opens com.construmax to javafx.fxml;
    exports com.construmax;
}
