module com.proiect {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires java.logging;
    requires java.net.http;
    requires org.json;
    requires java.sql;
    requires java.desktop;

    opens com.proiect to javafx.fxml;
    opens com.proiect.config to javafx.fxml;
    opens com.proiect.util to javafx.fxml;
    
    exports com.proiect;
    exports com.proiect.config;
    exports com.proiect.util;
}