package com.example.final13;

import javafx.scene.control.Slider;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FilledSliderSkin extends SkinBase<Slider> {
    private final Rectangle track;
    private final Rectangle fill;
    private final Region thumb;

    public FilledSliderSkin(Slider slider) {
        super(slider);

        track = new Rectangle();
        track.setHeight(4);
        track.setFill(Color.GRAY);
        track.setArcHeight(4);
        track.setArcWidth(4);

        fill = new Rectangle();
        fill.setHeight(4);
        fill.setFill(Color.web("#89CFF0"));
        fill.setArcHeight(4);
        fill.setArcWidth(4);

        thumb = new Region();
        thumb.setStyle("-fx-background-color: #D8C4B6; -fx-background-radius: 100%;");
        thumb.setPrefSize(16, 16);

        getChildren().addAll(track, fill, thumb);

        slider.valueProperty().addListener((obs, oldVal, newVal) -> update());
        slider.widthProperty().addListener((obs, oldVal, newVal) -> update());
        update();
    }

    private void update() {
        Slider s = getSkinnable();
        double width = s.getWidth() - thumb.getPrefWidth();
        double x = (s.getValue() - s.getMin()) / (s.getMax() - s.getMin()) * width;

        track.setWidth(width);
        track.setLayoutX(thumb.getPrefWidth() / 2);
        track.setLayoutY(thumb.getPrefHeight() / 2);

        fill.setWidth(x);
        fill.setLayoutX(thumb.getPrefWidth() / 2);
        fill.setLayoutY(thumb.getPrefHeight() / 2);

        thumb.setLayoutX(x);
        thumb.setLayoutY(0);
    }
}

