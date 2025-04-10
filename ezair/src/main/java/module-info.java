module com.proiect {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.logging;
    requires java.net.http; // If you use HttpClient
    requires org.json; // Add this line to allow access to org.json

    opens com.proiect to javafx.fxml;
    exports com.proiect;
}