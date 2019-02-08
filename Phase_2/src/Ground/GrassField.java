package Ground;

import Buildings.Well;
import Interfaces.Destructible;
import Utilities.Utility;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;

import java.util.HashMap;

public class GrassField implements Destructible {
    private Grass[][] grasses;

    private Well well = null;
    private double x, y;
    public final double dis_x = 30;
    public final double dis_y = 24;
    private int columns;
    private int rows;
    private final HashMap<Integer, Grass> cellsWithGrass;
    private static final AudioClip WATERING_SOUND = new AudioClip(GrassField.class.getResource("/res/Grass/Sounds/action_watering.mp3").toExternalForm());

    public Grass[][] getGrasses() {
        return grasses;
    }

    /*public GrassField(double x, double y, int columns, int rows, String continent) {
        this.x = x;
        this.y = y;
        this.columns = columns;
        this.rows = rows;
        cellsWithGrass = new HashMap<>();
        grasses = new Grass[columns][rows];
        for (int i = 0; i < grasses.length; ++i) {
            for (int j = 0; j < grasses[i].length; ++j) {
                grasses[i][j] = new Grass(continent, -1, i, j);
                Pane graphics = grasses[i][j].getGraphics();
                graphics.setLayoutX(dis_x * i + x);
                graphics.setLayoutY(dis_y * j + y);
                graphics.setViewOrder(-graphics.getLayoutY());
                Grass grass = grasses[i][j];
                graphics.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.PRIMARY && !e.isAltDown()) {
                        increaseGrass(grass.getX(), grass.getY());
                    }
                });
            }
        }
    }*/

    public GrassField(double centerX, double centerY, double radiusX, double radiusY, String continent) {
        this.x = centerX;
        this.y = centerY;
        this.columns = (int) (radiusX / dis_x) + 1;
        this.rows = (int) (radiusY / dis_y) + 1;
        cellsWithGrass = new HashMap<>();
        grasses = createEllipticGrassArray(centerX, centerY, radiusX, radiusY, continent);
    }

    private Grass createGrass(String continent, int amount, int i, int j, double layoutX, double layoutY) {
        Grass temp = new Grass(continent, amount, i, j);
        Pane graphics = temp.getGraphics();
        graphics.setLayoutX(layoutX);
        graphics.setLayoutY(layoutY);
        graphics.viewOrderProperty().bind(graphics.layoutYProperty().multiply(-1));
        graphics.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && !e.isAltDown()) {
                increaseGrass(temp.getX(), temp.getY());
            }
        });
        return temp;
    }

    private Grass[][] createEllipticGrassArray(double centerX, double centerY, double radiusX, double radiusY, String continent) {
        Grass[][] temp = new Grass[(int) ((2 * radiusX) / dis_x) + 1][];
        double x = -radiusX;
        for (int i = 0; i < temp.length; ++i) {
            double y = (radiusY / radiusX) * Math.sqrt(radiusX * radiusX - x * x);
            temp[i] = new Grass[(int) (2 * y / dis_y) + 1];
            int middleIndex = (temp[i].length / 2);
            int lastIndex = temp[i].length - 1;
            for (int j = 0; j <= middleIndex; ++j) {
                temp[i][j] = createGrass(continent, -1, i, j, centerX + x, centerY - j * dis_y);
                if (2 * j != lastIndex) {
                    temp[i][lastIndex - j] = createGrass(continent, -1, i, lastIndex - j, centerX + x, centerY + j * dis_y);
                }
            }
            x += dis_x;
        }
        return temp;
    }

    public void setWell(Well well) {
        this.well = well;
    }

    public void increaseGrass(int x, int y) {
        if (well.useWater()) {
            WATERING_SOUND.play();
            for (int i = Math.max(0, x - 1); i <= Math.min(x + 1, grasses.length - 1); ++i) {
                for (int j = Math.max(0, y - 1); j <= Math.min(y + 1, grasses[i].length - 1); ++j) {
                    int val = 4 * (Math.abs(x - i) + Math.abs(y - j)) - 20;
                    grasses[i][j].useGrass(val);
                    cellsWithGrass.put(i * columns + j, grasses[i][j]);
                }
            }
        } else {
            Utility.FOOL_ACTION_SOUND.play();
            well.spark();
        }
    }

    public synchronized int[] getNearestGrassBlock(double start_x, double start_y) {
        if (cellsWithGrass.size() == 0)
            return null;
        int i = 0, j = 0;
        double dis = Double.MAX_VALUE;
        for (Grass grass : cellsWithGrass.values()) {
            double x = grass.getPosX() - start_x;
            double y = grass.getPosY() - start_y;
            x *= x;
            y *= y;
            x = y + x;
            if (x < dis) {
                dis = x;
                i = grass.getX();
                j = grass.getY();
            }
        }
        return new int[]{i, j};
    }

    public double useGrass(int x, int y, double amount) {
        Grass grass = cellsWithGrass.getOrDefault(x * columns + y, null);
        if (grass == null) {
            return 0;
        }
        double val = grass.useGrass(amount);
        if (grasses[x][y].getAmount() == 0) {
            cellsWithGrass.remove(x * columns + y);
        }
        return val;
    }

    public double useGrass(double x, double y, double amount) {
        int i = Math.max((int) ((x - this.x) / dis_x) - 1, 0);
        int j = Math.max((int) ((y - this.y) / dis_y) - 1, 0);
        return grasses[i][j].useGrass(amount);
    }

    @Override
    public void destruct() {
        for (int i = 0; i < grasses.length; ++i) {
            for (int j = 0; j < grasses[i].length; ++j) {
                grasses[i][j].getGraphics().setOnMouseClicked(null);
            }
        }
        cellsWithGrass.clear();
    }
}
