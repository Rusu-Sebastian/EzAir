module com.proiect {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens com.proiect to javafx.fxml;
    exports com.proiect;
}
