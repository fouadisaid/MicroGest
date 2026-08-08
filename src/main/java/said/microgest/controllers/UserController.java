package said.microgest.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import said.microgest.entities.User;
import said.microgest.enums.Role;
import said.microgest.services.UserService;
import said.microgest.utils.AlertUtil;

public class UserController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nomColumn;

    @FXML
    private TableColumn<User, String> prenomColumn;

    @FXML
    private TableColumn<User, String> usernameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> telephoneColumn;

    @FXML
    private TableColumn<User, Role> roleColumn;

    @FXML
    private TableColumn<User, Boolean> actifColumn;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<Role> roleCombo;

    @FXML
    private CheckBox actifCheck;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    private final UserService userService = new UserService();
    private ObservableList<User> users = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initTableColumns();
        initComboBoxes();
        loadData();
        setupSelectionListener();
    }

    private void initTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        actifColumn.setCellValueFactory(new PropertyValueFactory<>("actif"));
        actifColumn.setCellFactory(col -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "✅ Actif" : "❌ Inactif");
                    setStyle(item ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });
    }

    private void initComboBoxes() {
        roleCombo.setItems(FXCollections.observableArrayList(Role.values()));
    }

    private void setupSelectionListener() {
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        afficherUser(selected);
                    }
                }
        );
    }

    private void loadData() {
        try {
            users.setAll(userService.findAll());
            userTable.setItems(users);
        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des utilisateurs : " + e.getMessage());
        }
    }

    private void afficherUser(User user) {
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        telephoneField.setText(user.getTelephone());
        passwordField.clear();
        roleCombo.setValue(user.getRole());
        actifCheck.setSelected(user.isActif());
    }

    @FXML
    private void handleSave() {
        try {
            User user = new User();

            if (userTable.getSelectionModel().getSelectedItem() != null) {
                user.setId(userTable.getSelectionModel().getSelectedItem().getId());
            }

            user.setNom(nomField.getText().trim());
            user.setPrenom(prenomField.getText().trim());
            user.setUsername(usernameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setTelephone(telephoneField.getText().trim());

            String password = passwordField.getText();
            if (password != null && !password.isEmpty()) {
                user.setPassword(password);
            } else if (user.getId() == 0) {
                AlertUtil.warning("Attention", "Le mot de passe est obligatoire pour un nouvel utilisateur.");
                return;
            }

            user.setRole(roleCombo.getValue());
            user.setActif(actifCheck.isSelected());

            if (user.getId() == 0) {
                userService.create(user);
                AlertUtil.information("Succès", "Utilisateur créé avec succès !");
            } else {
                userService.update(user);
                AlertUtil.information("Succès", "Utilisateur modifié avec succès !");
            }

            clearForm();
            loadData();

        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        User selected = userTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner un utilisateur.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Voulez-vous vraiment supprimer l'utilisateur " +
                selected.getFullName() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    userService.delete(selected.getId());
                    AlertUtil.information("Succès", "Utilisateur supprimé avec succès !");
                    clearForm();
                    loadData();
                } catch (RuntimeException e) {
                    AlertUtil.error("Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCancel() {
        clearForm();
    }

    private void clearForm() {
        nomField.clear();
        prenomField.clear();
        usernameField.clear();
        emailField.clear();
        telephoneField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
        actifCheck.setSelected(true);
        userTable.getSelectionModel().clearSelection();
    }
}