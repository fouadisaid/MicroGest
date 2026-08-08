package said.microgest.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import said.microgest.entities.Adherent;
import said.microgest.entities.Operation;
import said.microgest.enums.TypeOperation;
import said.microgest.services.AdherentService;
import said.microgest.services.OperationService;
import said.microgest.utils.AlertUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OperationController {

    @FXML
    private TableView<Operation> operationTable;

    @FXML
    private TableColumn<Operation, Integer> idColumn;

    @FXML
    private TableColumn<Operation, TypeOperation> typeColumn;

    @FXML
    private TableColumn<Operation, BigDecimal> montantColumn;

    @FXML
    private TableColumn<Operation, LocalDateTime> dateColumn;

    @FXML
    private TableColumn<Operation, String> observationColumn;

    @FXML
    private TableColumn<Operation, String> adherentColumn;

    @FXML
    private ComboBox<TypeOperation> typeCombo;

    @FXML
    private TextField montantField;

    @FXML
    private TextArea observationArea;

    @FXML
    private ComboBox<Adherent> adherentCombo;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    private final OperationService operationService = new OperationService();
    private final AdherentService adherentService = new AdherentService();
    private ObservableList<Operation> operations = FXCollections.observableArrayList();

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
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        montantColumn.setCellFactory(col -> new TableCell<Operation, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f FCFA", item));
                }
            }
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateOperation"));
        observationColumn.setCellValueFactory(new PropertyValueFactory<>("observation"));
        adherentColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAdherent() != null ?
                                cellData.getValue().getAdherent().getFullName() : ""
                )
        );
    }

    private void initComboBoxes() {
        typeCombo.setItems(FXCollections.observableArrayList(TypeOperation.values()));
    }

    private void loadAdherents() {
        try {
            List<Adherent> adherents = adherentService.findAll();
            adherentCombo.setItems(FXCollections.observableArrayList(adherents));

            adherentCombo.setCellFactory(listView -> new ListCell<Adherent>() {
                @Override
                protected void updateItem(Adherent item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getFullName());
                    }
                }
            });

            adherentCombo.setButtonCell(new ListCell<Adherent>() {
                @Override
                protected void updateItem(Adherent item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getFullName());
                    }
                }
            });

        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des adhérents : " + e.getMessage());
        }
    }

    private void setupSelectionListener() {
        operationTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        afficherOperation(selected);
                    }
                }
        );
    }

    private void loadData() {
        try {
            operations.setAll(operationService.findAll());
            operationTable.setItems(operations);
        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des opérations : " + e.getMessage());
        }
    }

    private void afficherOperation(Operation operation) {
        typeCombo.setValue(operation.getType());
        montantField.setText(operation.getMontant().toString());
        observationArea.setText(operation.getObservation());
        adherentCombo.setValue(operation.getAdherent());
    }

    @FXML
    private void handleSave() {
        try {
            Operation operation = new Operation();

            if (operationTable.getSelectionModel().getSelectedItem() != null) {
                operation.setId(operationTable.getSelectionModel().getSelectedItem().getId());
            }

            operation.setType(typeCombo.getValue());
            operation.setMontant(new BigDecimal(montantField.getText().trim()));
            operation.setObservation(observationArea.getText().trim());
            operation.setAdherent(adherentCombo.getValue());
            operation.setDateOperation(LocalDateTime.now());

            if (operation.getId() == 0) {
                operationService.create(operation);
                AlertUtil.information("Succès", "Opération créée avec succès !");
            } else {
                operationService.update(operation);
                AlertUtil.information("Succès", "Opération modifiée avec succès !");
            }

            clearForm();
            loadData();

        } catch (NumberFormatException e) {
            AlertUtil.error("Erreur", "Le montant doit être un nombre valide.");
        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Operation selected = operationTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner une opération.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'opération");
        alert.setContentText("Voulez-vous vraiment supprimer cette opération ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    operationService.delete(selected.getId());
                    AlertUtil.information("Succès", "Opération supprimée avec succès !");
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
        typeCombo.setValue(null);
        montantField.clear();
        observationArea.clear();
        adherentCombo.setValue(null);
        operationTable.getSelectionModel().clearSelection();
    }
}