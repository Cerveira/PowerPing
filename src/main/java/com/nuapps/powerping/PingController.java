package com.nuapps.powerping;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
public class PingController {
    private static final String ECO_N = "@ping -n 10 ";
    private static final String ECO_T = "@ping -t ";
    ObservableList<Host> data;
    SortedList<Host> sortedData;
    FilteredList<Host> filteredData;

    @FXML
    private TableView<Host> tabela;
    @FXML
    private TableColumn<Host, String> hostnameCol;
    @FXML
    private TableColumn<Host, String> ipaddressCol;
    @FXML
    private TableColumn<Host, String> locationCol;
    @FXML
    private CheckBox checkBox;
    @FXML
    private TextField txtFiltro;
    @FXML
    private void initialize() throws IOException {
        hostnameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHostname()));
        ipaddressCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIp()));
        locationCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));

        Excel excel = new Excel();
        data = FXCollections.observableArrayList(excel.hostList);
        tabela.setItems(data);
        tabela.getSelectionModel().selectFirst();
        tabela.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        filteredData = new FilteredList<>(data, b -> true);
        txtFiltro.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(host -> {
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = newValue.toLowerCase();

            if (host.getHostname().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (host.getIp().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else
                return host.getLocation().toLowerCase().contains(lowerCaseFilter); // Filter matches last name.
        }));

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(sortedData);
    } // fim do initialize

    public void doExit() {
        Platform.exit(); }

    @FXML
    private void metodoping() {
        try {
            String eco = (checkBox.isSelected() ? ECO_T : ECO_N);
            PingDriver pingDriver = new PingDriver();
            pingDriver.ping(tabela, eco);
        } catch (IOException ex) {
            ex.printStackTrace(); }
    }

    @FXML
    private void limpar() {
        txtFiltro.clear();
        txtFiltro.requestFocus(); }

    @FXML
    private void showDialogoMais() throws IOException {
        txtFiltro.clear();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(PingApplication.class.getResource("DialogoMais.fxml"));
        AnchorPane page = loader.load();
        Scene scene = new Scene(page);

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Mostrar linhas que:");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.setScene(scene);
        DialogoMaisController ctrl = loader.getController();
        ctrl.setDialogStage(dialogStage);
        dialogStage.setResizable(false);
        dialogStage.showAndWait();

        ComboBox cbb;
        cbb = ctrl.getComboBox();

        if (ctrl.isOkClicked()) filtrarMais(ctrl.getTf1(), ctrl.getTf2(), cbb.getSelectionModel().getSelectedItem().toString());
        if (ctrl.isCancelClicked()) cancelClicked();
    }

    @FXML
    private void filtrarMais(String aux1, String aux2, String selectedItem) {
        Predicate<Host> p1 = host -> {
            if (aux1 == null || aux1.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = aux1.toLowerCase();

            if (host.getHostname().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (host.getIp().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else
                return host.getLocation().toLowerCase().contains(lowerCaseFilter);
        };

        Predicate<Host> p2 = host2 -> {
            if (aux2 == null || aux2.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = aux2.toLowerCase();

            if (host2.getHostname().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else if (host2.getIp().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            } else
                return host2.getLocation().toLowerCase().contains(lowerCaseFilter);
        };

        if (selectedItem.equals("E")) {
            filteredData.setPredicate(p1.and(p2));
        }else if (selectedItem.equals("OU")) {
            filteredData.setPredicate(p1.or(p2));
        }else {
            System.out.println("Erro");
        }

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(sortedData);
    }

    @FXML
    private void cancelClicked() {
        Predicate<Host> predicate = host -> true;
        filteredData.setPredicate(predicate);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tabela.comparatorProperty());
        tabela.setItems(sortedData); }
}
