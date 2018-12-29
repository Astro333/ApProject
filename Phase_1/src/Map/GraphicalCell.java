package Map;

import javafx.scene.CacheHint;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class GraphicalCell extends Pane {
    private int pos_X = 0, pos_Y = 0;
    private int tinyDotsAmount = 0;
    public final int MAX_TINY_DOTS_SUPPORT = 3;
    public Rectangle getCellBounds() {
        return cellBounds;
    }

    private Rectangle cellBounds;

    GraphicalCell(double x, double y, double length) {
        cellBounds = new Rectangle(x, y, length, length);
        getChildren().add(cellBounds);
        setPickOnBounds(false);
        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }



    public void addTinyDot(Color color) {
        if(tinyDotsAmount < MAX_TINY_DOTS_SUPPORT) {
            ++tinyDotsAmount;
            Circle circle = new Circle(cellBounds.getX() + cellBounds.getWidth() / 2, cellBounds.getY() + cellBounds.getHeight() / 2, 5, color);
            getChildren().add(circle);
        }
    }

    private void organizeTinyDots(){

    }

    public int getPos_X() {
        return pos_X;
    }

    public void setPos_X(int pos_X) {
        this.pos_X = pos_X;
    }

    public int getPos_Y() {
        return pos_Y;
    }

    public void setPos_Y(int pos_Y) {
        this.pos_Y = pos_Y;
    }
}
