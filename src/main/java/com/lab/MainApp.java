package com.lab;

import com.lab.model.*;
import com.lab.model.visitor.JavaCodeGenerator;
import com.lab.ui.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainApp extends Application {

    private Pane canvas;
    private BlockView selectedSourceBlock = null;
    private boolean isLinkMode = false;
    private ToggleButton tglLinkMode;

    // --- МУЛЬТИПОТОКОВІСТЬ ---
    private int currentThreadId = 1;
    // Зберігаємо блоки кожного потоку окремо: Map<ThreadID, List<Block>>
    private final Map<Integer, List<Block>> projectStorage = new HashMap<>();
    // -------------------------

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Canvas & Output
        canvas = new Pane();
        canvas.setStyle("-fx-background-color: #f0f0f0;");
        TextArea codeArea = new TextArea();
        codeArea.setEditable(false);
        SplitPane splitPane = new SplitPane(canvas, codeArea);
        splitPane.setDividerPositions(0.7);
        root.setCenter(splitPane);

        // --- Toolbar ---
        ToolBar toolbar = new ToolBar();

        // Вибір потоку
        ComboBox<String> threadSelector = new ComboBox<>();
        for (int i = 1; i <= 100; i++) {
            threadSelector.getItems().add("Thread " + i);
        }
        threadSelector.setValue("Thread 1");

        threadSelector.setOnAction(e -> {
            String selected = threadSelector.getValue();
            int newId = Integer.parseInt(selected.replace("Thread ", ""));
            switchThread(newId);
        });

        // Кнопки
        Button btnSave = new Button("💾 Save"); // Поки спрощено (зберігає поточний стан UI)
        Button btnAddStart = new Button("Add Start");
        Button btnAddAssign = new Button("Add Assign");
        Button btnAddCond = new Button("Add Condition");
        Button btnAddPrint = new Button("Add Print");
        Button btnAddStop = new Button("Add Stop");
        tglLinkMode = new ToggleButton("🔗 Link Mode");
        Button btnGenerate = new Button("⚙️ Generate");
        Button btnTest = new Button("🧪 Test (Multi)");

        // Handlers
        btnAddStart.setOnAction(e -> addBlockToCanvas(new StartBlock()));
        btnAddAssign.setOnAction(e -> {
            AssignmentBlock ab = new AssignmentBlock();
            ab.setTargetVarIndex(0); ab.setConstantAssign(true); ab.setConstantValue(0);
            addBlockToCanvas(ab);
        });
        btnAddCond.setOnAction(e -> {
            ConditionBlock cb = new ConditionBlock();
            cb.setOperator(ConditionBlock.Operator.LESS_THAN);
            addBlockToCanvas(cb);
        });
        btnAddPrint.setOnAction(e -> {
            IOBlock b = new IOBlock(); b.setIoType(IOBlock.IOType.PRINT);
            addBlockToCanvas(b);
        });
        btnAddStop.setOnAction(e -> addBlockToCanvas(new StopBlock()));

        tglLinkMode.selectedProperty().addListener((o, old, v) -> {
            isLinkMode = v;
            if(!v) selectedSourceBlock = null;
        });

        // Генерація коду (тепер передаємо ВСІ потоки)
        btnGenerate.setOnAction(e -> {
            saveCurrentThreadToMemory(); // Спочатку зберегти поточне
            JavaCodeGenerator generator = new JavaCodeGenerator(projectStorage);
            codeArea.setText(generator.generateMultiThreaded());
        });

        // Тестування (тепер передаємо ВСІ потоки)
        btnTest.setOnAction(e -> {
            saveCurrentThreadToMemory();
            TestWindow.show(projectStorage);
        });

        toolbar.getItems().addAll(
                new Label("Select Thread:"), threadSelector, new Separator(),
                btnAddStart, btnAddAssign, btnAddCond, btnAddPrint, btnAddStop,
                tglLinkMode, new Separator(), btnGenerate, btnTest
        );
        root.setTop(toolbar);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Multi-Thread IDE");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- ЛОГІКА ПЕРЕМИКАННЯ ПОТОКІВ ---

    private void switchThread(int newThreadId) {
        if (currentThreadId == newThreadId) return;

        // 1. Зберігаємо поточний екран у пам'ять
        saveCurrentThreadToMemory();

        // 2. Очищаємо екран
        canvas.getChildren().clear();
        selectedSourceBlock = null;
        tglLinkMode.setSelected(false);

        // 3. Змінюємо ID
        currentThreadId = newThreadId;

        // 4. Відновлюємо блоки нового потоку (якщо вони були)
        List<Block> blocks = projectStorage.getOrDefault(currentThreadId, new ArrayList<>());

        // Map для відновлення зв'язків
        Map<Integer, BlockView> viewMap = new HashMap<>();

        // Крок А: Створюємо BlockView
        for (Block b : blocks) {
            BlockView view = createBlockView(b); // Допоміжний метод
            canvas.getChildren().add(view);
            viewMap.put(b.getId(), view);
        }

        // Крок Б: Малюємо лінії
        for (Block b : blocks) {
            BlockView src = viewMap.get(b.getId());
            if (b.getNextBlockId() != null && viewMap.containsKey(b.getNextBlockId())) {
                createConnection(src, viewMap.get(b.getNextBlockId()), false);
            }
            if (b instanceof ConditionBlock) {
                ConditionBlock cb = (ConditionBlock) b;
                if (cb.getFalseNextBlockId() != null && viewMap.containsKey(cb.getFalseNextBlockId())) {
                    createConnection(src, viewMap.get(cb.getFalseNextBlockId()), true);
                }
            }
        }
    }

    private void saveCurrentThreadToMemory() {
        List<Block> blocks = new ArrayList<>();
        for (javafx.scene.Node node : canvas.getChildren()) {
            if (node instanceof BlockView) {
                BlockView bv = (BlockView) node;
                Block b = bv.getModelBlock();
                b.setX(bv.getLayoutX());
                b.setY(bv.getLayoutY());
                blocks.add(b);
            }
        }
        // Оновлюємо мапу (копіюємо список)
        projectStorage.put(currentThreadId, new ArrayList<>(blocks));
    }

    // --- UI HELPERS ---

    private void addBlockToCanvas(Block modelBlock) {
        modelBlock.setId((int)(Math.random() * 1000000));
        BlockView view = createBlockView(modelBlock);
        canvas.getChildren().add(view);
    }

    private BlockView createBlockView(Block modelBlock) {
        BlockView view = new BlockView(modelBlock);
        view.setLayoutX(modelBlock.getX() > 0 ? modelBlock.getX() : 50);
        view.setLayoutY(modelBlock.getY() > 0 ? modelBlock.getY() : 50);
        view.refreshVisuals();

        // Left Click
        view.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                if (e.getClickCount() == 2) PropertiesEditor.showEditDialog(modelBlock, view::refreshVisuals);
                else handleLinkClick(view);
                e.consume();
            }
        });

        // Right Click (Condition False)
        if (modelBlock instanceof ConditionBlock) {
            ContextMenu cm = new ContextMenu();
            MenuItem item = new MenuItem("🔗 Connect False Branch");
            item.setOnAction(e -> {
                isLinkMode = true; tglLinkMode.setSelected(true);
                selectedSourceBlock = view;
                view.getProperties().put("isFalse", true);
                view.setEffect(new javafx.scene.effect.DropShadow(20, Color.RED));
            });
            cm.getItems().add(item);
            view.setOnContextMenuRequested(e -> cm.show(view, e.getScreenX(), e.getScreenY()));
        }
        return view;
    }

    private void handleLinkClick(BlockView view) {
        if (!isLinkMode) return;
        if (selectedSourceBlock == null) {
            selectedSourceBlock = view;
            view.setEffect(new javafx.scene.effect.DropShadow(20, Color.BLUE));
        } else {
            if (selectedSourceBlock != view) {
                boolean isFalse = selectedSourceBlock.getProperties().containsKey("isFalse");
                createConnection(selectedSourceBlock, view, isFalse);
                selectedSourceBlock.getProperties().remove("isFalse");
            }
            selectedSourceBlock.setEffect(null);
            selectedSourceBlock = null;
        }
    }

    private void createConnection(BlockView src, BlockView tgt, boolean isFalse) {
        ConnectionView cv = new ConnectionView(src, tgt);
        if (isFalse) {
            ((ConditionBlock)src.getModelBlock()).setFalseNextBlockId(tgt.getModelBlock().getId());
            cv.getChildren().forEach(n -> ((javafx.scene.shape.Shape)n).setStroke(Color.RED));
        } else {
            src.getModelBlock().setNextBlockId(tgt.getModelBlock().getId());
        }
        canvas.getChildren().add(0, cv);
    }

    public static void main(String[] args) { launch(args); }
}