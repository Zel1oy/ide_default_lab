package com.lab.ui;

import com.lab.model.Block;
import com.lab.test.TestEngine;
import com.lab.test.ExecutionState;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class TestWindow {

    public static void show(Map<Integer, List<Block>> projectStorage) {
        Stage stage = new Stage();
        stage.setTitle("Automatic Testing (Multi-Thread Space Exploration)");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Вхідні дані
        TextField inputField = new TextField();
        inputField.setPromptText("Input Data (e.g.: 1 2 3)");
        TextField expectedField = new TextField();
        expectedField.setPromptText("Expected Output (e.g.: 1 2 3)");
        TextField kField = new TextField("20");
        kField.setPromptText("K (Max Steps)");

        // Кнопки керування
        Button btnRun = new Button("▶ Run Test");
        Button btnStop = new Button("⏹ Stop");
        btnStop.setDisable(true); // Спочатку неактивна
        ProgressIndicator progress = new ProgressIndicator();
        progress.setVisible(false);

        HBox controls = new HBox(10, btnRun, btnStop, progress);

        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(300);

        // --- ЛОГІКА ЗАПУСКУ ---
        btnRun.setOnAction(e -> {
            if (projectStorage.isEmpty()) {
                resultArea.setText("No blocks to test.");
                return;
            }

            int K;
            try {
                K = Integer.parseInt(kField.getText());
                if (K < 1) throw new NumberFormatException();
            } catch (Exception ex) {
                resultArea.setText("Invalid K (must be integer >= 1)");
                return;
            }

            resultArea.setText("Running analysis... Please wait.\n");
            btnRun.setDisable(true);
            btnStop.setDisable(false);
            progress.setVisible(true);

            // Створюємо фонове завдання
            Task<TestEngine.TestResult> task = new Task<>() {
                @Override
                protected TestEngine.TestResult call() {
                    TestEngine engine = new TestEngine(projectStorage);
                    // Передаємо функцію перевірки скасування: () -> isCancelled()
                    return engine.runExploration(
                            inputField.getText(),
                            expectedField.getText(),
                            K,
                            this::isCancelled
                    );
                }
            };

            // Коли завдання завершилось (успішно або через Stop)
            task.setOnSucceeded(evt -> {
                TestEngine.TestResult result = task.getValue();
                displayResults(result, resultArea);
                resetUI(btnRun, btnStop, progress);
            });

            task.setOnCancelled(evt -> {
                resultArea.appendText("\n[!] Process Interrupted by User.\n");
                // Навіть якщо перервали, ми можемо хотіти побачити частковий результат,
                // але Task зазвичай не повертає значення при Cancel.
                // Тому в нашій логіці engine повертає результат, а ми просто обробляємо його в OnSucceeded.
                // Але стандартний Task.cancel кидає InterruptedException.
                // Тому ми покладаємось на логіку всередині TestEngine (if isCancelled break).
                resetUI(btnRun, btnStop, progress);
            });

            task.setOnFailed(evt -> {
                resultArea.setText("Error: " + task.getException().getMessage());
                task.getException().printStackTrace();
                resetUI(btnRun, btnStop, progress);
            });

            // Кнопка Stop
            btnStop.setOnAction(event -> {
                task.cancel(); // Встановлює прапорець isCancelled = true
            });

            // Запускаємо потік
            new Thread(task).start();
        });

        root.getChildren().addAll(
                new Label("Input:"), inputField,
                new Label("Expected:"), expectedField,
                new Label("Max Steps (K):"), kField,
                controls,
                new Label("Results:"), resultArea
        );

        Scene scene = new Scene(root, 500, 600);
        stage.setScene(scene);
        stage.show();
    }

    private static void resetUI(Button run, Button stop, ProgressIndicator pi) {
        run.setDisable(false);
        stop.setDisable(true);
        pi.setVisible(false);
    }

    private static void displayResults(TestEngine.TestResult result, TextArea area) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ANALYSIS REPORT ===\n");
        sb.append(result.getCoverageInfo()).append("\n\n");

        sb.append("--- Sample Execution Paths ---\n");
        int count = 0;
        for (ExecutionState state : result.getFinishedPaths()) {
            if (count++ > 50) { // Не показуємо більше 50 шляхів, щоб не завис текст
                sb.append("... (and " + (result.getFinishedPaths().size() - 50) + " more)\n");
                break;
            }
            sb.append(state.isPassed() ? "[PASS]" : "[FAIL]").append(" Output: ").append(state.getOutputString()).append("\n");
        }

        if (result.getFinishedPaths().isEmpty()) {
            sb.append("No completed paths found within the step limit.");
        }
        area.setText(sb.toString());
    }
}