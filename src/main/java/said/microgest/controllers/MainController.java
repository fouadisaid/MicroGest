package said.microgest.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import said.microgest.entities.User;
import said.microgest.enums.Role;
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

    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionContext.getCurrentUser();

        if (currentUser != null) {
            userLabel.setText(currentUser.getFullName());
            roleLabel.setText(currentUser.getRole().name());
        }

        configurerMenu();
        chargerVue("/fxml/dashboard.fxml");
    }

    private void configurerMenu() {
        // Le menu est déjà dans le FXML
        // On cache les éléments selon le rôle
        if (currentUser != null) {
            Role role = currentUser.getRole();

            // Par exemple : seul l'admin voit "Utilisateurs" et "Agences"
            // On peut ajouter des IDs aux éléments du menu pour les cacher

            // Si ce n'est pas ADMIN, cacher les menus sensibles
            // Les IDs seront définis dans le FXML
        }
    }

    @FXML
    private void showDashboard() {
        chargerVue("/fxml/dashboard.fxml");
    }

    @FXML
    private void showAdherents() {
        chargerVue("/fxml/adherents.fxml");
    }

    @FXML
    private void showOperations() {
        chargerVue("/fxml/operations.fxml");
    }

    @FXML
    private void showPrets() {
        chargerVue("/fxml/prets.fxml");
    }

    @FXML
    private void showAgences() {
        chargerVue("/fxml/agences.fxml");
    }

    @FXML
    private void showUsers() {
        chargerVue("/fxml/users.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionContext.clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();

            mainPane.getScene().setRoot(root);
            mainPane.getScene().getWindow().setWidth(500);
            mainPane.getScene().getWindow().setHeight(400);
            mainPane.getScene().getWindow().centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chargerVue(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            mainPane.setCenter(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}