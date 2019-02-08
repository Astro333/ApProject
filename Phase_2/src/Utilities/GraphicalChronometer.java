package Utilities;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;

public class GraphicalChronometer extends Chronometer {
    private final Pane graphics;
    private final Rectangle surface;
    private final Text time;
    private final Text time_shadow;
    private DoubleProperty GENERAL_TIME_MULTIPLIER = null;
    private double scale = 1;
    private final AnimationTimer animationTimer;
    private boolean wasReset = false;

    public boolean isFinished() {
        return finished.get();
    }

    public BooleanProperty finishedProperty() {
        return finished;
    }

    private final BooleanProperty finished;

    public GraphicalChronometer(DoubleProperty GENERAL_TIME_MULTIPLIER) {
        this.GENERAL_TIME_MULTIPLIER = GENERAL_TIME_MULTIPLIER;
        graphics = new Pane();
        graphics.setPickOnBounds(false);

        time = new Text("00:00:000");
        time_shadow = new Text("00:00:000");
        surface = new Rectangle(0, 0, 0, 0);
        setUpGraphicsStyle();
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                time.setText(String.format("%02d:%02d:%03d", getMinutes(), getSeconds(), getMilliseconds()));
            }
        };
        finished = new SimpleBooleanProperty(false);
    }

    public void Scale(double scale) {
        this.scale = scale;
        graphics.setScaleX(scale);
        graphics.setScaleY(scale);
    }

    public Pane getGraphic() {
        return graphics;
    }

    private void setUpGraphicsStyle() {
        surface.setStrokeWidth(3);
        surface.setStroke(Color.valueOf("#44c553"));
        time.setFill(Color.valueOf("#44C553"));
        time.setFont(Font.font("IONA-U1", 27));
        surface.setWidth(time.getLayoutBounds().getWidth() + 20);
        surface.setHeight(time.getLayoutBounds().getHeight() + 20);
        time.setX(surface.getX() + surface.getWidth() / 2 - time.getLayoutBounds().getWidth() / 2);
        time.setY(surface.getY() + surface.getHeight() / 2 + time.getLayoutBounds().getHeight() / 2);

        time_shadow.setFill(Color.valueOf("#44C553"));
        time_shadow.setFont(Font.font("IONA-U1", 27));
        time_shadow.setX(time.getX() - 4);
        time_shadow.setY(time.getY() - 4);
        time_shadow.setOpacity(0.4);
        time_shadow.textProperty().bind(time.textProperty());
        //graphic.getChildren().add(surface_shadow);
        graphics.getChildren().add(surface);
        graphics.getChildren().add(time);
        graphics.getChildren().add(time_shadow);
    }

    @Override
    public synchronized void start() {
        super.start();
        animationTimer.start();
    }

    public Rectangle getSurface() {
        return surface;
    }

    @Override
    public synchronized void stop() {
        super.stop();
        animationTimer.stop();
    }

    public int getHours() {
        return (((int) (getUpdatedChronometerTime() * GENERAL_TIME_MULTIPLIER.get()) / 1000) / 3600) % 60;
    }

    public int getMinutes() {
        return (((int) (getUpdatedChronometerTime() * GENERAL_TIME_MULTIPLIER.get()) / 1000) / 60) % 60;
    }

    public int getSeconds() {
        return ((int) (getUpdatedChronometerTime() * GENERAL_TIME_MULTIPLIER.get()) / 1000) % 60;
    }

    public int getMilliseconds() {
        return (int) (getUpdatedChronometerTime() * GENERAL_TIME_MULTIPLIER.get()) % 1000;
    }
}
