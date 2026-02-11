package com.lab.ui;

import javafx.scene.shape.Line;
import javafx.scene.Group;

public class ConnectionView extends Group {

    private final BlockView source;
    private final BlockView target;
    private final Line line;

    public ConnectionView(BlockView source, BlockView target) {
        this.source = source;
        this.target = target;
        this.line = new Line();

        // Стиль лінії
        line.setStrokeWidth(2);
        
        line.startXProperty().bind(source.layoutXProperty().add(source.widthProperty().divide(2)));
        line.startYProperty().bind(source.layoutYProperty().add(source.heightProperty().divide(2)));

        line.endXProperty().bind(target.layoutXProperty().add(target.widthProperty().divide(2)));
        line.endYProperty().bind(target.layoutYProperty().add(target.heightProperty().divide(2)));

        // Додаємо лінію в групу
        this.getChildren().add(line);
        
        // Щоб лінія не перехоплювала кліки миші (щоб можна було клікати крізь неї)
        this.setMouseTransparent(true); 
    }
}