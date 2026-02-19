/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.budgetpro.pages;

import com.example.budgetpro.services.StatistiqueService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import java.util.Map;

public class Statistique {

    @FXML
    private PieChart pieChart;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private LineChart<String, Number> lineChart;

    private StatistiqueService statistiqueService;

    @FXML
    public void initialize() {
        statistiqueService = new StatistiqueService();

        loadPieChart();
        loadBarChart();
        loadLineChart();
    }

    private void loadPieChart() {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        Map<String, Double> data = statistiqueService.getDepensesParCategorie();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            pieData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        pieChart.setData(pieData);
        pieChart.setTitle("");

        System.out.println("Pie Chart chargé");
    }

    private void loadBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Dépenses");

        Map<String, Double> data = statistiqueService.getDepensesParJour();

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        barChart.getData().add(series);

        System.out.println("Bar Chart chargé");
    }

    private void loadLineChart() {
        XYChart.Series<String, Number> seriesDepenses = new XYChart.Series<>();
        seriesDepenses.setName("Dépenses");

        Map<String, Double> depenses = statistiqueService.getDepensesParMois();
        for (Map.Entry<String, Double> entry : depenses.entrySet()) {
            seriesDepenses.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        XYChart.Series<String, Number> seriesRevenus = new XYChart.Series<>();
        seriesRevenus.setName("Revenus");

        Map<String, Double> revenus = statistiqueService.getRevenusParMois();
        for (Map.Entry<String, Double> entry : revenus.entrySet()) {
            seriesRevenus.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        lineChart.getData().addAll(seriesDepenses, seriesRevenus);

        System.out.println("Line Chart chargé");
    }
}