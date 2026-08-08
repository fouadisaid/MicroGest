package said.microgest.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import said.microgest.entities.Adherent;
import said.microgest.entities.Agence;
import said.microgest.enums.StatutAdherent;
import said.microgest.services.AdherentService;
import said.microgest.services.AgenceService;
import said.microgest.utils.AlertUtil;

import java.time.LocalDate;
import java.util.List;

public class AdherentController {

    @FXML
    private TableView<Adherent> adherentTable;

    @FXML
    private TableColumn<Adherent, Integer> idColumn;

    @FXML
    private TableColumn<Adherent, String> numeroColumn;

    @FXML
    private TableColumn<Adherent, String> nomColumn;

    @FXML
    private TableColumn<Adherent, String> prenomColumn;

    @FXML
    private TableColumn<Adherent, String> emailColumn;

    @FXML
    private TableColumn<Adherent, String> telephoneColumn;

    @FXML
    private TableColumn<Adherent, String> statutColumn;

    @FXML
    private TableColumn<Adherent, String> agenceColumn;

    // Pagination
    @FXML
    private ComboBox<Integer> sizeCombo;

    @FXML
    private Label pageLabel;

    @FXML
    private Label totalPagesLabel;

    @FXML
    private Label totalRecordsLabel;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    // Recherche
    @FXML
    private TextField searchField;

    // Filtres
    @FXML
    private ComboBox<StatutAdherent> statutFilter;

    @FXML
    private ComboBox<Agence> agenceFilter;

    @FXML
    private DatePicker dateDebutFilter;

    @FXML
    private DatePicker dateFinFilter;

    @FXML
    private Button applyFiltersButton;

    @FXML
    private Button resetFiltersButton;

    // Formulaire
    @FXML
    private TextField numeroField;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private ComboBox<String> sexeCombo;

    @FXML
    private DatePicker dateNaissancePicker;

    @FXML
    private TextField adresseField;

    @FXML
    private TextField telephoneField;

    @FXML
    private TextField emailField;

    @FXML
    private DatePicker dateAdhesionPicker;

    @FXML
    private ComboBox<StatutAdherent> statutCombo;

    @FXML
    private ComboBox<Agence> agenceCombo;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    private final AdherentService adherentService = new AdherentService();
    private final AgenceService agenceService = new AgenceService();

    private ObservableList<Adherent> adherents = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 10;
    private long totalRecords = 0;

    @FXML
    public void initialize() {
        initTableColumns();
        initComboBoxes();
        loadAgences();
        loadStatuts();
        setupPagination();
        setupSearch();
        setupFilters();
        loadData();
    }

    private void initTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numeroAdherent"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        agenceColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAgence() != null ?
                                cellData.getValue().getAgence().getNom() : ""
                )
        );

        adherentTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        afficherAdherent(selected);
                    }
                }
        );
    }

    private void initComboBoxes() {
        sizeCombo.setItems(FXCollections.observableArrayList(5, 10, 20, 50, 100));
        sizeCombo.setValue(10);
        sizeCombo.setOnAction(e -> {
            pageSize = sizeCombo.getValue();
            currentPage = 1;
            loadData();
        });

        sexeCombo.setItems(FXCollections.observableArrayList("M", "F"));
    }

    private void loadAgences() {
        List<Agence> agences = agenceService.findAll();
        agenceFilter.setItems(FXCollections.observableArrayList(agences));
        agenceCombo.setItems(FXCollections.observableArrayList(agences));
    }

    private void loadStatuts() {
        ObservableList<StatutAdherent> statuts = FXCollections.observableArrayList(StatutAdherent.values());
        statutFilter.setItems(statuts);
        statutCombo.setItems(statuts);
    }

    private void setupPagination() {
        prevButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadData();
            }
        });

        nextButton.setOnAction(e -> {
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (currentPage < totalPages) {
                currentPage++;
                loadData();
            }
        });
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, val) -> {
            currentPage = 1;
            loadData();
        });
    }

    private void setupFilters() {
        applyFiltersButton.setOnAction(e -> {
            currentPage = 1;
            loadData();
        });

        resetFiltersButton.setOnAction(e -> {
            searchField.clear();
            statutFilter.setValue(null);
            agenceFilter.setValue(null);
            dateDebutFilter.setValue(null);
            dateFinFilter.setValue(null);
            currentPage = 1;
            loadData();
        });
    }

    private void loadData() {
        String keyword = searchField.getText();
        StatutAdherent statut = statutFilter.getValue();
        Agence agence = agenceFilter.getValue();
        LocalDate dateDebut = dateDebutFilter.getValue();
        LocalDate dateFin = dateFinFilter.getValue();

        try {
            List<Adherent> data;

            if (keyword != null && !keyword.isEmpty()) {
                data = adherentService.search(keyword);
            } else if (statut != null || agence != null || dateDebut != null || dateFin != null) {
                data = adherentService.filter(statut, agence != null ? agence.getId() : null, dateDebut, dateFin);
            } else {
                data = adherentService.findPaginated(currentPage, pageSize);
                totalRecords = adherentService.count();
            }

            if (data != null) {
                adherents.setAll(data);
                updatePaginationInfo();
            }

            adherentTable.setItems(adherents);

        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des données : " + e.getMessage());
        }
    }

    private void updatePaginationInfo() {
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
        pageLabel.setText(String.valueOf(currentPage));
        totalPagesLabel.setText(String.valueOf(totalPages));
        totalRecordsLabel.setText(String.valueOf(totalRecords));

        prevButton.setDisable(currentPage <= 1);
        nextButton.setDisable(currentPage >= totalPages);
    }

    private void afficherAdherent(Adherent adherent) {
        numeroField.setText(adherent.getNumeroAdherent());
        nomField.setText(adherent.getNom());
        prenomField.setText(adherent.getPrenom());
        sexeCombo.setValue(adherent.getSexe());
        dateNaissancePicker.setValue(adherent.getDateNaissance());
        adresseField.setText(adherent.getAdresse());
        telephoneField.setText(adherent.getTelephone());
        emailField.setText(adherent.getEmail());
        dateAdhesionPicker.setValue(adherent.getDateAdhesion());
        statutCombo.setValue(adherent.getStatut());
        agenceCombo.setValue(adherent.getAgence());
    }

    @FXML
    private void handleSave() {
        try {
            Adherent adherent = new Adherent();

            // On récupére les données du formulaire
            adherent.setNumeroAdherent(numeroField.getText());
            adherent.setNom(nomField.getText());
            adherent.setPrenom(prenomField.getText());
            adherent.setSexe(sexeCombo.getValue());
            adherent.setDateNaissance(dateNaissancePicker.getValue());
            adherent.setAdresse(adresseField.getText());
            adherent.setTelephone(telephoneField.getText());
            adherent.setEmail(emailField.getText());
            adherent.setDateAdhesion(dateAdhesionPicker.getValue());
            adherent.setStatut(statutCombo.getValue());
            adherent.setAgence(agenceCombo.getValue());

            if (adherent.getId() == 0) {
                adherentService.create(adherent);
                AlertUtil.information("Succès", "Adhérent créé avec succès !");
            } else {
                adherentService.update(adherent);
                AlertUtil.information("Succès", "Adhérent modifié avec succès !");
            }

            clearForm();
            loadData();

        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Adherent selected = adherentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner un adhérent.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'adhérent");
        alert.setContentText("Voulez-vous vraiment supprimer l'adhérent " +
                selected.getFullName() + " ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    adherentService.delete(selected.getId());
                    AlertUtil.information("Succès", "Adhérent supprimé avec succès !");
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

    @FXML
    private void handleToggleStatut() {
        Adherent selected = adherentTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner un adhérent.");
            return;
        }

        try {
            adherentService.toggleStatut(selected.getId());
            loadData();
            AlertUtil.information("Succès", "Statut modifié avec succès !");
        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    private void clearForm() {
        numeroField.clear();
        nomField.clear();
        prenomField.clear();
        sexeCombo.setValue(null);
        dateNaissancePicker.setValue(null);
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        dateAdhesionPicker.setValue(null);
        statutCombo.setValue(null);
        agenceCombo.setValue(null);
        adherentTable.getSelectionModel().clearSelection();
    }
}