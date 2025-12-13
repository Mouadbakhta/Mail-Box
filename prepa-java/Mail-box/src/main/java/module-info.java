
module org.example.mailbox {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.mailbox to javafx.fxml;
    exports org.example.mailbox;
}