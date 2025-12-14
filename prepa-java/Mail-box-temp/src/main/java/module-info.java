module org.example.mailbox {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.mail;
    requires java.sql;

    opens org.example.mailbox to javafx.fxml;
    exports org.example.mailbox;
}