package said.microgest.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import said.microgest.services.DashboardService;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardController {

    @FXML
    private Label totalAdherentsLabel;

    @FXML
    private Label totalEpargneLabel;

    @FXML
    private Label totalPretsLabel;

    @FXML
    private Label operationsMoisLabel;

    @FXML
    private Label depotsMoisLabel;

    @FXML
    private Label retraitsMoisLabel;

    @FXML
    private Label pretsEnAttenteLabel;

    @FXML
    private Label pretsValidesLabel;

    @FXML
    private Label pretsRemboursesLabel;

    @FXML
    private Label pretsRejetesLabel;

    @FXML
    private PieChart statutPieChart;

    @FXML
    private PieChart agencePieChart;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        chargerIndicateurs();
        chargerStatutPieChart();
        chargerAgencePieChart();
    }

    private void chargerIndicateurs() {
        try {
            Map<String, Object> data = dashboardService.getDashboardIndicateurs();

            totalAdherentsLabel.setText(String.valueOf(data.get("totalAdherents")));
            totalEpargneLabel.setText(formatMontant((BigDecimal) data.get("totalEpargne")));
            totalPretsLabel.setText(formatMontant((BigDecimal) data.get("totalPretsValides")));
            operationsMoisLabel.setText(String.valueOf(data.get("totalOperationsMois")));
            depotsMoisLabel.setText(formatMontant((BigDecimal) data.get("totalDepotsMois")));
            retraitsMoisLabel.setText(formatMontant((BigDecimal) data.get("totalRetraitsMois")));
            pretsEnAttenteLabel.setText(String.valueOf(data.get("nbPretsEnAttente")));
            pretsValidesLabel.setText(String.valueOf(data.get("nbPretsValides")));
            pretsRemboursesLabel.setText(String.valueOf(data.get("nbPretsRembourses")));
            pretsRejetesLabel.setText(String.valueOf(data.get("nbPretsRejetes")));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void chargerStatutPieChart() {
        try {
            Map<String, Long> stats = dashboardService.getStatistiquesParStatut();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            for (Map.Entry<String, Long> entry : stats.entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
            }

            statutPieChart.setData(pieData);
            statutPieChart.setTitle("Répartition des adhérents par statut");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void chargerAgencePieChart() {
        try {
            Map<String, Long> stats = dashboardService.getStatistiquesParAgence();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            for (Map.Entry<String, Long> entry : stats.entrySet()) {
                if (entry.getValue() > 0) {
                    pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                }
            }

            agencePieChart.setData(pieData);
            agencePieChart.setTitle("Répartition des adhérents par agence");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatMontant(BigDecimal montant) {
        if (montant == null) return "0 FCFA";
        return String.format("%,.0f FCFA", montant);
    }
}