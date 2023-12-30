package com.nuapps.powerping;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

public class PingController {
    private static final String PING_N = "@ping -n 10 ";
    private static final String PING_T = "@ping -t ";

    @FXML
    private TableView<Hosts> hostTableView;
    @FXML
    private TableColumn<Hosts, String> hostNameTableColumn;
    @FXML
    private TableColumn<Hosts, String> ipAddressTableColumn;
    @FXML
    private TableColumn<Hosts, String> locationTableColumn;
    @FXML
    private CheckBox tCheckBox;
    @FXML
    private TextField searchTextField;

    private ObservableList<Hosts> dataObservableList;
    private FilteredList<Hosts> filteredData;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadDataObservableList();
        setupFiltering();
        setupCellFactories();
        setupListeners();
    }

    private void setupTableColumns() {
        hostNameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHostName()));
        ipAddressTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIpAddress()));
        locationTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
    }

    private void loadDataObservableList() {
        Path path = Path.of("devices.xlsx");

        if (Files.exists(path)) {
            try {
                dataObservableList = FXCollections.observableArrayList(new ExcelReader().getHostsList());
                hostTableView.setItems(dataObservableList);
                hostTableView.getSelectionModel().selectFirst();
                hostTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            } catch (IOException exception) {
                showErrorDialog("Error loading data", exception.getMessage());
            }
        } else {
            showErrorDialog("File not found", path + " does not exist");
        }
    }

    private void setupFiltering() {
        filteredData = new FilteredList<>(dataObservableList, b -> true);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> filteredData.setPredicate(host -> {
            if (newValue == null || newValue.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = newValue.toLowerCase();
            return host.getHostName().toLowerCase().contains(lowerCaseFilter)
                    || host.getIpAddress().toLowerCase().contains(lowerCaseFilter)
                    || host.getLocation().toLowerCase().contains(lowerCaseFilter);
        }));
    }

    private void setupCellFactories() {
        hostNameTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        hostNameTableColumn.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setHostName(t.getNewValue()));
        ipAddressTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        ipAddressTableColumn.setOnEditCommit(t -> t.getTableView().getItems().get(t.getTablePosition().getRow()).setIpAddress(t.getNewValue()));
    }

    private void setupListeners() {
        filteredData.addListener((ListChangeListener<Hosts>) change -> {
            SortedList<Hosts> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(hostTableView.comparatorProperty());
            hostTableView.setItems(sortedData);
        });
    }

    public void doExit() {
        Platform.exit();
    }

    @FXML
    private void runPing() throws IOException {
        String strParameters = (tCheckBox.isSelected() ? PING_T : PING_N);
        PingDriver.ping(hostTableView, strParameters);
    }

    @FXML
    private void clearTextField() {
        searchTextField.clear();
        searchTextField.requestFocus();
    }

    @FXML
    private void showAdvancedFilterDialog() {
        try {
            searchTextField.clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("advancedFilterDialog.fxml"));
            AnchorPane page = loader.load();
            Scene scene2 = new Scene(page);

            Stage advancedFilterDialogStage = new Stage();
            advancedFilterDialogStage.setTitle("Mostrar linhas que:");
            advancedFilterDialogStage.initModality(Modality.WINDOW_MODAL);
            advancedFilterDialogStage.setScene(scene2);
            AdvancedFilterDialogController ctrl = loader.getController();
            ctrl.setAdvancedFilterDialogStage(advancedFilterDialogStage);
            advancedFilterDialogStage.setResizable(false);
            advancedFilterDialogStage.showAndWait();

            if (ctrl.isOkClicked()) {
                applyFilter(ctrl.getStringSearchField1(), ctrl.getStringSearchField2(),
                        ctrl.getAndOrComboBox().getSelectionModel().getSelectedItem());
            }
            if (ctrl.isCancelClicked()) cancelClicked();
        } catch (IOException exception) {
            showErrorDialog("Error", exception.getMessage());
        }
    }

    @FXML
    private void applyFilter(String aux1, String aux2, String selectedItem) {
        Predicate<Hosts> p1 = host -> {
            if (aux1 == null || aux1.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = aux1.toLowerCase();
            return host.getHostName().toLowerCase().contains(lowerCaseFilter)
                    || host.getIpAddress().toLowerCase().contains(lowerCaseFilter)
                    || host.getLocation().toLowerCase().contains(lowerCaseFilter);
        };

        Predicate<Hosts> p2 = host -> {
            if (aux2 == null || aux2.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = aux2.toLowerCase();
            return host.getHostName().toLowerCase().contains(lowerCaseFilter)
                    || host.getIpAddress().toLowerCase().contains(lowerCaseFilter)
                    || host.getLocation().toLowerCase().contains(lowerCaseFilter);
        };

        if (selectedItem.equals("AND")) {
            filteredData.setPredicate(p1.and(p2));
        } else if (selectedItem.equals("OR")) {
            filteredData.setPredicate(p1.or(p2));
        } else {
            showErrorDialog("Error", "Invalid selection");
        }
    }

    @FXML
    private void cancelClicked() {
        Predicate<Hosts> predicate = host -> true;
        filteredData.setPredicate(predicate);
    }

    @FXML
    private void showAboutDialog() {
        try {
            // Carrega o arquivo FXML do diálogo "About"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("aboutDialog.fxml"));
            Scene scene = new Scene(loader.load());

            // Cria um novo palco (Stage) para o diálogo
            Stage aboutStage = new Stage();
            aboutStage.setTitle("About PowerPing");
            aboutStage.initModality(Modality.WINDOW_MODAL);

            // Define a cena no palco do diálogo
            aboutStage.setScene(scene);

            aboutStage.setResizable(false);
            aboutStage.showAndWait();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

