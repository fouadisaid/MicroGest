package said.microgest.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import said.microgest.entities.User;
import said.microgest.services.UserService;
import said.microgest.utils.AlertUtil;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Définir un placeholder pour le champ username
        usernameField.setPromptText("Nom d'utilisateur ou email");

        // Action lors de l'appui sur Entrée
        usernameField.setOnAction(event -> passwordField.requestFocus());
        passwordField.setOnAction(event -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String login = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation des champs
        if (login.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Veuillez remplir tous les champs.");
            return;
        }

        try {
            // Tentative de connexion
            User user = userService.login(login, password);

            if (user != null) {
                messageLabel.setText("Connexion réussie ! Bienvenue " + user.getFullName());
                messageLabel.setStyle("-fx-text-fill: green;");

                // Charger l'interface principale
                chargerInterfacePrincipale();
            }

        } catch (RuntimeException e) {
            messageLabel.setText(e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            passwordField.clear();
            usernameField.requestFocus();
        }
    }

    private void chargerInterfacePrincipale() {
        try {
            // Charger le fichier FXML principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur principal
            MainController mainController = loader.getController();

            // Obtenir la scène actuelle
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Créer une nouvelle scène
            Scene scene = new Scene(root, 1200, 700);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

            // Appliquer la scène
            stage.setScene(scene);
            stage.setTitle("MicroGest - Gestion de Microfinance");
            stage.setMaximized(true);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.error("Erreur", "Impossible de charger l'interface principale.");
        }
    }
}