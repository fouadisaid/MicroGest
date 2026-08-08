package said.microgest.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import said.microgest.entities.Agence;
import said.microgest.services.AgenceService;
import said.microgest.utils.AlertUtil;

public class AgenceController {

    @FXML
    private TableView<Agence> agenceTable;

    @FXML
    private TableColumn<Agence, Integer> idColumn;

    @FXML
    private TableColumn<Agence, String> nomColumn;

    @FXML
    private TableColumn<Agence, String> adresseColumn;

    @FXML
    private TableColumn<Agence, String> telephoneColumn;

    @FXML
    private TableColumn<Agence, String> emailColumn;

    @FXML
    private TextField nomField;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField telephoneField;

    @FXML
    private TextField emailField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    private final AgenceService agenceService = new AgenceService();
    private ObservableList<Agence> agences = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initTableColumns();
        loadData();
        setupSelectionListener();
    }

    private void initTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void setupSelectionListener() {
        agenceTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        afficherAgence(selected);
                    }
                }
        );
    }

    private void loadData() {
        try {
            agences.setAll(agenceService.findAll());
            agenceTable.setItems(agences);
        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des agences : " + e.getMessage());
        }
    }

    private void afficherAgence(Agence agence) {
        nomField.setText(agence.getNom());
        adresseField.setText(agence.getAdresse());
        telephoneField.setText(agence.getTelephone());
        emailField.setText(agence.getEmail());
    }

    @FXML
    private void handleSave() {
        try {
            Agence agence = new Agence();

            if (agenceTable.getSelectionModel().getSelectedItem() != null) {
                agence.setId(agenceTable.getSelectionModel().getSelectedItem().getId());
            }

            agence.setNom(nomField.getText().trim());
            agence.setAdresse(adresseField.getText().trim());
            agence.setTelephone(telephoneField.getText().trim());
            agence.setEmail(emailField.getText().trim());

            if (agence.getId() == 0) {
                agenceService.create(agence);
                AlertUtil.information("Succès", "Agence créée avec succès !");
            } else {
                agenceService.update(agence);
                AlertUtil.information("Succès", "Agence modifiée avec succès !");
            }

            clearForm();
            loadData();

        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Agence selected = agenceTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner une agence.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'agence");
        alert.setContentText("Voulez-vous vraiment supprimer l'agence " +
                selected.getNom() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    agenceService.delete(selected.getId());
                    AlertUtil.information("Succès", "Agence supprimée avec succès !");
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
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        agenceTable.getSelectionModel().clearSelection();
    }
}