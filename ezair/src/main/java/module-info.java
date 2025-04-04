module com.proiect {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.logging;

    opens com.proiect to javafx.fxml;
    exports com.proiect;
}