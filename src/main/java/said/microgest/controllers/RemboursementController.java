package said.microgest.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import said.microgest.entities.Pret;
import said.microgest.entities.Remboursement;
import said.microgest.services.PretService;
import said.microgest.services.RemboursementService;
import said.microgest.utils.AlertUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RemboursementController {

    @FXML
    private TableView<Remboursement> remboursementTable;

    @FXML
    private TableColumn<Remboursement, Integer> idColumn;

    @FXML
    private TableColumn<Remboursement, BigDecimal> montantColumn;

    @FXML
    private TableColumn<Remboursement, LocalDate> dateColumn;

    @FXML
    private TableColumn<Remboursement, Integer> echeanceColumn;

    @FXML
    private TableColumn<Remboursement, String> pretColumn;

    @FXML
    private ComboBox<Pret> pretCombo;

    @FXML
    private TextField montantField;

    @FXML
    private DatePicker datePaiementPicker;

    @FXML
    private TextField numeroEcheanceField;

    @FXML
    private Label montantRestantLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    private final RemboursementService remboursementService = new RemboursementService();
    private final PretService pretService = new PretService();
    private ObservableList<Remboursement> remboursements = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initTableColumns();
        loadPrets();
        loadData();
        setupSelectionListener();
        setupPretListener();
    }

    private void initTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("datePaiement"));
        echeanceColumn.setCellValueFactory(new PropertyValueFactory<>("numeroEcheance"));
        pretColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getPret() != null ?
                                "Prêt #" + cellData.getValue().getPret().getId() : ""
                )
        );
    }

    private void loadPrets() {
        try {
            List<Pret> prets = pretService.findValides();
            pretCombo.setItems(FXCollections.observableArrayList(prets));
        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des prêts : " + e.getMessage());
        }
    }

    private void setupPretListener() {
        pretCombo.valueProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                try {
                    BigDecimal restant = pretService.getMontantRestant(selected.getId());
                    montantRestantLabel.setText("Montant restant : " + restant + " FCFA");
                } catch (Exception e) {
                    montantRestantLabel.setText("Montant restant : -");
                }
            } else {
                montantRestantLabel.setText("Montant restant : -");
            }
        });
    }

    private void setupSelectionListener() {
        remboursementTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        afficherRemboursement(selected);
                    }
                }
        );
    }

    private void loadData() {
        try {
            remboursements.setAll(remboursementService.findAll());
            remboursementTable.setItems(remboursements);
        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des remboursements : " + e.getMessage());
        }
    }

    private void afficherRemboursement(Remboursement remboursement) {
        pretCombo.setValue(remboursement.getPret());
        montantField.setText(remboursement.getMontant().toString());
        datePaiementPicker.setValue(remboursement.getDatePaiement());
        numeroEcheanceField.setText(String.valueOf(remboursement.getNumeroEcheance()));
    }

    @FXML
    private void handleSave() {
        try {
            Remboursement remboursement = new Remboursement();

            if (remboursementTable.getSelectionModel().getSelectedItem() != null) {
                remboursement.setId(remboursementTable.getSelectionModel().getSelectedItem().getId());
            }

            remboursement.setPret(pretCombo.getValue());
            remboursement.setMontant(new BigDecimal(montantField.getText().trim()));
            remboursement.setDatePaiement(datePaiementPicker.getValue());

            if (numeroEcheanceField.getText() != null && !numeroEcheanceField.getText().isEmpty()) {
                remboursement.setNumeroEcheance(Integer.parseInt(numeroEcheanceField.getText().trim()));
            }

            if (remboursement.getId() == 0) {
                remboursementService.create(remboursement);
                AlertUtil.information("Succès", "Remboursement enregistré avec succès !");
            } else {
                remboursementService.update(remboursement);
                AlertUtil.information("Succès", "Remboursement modifié avec succès !");
            }

            clearForm();
            loadData();
            loadPrets();

        } catch (NumberFormatException e) {
            AlertUtil.error("Erreur", "Veuillez saisir des valeurs numériques valides.");
        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Remboursement selected = remboursementTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner un remboursement.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le remboursement");
        alert.setContentText("Voulez-vous vraiment supprimer ce remboursement ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    remboursementService.delete(selected.getId());
                    AlertUtil.information("Succès", "Remboursement supprimé avec succès !");
                    clearForm();
                    loadData();
                    loadPrets();
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
        pretCombo.setValue(null);
        montantField.clear();
        datePaiementPicker.setValue(null);
        numeroEcheanceField.clear();
        montantRestantLabel.setText("Montant restant : -");
        remboursementTable.getSelectionModel().clearSelection();
    }
}