package said.microgest.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import said.microgest.entities.Adherent;
import said.microgest.entities.Epargne;
import said.microgest.services.AdherentService;
import said.microgest.services.EpargneService;
import said.microgest.utils.AlertUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EpargneController {

    @FXML
    private TableView<Epargne> epargneTable;

    @FXML
    private TableColumn<Epargne, Integer> idColumn;

    @FXML
    private TableColumn<Epargne, BigDecimal> soldeColumn;

    @FXML
    private TableColumn<Epargne, LocalDate> dateOuvertureColumn;

    @FXML
    private TableColumn<Epargne, String> adherentColumn;

    @FXML
    private ComboBox<Adherent> adherentCombo;

    @FXML
    private TextField soldeField;

    @FXML
    private DatePicker dateOuverturePicker;

    @FXML
    private Label totalEpargneLabel;

    @FXML
    private Label interetsLabel;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button calculerInteretsButton;

    @FXML
    private Button ajouterInteretsButton;

    private final EpargneService epargneService = new EpargneService();
    private final AdherentService adherentService = new AdherentService();
    private ObservableList<Epargne> epargnes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        initTableColumns();
        loadAdherents();
        loadData();
        setupSelectionListener();
        chargerTotalEpargne();
        setupAdherentListener();
    }

    private void initTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        soldeColumn.setCellValueFactory(new PropertyValueFactory<>("solde"));
        soldeColumn.setCellFactory(col -> new TableCell<Epargne, BigDecimal>() {
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
        dateOuvertureColumn.setCellValueFactory(new PropertyValueFactory<>("dateOuverture"));

        adherentColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAdherent() != null ?
                                cellData.getValue().getAdherent().getFullName() : ""
                )
        );
    }

    private void loadAdherents() {
        try {
            List<Adherent> adherents = adherentService.findAll();
            adherentCombo.setItems(FXCollections.observableArrayList(adherents));

            // Ajout d'un convertisseur pour afficher le nom complet dans le ComboBox
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

    private void setupAdherentListener() {
        adherentCombo.valueProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                try {
                    // Vérifier si l'adhérent a déjà une épargne
                    epargneService.findByAdherent(selected.getId());
                    AlertUtil.warning("Attention", "Cet adhérent a déjà une épargne.");
                    adherentCombo.setValue(null);
                } catch (RuntimeException e) {
                    // L'adhérent n'a pas d'épargne, c'est bon
                }
            }
        });
    }

    private void setupSelectionListener() {
        epargneTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> {
                    if (selected != null) {
                        afficherEpargne(selected);
                    }
                }
        );
    }

    private void loadData() {
        try {
            epargnes.setAll(epargneService.findAll());
            epargneTable.setItems(epargnes);
        } catch (Exception e) {
            AlertUtil.error("Erreur", "Erreur lors du chargement des épargnes : " + e.getMessage());
        }
    }

    private void chargerTotalEpargne() {
        try {
            BigDecimal total = epargneService.getTotalEpargne();
            totalEpargneLabel.setText("Total épargne : " + String.format("%,.0f FCFA", total));
        } catch (Exception e) {
            totalEpargneLabel.setText("Total épargne : -");
        }
    }

    private void afficherEpargne(Epargne epargne) {
        adherentCombo.setValue(epargne.getAdherent());
        soldeField.setText(epargne.getSolde().toString());
        dateOuverturePicker.setValue(epargne.getDateOuverture());

        // Calculer les intérêts
        try {
            BigDecimal interets = epargneService.calculerInterets(epargne);
            interetsLabel.setText("Intérêts estimés : " + String.format("%,.2f FCFA", interets));
        } catch (Exception e) {
            interetsLabel.setText("Intérêts estimés : -");
        }
    }

    @FXML
    private void handleSave() {
        try {
            Epargne epargne = new Epargne();

            if (epargneTable.getSelectionModel().getSelectedItem() != null) {
                epargne.setId(epargneTable.getSelectionModel().getSelectedItem().getId());
            }

            epargne.setAdherent(adherentCombo.getValue());
            epargne.setSolde(new BigDecimal(soldeField.getText().trim()));
            epargne.setDateOuverture(dateOuverturePicker.getValue());

            if (epargne.getId() == 0) {
                epargneService.create(epargne);
                AlertUtil.information("Succès", "Épargne créée avec succès !");
            } else {
                epargneService.update(epargne);
                AlertUtil.information("Succès", "Épargne modifiée avec succès !");
            }

            clearForm();
            loadData();
            chargerTotalEpargne();

        } catch (NumberFormatException e) {
            AlertUtil.error("Erreur", "Veuillez saisir un montant valide.");
        } catch (RuntimeException e) {
            AlertUtil.error("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Epargne selected = epargneTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner une épargne.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'épargne");
        alert.setContentText("Voulez-vous vraiment supprimer cette épargne ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    epargneService.delete(selected.getId());
                    AlertUtil.information("Succès", "Épargne supprimée avec succès !");
                    clearForm();
                    loadData();
                    chargerTotalEpargne();
                } catch (RuntimeException e) {
                    AlertUtil.error("Erreur", e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCalculerInterets() {
        Epargne selected = epargneTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            // Calculer à partir du formulaire
            if (adherentCombo.getValue() != null) {
                try {
                    Epargne epargne = new Epargne();
                    epargne.setAdherent(adherentCombo.getValue());
                    epargne.setSolde(new BigDecimal(soldeField.getText().trim()));
                    epargne.setDateOuverture(dateOuverturePicker.getValue());

                    BigDecimal interets = epargneService.calculerInterets(epargne);
                    interetsLabel.setText("Intérêts estimés : " + String.format("%,.2f FCFA", interets));
                } catch (Exception e) {
                    AlertUtil.error("Erreur", "Erreur lors du calcul : " + e.getMessage());
                }
            } else {
                AlertUtil.warning("Avertissement", "Veuillez sélectionner un adhérent.");
            }
        } else {
            try {
                BigDecimal interets = epargneService.calculerInterets(selected);
                AlertUtil.information("Intérêts", "Intérêts estimés : " + String.format("%,.2f FCFA", interets));
            } catch (Exception e) {
                AlertUtil.error("Erreur", e.getMessage());
            }
        }
    }

    @FXML
    private void handleAjouterInterets() {
        Epargne selected = epargneTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            AlertUtil.warning("Avertissement", "Veuillez sélectionner une épargne.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Ajouter les intérêts");
        alert.setContentText("Voulez-vous ajouter les intérêts à cette épargne ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    BigDecimal interets = epargneService.ajouterInterets(selected.getAdherent().getId());
                    AlertUtil.information("Succès", "Intérêts ajoutés : " + String.format("%,.2f FCFA", interets));
                    loadData();
                    chargerTotalEpargne();
                } catch (Exception e) {
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
        adherentCombo.setValue(null);
        soldeField.clear();
        dateOuverturePicker.setValue(null);
        interetsLabel.setText("Intérêts estimés : -");
        epargneTable.getSelectionModel().clearSelection();
    }
}