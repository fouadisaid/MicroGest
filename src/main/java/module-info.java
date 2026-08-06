module said.microgest {
    requires javafx.controls;
    requires javafx.fxml;


    opens said.microgest to javafx.fxml;
    exports said.microgest;
}