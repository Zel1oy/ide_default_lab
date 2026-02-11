package com.lab.ui;

import com.lab.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

// Цей клас відповідає за діалогові вікна
public class PropertiesEditor {

    public static void showEditDialog(Block block, Runnable onSaveCallback) {
        if (block instanceof AssignmentBlock) {
            showAssignmentDialog((AssignmentBlock) block, onSaveCallback);
        } else if (block instanceof IOBlock) {
            showIODialog((IOBlock) block, onSaveCallback);
        } else if (block instanceof ConditionBlock) {
            showConditionDialog((ConditionBlock) block, onSaveCallback);
        }
    }

    private static void showAssignmentDialog(AssignmentBlock block, Runnable onSave) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Assignment");
        dialog.setHeaderText("Set V_target = Source");

        // Кнопки ОК/Cancel
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Поля форми
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField targetVarField = new TextField(String.valueOf(block.getTargetVarIndex()));
        
        CheckBox isConstCheck = new CheckBox("Is Constant?");
        isConstCheck.setSelected(block.isConstantAssign());

        TextField valueField = new TextField();
        // Якщо константа - показуємо значення, якщо ні - індекс змінної
        valueField.setText(block.isConstantAssign() ? 
                String.valueOf(block.getConstantValue()) : 
                String.valueOf(block.getSourceVarIndex()));

        grid.add(new Label("Target Variable (Index): V"), 0, 0);
        grid.add(targetVarField, 1, 0);
        grid.add(isConstCheck, 0, 1);
        grid.add(new Label("Value / Source Index:"), 0, 2);
        grid.add(valueField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Обробка результату
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    block.setTargetVarIndex(Integer.parseInt(targetVarField.getText()));
                    block.setConstantAssign(isConstCheck.isSelected());
                    int val = Integer.parseInt(valueField.getText());
                    if (block.isConstantAssign()) {
                        block.setConstantValue(val);
                    } else {
                        block.setSourceVarIndex(val);
                    }
                    return true;
                } catch (NumberFormatException e) {
                    showAlert("Invalid Number format");
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) onSave.run();
        });
    }

    private static void showIODialog(IOBlock block, Runnable onSave) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit I/O");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<IOBlock.IOType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(IOBlock.IOType.values());
        typeBox.setValue(block.getIoType());

        TextField varField = new TextField(String.valueOf(block.getVarIndex()));

        grid.add(new Label("Type:"), 0, 0);
        grid.add(typeBox, 1, 0);
        grid.add(new Label("Variable Index: V"), 0, 1);
        grid.add(varField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    block.setIoType(typeBox.getValue());
                    block.setVarIndex(Integer.parseInt(varField.getText()));
                    return true;
                } catch (Exception e) {
                    showAlert("Error parsing inputs");
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(s -> { if(s) onSave.run(); });
    }
    
    // Спрощений діалог для Conditions
    private static void showConditionDialog(ConditionBlock block, Runnable onSave) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Edit Condition");
        dialog.setHeaderText("Condition Logic: V [Op] C");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // 1. Поле для індексу змінної (V)
        TextField varIndexField = new TextField(String.valueOf(block.getLeftVarIndex()));

        // 2. Випадаючий список для оператора (== або <)
        ComboBox<ConditionBlock.Operator> operatorBox = new ComboBox<>();
        operatorBox.getItems().addAll(ConditionBlock.Operator.values());
        operatorBox.setValue(block.getOperator());

        // Перетворюємо назви Enum у красиві символи для списку (візуально)
        operatorBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ConditionBlock.Operator item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item == ConditionBlock.Operator.EQUALS ? "==" : "<");
            }
        });
        // Те саме для обраного значення
        operatorBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(ConditionBlock.Operator item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item == ConditionBlock.Operator.EQUALS ? "==" : "<");
            }
        });

        // 3. Поле для константи (C)
        TextField constField = new TextField(String.valueOf(block.getRightConstant()));

        // Розміщення на сітці
        grid.add(new Label("Variable Index (V):"), 0, 0);
        grid.add(varIndexField, 1, 0);

        grid.add(new Label("Operator:"), 0, 1);
        grid.add(operatorBox, 1, 1);

        grid.add(new Label("Constant (C):"), 0, 2);
        grid.add(constField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int vIndex = Integer.parseInt(varIndexField.getText());
                    int cValue = Integer.parseInt(constField.getText());

                    block.setLeftVarIndex(vIndex);
                    block.setOperator(operatorBox.getValue());
                    block.setRightConstant(cValue);

                    return true;
                } catch (NumberFormatException e) {
                    showAlert("Invalid number format!");
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(success -> {
            if (success) onSave.run();
        });
    }

    private static void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText(msg);
        alert.show();
    }
}