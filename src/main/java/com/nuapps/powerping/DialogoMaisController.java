package com.nuapps.powerping;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogoMaisController {
    @FXML
    private TextField upField;
    @FXML
    private TextField downField;
    @FXML
    private ComboBox<String> comboBox;

    private Stage dialogStage;
    private boolean okClicked = false;
    private boolean cancelClicked = false;
    private String tf1 = "";
    private String tf2 = "";

    @FXML
    private void initialize() {
        //comboBox.getItems().add("E");
        comboBox.getItems().add("AND");
        //comboBox.getItems().add("OU");
        comboBox.getItems().add("OR");
        comboBox.getSelectionModel().select(0);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public boolean isCancelClicked() {
        return cancelClicked;
    }

    public ComboBox<String> getComboBox() {
        return comboBox;
    }

    public String getTf1() {
        return tf1;
    }

    public String getTf2() {
        return tf2;
    }

    @FXML
    private void handleOk() {
        if (isInputValid()) {
            tf1 = upField.getText();
            tf2 = downField.getText();
            dialogStage.close();
            okClicked = true;
        }
    }

    @FXML
    private void handleLimpar() {
        upField.clear();
        downField.clear();
        upField.requestFocus();
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
        cancelClicked = true;
    }

    private boolean isInputValid() {
        if (upField.getText() == null || upField.getText().length() == 0
                || downField.getText() == null || downField.getText().length() == 0) {
            upField.requestFocus();
            return false;
        } else return true;
    }
}
