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

import said.microgest.entities.Epargne;
import said.microgest.entities.Operation;
import said.microgest.services.EpargneService;
import said.microgest.utils.AlertUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class EpargneController {

    @FXML private StackPane rootPane;
    @FXML private VBox listPane;
    @FXML private VBox detailPane;

    @FXML private TableView<Epargne> epargneTable;

    @FXML private TableColumn<Epargne, Number> idColumn;
    @FXML private TableColumn<Epargne, String> adherentColumn;
    @FXML private TableColumn<Epargne, String> numeroColumn;
    @FXML private TableColumn<Epargne, String> soldeColumn;
    @FXML private TableColumn<Epargne, String> dateColumn;

    @FXML private TextField searchField;
    @FXML private Label totalLabel;

    @FXML private ComboBox<Integer> sizeCombo;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private Label pageLabel;
    @FXML private Label totalPagesLabel;
    @FXML private Label totalRecordsLabel;

    @FXML private Button detailButton;

    @FXML private Label detailAdherentLabel;
    @FXML private Label detailNumeroLabel;
    @FXML private Label detailDateLabel;
    @FXML private Label detailSoldeLabel;

    @FXML private TableView<Operation> operationTable;
    @FXML private TableColumn<Operation, String> opTypeColumn;
    @FXML private TableColumn<Operation, String> opMontantColumn;
    @FXML private TableColumn<Operation, String> opDateColumn;
    @FXML private TableColumn<Operation, String> opObservationColumn;

    private final EpargneService epargneService = new EpargneService();

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int currentPage = 1;
    private int pageSize = 10;
    private long totalRecords = 0;
    private int totalPages = 1;

    @FXML
    public void initialize() {

        if (epargneTable != null) {

            configurerColonnesEpargne();

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

            epargneTable.getSelectionModel()
                    .selectedItemProperty()
                    .addListener(
                            (obs, oldSelection, newSelection) ->
                                    detailButton.setDisable(newSelection == null)
                    );

            searchField.textProperty()
                    .addListener(
                            (obs, oldValue, newValue) -> {

                                currentPage = 1;
                                chargerDonnees();
                            }
                    );

            chargerDonnees();
            chargerTotal();
        }

        if (operationTable != null) {

            configurerColonnesOperations();
        }

        if (listPane != null && detailPane != null) {

            listPane.setVisible(true);
            listPane.setManaged(true);

            detailPane.setVisible(false);
            detailPane.setManaged(false);
        }
    }

    private void configurerColonnesEpargne() {

        idColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getId())
        );

        adherentColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getAdherent() != null
                                ? data.getValue().getAdherent().getFullName()
                                : ""
                )
        );

        numeroColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getAdherent() != null
                                ? data.getValue().getAdherent().getNumeroAdherent()
                                : ""
                )
        );

        soldeColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.format("%,.0f FCFA", data.getValue().getSolde())
                )
        );

        dateColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getDateOuverture() != null
                                ? data.getValue().getDateOuverture().format(dateFormatter)
                                : ""
                )
        );
    }

    private void configurerColonnesOperations() {

        opTypeColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getType() != null
                                ? data.getValue().getType().name()
                                : ""
                )
        );

        opTypeColumn.setCellFactory(column ->
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
                                    "-fx-text-fill: #27ae60; -fx-font-weight: bold;"
                            );

                        } else {

                            setText("RETRAIT");

                            setStyle(
                                    "-fx-text-fill: #e74c3c; -fx-font-weight: bold;"
                            );
                        }
                    }
                }
        );

        opMontantColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.format("%,.0f FCFA", data.getValue().getMontant())
                )
        );

        opDateColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getDateOperation() != null
                                ? data.getValue().getDateOperation().format(dateTimeFormatter)
                                : ""
                )
        );

        opObservationColumn.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getObservation() != null
                                ? data.getValue().getObservation()
                                : ""
                )
        );
    }

    private void chargerDonnees() {

        try {

            String keyword = searchField.getText();

            if (keyword != null && !keyword.isBlank()) {

                List<Epargne> result = epargneService.search(keyword.trim());

                epargneTable.setItems(FXCollections.observableArrayList(result));

                totalRecords = result.size();
                totalPages = 1;
                currentPage = 1;

                mettreAJourPagination();

                return;
            }

            List<Epargne> epargnes = epargneService.findPaginated(currentPage, pageSize);

            epargneTable.setItems(FXCollections.observableArrayList(epargnes));

            totalRecords = epargneService.count();

            totalPages = (int) Math.ceil((double) totalRecords / pageSize);

            if (totalPages < 1) {
                totalPages = 1;
            }

            if (currentPage > totalPages) {

                currentPage = totalPages;

                epargnes = epargneService.findPaginated(currentPage, pageSize);

                epargneTable.setItems(FXCollections.observableArrayList(epargnes));
            }

            mettreAJourPagination();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger les épargnes : " + e.getMessage()
            );
        }
    }

    private void chargerTotal() {

        try {

            totalLabel.setText(
                    "Total épargné : "
                            + String.format("%,.0f FCFA", epargneService.getTotalEpargne())
            );

        } catch (Exception e) {

            totalLabel.setText("Total épargné : —");
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

    @FXML
    private void handleDetail() {

        Epargne epargne = epargneTable.getSelectionModel().getSelectedItem();

        if (epargne == null) {

            AlertUtil.warning("Attention", "Veuillez sélectionner un compte épargne.");
            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/epargne-view.fxml")
            );

            Parent root = loader.load();

            EpargneController controller = loader.getController();

            controller.setEpargneDetail(epargne);

            Stage stage = new Stage();

            stage.setTitle("Détail du compte épargne");

            stage.initModality(Modality.APPLICATION_MODAL);

            stage.setScene(new Scene(root));

            stage.showAndWait();

        } catch (Exception e) {

            e.printStackTrace();

            AlertUtil.error(
                    "Erreur",
                    "Impossible d'ouvrir le détail :\n\n" + e.getMessage()
            );
        }
    }

    public void setEpargneDetail(Epargne epargne) {

        if (listPane == null || detailPane == null) {
            return;
        }

        listPane.setVisible(false);
        listPane.setManaged(false);

        detailPane.setVisible(true);
        detailPane.setManaged(true);

        detailAdherentLabel.setText(
                epargne.getAdherent() != null
                        ? epargne.getAdherent().getFullName()
                        : "—"
        );

        detailNumeroLabel.setText(
                epargne.getAdherent() != null
                        ? epargne.getAdherent().getNumeroAdherent()
                        : "—"
        );

        detailDateLabel.setText(
                epargne.getDateOuverture() != null
                        ? epargne.getDateOuverture().format(dateFormatter)
                        : "—"
        );

        detailSoldeLabel.setText(
                String.format("%,.0f FCFA", epargne.getSolde())
        );

        try {

            List<Operation> historique =
                    epargneService.getHistoriqueOperations(epargne.getAdherent().getId());

            operationTable.setItems(FXCollections.observableArrayList(historique));

        } catch (Exception e) {

            AlertUtil.error(
                    "Erreur",
                    "Impossible de charger l'historique : " + e.getMessage()
            );
        }
    }

    @FXML
    private void handleCloseDetail() {

        if (detailAdherentLabel != null && detailAdherentLabel.getScene() != null) {

            Stage stage = (Stage) detailAdherentLabel.getScene().getWindow();
            stage.close();
        }
    }
}