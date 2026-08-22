package said.microgest.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import said.microgest.entities.User;
import said.microgest.enums.Role;
import said.microgest.services.UserService;
import said.microgest.utils.AlertUtil;
import said.microgest.utils.SessionContext;

import java.util.List;

public class UserController {

    @FXML private StackPane rootPane;
    @FXML private VBox listPane;
    @FXML private VBox formPane;

    @FXML private TableView<User> userTable;

    @FXML private TableColumn<User, Number> idColumn;
    @FXML private TableColumn<User, String> nomColumn;
    @FXML private TableColumn<User, String> prenomColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> telephoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> actifColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<Role> roleFilter;

    @FXML private ComboBox<Integer> sizeCombo;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Label totalPagesLabel;
    @FXML private Label totalRecordsLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button toggleActifButton;

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private ComboBox<Role> roleCombo;
    @FXML private Label passwordLabel;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox actifCheck;
    @FXML private Label errorLabel;

    private final UserService userService = new UserService();

    private int currentPage = 1;
    private int pageSize = 10;
    private long totalRecords = 0;
    private int totalPages = 1;

    private User userForm;

    @FXML
    public void initialize() {

        configurerColonnes();
        chargerFiltres();

        sizeCombo.setItems(
                FXCollections.observableArrayList(5, 10, 20, 50)
        );

        sizeCombo.setValue(pageSize);

        sizeCombo.setOnAction(event -> {

            Integer valeur = sizeCombo.getValue();

            if (valeur != null) {

                pageSize = valeur;
                currentPage = 1;

                chargerDonnees();
            }
        });

        userTable.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, oldSelection, newSelection) ->
                                mettreAJourBoutons()
                );

        searchField.textProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            currentPage = 1;
                            chargerDonnees();
                        }
                );

        chargerDonnees();
        mettreAJourBoutons();

        initialiserFormulaire();

        listPane.setVisible(true);
        listPane.setManaged(true);

        formPane.setVisible(false);
        formPane.setManaged(false);
    }

    private void configurerColonnes() {

        idColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getId())
        );

        nomColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getNom())
        );

        prenomColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getPrenom())
        );

        usernameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getUsername())
        );

        emailColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getEmail())
        );

        telephoneColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getTelephone())
        );

        roleColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getRole() != null
                                ? data.getValue().getRole().name()
                                : ""
                )
        );

        actifColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().isActif() ? "ACTIF" : "INACTIF"
                )
        );

        actifColumn.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(String statut, boolean empty) {

                        super.updateItem(statut, empty);

                        if (empty || statut == null) {

                            setText(null);
                            setStyle("");

                            return;
                        }

                        setText(statut);

                        if (statut.equals("ACTIF")) {

                            setStyle(
                                    "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                            );

                        } else {

                            setStyle(
                                    "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                            );
                        }
                    }
                }
        );
    }

    private void chargerFiltres() {

        roleFilter.setItems(
                FXCollections.observableArrayList(Role.values())
        );

        roleFilter.valueProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            currentPage = 1;
                            chargerDonnees();
                        }
                );
    }

    private void chargerDonnees() {

        try {

            String keyword = searchField.getText();

            if (keyword != null && !keyword.isBlank()) {

                List<User> result = userService.search(keyword.trim());

                userTable.setItems(FXCollections.observableArrayList(result));

                totalRecords = result.size();
                totalPages = 1;
                currentPage = 1;

                mettreAJourPagination();

                return;
            }

            Role role = roleFilter.getValue();

            if (role != null) {

                List<User> result = userService.findByRole(role);

                userTable.setItems(FXCollections.observableArrayList(result));

                totalRecords = result.size();
                totalPages = 1;
                currentPage = 1;

                mettreAJourPagination();

                return;
            }

            List<User> users = userService.findPaginated(currentPage, pageSize);

            userTable.setItems(FXCollections.observableArrayList(users));

            totalRecords = userService.count();

            totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            if (totalPages < 1) {
                totalPages = 1;
            }

            if (currentPage > totalPages) {

                currentPage = totalPages;

                users = userService.findPaginated(currentPage, pageSize);

                userTable.setItems(FXCollections.observableArrayList(users));
            }

            mettreAJourPagination();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger les utilisateurs : " + e.getMessage()
            );
        }
    }

    @FXML
    private void resetFilters() {

        searchField.clear();
        roleFilter.setValue(null);

        currentPage = 1;

        chargerDonnees();
    }

    @FXML
    private void previousPage() {

        if (currentPage > 1) {

            currentPage--;
            chargerDonnees();
        }
    }

    @FXML
    private void nextPage() {

        if (currentPage < totalPages) {

            currentPage++;
            chargerDonnees();
        }
    }

    private void mettreAJourPagination() {

        pageLabel.setText(String.valueOf(currentPage));
        totalPagesLabel.setText(String.valueOf(totalPages));
        totalRecordsLabel.setText(String.valueOf(totalRecords));

        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= totalPages);
    }

    @FXML
    private void handleAdd() {
        ouvrirFormulaire(null);
    }

    @FXML
    private void handleEdit() {

        User user = userTable.getSelectionModel().getSelectedItem();

        if (user == null) {

            AlertUtil.warning("Attention", "Veuillez sélectionner un utilisateur.");
            return;
        }

        ouvrirFormulaire(user);
    }

    @FXML
    private void handleDelete() {

        User user = userTable.getSelectionModel().getSelectedItem();

        if (user == null) {

            AlertUtil.warning("Attention", "Veuillez sélectionner un utilisateur.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'utilisateur");

        confirmation.setContentText(
                "Voulez-vous vraiment supprimer l'utilisateur "
                        + user.getFullName() + " ?"
        );

        confirmation.showAndWait()
                .ifPresent(response -> {

                    if (response == ButtonType.OK) {

                        try {

                            userService.delete(user.getId());

                            chargerDonnees();

                            AlertUtil.information(
                                    "Succès",
                                    "Utilisateur supprimé avec succès."
                            );

                        } catch (Exception e) {

                            AlertUtil.error("Erreur", e.getMessage());
                        }
                    }
                });
    }

    @FXML
    private void handleToggleActif() {

        User user = userTable.getSelectionModel().getSelectedItem();

        if (user == null) {

            AlertUtil.warning("Attention", "Veuillez sélectionner un utilisateur.");
            return;
        }

        try {

            userService.toggleActif(user.getId());

            chargerDonnees();

            AlertUtil.information(
                    "Succès",
                    "Le statut de l'utilisateur a été modifié avec succès."
            );

        } catch (Exception e) {

            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    private void ouvrirFormulaire(User user) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/user-form.fxml")
            );

            Parent root = loader.load();

            UserController controller = loader.getController();

            controller.setUser(user);

            Stage stage = new Stage();

            stage.setTitle(
                    user == null
                            ? "Ajouter un utilisateur"
                            : "Modifier un utilisateur"
            );

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(new Scene(root));

            stage.showAndWait();

            chargerDonnees();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible d'ouvrir le formulaire :\n\n" + e.getMessage()
            );
        }
    }

    public void setUser(User user) {

        this.userForm = user;

        if (listPane == null || formPane == null) {
            return;
        }

        listPane.setVisible(false);
        listPane.setManaged(false);

        formPane.setVisible(true);
        formPane.setManaged(true);

        if (userForm == null) {

            nomField.clear();
            prenomField.clear();
            usernameField.clear();
            emailField.clear();
            telephoneField.clear();
            passwordField.clear();

            roleCombo.setValue(null);
            actifCheck.setSelected(true);

            passwordLabel.setText("Mot de passe * :");
            passwordField.setPromptText("Mot de passe");

        } else {

            nomField.setText(userForm.getNom());
            prenomField.setText(userForm.getPrenom());
            usernameField.setText(userForm.getUsername());
            emailField.setText(userForm.getEmail());
            telephoneField.setText(userForm.getTelephone());
            passwordField.clear();

            roleCombo.setValue(userForm.getRole());
            actifCheck.setSelected(userForm.isActif());

            passwordLabel.setText("Mot de passe :");
            passwordField.setPromptText("Laisser vide pour conserver l'ancien mot de passe");
        }

        errorLabel.setText("");

        javafx.application.Platform.runLater(
                () -> nomField.requestFocus()
        );
    }

    @FXML
    private void handleSave() {

        try {

            if (nomField.getText() == null || nomField.getText().isBlank()) {

                errorLabel.setText("Le nom est obligatoire.");
                return;
            }

            if (prenomField.getText() == null || prenomField.getText().isBlank()) {

                errorLabel.setText("Le prénom est obligatoire.");
                return;
            }

            if (usernameField.getText() == null || usernameField.getText().isBlank()) {

                errorLabel.setText("Le nom d'utilisateur est obligatoire.");
                return;
            }

            if (emailField.getText() == null || emailField.getText().isBlank()) {

                errorLabel.setText("L'email est obligatoire.");
                return;
            }

            if (telephoneField.getText() == null || telephoneField.getText().isBlank()) {

                errorLabel.setText("Le téléphone est obligatoire.");
                return;
            }

            if (roleCombo.getValue() == null) {

                errorLabel.setText("Le rôle est obligatoire.");
                return;
            }

            if (userForm == null &&
                    (passwordField.getText() == null || passwordField.getText().isBlank())) {

                errorLabel.setText("Le mot de passe est obligatoire pour un nouvel utilisateur.");
                return;
            }

            User objet = (userForm == null) ? new User() : userForm;

            objet.setNom(nomField.getText().trim());
            objet.setPrenom(prenomField.getText().trim());
            objet.setUsername(usernameField.getText().trim());
            objet.setEmail(emailField.getText().trim());
            objet.setTelephone(telephoneField.getText().trim());
            objet.setRole(roleCombo.getValue());
            objet.setActif(actifCheck.isSelected());

            if (passwordField.getText() != null && !passwordField.getText().isBlank()) {
                objet.setPassword(passwordField.getText());
            }

            if (userForm == null) {

                userService.create(objet);

                AlertUtil.information("Succès", "Utilisateur créé avec succès.");

            } else {

                userService.update(objet);

                AlertUtil.information("Succès", "Utilisateur modifié avec succès.");
            }

            closeForm();

        } catch (Exception e) {

            e.printStackTrace();

            errorLabel.setText(
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Une erreur est survenue."
            );
        }
    }

    @FXML
    private void handleCancel() {
        closeForm();
    }

    private void closeForm() {

        if (nomField != null && nomField.getScene() != null) {

            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();
        }
    }

    private void initialiserFormulaire() {

        if (roleCombo == null) {
            return;
        }

        roleCombo.setItems(
                FXCollections.observableArrayList(Role.values())
        );
    }

    private void mettreAJourBoutons() {

        if (userTable == null) {
            return;
        }

        User selected = userTable.getSelectionModel().getSelectedItem();

        boolean hasSelection = selected != null;

        boolean estUtilisateurConnecte = hasSelection &&
                SessionContext.getCurrentUser() != null &&
                SessionContext.getCurrentUser().getId() == selected.getId();

        if (editButton != null) {
            editButton.setDisable(!hasSelection);
        }

        if (deleteButton != null) {
            deleteButton.setDisable(!hasSelection || estUtilisateurConnecte);
        }

        if (toggleActifButton != null) {
            toggleActifButton.setDisable(!hasSelection || estUtilisateurConnecte);
        }
    }
}