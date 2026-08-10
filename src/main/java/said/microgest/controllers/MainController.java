package said.microgest.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import said.microgest.entities.User;
import said.microgest.enums.Role;
import said.microgest.utils.AlertUtil;
import said.microgest.utils.SessionContext;

import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label userLabel;

    @FXML
    private Label roleLabel;

    @FXML
    private VBox menuContainer;

    // Boutons du menu
    @FXML
    private Button dashboardButton;

    @FXML
    private Button adherentsButton;

    @FXML
    private Button operationsButton;

    @FXML
    private Button pretsButton;

    @FXML
    private Button agencesButton;

    @FXML
    private Button usersButton;

    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionContext.getCurrentUser();

        if (currentUser != null) {
            userLabel.setText(currentUser.getFullName());
            roleLabel.setText(currentUser.getRole().name());
        }

        configurerMenu();
        chargerVue("/views/dashboard-view.fxml");
    }

    private void configurerMenu() {
        if (currentUser == null) {
            return;
        }

        Role role = currentUser.getRole();

        // Par défaut, tout le monde voit Dashboard, Adhérents, Opérations, Prêts
        // Seul l'ADMIN voit Agences et Utilisateurs

        switch (role) {
            case ADMIN:
                // ADMIN voit tout
                dashboardButton.setVisible(true);
                adherentsButton.setVisible(true);
                operationsButton.setVisible(true);
                pretsButton.setVisible(true);
                agencesButton.setVisible(true);
                usersButton.setVisible(true);
                break;

            case AGENT:
                // AGENT voit tout sauf Agences et Utilisateurs
                dashboardButton.setVisible(true);
                adherentsButton.setVisible(true);
                operationsButton.setVisible(true);
                pretsButton.setVisible(true);
                agencesButton.setVisible(false);
                usersButton.setVisible(false);
                break;

            case SUPERVISEUR:
                // SUPERVISEUR voit Dashboard, Opérations, Prêts
                dashboardButton.setVisible(true);
                adherentsButton.setVisible(false);
                operationsButton.setVisible(true);
                pretsButton.setVisible(true);
                agencesButton.setVisible(false);
                usersButton.setVisible(false);
                break;

            default:
                // Par sécurité, on cache tout
                dashboardButton.setVisible(false);
                adherentsButton.setVisible(false);
                operationsButton.setVisible(false);
                pretsButton.setVisible(false);
                agencesButton.setVisible(false);
                usersButton.setVisible(false);
                break;
        }
    }

    @FXML
    private void showDashboard() {
        chargerVue("/views/dashboard-view.fxml");
    }

    @FXML
    private void showAdherents() {
        chargerVue("/views/adherent-form.fxml");
    }

    @FXML
    private void showOperations() {
        chargerVue("/views/operation-form.fxml");
    }

    @FXML
    private void showPrets() {
        chargerVue("/views/pret-form.fxml");
    }

    @FXML
    private void showAgences() {
        chargerVue("/views/agence-form.fxml");
    }

    @FXML
    private void showUsers() {
        chargerVue("/views/user-form.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionContext.clear();

        try {
            Scene currentScene = mainPane.getScene();

            if (currentScene == null) {
                AlertUtil.error("Erreur", "Impossible de trouver la scène courante.");
                return;
            }

            Stage stage = (Stage) currentScene.getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login-view.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 500, 400);
            String css = getClass().getResource("/css/application.css").toExternalForm();
            if (css != null) {
                scene.getStylesheets().add(css);
            }

            stage.setScene(scene);
            stage.setTitle("MicroGest - Connexion");
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.error("Erreur", "Impossible de charger l'écran de connexion.");
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.error("Erreur", "Erreur lors de la déconnexion : " + e.getMessage());
        }
    }

    private void chargerVue(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            mainPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.error("Erreur", "Impossible de charger la vue : " + fxmlPath);
        }
    }
}