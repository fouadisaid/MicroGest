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
import said.microgest.entities.Pret;
import said.microgest.enums.StatutPret;
import said.microgest.services.AdherentService;
import said.microgest.services.PretService;
import said.microgest.utils.AlertUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PretController {

    @FXML private StackPane rootPane;
    @FXML private VBox listPane;
    @FXML private VBox formPane;

    @FXML private TableView<Pret> pretTable;

    @FXML private TableColumn<Pret, Number> idColumn;
    @FXML private TableColumn<Pret, String> montantColumn;
    @FXML private TableColumn<Pret, String> tauxColumn;
    @FXML private TableColumn<Pret, Number> dureeColumn;
    @FXML private TableColumn<Pret, String> dateColumn;
    @FXML private TableColumn<Pret, String> statutColumn;
    @FXML private TableColumn<Pret, String> adherentColumn;
    @FXML private TableColumn<Pret, String> montantRestantColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<StatutPret> statutFilter;

    @FXML private ComboBox<Integer> sizeCombo;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Label totalPagesLabel;
    @FXML private Label totalRecordsLabel;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button validerButton;
    @FXML private Button rejeterButton;
    @FXML private Button rembourserButton;

    @FXML private ComboBox<Adherent> adherentCombo;
    @FXML private TextField montantField;
    @FXML private TextField tauxField;
    @FXML private TextField dureeField;
    @FXML private DatePicker datePretPicker;
    @FXML private Button calculerMensualiteButton;
    @FXML private Label mensualiteLabel;
    @FXML private Label errorLabel;

    private final PretService pretService = new PretService();
    private final AdherentService adherentService = new AdherentService();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private int currentPage = 1;
    private int pageSize = 10;
    private long totalRecords = 0;
    private int totalPages = 1;

    private Pret pretForm;

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

        pretTable.getSelectionModel()
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

        montantColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.format("%,.0f FCFA", data.getValue().getMontant())
                )
        );

        tauxColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getTaux() + " %"
                )
        );

        dureeColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getDuree())
        );

        dateColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getDatePret() != null
                                ? data.getValue().getDatePret().format(dateFormatter)
                                : ""
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

                            case "REJETE" -> setStyle(
                                    "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
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

        adherentColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getAdherent() != null
                                ? data.getValue().getAdherent().getFullName()
                                : ""
                )
        );

        montantRestantColumn.setCellValueFactory(
                data -> {

                    Pret pret = data.getValue();

                    if (pret.getStatut() == StatutPret.EN_ATTENTE ||
                            pret.getStatut() == StatutPret.REJETE) {

                        return new SimpleStringProperty("—");
                    }

                    try {

                        BigDecimal restant = pretService.getMontantRestant(pret.getId());

                        return new SimpleStringProperty(
                                String.format("%,.0f FCFA", restant)
                        );

                    } catch (Exception e) {

                        return new SimpleStringProperty("—");
                    }
                }
        );
    }

    private void chargerFiltres() {

        statutFilter.setItems(
                FXCollections.observableArrayList(StatutPret.values())
        );

        statutFilter.valueProperty()
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

                List<Pret> result = pretService.search(keyword.trim());

                pretTable.setItems(FXCollections.observableArrayList(result));

                totalRecords = result.size();
                totalPages = 1;
                currentPage = 1;

                mettreAJourPagination();

                return;
            }

            StatutPret statut = statutFilter.getValue();

            if (statut != null) {

                List<Pret> result = pretService.findByStatut(statut);

                pretTable.setItems(FXCollections.observableArrayList(result));

                totalRecords = result.size();
                totalPages = 1;
                currentPage = 1;

                mettreAJourPagination();

                return;
            }

            List<Pret> prets = pretService.findPaginated(currentPage, pageSize);

            pretTable.setItems(FXCollections.observableArrayList(prets));

            totalRecords = pretService.count();

            totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            if (totalPages < 1) {
                totalPages = 1;
            }

            if (currentPage > totalPages) {

                currentPage = totalPages;

                prets = pretService.findPaginated(currentPage, pageSize);

                pretTable.setItems(FXCollections.observableArrayList(prets));
            }

            mettreAJourPagination();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger les prêts : " + e.getMessage()
            );
        }
    }

    @FXML
    private void resetFilters() {

        searchField.clear();
        statutFilter.setValue(null);

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

        Pret pret = pretTable.getSelectionModel().getSelectedItem();

        if (pret == null) {

            AlertUtil.warning("Attention", "Veuillez sélectionner un prêt.");
            return;
        }

        if (pret.getStatut() != StatutPret.EN_ATTENTE) {

            AlertUtil.warning(
                    "Attention",
                    "Seul un prêt en attente peut être modifié."
            );

            return;
        }

        ouvrirFormulaire(pret);
    }

    @FXML
    private void handleDelete() {

        Pret pret = pretTable.getSelectionModel().getSelectedItem();

        if (pret == null) {

            AlertUtil.warning("Attention", "Veuillez sélectionner un prêt.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le prêt");

        confirmation.setContentText(
                "Voulez-vous vraiment supprimer ce prêt de "
                        + String.format("%,.0f FCFA", pret.getMontant())
                        + " ?"
        );

        confirmation.showAndWait()
                .ifPresent(response -> {

                    if (response == ButtonType.OK) {

                        try {

                            pretService.delete(pret.getId());

                            chargerDonnees();

                            AlertUtil.information(
                                    "Succès",
                                    "Prêt supprimé avec succès."
                            );

                        } catch (Exception e) {

                            AlertUtil.error("Erreur", e.getMessage());
                        }
                    }
                });
    }

    @FXML
    private void handleValider() {

        Pret pret = pretTable.getSelectionModel().getSelectedItem();

        if (pret == null) {

            AlertUtil.warning("Attention", "Veuillez sélectionner un prêt.");
            return;
        }

        try {

            pretService.valider(pret.getId());

            chargerDonnees();

            AlertUtil.information("Succès", "Prêt validé avec succès.");

        } catch (Exception e) {

            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleRejeter() {

        Pret pret = pretTable.getSelectionModel().getSelectedItem();

        if (pret == null) {

            AlertUtil.warning("Attention", "Veuillez sélectionner un prêt.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);

        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Rejeter le prêt");
        confirmation.setContentText("Voulez-vous vraiment rejeter ce prêt ?");

        confirmation.showAndWait()
                .ifPresent(response -> {

                    if (response == ButtonType.OK) {

                        try {

                            pretService.rejeter(pret.getId());

                            chargerDonnees();

                            AlertUtil.information(
                                    "Succès",
                                    "Prêt rejeté avec succès."
                            );

                        } catch (Exception e) {

                            AlertUtil.error("Erreur", e.getMessage());
                        }
                    }
                });
    }

    @FXML
    private void handleRembourser() {

        Pret pret = pretTable.getSelectionModel().getSelectedItem();

        if (pret == null) {

            AlertUtil.warning("Attention", "Veuillez sélectionner un prêt.");
            return;
        }

        if (pret.getStatut() != StatutPret.VALIDE) {

            AlertUtil.warning(
                    "Attention",
                    "Seul un prêt validé peut être remboursé."
            );

            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/remboursement-form.fxml")
            );

            Parent root = loader.load();

            RemboursementController controller = loader.getController();

            controller.setPretCible(pret);

            Stage stage = new Stage();

            stage.setTitle("Enregistrer un remboursement");

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(new Scene(root));

            stage.showAndWait();

            chargerDonnees();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible d'ouvrir le formulaire de remboursement :\n\n"
                            + e.getMessage()
            );
        }
    }

    private void ouvrirFormulaire(Pret pret) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/pret-form.fxml")
            );

            Parent root = loader.load();

            PretController controller = loader.getController();

            controller.setPret(pret);

            Stage stage = new Stage();

            stage.setTitle(
                    pret == null
                            ? "Ajouter un prêt"
                            : "Modifier un prêt"
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

    public void setPret(Pret pret) {

        this.pretForm = pret;

        if (listPane == null || formPane == null) {
            return;
        }

        listPane.setVisible(false);
        listPane.setManaged(false);

        formPane.setVisible(true);
        formPane.setManaged(true);

        if (pretForm == null) {

            adherentCombo.setValue(null);
            montantField.clear();
            tauxField.clear();
            dureeField.clear();

            datePretPicker.setValue(LocalDate.now());

            mensualiteLabel.setText("Mensualité : —");

        } else {

            if (pretForm.getAdherent() != null) {

                adherentCombo.getItems()
                        .stream()
                        .filter(a -> a.getId() == pretForm.getAdherent().getId())
                        .findFirst()
                        .ifPresent(adherentCombo::setValue);
            }

            montantField.setText(pretForm.getMontant().toPlainString());
            tauxField.setText(pretForm.getTaux().toPlainString());
            dureeField.setText(String.valueOf(pretForm.getDuree()));

            datePretPicker.setValue(pretForm.getDatePret());

            afficherMensualite();
        }

        errorLabel.setText("");

        javafx.application.Platform.runLater(
                () -> adherentCombo.requestFocus()
        );
    }

    @FXML
    private void handleCalculerMensualite() {
        afficherMensualite();
    }

    private void afficherMensualite() {

        try {

            BigDecimal montant = new BigDecimal(
                    montantField.getText().trim().replace(",", ".")
            );

            BigDecimal taux = new BigDecimal(
                    tauxField.getText().trim().replace(",", ".")
            );

            int duree = Integer.parseInt(dureeField.getText().trim());

            Pret temp = Pret.builder()
                    .montant(montant)
                    .taux(taux)
                    .duree(duree)
                    .build();

            BigDecimal mensualite = pretService.calculerMensualite(temp);

            mensualiteLabel.setText(
                    "Mensualité : " + String.format("%,.0f FCFA", mensualite)
            );

        } catch (Exception e) {

            mensualiteLabel.setText("Mensualité : —");
        }
    }

    @FXML
    private void handleSave() {

        try {

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

            String tauxText = tauxField.getText() == null
                    ? ""
                    : tauxField.getText().trim().replace(",", ".");

            if (tauxText.isBlank()) {

                errorLabel.setText("Le taux est obligatoire.");
                return;
            }

            BigDecimal taux;

            try {

                taux = new BigDecimal(tauxText);

            } catch (NumberFormatException e) {

                errorLabel.setText("Le taux doit être un nombre valide.");
                return;
            }

            if (taux.compareTo(BigDecimal.ZERO) < 0 ||
                    taux.compareTo(BigDecimal.valueOf(100)) > 0) {

                errorLabel.setText("Le taux doit être compris entre 0 et 100.");
                return;
            }

            String dureeText = dureeField.getText() == null
                    ? ""
                    : dureeField.getText().trim();

            if (dureeText.isBlank()) {

                errorLabel.setText("La durée est obligatoire.");
                return;
            }

            int duree;

            try {

                duree = Integer.parseInt(dureeText);

            } catch (NumberFormatException e) {

                errorLabel.setText("La durée doit être un nombre entier valide.");
                return;
            }

            if (duree <= 0 || duree > 360) {

                errorLabel.setText("La durée doit être comprise entre 1 et 360 mois.");
                return;
            }

            if (datePretPicker.getValue() != null &&
                    datePretPicker.getValue().isAfter(LocalDate.now())) {

                errorLabel.setText("La date du prêt ne peut pas être dans le futur.");
                return;
            }

            Pret objet = (pretForm == null) ? new Pret() : pretForm;

            objet.setAdherent(adherentCombo.getValue());
            objet.setMontant(montant);
            objet.setTaux(taux);
            objet.setDuree(duree);
            objet.setDatePret(
                    datePretPicker.getValue() != null
                            ? datePretPicker.getValue()
                            : LocalDate.now()
            );

            if (pretForm == null) {

                pretService.create(objet);

                AlertUtil.information("Succès", "Prêt créé avec succès.");

            } else {

                pretService.update(objet);

                AlertUtil.information("Succès", "Prêt modifié avec succès.");
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

        if (adherentCombo != null && adherentCombo.getScene() != null) {

            Stage stage = (Stage) adherentCombo.getScene().getWindow();
            stage.close();
        }
    }

    private void initialiserFormulaire() {

        if (adherentCombo == null) {
            return;
        }

        adherentCombo.setItems(
                FXCollections.observableArrayList(adherentService.findAll())
        );

        adherentCombo.setConverter(
                new StringConverter<>() {

                    @Override
                    public String toString(Adherent adherent) {
                        return adherent != null ? adherent.getFullName() : "";
                    }

                    @Override
                    public Adherent fromString(String string) {
                        return null;
                    }
                }
        );
    }

    private void mettreAJourBoutons() {

        if (pretTable == null) {
            return;
        }

        Pret selected = pretTable.getSelectionModel().getSelectedItem();

        boolean hasSelection = selected != null;

        boolean enAttente = hasSelection &&
                selected.getStatut() == StatutPret.EN_ATTENTE;

        boolean valide = hasSelection &&
                selected.getStatut() == StatutPret.VALIDE;

        boolean supprimable = hasSelection &&
                selected.getStatut() != StatutPret.VALIDE &&
                selected.getStatut() != StatutPret.REMBOURSE;

        if (editButton != null) {
            editButton.setDisable(!enAttente);
        }

        if (deleteButton != null) {
            deleteButton.setDisable(!supprimable);
        }

        if (validerButton != null) {
            validerButton.setDisable(!enAttente);
        }

        if (rejeterButton != null) {
            rejeterButton.setDisable(!enAttente);
        }

        if (rembourserButton != null) {
            rembourserButton.setDisable(!valide);
        }
    }

    @FXML
    private void handleVerifierEcheances() {

        try {

            List<Pret> notifies = pretService.verifierEcheancesProches(7);

            AlertUtil.information(
                    "Rappels envoyés",
                    notifies.isEmpty()
                            ? "Aucune échéance proche à signaler pour le moment."
                            : notifies.size() + " rappel(s) d'échéance envoyé(s) par email."
            );

        } catch (Exception e) {

            AlertUtil.error(
                    "Erreur",
                    "Impossible de vérifier les échéances : " + e.getMessage()
            );
        }
    }
}