module com.construmax {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.construmax to javafx.fxml;
    exports com.construmax;
}
