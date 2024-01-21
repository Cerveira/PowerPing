package com.nuapps.powerping;

import com.nuapps.powerping.model.RowData;
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Predicate;

public class PingController {
    private static final String PING_N = "@ping -n 10 ";
    private static final String PING_T = "@ping -t ";
    private final ObservableList<RowData> rowDataList = FXCollections.observableArrayList();
    @FXML
    private TableView<RowData> tableView;
    @FXML
    private TableColumn<RowData, String> hostNameTableColumn;
    @FXML
    private TableColumn<RowData, String> ipAddressTableColumn;
    @FXML
    private TableColumn<RowData, String> locationTableColumn;
    @FXML
    private CheckBox tCheckBox;
    @FXML
    private TextField searchTextField;
    private FilteredList<RowData> filteredData;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadExcelData();
        setupFiltering();
        setupCellFactories();
        setupListeners();
    }

    private void setupTableColumns() {
        hostNameTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHostName()));
        ipAddressTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIpAddress()));
        locationTableColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
    }

    private void loadExcelData() {
        try {
            FileInputStream file = new FileInputStream("devices.xlsx");
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheetAt(0); // Assumindo que os dados estão na primeira planilha

            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                // Lendo os dados da planilha e adicionando à lista
                String col1Value = row.getCell(0).getStringCellValue();
                String col2Value = row.getCell(1).getStringCellValue();
                String col3Value = row.getCell(2).getStringCellValue();
                rowDataList.add(new RowData(col1Value, col2Value, col3Value));
                tableView.setItems(rowDataList);
                tableView.getSelectionModel().selectFirst();
                tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            }
            file.close();
        } catch (FileNotFoundException e) {
            showErrorDialog("File not found", Path.of("devices.xlsx") + " does not exist");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupFiltering() {
        filteredData = new FilteredList<>(rowDataList, b -> true);
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
        filteredData.addListener((ListChangeListener<RowData>) change -> {
            SortedList<RowData> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(tableView.comparatorProperty());
            tableView.setItems(sortedData);
        });
    }

    public void doExit() {
        Platform.exit();
    }

    @FXML
    private void runPing() {
        if (tableView.getSelectionModel().getSelectedIndex() >= 0) {
            ObservableList<RowData> data = tableView.getSelectionModel().getSelectedItems();
            try {
                for (RowData rowData : data) {
                    String hostName = rowData.getHostName();
                    String pingCommand = (tCheckBox.isSelected() ? PING_T : PING_N) + rowData.getIpAddress();
                    String ipAddress = rowData.getIpAddress();
                    int lineNumber = data.indexOf(rowData);

                    String env_temp = System.getenv("TEMP");
                    String batFileName = env_temp + "/powerping/ping" + lineNumber + ".bat";
                    FileWriter bat = new FileWriter(batFileName);

                    BufferedWriter bufferedWriter = new BufferedWriter(bat);
                    bufferedWriter.write("@echo off");
                    bufferedWriter.newLine();
                    bufferedWriter.write("@cls");
                    bufferedWriter.newLine();
                    bufferedWriter.write("@color 17");
                    bufferedWriter.newLine();
                    bufferedWriter.write("@title Ping  " + hostName + "  [" + ipAddress + "]");
                    bufferedWriter.newLine();
                    bufferedWriter.write(pingCommand);
                    bufferedWriter.newLine();
                    bufferedWriter.write("@pause");
                    bufferedWriter.close();
                    bat.close();

                    ProcessBuilder processBuilder = new ProcessBuilder("rundll32", "SHELL32.DLL,ShellExec_RunDLL", batFileName);
                    processBuilder.start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
            advancedFilterDialogStage.setTitle("Show lines that:");
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
        Predicate<RowData> p1 = host -> {
            if (aux1 == null || aux1.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = aux1.toLowerCase();
            return host.getHostName().toLowerCase().contains(lowerCaseFilter)
                    || host.getIpAddress().toLowerCase().contains(lowerCaseFilter)
                    || host.getLocation().toLowerCase().contains(lowerCaseFilter);
        };

        Predicate<RowData> p2 = host -> {
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
        Predicate<RowData> predicate = host -> true;
        filteredData.setPredicate(predicate);
    }

    @FXML
    private void showAboutDialog() {
        try {
            // Carrega o arquivo FXML do diálogo "About"
            FXMLLoader loader = new FXMLLoader(getClass().getResource("aboutDialog.fxml"));
            Scene scene = new Scene(loader.load());

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
