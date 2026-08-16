package said.microgest.controllers;

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
import javafx.util.StringConverter;

import said.microgest.entities.Adherent;
import said.microgest.entities.Operation;
import said.microgest.enums.TypeOperation;
import said.microgest.services.AdherentService;
import said.microgest.services.OperationService;
import said.microgest.utils.AlertUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OperationController {

    @FXML private StackPane rootPane;
    @FXML private VBox listPane;
    @FXML private VBox formPane;

    @FXML private TableView<Operation> operationTable;

    @FXML private TableColumn<Operation, Number> idColumn;
    @FXML private TableColumn<Operation, String> typeColumn;
    @FXML private TableColumn<Operation, String> montantColumn;
    @FXML private TableColumn<Operation, String> dateColumn;
    @FXML private TableColumn<Operation, String> observationColumn;
    @FXML private TableColumn<Operation, String> adherentColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<TypeOperation> searchTypeCombo;

    @FXML private ComboBox<Integer> sizeCombo;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Label totalPagesLabel;
    @FXML private Label totalRecordsLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private ComboBox<TypeOperation> typeCombo;
    @FXML private TextField montantField;
    @FXML private TextArea observationArea;
    @FXML private ComboBox<Adherent> adherentCombo;
    @FXML private Label soldeLabel;
    @FXML private Label errorLabel;

    private final OperationService operationService =
            new OperationService();

    private final AdherentService adherentService =
            new AdherentService();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int currentPage = 1;
    private int pageSize = 10;
    private long totalRecords = 0;
    private int totalPages = 1;

    private Operation operationForm;

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

        operationTable.getSelectionModel()
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
                data -> new javafx.beans.property.SimpleIntegerProperty(
                        data.getValue().getId()
                )
        );

        typeColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getType() != null
                                ? data.getValue().getType().name()
                                : ""
                )
        );

        typeColumn.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(String type, boolean empty) {

                        super.updateItem(type, empty);

                        if (empty || type == null) {

                            setText(null);
                            setStyle("");

                            return;
                        }

                        if (type.equals("DEPOT")) {

                            setText("DÉPÔT");

                            setStyle(
                                    "-fx-text-fill: #27ae60; " +
                                            "-fx-font-weight: bold;"
                            );

                        } else {

                            setText("RETRAIT");

                            setStyle(
                                    "-fx-text-fill: #e74c3c; " +
                                            "-fx-font-weight: bold;"
                            );
                        }
                    }
                }
        );

        montantColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.format(
                                "%,.0f FCFA",
                                data.getValue().getMontant()
                        )
                )
        );

        dateColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getDateOperation() != null
                                ? data.getValue()
                                .getDateOperation()
                                .format(dateFormatter)
                                : ""
                )
        );

        observationColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getObservation() != null
                                ? data.getValue().getObservation()
                                : ""
                )
        );

        adherentColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getAdherent() != null
                                ? data.getValue()
                                .getAdherent()
                                .getFullName()
                                : ""
                )
        );
    }

    private void chargerFiltres() {

        searchTypeCombo.setItems(
                FXCollections.observableArrayList(
                        TypeOperation.values()
                )
        );

        searchTypeCombo.setConverter(typeConverter());

        searchTypeCombo.valueProperty()
                .addListener(
                        (obs, oldValue, newValue) -> {

                            currentPage = 1;
                            chargerDonnees();
                        }
                );
    }

    private StringConverter<TypeOperation> typeConverter() {

        return new StringConverter<>() {

            @Override
            public String toString(TypeOperation type) {

                if (type == null) {
                    return "";
                }

                return type == TypeOperation.DEPOT
                        ? "DÉPÔT"
                        : "RETRAIT";
            }

            @Override
            public TypeOperation fromString(String string) {
                return null;
            }
        };
    }

    private void chargerDonnees() {

        try {

            String keyword = searchField.getText();

            if (keyword != null && !keyword.isBlank()) {

                List<Operation> result =
                        operationService.search(keyword.trim());

                operationTable.setItems(
                        FXCollections.observableArrayList(result)
                );

                totalRecords = result.size();
                totalPages = 1;
                currentPage = 1;

                mettreAJourPagination();

                return;
            }

            TypeOperation typeSelectionne = searchTypeCombo.getValue();

            if (typeSelectionne != null) {

                List<Operation> result =
                        operationService.findByType(typeSelectionne);

                operationTable.setItems(
                        FXCollections.observableArrayList(result)
                );

                totalRecords = result.size();
                totalPages = 1;
                currentPage = 1;

                mettreAJourPagination();

                return;
            }

            List<Operation> operations =
                    operationService.findPaginated(currentPage, pageSize);

            operationTable.setItems(
                    FXCollections.observableArrayList(operations)
            );

            totalRecords = operationService.count();

            totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            if (totalPages < 1) {
                totalPages = 1;
            }

            if (currentPage > totalPages) {

                currentPage = totalPages;

                operations =
                        operationService.findPaginated(currentPage, pageSize);

                operationTable.setItems(
                        FXCollections.observableArrayList(operations)
                );
            }

            mettreAJourPagination();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger les opérations : " + e.getMessage()
            );
        }
    }

    @FXML
    private void applyFilters() {

        currentPage = 1;
        chargerDonnees();
    }

    @FXML
    private void resetFilters() {

        searchField.clear();
        searchTypeCombo.setValue(null);

        currentPage = 1;

        chargerDonnees();
    }

    @FXML
    private void handleSearch() {

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

        Operation operation =
                operationTable.getSelectionModel().getSelectedItem();

        if (operation == null) {

            AlertUtil.warning(
                    "Attention",
                    "Veuillez sélectionner une opération."
            );

            return;
        }

        ouvrirFormulaire(operation);
    }

    @FXML
    private void handleDelete() {

        Operation operation =
                operationTable.getSelectionModel().getSelectedItem();

        if (operation == null) {

            AlertUtil.warning(
                    "Attention",
                    "Veuillez sélectionner une opération."
            );

            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer l'opération");

        confirmation.setContentText(
                "Voulez-vous vraiment supprimer cette opération de "
                        + String.format("%,.0f FCFA", operation.getMontant())
                        + " ?"
        );

        confirmation.showAndWait()
                .ifPresent(response -> {

                    if (response == ButtonType.OK) {

                        try {

                            operationService.delete(operation.getId());

                            chargerDonnees();

                            AlertUtil.information(
                                    "Succès",
                                    "Opération supprimée avec succès."
                            );

                        } catch (Exception e) {

                            e.printStackTrace();

                            AlertUtil.error("Erreur", e.getMessage());
                        }
                    }
                });
    }

    private void ouvrirFormulaire(Operation operation) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/operation-form.fxml")
            );

            Parent root = loader.load();

            OperationController controller = loader.getController();

            controller.setOperation(operation);

            Stage stage = new Stage();

            stage.setTitle(
                    operation == null
                            ? "Ajouter une opération"
                            : "Modifier une opération"
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

    public void setOperation(Operation operation) {

        this.operationForm = operation;

        if (listPane == null || formPane == null) {
            return;
        }

        listPane.setVisible(false);
        listPane.setManaged(false);

        formPane.setVisible(true);
        formPane.setManaged(true);

        if (operationForm == null) {

            typeCombo.setValue(null);
            montantField.clear();
            observationArea.clear();
            adherentCombo.setValue(null);

            soldeLabel.setText("Solde actuel : —");

        } else {

            typeCombo.setValue(operationForm.getType());

            montantField.setText(
                    operationForm.getMontant().toPlainString()
            );

            observationArea.setText(
                    operationForm.getObservation() != null
                            ? operationForm.getObservation()
                            : ""
            );

            if (operationForm.getAdherent() != null) {

                adherentCombo.getItems()
                        .stream()
                        .filter(a -> a.getId()
                                == operationForm.getAdherent().getId())
                        .findFirst()
                        .ifPresent(adherentCombo::setValue);
            }

            updateSolde(adherentCombo.getValue());
        }

        errorLabel.setText("");

        javafx.application.Platform.runLater(
                () -> typeCombo.requestFocus()
        );
    }

    @FXML
    private void handleSave() {

        try {

            if (typeCombo.getValue() == null) {

                errorLabel.setText("Le type d'opération est obligatoire.");
                return;
            }

            if (adherentCombo.getValue() == null) {

                errorLabel.setText("L'adhérent est obligatoire.");
                return;
            }

            String montantText = montantField.getText() == null
                    ? ""
                    : montantField.getText().trim().replace(",", ".");

            if (montantText.isBlank()) {

                errorLabel.setText("Le montant est obligatoire.");
                return;
            }

            BigDecimal montant;

            try {

                montant = new BigDecimal(montantText);

            } catch (NumberFormatException e) {

                errorLabel.setText("Le montant doit être un nombre valide.");
                return;
            }

            if (montant.compareTo(BigDecimal.ZERO) <= 0) {

                errorLabel.setText("Le montant doit être supérieur à zéro.");
                return;
            }

            String observation = observationArea.getText() == null
                    ? ""
                    : observationArea.getText().trim();

            if (observation.length() > 255) {

                errorLabel.setText(
                        "L'observation ne doit pas dépasser 255 caractères."
                );

                return;
            }

            if (typeCombo.getValue() == TypeOperation.RETRAIT) {

                BigDecimal solde = operationService.getSoldeAdherent(
                        adherentCombo.getValue().getId()
                );

                if (operationForm != null &&
                        operationForm.getType() == TypeOperation.RETRAIT) {

                    solde = solde.add(operationForm.getMontant());
                }

                if (montant.compareTo(solde) > 0) {

                    errorLabel.setText(
                            "Le montant du retrait ("
                                    + String.format("%,.0f FCFA", montant)
                                    + ") dépasse le solde disponible ("
                                    + String.format("%,.0f FCFA", solde)
                                    + ")."
                    );

                    return;
                }
            }

            Operation objet = (operationForm == null)
                    ? new Operation()
                    : operationForm;

            objet.setType(typeCombo.getValue());
            objet.setMontant(montant);
            objet.setObservation(observation);
            objet.setAdherent(adherentCombo.getValue());
            objet.setDateOperation(LocalDateTime.now());

            if (operationForm == null) {

                operationService.create(objet);

                AlertUtil.information(
                        "Succès",
                        "Opération créée avec succès."
                );

            } else {

                operationService.update(objet);

                AlertUtil.information(
                        "Succès",
                        "Opération modifiée avec succès."
                );
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

        if (typeCombo != null && typeCombo.getScene() != null) {

            Stage stage = (Stage) typeCombo.getScene().getWindow();
            stage.close();
        }
    }

    private void initialiserFormulaire() {

        if (typeCombo == null) {
            return;
        }

        typeCombo.setItems(
                FXCollections.observableArrayList(TypeOperation.values())
        );

        typeCombo.setConverter(typeConverter());

        adherentCombo.setItems(
                FXCollections.observableArrayList(
                        adherentService.findAll()
                )
        );

        StringConverter<Adherent> adherentConverter = new StringConverter<>() {

            @Override
            public String toString(Adherent adherent) {
                return adherent != null ? adherent.getFullName() : "";
            }

            @Override
            public Adherent fromString(String string) {
                return null;
            }
        };

        adherentCombo.setConverter(adherentConverter);

        adherentCombo.valueProperty()
                .addListener(
                        (obs, oldValue, newValue) -> updateSolde(newValue)
                );
    }

    private void updateSolde(Adherent adherent) {

        if (adherent == null) {

            soldeLabel.setText("Solde actuel : —");
            return;
        }

        try {

            BigDecimal solde =
                    operationService.getSoldeAdherent(adherent.getId());

            soldeLabel.setText(
                    String.format("Solde actuel : %,.0f FCFA", solde)
            );

        } catch (RuntimeException e) {

            soldeLabel.setText("Solde actuel : indisponible");
        }
    }

    private void mettreAJourBoutons() {

        if (operationTable == null) {
            return;
        }

        boolean selected =
                operationTable.getSelectionModel().getSelectedItem() != null;

        if (editButton != null) {
            editButton.setDisable(!selected);
        }

        if (deleteButton != null) {
            deleteButton.setDisable(!selected);
        }
    }
}