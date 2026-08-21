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

import said.microgest.entities.Pret;
import said.microgest.entities.Remboursement;
import said.microgest.services.RemboursementService;
import said.microgest.utils.AlertUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RemboursementController {

    @FXML private StackPane rootPane;
    @FXML private VBox listPane;
    @FXML private VBox formPane;

    @FXML private TableView<Remboursement> remboursementTable;

    @FXML private TableColumn<Remboursement, Number> idColumn;
    @FXML private TableColumn<Remboursement, String> adherentColumn;
    @FXML private TableColumn<Remboursement, String> montantPretColumn;
    @FXML private TableColumn<Remboursement, String> montantColumn;
    @FXML private TableColumn<Remboursement, Number> echeanceColumn;
    @FXML private TableColumn<Remboursement, String> dateColumn;
    @FXML private TableColumn<Remboursement, String> montantRestantColumn;
    @FXML private TableColumn<Remboursement, String> statutColumn;
    @FXML private TableColumn<Remboursement, String> modifiableColumn;

    @FXML private TextField searchField;

    @FXML private ComboBox<Integer> sizeCombo;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Label totalPagesLabel;
    @FXML private Label totalRecordsLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;

    @FXML private Label formTitleLabel;
    @FXML private ComboBox<Pret> pretCombo;
    @FXML private Label montantRestantLabel;
    @FXML private TextField montantField;
    @FXML private DatePicker datePaiementPicker;
    @FXML private Label errorLabel;

    private final RemboursementService remboursementService = new RemboursementService();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int currentPage = 1;
    private int pageSize = 10;
    private long totalRecords = 0;
    private int totalPages = 1;

    // null tant qu'on est en mode "ajout" ; renseigné en mode "modification"
    private Remboursement remboursementForm;

    @FXML
    public void initialize() {

        if (remboursementTable != null) {

            configurerColonnes();

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

            remboursementTable.getSelectionModel()
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
        }

        if (pretCombo != null) {

            pretCombo.setConverter(
                    new StringConverter<>() {

                        @Override
                        public String toString(Pret pret) {

                            if (pret == null) {
                                return "";
                            }

                            BigDecimal restant =
                                    remboursementService.getMontantRestant(pret.getId());

                            return pret.getAdherent().getFullName()
                                    + " — Restant : "
                                    + String.format("%,.0f FCFA", restant);
                        }

                        @Override
                        public Pret fromString(String string) {
                            return null;
                        }
                    }
            );

            pretCombo.valueProperty()
                    .addListener(
                            (obs, oldValue, newValue) ->
                                    mettreAJourMontantRestant(newValue)
                    );
        }

        if (listPane != null && formPane != null) {

            listPane.setVisible(true);
            listPane.setManaged(true);

            formPane.setVisible(false);
            formPane.setManaged(false);
        }
    }

    private void configurerColonnes() {

        idColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getId())
        );

        adherentColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getPret() != null &&
                                data.getValue().getPret().getAdherent() != null
                                ? data.getValue().getPret().getAdherent().getFullName()
                                : ""
                )
        );

        montantPretColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getPret() != null
                                ? String.format(
                                "%,.0f FCFA",
                                data.getValue().getPret().getMontant()
                        )
                                : ""
                )
        );

        montantColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.format("%,.0f FCFA", data.getValue().getMontant())
                )
        );

        echeanceColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(
                        data.getValue().getNumeroEcheance() != null
                                ? data.getValue().getNumeroEcheance()
                                : 0
                )
        );

        dateColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getDatePaiement() != null
                                ? data.getValue().getDatePaiement().format(dateFormatter)
                                : ""
                )
        );

        montantRestantColumn.setCellValueFactory(
                data -> {

                    try {

                        BigDecimal restant =
                                remboursementService.getMontantRestantApres(data.getValue());

                        return new SimpleStringProperty(
                                String.format("%,.0f FCFA", restant)
                        );

                    } catch (Exception e) {

                        return new SimpleStringProperty("—");
                    }
                }
        );

        statutColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getPret() != null &&
                                data.getValue().getPret().getStatut() != null
                                ? data.getValue().getPret().getStatut().name()
                                : ""
                )
        );

        statutColumn.setCellFactory(column ->
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

                        switch (statut) {

                            case "VALIDE" -> setStyle(
                                    "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                            );

                            case "REMBOURSE" -> setStyle(
                                    "-fx-text-fill: #2980b9; -fx-font-weight: bold;"
                            );

                            default -> setStyle(
                                    "-fx-text-fill: #f39c12; -fx-font-weight: bold;"
                            );
                        }
                    }
                }
        );

        modifiableColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        remboursementService.estModifiable(data.getValue())
                                ? "Oui"
                                : ""
                )
        );

        modifiableColumn.setCellFactory(column ->
                new TableCell<>() {

                    @Override
                    protected void updateItem(String valeur, boolean empty) {

                        super.updateItem(valeur, empty);

                        if (empty || valeur == null || valeur.isBlank()) {

                            setText(null);
                            setStyle("");

                            return;
                        }

                        setText("✓ " + valeur);

                        setStyle(
                                "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                        );
                    }
                }
        );
    }

    private void chargerDonnees() {

        try {

            String keyword = searchField.getText();

            if (keyword != null && !keyword.isBlank()) {

                List<Remboursement> result =
                        remboursementService.search(keyword.trim());

                remboursementTable.setItems(
                        FXCollections.observableArrayList(result)
                );

                totalRecords = result.size();
                totalPages = 1;
                currentPage = 1;

                mettreAJourPagination();

                return;
            }

            List<Remboursement> remboursements =
                    remboursementService.findPaginated(currentPage, pageSize);

            remboursementTable.setItems(
                    FXCollections.observableArrayList(remboursements)
            );

            totalRecords = remboursementService.count();

            totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            if (totalPages < 1) {
                totalPages = 1;
            }

            if (currentPage > totalPages) {

                currentPage = totalPages;

                remboursements =
                        remboursementService.findPaginated(currentPage, pageSize);

                remboursementTable.setItems(
                        FXCollections.observableArrayList(remboursements)
                );
            }

            mettreAJourPagination();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger les remboursements : " + e.getMessage()
            );
        }
    }

    @FXML
    private void resetFilters() {

        searchField.clear();

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


    // AJOUT
    @FXML
    private void handleAdd() {
        ouvrirFormulaireAjout(null);
    }

    private void ouvrirFormulaireAjout(Pret pretPreselectionne) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/remboursement-form.fxml")
            );

            Parent root = loader.load();

            RemboursementController controller = loader.getController();

            controller.setPretCible(pretPreselectionne);

            Stage stage = new Stage();

            stage.setTitle("Enregistrer un remboursement");

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(new Scene(root));

            stage.showAndWait();

            // Rechargement IMMEDIAT après fermeture du popup, ajout confirmé ou non
            chargerDonnees();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible d'ouvrir le formulaire :\n\n" + e.getMessage()
            );
        }
    }


    public void setPretCible(Pret pretPreselectionne) {

        this.remboursementForm = null;

        if (listPane == null || formPane == null) {
            return;
        }

        listPane.setVisible(false);
        listPane.setManaged(false);

        formPane.setVisible(true);
        formPane.setManaged(true);

        formTitleLabel.setText("Enregistrer un remboursement");

        List<Pret> items = pretPreselectionne != null
                ? List.of(pretPreselectionne)
                : remboursementService.findPretsRemboursables();

        pretCombo.setItems(FXCollections.observableArrayList(items));

        pretCombo.setValue(pretPreselectionne);

        pretCombo.setDisable(pretPreselectionne != null);

        montantField.clear();

        datePaiementPicker.setValue(LocalDate.now());

        errorLabel.setText("");

        mettreAJourMontantRestant(pretCombo.getValue());

        javafx.application.Platform.runLater(
                () -> montantField.requestFocus()
        );
    }

    // MODIFICATION (uniquement le dernier remboursement d'un prêt)
    @FXML
    private void handleEdit() {

        Remboursement selected =
                remboursementTable.getSelectionModel().getSelectedItem();

        if (selected == null) {

            AlertUtil.warning(
                    "Attention",
                    "Veuillez sélectionner un remboursement."
            );

            return;
        }

        if (!remboursementService.estModifiable(selected)) {

            AlertUtil.warning(
                    "Attention",
                    "Seul le dernier remboursement enregistré sur ce prêt peut être modifié."
            );

            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/remboursement-form.fxml")
            );

            Parent root = loader.load();

            RemboursementController controller = loader.getController();

            controller.setRemboursementAModifier(selected);

            Stage stage = new Stage();

            stage.setTitle("Modifier un remboursement");

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(new Scene(root));

            stage.showAndWait();

            // Rechargement IMMEDIAT après fermeture du popup
            chargerDonnees();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible d'ouvrir le formulaire :\n\n" + e.getMessage()
            );
        }
    }

    public void setRemboursementAModifier(Remboursement remboursement) {

        this.remboursementForm = remboursement;

        if (listPane == null || formPane == null) {
            return;
        }

        listPane.setVisible(false);
        listPane.setManaged(false);

        formPane.setVisible(true);
        formPane.setManaged(true);

        formTitleLabel.setText("Modifier un remboursement");

        pretCombo.setItems(
                FXCollections.observableArrayList(remboursement.getPret())
        );

        pretCombo.setValue(remboursement.getPret());

        // Le prêt n'est jamais réassignable lors d'une modification
        pretCombo.setDisable(true);

        montantField.setText(remboursement.getMontant().toPlainString());

        datePaiementPicker.setValue(
                remboursement.getDatePaiement() != null
                        ? remboursement.getDatePaiement()
                        : LocalDate.now()
        );

        errorLabel.setText("");

        mettreAJourMontantRestant(remboursement.getPret());

        javafx.application.Platform.runLater(
                () -> montantField.requestFocus()
        );
    }

    private void mettreAJourMontantRestant(Pret pret) {

        if (pret == null) {

            montantRestantLabel.setText("Montant restant : —");
            return;
        }

        try {

            BigDecimal restant =
                    remboursementService.getMontantRestant(pret.getId());

            // En mode modification, on réintègre virtuellement l'ancien montant
            // pour afficher le "vrai" plafond disponible pour la correction.
            if (remboursementForm != null &&
                    remboursementForm.getPret().getId() == pret.getId()) {

                restant = restant.add(remboursementForm.getMontant());
            }

            montantRestantLabel.setText(
                    "Montant restant : " + String.format("%,.0f FCFA", restant)
            );

        } catch (Exception e) {

            montantRestantLabel.setText("Montant restant : indisponible");
        }
    }


    // ENREGISTREMENT (création ou modification)
    @FXML
    private void handleSave() {

        try {

            Pret pret = pretCombo.getValue();

            if (pret == null) {

                errorLabel.setText("Le prêt est obligatoire.");
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

            LocalDate date = datePaiementPicker.getValue();

            if (date == null) {
                date = LocalDate.now();
            }

            if (date.isAfter(LocalDate.now())) {

                errorLabel.setText(
                        "La date de paiement ne peut pas être dans le futur."
                );

                return;
            }

            if (remboursementForm == null) {

                // ===== CAS AJOUT =====

                BigDecimal restant =
                        remboursementService.getMontantRestant(pret.getId());

                if (montant.compareTo(restant) > 0) {

                    errorLabel.setText(
                            "Le montant (" + String.format("%,.0f FCFA", montant)
                                    + ") dépasse le montant restant ("
                                    + String.format("%,.0f FCFA", restant) + ")."
                    );

                    return;
                }

                remboursementService.rembourser(pret.getId(), montant, date);

                AlertUtil.information(
                        "Succès",
                        "Remboursement enregistré avec succès."
                );

            } else {

                // ===== CAS MISE A JOUR =====

                remboursementService.update(
                        remboursementForm.getId(),
                        montant,
                        date
                );

                AlertUtil.information(
                        "Succès",
                        "Remboursement modifié avec succès."
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

        if (montantField != null && montantField.getScene() != null) {

            Stage stage = (Stage) montantField.getScene().getWindow();
            stage.close();
        }
    }

    private void mettreAJourBoutons() {

        if (remboursementTable == null) {
            return;
        }

        Remboursement selected =
                remboursementTable.getSelectionModel().getSelectedItem();

        boolean modifiable = selected != null &&
                remboursementService.estModifiable(selected);

        if (editButton != null) {
            editButton.setDisable(!modifiable);
        }
    }
}