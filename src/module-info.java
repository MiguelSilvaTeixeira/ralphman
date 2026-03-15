module ralphman {
    requires javafx.controls;
    requires javafx.fxml;

    opens pckRalphman to javafx.fxml;
    exports pckRalphman;
}