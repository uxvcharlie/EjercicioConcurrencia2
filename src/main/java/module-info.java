module com.example.ejercicioconcurrencia2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ejercicioconcurrencia2 to javafx.fxml;
    exports com.example.ejercicioconcurrencia2;
}