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
import javafx.util.StringConverter;

import said.microgest.entities.Adherent;
import said.microgest.entities.Agence;
import said.microgest.enums.StatutAdherent;
import said.microgest.services.AdherentService;
import said.microgest.services.AgenceService;
import said.microgest.utils.AlertUtil;

import java.time.LocalDate;
import java.util.List;

public class AdherentController {

    @FXML private StackPane rootPane;
    @FXML private VBox listPane;
    @FXML private VBox formPane;

    @FXML private TableView<Adherent> adherentTable;

    @FXML private TableColumn<Adherent, Number> idColumn;
    @FXML private TableColumn<Adherent, String> numeroColumn;
    @FXML private TableColumn<Adherent, String> nomColumn;
    @FXML private TableColumn<Adherent, String> prenomColumn;
    @FXML private TableColumn<Adherent, String> sexeColumn;
    @FXML private TableColumn<Adherent, String> telephoneColumn;
    @FXML private TableColumn<Adherent, String> emailColumn;
    @FXML private TableColumn<Adherent, String> adresseColumn;
    @FXML private TableColumn<Adherent, String> statutColumn;
    @FXML private TableColumn<Adherent, String> agenceColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<StatutAdherent> statutFilter;
    @FXML private ComboBox<Agence> agenceFilter;

    @FXML private ComboBox<Integer> sizeCombo;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Label totalPagesLabel;
    @FXML private Label totalRecordsLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button toggleStatutButton;

    @FXML private TextField numeroField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private ComboBox<String> sexeCombo;
    @FXML private DatePicker dateNaissancePicker;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField adresseField;
    @FXML private DatePicker dateAdhesionPicker;
    @FXML private ComboBox<StatutAdherent> statutCombo;
    @FXML private ComboBox<Agence> agenceCombo;
    @FXML private Label errorLabel;

    private final AdherentService adherentService =
            new AdherentService();

    private final AgenceService agenceService =
            new AgenceService();

    private int currentPage = 1;
    private int pageSize = 10;
    private long totalRecords = 0;
    private int totalPages = 1;

    private Adherent adherentForm;

    @FXML
    public void initialize() {

        configurerColonnes();
        chargerFiltres();

        sizeCombo.setItems(
                FXCollections.observableArrayList(
                        5, 10, 20, 50
                )
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

        adherentTable.getSelectionModel()
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
                data -> new SimpleIntegerProperty(
                        data.getValue().getId()
                )
        );

        numeroColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getNumeroAdherent()
                )
        );

        nomColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getNom()
                )
        );

        prenomColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getPrenom()
                )
        );

        sexeColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getSexe()
                )
        );

        telephoneColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getTelephone()
                )
        );

        emailColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getEmail()
                )
        );

        adresseColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getAdresse()
                )
        );

        statutColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getStatut() != null
                                ? data.getValue().getStatut().name()
                                : ""
                )
        );

        statutColumn.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(
                            String statut,
                            boolean empty
                    ) {

                        super.updateItem(statut, empty);

                        if (empty || statut == null) {

                            setText(null);
                            setStyle("");

                            return;
                        }

                        setText(statut);

                        String valeur =
                                statut.toUpperCase();

                        if (valeur.equals("ACTIF")) {

                            setStyle(
                                    "-fx-text-fill: #27ae60; " +
                                            "-fx-font-weight: bold;"
                            );

                        } else if (
                                valeur.equals("SUSPENDU") ||
                                        valeur.equals("INACTIF")
                        ) {

                            setStyle(
                                    "-fx-text-fill: #e74c3c; " +
                                            "-fx-font-weight: bold;"
                            );

                        } else {

                            setStyle(
                                    "-fx-text-fill: #f39c12; " +
                                            "-fx-font-weight: bold;"
                            );
                        }
                    }
                }
        );

        agenceColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getAgence() != null
                                ? data.getValue()
                                .getAgence()
                                .getNom()
                                : ""
                )
        );
    }

    private void chargerFiltres() {

        statutFilter.setItems(
                FXCollections.observableArrayList(
                        StatutAdherent.values()
                )
        );

        List<Agence> agences =
                agenceService.findAll();

        agenceFilter.setItems(
                FXCollections.observableArrayList(
                        agences
                )
        );

        agenceFilter.setConverter(
                new StringConverter<>() {

                    @Override
                    public String toString(Agence agence) {

                        return agence != null
                                ? agence.getNom()
                                : "";
                    }

                    @Override
                    public Agence fromString(String string) {

                        return null;
                    }
                }
        );
    }

    private void chargerDonnees() {

        try {

            String keyword =
                    searchField.getText();

            if (keyword != null &&
                    !keyword.isBlank()) {

                List<Adherent> result =
                        adherentService.search(
                                keyword.trim()
                        );

                adherentTable.setItems(
                        FXCollections.observableArrayList(
                                result
                        )
                );

                totalRecords = result.size();
                totalPages = 1;
                currentPage = 1;

                mettreAJourPagination();

                return;
            }

            List<Adherent> adherents =
                    adherentService.findPaginated(
                            currentPage,
                            pageSize
                    );

            adherentTable.setItems(
                    FXCollections.observableArrayList(
                            adherents
                    )
            );

            totalRecords =
                    adherentService.count();

            totalPages =
                    (int) Math.ceil(
                            (double) totalRecords
                                    / pageSize
                    );

            if (totalPages < 1) {
                totalPages = 1;
            }

            if (currentPage > totalPages) {

                currentPage = totalPages;

                adherents =
                        adherentService.findPaginated(
                                currentPage,
                                pageSize
                        );

                adherentTable.setItems(
                        FXCollections.observableArrayList(
                                adherents
                        )
                );
            }

            mettreAJourPagination();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger les adhérents : "
                            + e.getMessage()
            );
        }
    }

    @FXML
    private void applyFilters() {

        try {

            StatutAdherent statut =
                    statutFilter.getValue();

            Agence agence =
                    agenceFilter.getValue();

            Integer agenceId =
                    agence != null
                            ? agence.getId()
                            : null;

            List<Adherent> result =
                    adherentService.filter(
                            statut,
                            agenceId,
                            null,
                            null
                    );

            adherentTable.setItems(
                    FXCollections.observableArrayList(
                            result
                    )
            );

            totalRecords = result.size();
            totalPages = 1;
            currentPage = 1;

            mettreAJourPagination();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void resetFilters() {

        searchField.clear();

        statutFilter.setValue(null);

        agenceFilter.setValue(null);

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

        pageLabel.setText(
                String.valueOf(currentPage)
        );

        totalPagesLabel.setText(
                String.valueOf(totalPages)
        );

        totalRecordsLabel.setText(
                String.valueOf(totalRecords)
        );

        prevButton.setDisable(
                currentPage <= 1
        );

        nextButton.setDisable(
                currentPage >= totalPages
        );
    }

    @FXML
    private void handleAdd() {

        ouvrirFormulaire(null);
    }

    @FXML
    private void handleEdit() {

        Adherent adherent =
                adherentTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (adherent == null) {

            AlertUtil.warning(
                    "Attention",
                    "Veuillez sélectionner un adhérent."
            );

            return;
        }

        ouvrirFormulaire(adherent);
    }

    @FXML
    private void handleDelete() {

        Adherent adherent =
                adherentTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (adherent == null) {

            AlertUtil.warning(
                    "Attention",
                    "Veuillez sélectionner un adhérent."
            );

            return;
        }

        Alert confirmation =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirmation.setTitle("Confirmation");

        confirmation.setHeaderText(
                "Supprimer l'adhérent"
        );

        confirmation.setContentText(
                "Voulez-vous vraiment supprimer "
                        + adherent.getNom()
                        + " "
                        + adherent.getPrenom()
                        + " ?"
        );

        confirmation.showAndWait()
                .ifPresent(response -> {

                    if (response == ButtonType.OK) {

                        try {

                            adherentService.delete(
                                    adherent.getId()
                            );

                            chargerDonnees();

                            AlertUtil.information(
                                    "Succès",
                                    "Adhérent supprimé avec succès."
                            );

                        } catch (Exception e) {

                            e.printStackTrace();

                            AlertUtil.error(
                                    "Erreur",
                                    e.getMessage()
                            );
                        }
                    }
                });
    }

    @FXML
    private void handleToggleStatut() {

        Adherent adherent =
                adherentTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (adherent == null) {

            AlertUtil.warning(
                    "Attention",
                    "Veuillez sélectionner un adhérent."
            );

            return;
        }

        try {

            adherentService.toggleStatut(
                    adherent.getId()
            );

            chargerDonnees();

            AlertUtil.information(
                    "Succès",
                    "Le statut de l'adhérent a été modifié avec succès."
            );

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    e.getMessage()
            );
        }
    }

    private void ouvrirFormulaire(
            Adherent adherent
    ) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/views/adherent-form.fxml"
                            )
                    );

            Parent root = loader.load();

            AdherentController controller =
                    loader.getController();

            controller.setAdherent(adherent);

            Stage stage = new Stage();

            stage.setTitle(
                    adherent == null
                            ? "Ajouter un adhérent"
                            : "Modifier un adhérent"
            );

            stage.initModality(
                    Modality.APPLICATION_MODAL
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.showAndWait();

            chargerDonnees();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible d'ouvrir le formulaire :\n\n"
                            + e.getMessage()
            );
        }
    }

    public void setAdherent(
            Adherent adherent
    ) {

        this.adherentForm = adherent;

        if (listPane == null ||
                formPane == null) {

            return;
        }

        listPane.setVisible(false);
        listPane.setManaged(false);

        formPane.setVisible(true);
        formPane.setManaged(true);

        initialiserFormulaire();

        if (adherentForm == null) {

            numeroField.setText("Auto-généré");

            nomField.clear();
            prenomField.clear();
            sexeCombo.setValue(null);
            dateNaissancePicker.setValue(null);
            telephoneField.clear();
            emailField.clear();
            adresseField.clear();

            dateAdhesionPicker.setValue(
                    LocalDate.now()
            );

            statutCombo.setValue(
                    StatutAdherent.ACTIF
            );

            agenceCombo.setValue(null);

        } else {

            numeroField.setText(
                    adherentForm.getNumeroAdherent()
            );

            nomField.setText(
                    adherentForm.getNom()
            );

            prenomField.setText(
                    adherentForm.getPrenom()
            );

            sexeCombo.setValue(
                    adherentForm.getSexe()
            );

            dateNaissancePicker.setValue(
                    adherentForm.getDateNaissance()
            );

            telephoneField.setText(
                    adherentForm.getTelephone()
            );

            emailField.setText(
                    adherentForm.getEmail()
            );

            adresseField.setText(
                    adherentForm.getAdresse()
            );

            dateAdhesionPicker.setValue(
                    adherentForm.getDateAdhesion()
            );

            statutCombo.setValue(
                    adherentForm.getStatut()
            );

            if (adherentForm.getAgence() != null) {

                agenceCombo.getItems()
                        .stream()
                        .filter(
                                agence ->
                                        agence.getId()
                                                == adherentForm
                                                .getAgence()
                                                .getId()
                        )
                        .findFirst()
                        .ifPresent(
                                agenceCombo::setValue
                        );
            }
        }

        errorLabel.setText("");

        javafx.application.Platform.runLater(
                () -> nomField.requestFocus()
        );
    }

    @FXML
    private void handleSave() {

        try {

            if (nomField.getText() == null ||
                    nomField.getText().isBlank()) {

                errorLabel.setText(
                        "Le nom est obligatoire."
                );

                return;
            }

            if (prenomField.getText() == null ||
                    prenomField.getText().isBlank()) {

                errorLabel.setText(
                        "Le prénom est obligatoire."
                );

                return;
            }

            if (sexeCombo.getValue() == null) {

                errorLabel.setText(
                        "Le sexe est obligatoire."
                );

                return;
            }

            if (telephoneField.getText() == null ||
                    telephoneField.getText().isBlank()) {

                errorLabel.setText(
                        "Le téléphone est obligatoire."
                );

                return;
            }

            if (agenceCombo.getValue() == null) {

                errorLabel.setText(
                        "L'agence est obligatoire."
                );

                return;
            }

            Adherent objet;

            if (adherentForm == null) {

                objet = new Adherent();

            } else {

                objet = adherentForm;
            }

            objet.setNom(
                    nomField.getText().trim()
            );

            objet.setPrenom(
                    prenomField.getText().trim()
            );

            objet.setSexe(
                    sexeCombo.getValue()
            );

            objet.setDateNaissance(
                    dateNaissancePicker.getValue()
            );

            objet.setTelephone(
                    telephoneField.getText().trim()
            );

            objet.setEmail(
                    emailField.getText().trim()
            );

            objet.setAdresse(
                    adresseField.getText().trim()
            );

            objet.setDateAdhesion(
                    dateAdhesionPicker.getValue()
            );

            objet.setStatut(
                    statutCombo.getValue()
            );

            objet.setAgence(
                    agenceCombo.getValue()
            );

            if (adherentForm == null) {

                adherentService.create(objet);

                AlertUtil.information(
                        "Succès",
                        "Adhérent créé avec succès."
                );

            } else {

                adherentService.update(objet);

                AlertUtil.information(
                        "Succès",
                        "Adhérent modifié avec succès."
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

        if (nomField != null &&
                nomField.getScene() != null) {

            Stage stage =
                    (Stage) nomField
                            .getScene()
                            .getWindow();

            stage.close();
        }
    }

    private void initialiserFormulaire() {

        if (nomField == null) {
            return;
        }

        agenceCombo.setItems(
                FXCollections.observableArrayList(
                        agenceService.findAll()
                )
        );

        agenceCombo.setConverter(
                new StringConverter<>() {

                    @Override
                    public String toString(Agence agence) {

                        return agence != null
                                ? agence.getNom()
                                : "";
                    }

                    @Override
                    public Agence fromString(String string) {

                        return null;
                    }
                }
        );

        statutCombo.setItems(
                FXCollections.observableArrayList(
                        StatutAdherent.values()
                )
        );

        sexeCombo.setItems(
                FXCollections.observableArrayList(
                        "M",
                        "F"
                )
        );
    }

    private void mettreAJourBoutons() {

        if (adherentTable == null) {
            return;
        }

        boolean selected =
                adherentTable
                        .getSelectionModel()
                        .getSelectedItem()
                        != null;

        if (editButton != null) {

            editButton.setDisable(
                    !selected
            );
        }

        if (deleteButton != null) {

            deleteButton.setDisable(
                    !selected
            );
        }

        if (toggleStatutButton != null) {

            toggleStatutButton.setDisable(
                    !selected
            );
        }
    }
}