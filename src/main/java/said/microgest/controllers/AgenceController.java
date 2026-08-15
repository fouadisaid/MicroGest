package said.microgest.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import said.microgest.entities.Agence;
import said.microgest.services.AgenceService;
import said.microgest.utils.AlertUtil;

public class AgenceController {

    @FXML
    private StackPane rootPane;

    @FXML
    private VBox listPane;

    @FXML
    private VBox formPane;

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
    private TextField searchField;

    @FXML
    private ComboBox<Integer> sizeCombo;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Label pageLabel;

    @FXML
    private Label totalPagesLabel;

    @FXML
    private Label totalRecordsLabel;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private TextField nomField;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField telephoneField;

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    private final AgenceService agenceService =
            new AgenceService();

    private int currentPage = 1;

    private int pageSize = 10;

    private long totalRecords = 0;

    private int totalPages = 1;

    private Agence agenceForm;

    @FXML
    public void initialize() {

        configurerColonnes();

        if (sizeCombo != null) {

            sizeCombo.setItems(
                    FXCollections.observableArrayList(
                            5,
                            10,
                            20,
                            50
                    )
            );

            sizeCombo.setValue(pageSize);

            sizeCombo.setOnAction(event -> {

                Integer valeur =
                        sizeCombo.getValue();

                if (valeur != null) {

                    pageSize = valeur;
                    currentPage = 1;

                    chargerDonnees();
                }
            });
        }

        if (agenceTable != null) {

            agenceTable.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(
                            (obs, oldSelection, newSelection) ->
                                    mettreAJourBoutons()
                    );
        }

        if (searchField != null) {

            searchField.textProperty()
                    .addListener(
                            (obs, oldValue, newValue) -> {

                                currentPage = 1;

                                chargerDonnees();
                            }
                    );
        }

        if (agenceTable != null) {

            chargerDonnees();

            mettreAJourBoutons();
        }


        if (listPane != null) {

            listPane.setVisible(true);
            listPane.setManaged(true);
        }

        if (formPane != null) {

            formPane.setVisible(false);
            formPane.setManaged(false);
        }
    }

    private void configurerColonnes() {

        if (idColumn != null) {

            idColumn.setCellValueFactory(
                    new PropertyValueFactory<>("id")
            );
        }

        if (nomColumn != null) {

            nomColumn.setCellValueFactory(
                    new PropertyValueFactory<>("nom")
            );
        }

        if (adresseColumn != null) {

            adresseColumn.setCellValueFactory(
                    new PropertyValueFactory<>("adresse")
            );
        }

        if (telephoneColumn != null) {

            telephoneColumn.setCellValueFactory(
                    new PropertyValueFactory<>("telephone")
            );
        }

        if (emailColumn != null) {

            emailColumn.setCellValueFactory(
                    new PropertyValueFactory<>("email")
            );
        }
    }

    private void chargerDonnees() {


        if (agenceTable == null) {
            return;
        }

        try {

            String keyword =
                    searchField != null
                            ? searchField.getText().trim()
                            : "";

            if (!keyword.isBlank()) {

                var result =
                        agenceService.search(keyword);

                totalRecords =
                        agenceService.countSearch(keyword);

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
                }

                int fromIndex =
                        (currentPage - 1) * pageSize;

                int toIndex =
                        Math.min(
                                fromIndex + pageSize,
                                result.size()
                        );

                if (fromIndex >= result.size()) {

                    agenceTable.setItems(
                            FXCollections.observableArrayList()
                    );

                } else {

                    agenceTable.setItems(
                            FXCollections.observableArrayList(
                                    result.subList(
                                            fromIndex,
                                            toIndex
                                    )
                            )
                    );
                }

                mettreAJourPagination();

                return;
            }

            var agences =
                    agenceService.findPaginated(
                            currentPage,
                            pageSize
                    );

            agenceTable.setItems(
                    FXCollections.observableArrayList(
                            agences
                    )
            );

            totalRecords =
                    agenceService.count();

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

                chargerDonnees();

                return;
            }

            mettreAJourPagination();

            agenceTable.refresh();

        } catch (Exception e) {

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger les agences : "
                            + e.getMessage()
            );
        }
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

        if (pageLabel != null) {

            pageLabel.setText(
                    String.valueOf(currentPage)
            );
        }

        if (totalPagesLabel != null) {

            totalPagesLabel.setText(
                    String.valueOf(totalPages)
            );
        }

        if (totalRecordsLabel != null) {

            totalRecordsLabel.setText(
                    String.valueOf(totalRecords)
            );
        }

        if (prevButton != null) {

            prevButton.setDisable(
                    currentPage <= 1
            );
        }

        if (nextButton != null) {

            nextButton.setDisable(
                    currentPage >= totalPages
            );
        }
    }

    @FXML
    private void handleAdd() {

        ouvrirFormulaire(null);
    }

    @FXML
    private void handleEdit() {

        Agence agence =
                agenceTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (agence == null) {

            AlertUtil.warning(
                    "Attention",
                    "Veuillez sélectionner une agence."
            );

            return;
        }

        ouvrirFormulaire(agence);
    }

    @FXML
    private void handleDelete() {

        Agence selected =
                agenceTable
                        .getSelectionModel()
                        .getSelectedItem();

        if (selected == null) {

            AlertUtil.warning(
                    "Attention",
                    "Veuillez sélectionner une agence."
            );

            return;
        }

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle("Confirmation");

        alert.setHeaderText(
                "Supprimer l'agence"
        );

        alert.setContentText(
                "Voulez-vous vraiment supprimer l'agence "
                        + selected.getNom()
                        + " ?"
        );

        alert.showAndWait()
                .ifPresent(response -> {

                    if (response == ButtonType.OK) {

                        try {

                            agenceService.delete(
                                    selected.getId()
                            );

                            chargerDonnees();

                            mettreAJourBoutons();

                            AlertUtil.information(
                                    "Succès",
                                    "Agence supprimée avec succès."
                            );

                        } catch (RuntimeException e) {

                            AlertUtil.error(
                                    "Erreur",
                                    e.getMessage()
                            );
                        }
                    }
                });
    }

    private void ouvrirFormulaire(Agence agence) {

        try {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/views/agence-form.fxml"
                            )
                    );

            Parent root =
                    loader.load();

            AgenceController controller =
                    loader.getController();

            controller.setAgence(agence);

            Stage stage =
                    new Stage();

            stage.setTitle(
                    agence == null
                            ? "Ajouter une agence"
                            : "Modifier une agence"
            );

            stage.initModality(
                    Modality.APPLICATION_MODAL
            );

            stage.setScene(
                    new Scene(root)
            );

            stage.setResizable(true);

            stage.showAndWait();


            chargerDonnees();

            mettreAJourBoutons();

        } catch (Exception e) {

            AlertUtil.error(
                    "Erreur",
                    "Impossible d'ouvrir le formulaire : "
                            + e.getMessage()
            );
        }
    }

    public void setAgence(Agence agence) {

        this.agenceForm = agence;

        if (formPane == null) {
            return;
        }

        if (listPane != null) {

            listPane.setVisible(false);
            listPane.setManaged(false);
        }

        formPane.setVisible(true);
        formPane.setManaged(true);

        if (agenceForm == null) {

            nomField.clear();
            adresseField.clear();
            telephoneField.clear();
            emailField.clear();

        } else {

            nomField.setText(
                    agenceForm.getNom()
            );

            adresseField.setText(
                    agenceForm.getAdresse()
            );

            telephoneField.setText(
                    agenceForm.getTelephone()
            );

            emailField.setText(
                    agenceForm.getEmail()
            );
        }

        if (errorLabel != null) {

            errorLabel.setText("");
        }


         /* Le curseur se place automatiquement sur le champ Nom.*/

        javafx.application.Platform.runLater(
                () -> nomField.requestFocus()
        );
    }

    @FXML
    private void handleSave() {

        try {


            Agence agence;

            if (agenceForm == null) {

                agence = new Agence();

            } else {

                agence = agenceForm;
            }

            agence.setNom(
                    nomField.getText().trim()
            );

            agence.setAdresse(
                    adresseField.getText().trim()
            );

            agence.setTelephone(
                    telephoneField.getText().trim()
            );

            agence.setEmail(
                    emailField.getText().trim()
            );

            if (agence.getId() == 0) {

                agenceService.create(agence);

                AlertUtil.information(
                        "Succès",
                        "Agence créée avec succès !"
                );

            } else {

                agenceService.update(agence);

                AlertUtil.information(
                        "Succès",
                        "Agence modifiée avec succès !"
                );
            }

            /*
             * TRÈS IMPORTANT :
             *
             * On NE recharge PAS la liste ici.
             * On ferme simplement le formulaire.
             *
             * Le contrôleur principal reprendra
             * l'exécution après showAndWait()
             * et appellera chargerDonnees().
             */
            closeForm();

        } catch (RuntimeException e) {

            AlertUtil.error(
                    "Erreur",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleCancel() {

        closeForm();
    }

    private void closeForm() {

        if (nomField == null ||
                nomField.getScene() == null) {

            return;
        }

        Stage stage =
                (Stage) nomField
                        .getScene()
                        .getWindow();

        stage.close();
    }

    private void mettreAJourBoutons() {

        if (agenceTable == null) {
            return;
        }

        boolean selected =
                agenceTable
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
    }
}