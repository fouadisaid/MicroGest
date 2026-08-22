package said.microgest.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import said.microgest.utils.AlertUtil;
import said.microgest.utils.SessionContext;

public class SettingsController {

    @FXML
    private Label userLabel;

    @FXML
    private TextField appNameField;

    @FXML
    private TextField companyField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private CheckBox notificationCheck;

    @FXML
    private CheckBox autoSaveCheck;

    @FXML
    public void initialize() {
        var currentUser = SessionContext.getCurrentUser();
        if (currentUser != null) {
            userLabel.setText("⚙️ Paramétrage - " + currentUser.getFullName());
        }

        // Charger les paramètres (à implémenter avec un fichier de config ou BDD)
        chargerParametres();
    }

    private void chargerParametres() {
        // Valeurs par défaut
        appNameField.setText("MicroGest");
        companyField.setText("MicroFinance SA");
        emailField.setText("contact@microgest.com");
        phoneField.setText("+221 33 123 45 67");
        addressField.setText("Dakar, Sénégal");
        notificationCheck.setSelected(true);
        autoSaveCheck.setSelected(false);
    }

    @FXML
    private void handleSave() {
        try {
            String appName = appNameField.getText().trim();
            String company = companyField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String address = addressField.getText().trim();
            boolean notifications = notificationCheck.isSelected();
            boolean autoSave = autoSaveCheck.isSelected();

            // Validation
            if (appName.isEmpty()) {
                AlertUtil.error("Erreur", "Le nom de l'application est obligatoire.");
                return;
            }

            if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                AlertUtil.error("Erreur", "Adresse email invalide.");
                return;
            }

            // Sauvegarde (à implémenter)
            System.out.println("✅ Paramètres sauvegardés :");
            System.out.println("   Nom application : " + appName);
            System.out.println("   Société : " + company);
            System.out.println("   Email : " + email);
            System.out.println("   Téléphone : " + phone);
            System.out.println("   Adresse : " + address);
            System.out.println("   Notifications : " + (notifications ? "Activées" : "Désactivées"));
            System.out.println("   Sauvegarde auto : " + (autoSave ? "Activée" : "Désactivée"));

            AlertUtil.information("Succès", "Paramètres enregistrés avec succès !");

        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        chargerParametres();
        AlertUtil.information("Info", "Paramètres réinitialisés.");
    }
}