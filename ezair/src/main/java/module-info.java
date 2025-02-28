module com.proiect {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.proiect to javafx.fxml;
    exports com.proiect;
}
