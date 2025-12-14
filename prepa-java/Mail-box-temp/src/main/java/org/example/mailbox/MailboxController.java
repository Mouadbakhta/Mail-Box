package org.example.mailbox;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import org.example.mailbox.email.*;
import org.example.mailbox.db.*;

import java.util.List;

public class MailboxController {
    @FXML private TextField toField;
    @FXML private TextField subjectField;
    @FXML private TextArea bodyArea;

    @FXML private ListView<EmailItem> emailListView;
    @FXML private Label senderLabel;
    @FXML private Label subjectLabel;
    @FXML private Label dateLabel;
    @FXML private TextArea emailBodyArea;
    @FXML private Label userEmailLabel;

    private EmailService emailService;
    private EmailConfig config;
    private EmailDao emailDao;

    @FXML
    public void initialize() {
        config = new EmailConfig();
        emailService = config.isMockEnabled() ? new MockEmailService() : new ImapEmailService(config);
        userEmailLabel.setText(config.getFromAddress());
        emailDao = new EmailDao(new DbConfig(config));

        emailListView.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                senderLabel.setText("De: " + sel.getFrom());
                subjectLabel.setText("Objet: " + sel.getSubject());
                dateLabel.setText("Date: " + sel.getDate());
                loadBody(sel.getId());
            }
        });
        handleRefresh();
    }

    private void loadBody(String id) {
        Task<String> t = new Task<>() {
            @Override protected String call() throws Exception { return emailService.fetchBody(id); }
        };
        t.setOnSucceeded(ev -> emailBodyArea.setText(t.getValue()));
        t.setOnFailed(ev -> showError("Impossible de charger le contenu"));
        new Thread(t).start();
    }

    @FXML
    public void handleRefresh(){
        ButtonType prev = null;
        Task<List<EmailItem>> t = new Task<>() {
            @Override protected List<EmailItem> call() throws Exception { return emailService.fetchInbox(); }
        };
        t.setOnSucceeded(ev -> emailListView.setItems(FXCollections.observableArrayList(t.getValue())));
        t.setOnSucceeded(ev -> {
            emailListView.setItems(FXCollections.observableArrayList(t.getValue()));
            // Persist basic received items bodies into DB (optional minimal)
            for (EmailItem item : t.getValue()) {
                try {
                    String body = emailService.fetchBody(item.getId());
                    emailDao.saveReceived(item, body);
                } catch (Exception ignore) {}
            }
        });
        t.setOnFailed(ev -> showError("Échec de l'actualisation"));
        new Thread(t).start();
    }

    @FXML
    public void handleNewEmail(){
        toField.clear();
        subjectField.clear();
        bodyArea.clear();
    }

    @FXML
    public void handleDeleteEmail(){
        EmailItem sel = emailListView.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Sélectionnez un email à supprimer"); return; }
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception { emailService.delete(sel.getId()); return null; }
        };
        t.setOnSucceeded(ev -> { handleRefresh(); showInfo("Email supprimé"); });
        t.setOnFailed(ev -> showError("Échec de la suppression"));
        new Thread(t).start();
    }

    @FXML
    void handleSendEmail() {
        String to = toField.getText();
        String subject = subjectField.getText();
        String body = bodyArea.getText();
        if (to == null || to.isBlank()) { showError("Le destinataire est requis"); return; }
        if (subject == null || subject.isBlank()) { showError("L'objet est requis"); return; }
        Task<Void> t = new Task<>() {
            @Override protected Void call() throws Exception {
                EmailService smtp = config.isMockEnabled() ? emailService : new SmtpEmailService(config);
                smtp.sendEmail(to, subject, body);
                return null;
            }
        };
        t.setOnSucceeded(ev -> { showInfo("Email envoyé"); handleNewEmail();
            try { new EmailDao(new DbConfig(config)).saveSent(to, subject, body); } catch (Exception ignored) {}
        });
        t.setOnFailed(ev -> showError("Échec de l'envoi: " + ev.getSource().getException().getMessage()));
        new Thread(t).start();
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
