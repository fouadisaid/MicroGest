package said.microgest.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import said.microgest.entities.Adherent;
import said.microgest.entities.Pret;
import said.microgest.enums.StatutPret;
import said.microgest.services.AdherentService;
import said.microgest.services.PretService;
import said.microgest.utils.AlertUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PretController {

    @FXML
    private TableView<Pret> pretTable;

    @FXML
    private TableColumn<Pret, Integer> idColumn;

    @FXML
    private TableColumn<Pret, BigDecimal> montantColumn;

    @FXML
    private TableColumn<Pret, BigDecimal> tauxColumn;

    @FXML
    private TableColumn<Pret, Integer> dureeColumn;

    @FXML
    private TableColumn<Pret, LocalDate> dateColumn;

    @FXML
    private TableColumn<Pret, StatutPret> statutColumn;

    @FXML
    private TableColumn<Pret, String> adherentColumn;

    @FXML
    private TextField montantField;

    @FXML
    private TextField tauxField;

    @FXML
    private TextField dureeField;

    @FXML
    private DatePicker datePretPicker;

    @FXML
    private ComboBox<StatutPret> statutCombo;

    @FXML
    private ComboBox<Adherent> adherentCombo;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button validerButton;

    @FXML
    private Button rejeterButton;

    @FXML
    private Button calculerMensualiteButton;

    @FXML
    private Label mensualiteLabel;

    private final PretService pretService = new PretService();
    private final AdherentService adherentService = new AdherentService();
    private ObservableList<Pret> prets = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initTableColumns();
        initComboBoxes();
        loadAdherents();
        loadData();
        setupSelectionListener();
    }

    private void initTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        tauxColumn.setCellValueFactory(new PropertyValueFactory<>("taux"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("datePret"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        statutColumn.setCellFactory(col -> new TableCell<Pret, StatutPret>() {
            @Override
            protected void updateItem(StatutPret item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.name());
                    switch (item) {
                        case EN_ATTENTE -> setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        case VALIDE -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        case REJETE -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        case REMBOURSE -> setStyle("-fx-text-fill: #2980b9; -fx-font-weight: bold;");
                    }
                }
            }
        });
        adherentColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAdherent() != null ?
                                cellData.getValue().getAdherent().getFullName() : ""
                )
        );
    }

    private void initComboBoxes() {
        statutCombo.setItems(FXCollections.observableArrayList(StatutPret.values()));
    }

    private void loadAdherents() {
        try {
            List<Adherent> adherents = adherentService.findAll();
            adherentCombo.setItems(FXCollections.observableArrayList(adherents));
        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des adhérents : " + e.getMessage());
        }
    }

    private void setupSelectionListener() {
        pretTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        afficherPret(selected);
                    }
                }
        );
    }

    private void loadData() {
        try {
            prets.setAll(pretService.findAll());
            pretTable.setItems(prets);
        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des prêts : " + e.getMessage());
        }
    }

    private void afficherPret(Pret pret) {
        montantField.setText(pret.getMontant().toString());
        tauxField.setText(pret.getTaux().toString());
        dureeField.setText(pret.getDuree().toString());
        datePretPicker.setValue(pret.getDatePret());
        statutCombo.setValue(pret.getStatut());
        adherentCombo.setValue(pret.getAdherent());

        // Calculer et afficher la mensualité
        try {
            BigDecimal mensualite = pretService.calculerMensualite(pret);
            mensualiteLabel.setText("Mensualité : " + mensualite + " FCFA");
        } catch (Exception e) {
            mensualiteLabel.setText("Mensualité : -");
        }
    }

    @FXML
    private void handleSave() {
        try {
            Pret pret = new Pret();

            if (pretTable.getSelectionModel().getSelectedItem() != null) {
                pret.setId(pretTable.getSelectionModel().getSelectedItem().getId());
            }

            pret.setMontant(new BigDecimal(montantField.getText().trim()));
            pret.setTaux(new BigDecimal(tauxField.getText().trim()));
            pret.setDuree(Integer.parseInt(dureeField.getText().trim()));
            pret.setDatePret(datePretPicker.getValue());
            pret.setStatut(statutCombo.getValue());
            pret.setAdherent(adherentCombo.getValue());

            if (pret.getId() == 0) {
                pretService.create(pret);
                AlertUtil.information("Succès", "Prêt créé avec succès !");
            } else {
                pretService.update(pret);
                AlertUtil.information("Succès", "Prêt modifié avec succès !");
            }

            clearForm();
            loadData();

        } catch (NumberFormatException e) {
            AlertUtil.error("Erreur", "Veuillez saisir des valeurs numériques valides.");
        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Pret selected = pretTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner un prêt.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le prêt");
        alert.setContentText("Voulez-vous vraiment supprimer ce prêt ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    pretService.delete(selected.getId());
                    AlertUtil.information("Succès", "Prêt supprimé avec succès !");
                    clearForm();
                    loadData();
                } catch (RuntimeException e) {
                    AlertUtil.error("Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleValider() {
        Pret selected = pretTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner un prêt.");
            return;
        }

        try {
            pretService.valider(selected.getId());
            AlertUtil.information("Succès", "Prêt validé avec succès !");
            loadData();
            clearForm();
        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleRejeter() {
        Pret selected = pretTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner un prêt.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Rejeter le prêt");
        alert.setContentText("Voulez-vous vraiment rejeter ce prêt ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    pretService.rejeter(selected.getId());
                    AlertUtil.information("Succès", "Prêt rejeté avec succès !");
                    loadData();
                    clearForm();
                } catch (RuntimeException e) {
                    AlertUtil.error("Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCalculerMensualite() {
        try {
            Pret pret = new Pret();
            pret.setMontant(new BigDecimal(montantField.getText().trim()));
            pret.setTaux(new BigDecimal(tauxField.getText().trim()));
            pret.setDuree(Integer.parseInt(dureeField.getText().trim()));

            BigDecimal mensualite = pretService.calculerMensualite(pret);
            mensualiteLabel.setText("Mensualité : " + mensualite + " FCFA");

        } catch (NumberFormatException e) {
            AlertUtil.error("Erreur", "Veuillez saisir des valeurs numériques valides.");
        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
    }

    private void clearForm() {
        montantField.clear();
        tauxField.clear();
        dureeField.clear();
        datePretPicker.setValue(null);
        statutCombo.setValue(null);
        adherentCombo.setValue(null);
        mensualiteLabel.setText("Mensualité : -");
        pretTable.getSelectionModel().clearSelection();
    }
}