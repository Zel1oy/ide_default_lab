package com.lab.ui;

import com.lab.model.Block;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import lombok.Getter;

// StackPane дозволяє накладати текст поверх прямокутника
public class BlockView extends StackPane {

    @Getter
    private final Block modelBlock; // Посилання на дані (Backend)
    private double mouseAnchorX;
    private double mouseAnchorY;

    public BlockView(Block modelBlock) {
        this.modelBlock = modelBlock;
        
        // 1. Створення візуальної частини
        Rectangle rect = new Rectangle(120, 60); // Ширина, Висота
        rect.setFill(getColorForType());
        rect.setStroke(Color.BLACK);
        rect.setArcWidth(10); // Закруглені кути
        rect.setArcHeight(10);

        Label label = new Label(getBlockText());
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.WHITE);

        this.getChildren().addAll(rect, label);

        // 2. Налаштування позиції (початкові координати)
        // В реальному проекті ми б зберігали X/Y в моделі, але поки хай буде 50,50
        this.setLayoutX(50);
        this.setLayoutY(50);

        // 3. Додавання логіки перетягування (Drag & Drop)
        initDragEvents();
        refreshVisuals();
    }

    public void refreshVisuals() {
        Label label = (Label) this.getChildren().get(1); // 0 - Rectangle, 1 - Label
        label.setText(generateLabelText());
    }

    private String generateLabelText() {
        String type = modelBlock.getType();

        switch (modelBlock) {
            case com.lab.model.AssignmentBlock b -> {
                String rightSide = b.isConstantAssign() ? String.valueOf(b.getConstantValue()) : "V" + b.getSourceVarIndex();
                return "V" + b.getTargetVarIndex() + " = " + rightSide;
            }
            case com.lab.model.IOBlock b -> {
                String action = (b.getIoType() == com.lab.model.IOBlock.IOType.INPUT) ? "INPUT" : "PRINT";
                return action + " V" + b.getVarIndex();
            }
            case com.lab.model.ConditionBlock b -> {
                // Визначаємо значок
                String op = (b.getOperator() == com.lab.model.ConditionBlock.Operator.EQUALS) ? "==" : "<";
                // Формуємо рядок: V0 < 10 ?
                return "V" + b.getLeftVarIndex() + " " + op + " " + b.getRightConstant() + " ?";
            }
            default -> {
            }
        }

        return type + "\nID: " + modelBlock.getId();
    }

    private void initDragEvents() {
        // Коли натиснули кнопку миші - запам'ятовуємо, де саме схопили блок
        this.setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX() - this.getLayoutX();
            mouseAnchorY = event.getSceneY() - this.getLayoutY();
        });

        // Коли тягнемо мишу - змінюємо координати блоку
        this.setOnMouseDragged(event -> {
            this.setLayoutX(event.getSceneX() - mouseAnchorX);
            this.setLayoutY(event.getSceneY() - mouseAnchorY);
        });
    }

    // Допоміжний метод для кольору залежно від типу
    private Color getColorForType() {
        return switch (modelBlock.getType()) {
            case "START", "STOP" -> Color.DARKGREEN;
            case "ASSIGN" -> Color.ROYALBLUE;
            case "CONDITION" -> Color.DARKORANGE;
            case "IO" -> Color.PURPLE;
            default -> Color.GRAY;
        };
    }

    // Допоміжний метод для тексту
    private String getBlockText() {
        return modelBlock.getType() + "\nID: " + modelBlock.getId();
    }
}