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
import said.microgest.enums.Permissions;
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

    @FXML
    private Button epargnesButton;

    @FXML
    private Button remboursementsButton;

    @FXML
    private Button parametrageButton;

    private User currentUser;
    private Role currentRole;

    @FXML
    public void initialize() {

        currentUser = SessionContext.getCurrentUser();

        if (currentUser != null) {
            userLabel.setText(currentUser.getFullName());
            roleLabel.setText(currentUser.getRole().name());
            currentRole = currentUser.getRole();
        }

        configurerMenu();

        chargerVue("/views/dashboard-view.fxml");
    }

    private void configurerMenu() {

        if (currentRole == null) {
            return;
        }

        dashboardButton.setVisible(
                currentRole.hasPermission(Permissions.VIEW_DASHBOARD)
        );

        adherentsButton.setVisible(
                currentRole.hasPermission(Permissions.MANAGE_ADHERENTS)
        );

        operationsButton.setVisible(
                currentRole.hasPermission(Permissions.MANAGE_OPERATIONS)
        );

        pretsButton.setVisible(
                currentRole.hasPermission(Permissions.MANAGE_PRETS)
        );

        agencesButton.setVisible(
                currentRole.hasPermission(Permissions.MANAGE_AGENCES)
        );

        usersButton.setVisible(
                currentRole.hasPermission(Permissions.MANAGE_USERS)
        );

        remboursementsButton.setVisible(currentRole.hasPermission(Permissions.MANAGE_REMBOURSEMENTS));

        epargnesButton.setVisible(currentRole.hasPermission(Permissions.VIEW_EPARGNE));

        parametrageButton.setVisible(
                currentRole.hasPermission(Permissions.MANAGE_SETTINGS)
        );
    }

    @FXML
    private void showDashboard() {

        verifierPermissionEtCharger(
                Permissions.VIEW_DASHBOARD,
                "/views/dashboard-view.fxml"
        );
    }

    @FXML
    private void showAdherents() {

        verifierPermissionEtCharger(
                Permissions.MANAGE_ADHERENTS,
                "/views/adherent-form.fxml"
        );
    }

    @FXML
    private void showOperations() {

        verifierPermissionEtCharger(
                Permissions.MANAGE_OPERATIONS,
                "/views/operation-form.fxml"
        );
    }

    @FXML
    private void showPrets() {

        verifierPermissionEtCharger(
                Permissions.MANAGE_PRETS,
                "/views/pret-form.fxml"
        );
    }

    @FXML
    private void showAgences() {

        verifierPermissionEtCharger(
                Permissions.MANAGE_AGENCES,
                "/views/agence-form.fxml"
        );
    }

    @FXML
    private void showUsers() {

        verifierPermissionEtCharger(
                Permissions.MANAGE_USERS,
                "/views/user-form.fxml"
        );
    }

    @FXML
    private void showEpargnes() {
        verifierPermissionEtCharger(Permissions.VIEW_EPARGNE, "/views/epargne-form.fxml");
    }

    @FXML
    private void showRemboursements() {
        verifierPermissionEtCharger(Permissions.MANAGE_REMBOURSEMENTS, "/views/remboursement-form.fxml");
    }

    @FXML
    private void showParametrage() {

        verifierPermissionEtCharger(
                Permissions.MANAGE_SETTINGS,
                "/views/settings-view.fxml"
        );
    }

    private void verifierPermissionEtCharger(
            Permissions permission,
            String fxmlPath
    ) {

        if (currentRole == null ||
                !currentRole.hasPermission(permission)) {

            AlertUtil.error(
                    "Accès refusé",
                    "Vous n'avez pas les droits nécessaires pour accéder à cette fonctionnalité."
            );

            return;
        }

        chargerVue(fxmlPath);
    }

    private void chargerVue(String fxmlPath) {

        try {

            var resource =
                    getClass().getResource(fxmlPath);

            if (resource == null) {

                throw new RuntimeException(
                        "Fichier FXML introuvable : " + fxmlPath
                );
            }

            FXMLLoader loader =
                    new FXMLLoader(resource);

            Parent content =
                    loader.load();

            mainPane.setCenter(content);

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger la vue : "
                            + fxmlPath
                            + "\n\nCause : "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void handleLogout() {

        SessionContext.clear();

        try {

            Scene currentScene =
                    mainPane.getScene();

            if (currentScene == null) {

                AlertUtil.error(
                        "Erreur",
                        "Impossible de trouver la scène courante."
                );

                return;
            }

            Stage stage =
                    (Stage) currentScene.getWindow();

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/views/login-view.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            Scene scene =
                    new Scene(root, 500, 400);

            String css =
                    getClass()
                            .getResource(
                                    "/css/application.css"
                            )
                            .toExternalForm();

            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.setTitle("MicroGest - Connexion");
            stage.centerOnScreen();
            stage.setResizable(false);
            stage.show();

        } catch (IOException e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger l'écran de connexion."
            );

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Erreur lors de la déconnexion : "
                            + e.getMessage()
            );
        }
    }
}