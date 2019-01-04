package Map;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MapGraphics extends Application {

    public static MapGraphics getCurrentInstance() {
        return currentInstance;
    }

    private static MapGraphics currentInstance;

    public static final Object latch = new Object();

    private Map map = null;
    private Stage primaryStage;
    private GraphicalCell[][] mapCells;

    public MapGraphics() {
        currentInstance = this;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    @Override
    public void start(Stage primaryStage) {
        synchronized (latch) {
            Platform.setImplicitExit(false);
            primaryStage.initStyle(StageStyle.UNDECORATED);
            this.primaryStage = primaryStage;
            latch.notify();
        }
    }

    public void initialize() throws IOException {
        if (map != null)
            Platform.runLater(() -> initialize(primaryStage));
        else
            throw new IOException("Map Has Not Been Set Yet.");
    }

    public GraphicalCell[][] getMapCells() {
        return mapCells;
    }

    public void updateElementsPosition() {
        for(int i = 0; i < map.cellsWidth; ++i){
            for(int j = 0; j < map.cellsHeight; ++j){
                mapCells[i][j].getCellBounds().setFill(Color.BLACK);
            }
        }
        for(Cell cell : map.getCellsWithGrass()){
            mapCells[cell.getX()][cell.getY()].getCellBounds().setFill(Color.valueOf("#44c553"));
        }
    }

    private void initialize(Stage primaryStage) {
        synchronized (latch) {
            Pane root = new Pane();
            mapCells = new GraphicalCell[map.cellsWidth][map.cellsHeight];
            int length = 60;
            int strokeWidth = 3;
            double width = (length) * map.cellsWidth;
            double height = (length) * map.cellsHeight;
            for (int i = 0; i < map.cellsWidth; ++i) {
                for (int j = 0; j < map.cellsHeight; ++j) {

                    mapCells[i][j] = new GraphicalCell(i * (length), j * (length), length);

                    mapCells[i][j].setPos_X(i);
                    mapCells[i][j].setPos_Y(j);

                    mapCells[i][j].getCellBounds().setStrokeWidth(strokeWidth);
                    mapCells[i][j].getCellBounds().setStroke(Color.BLUE);
                    mapCells[i][j].getCellBounds().setStrokeType(StrokeType.INSIDE);

                    root.getChildren().add(mapCells[i][j]);
                }
            }
            Scene scene = new Scene(root, width, height);
            primaryStage.setScene(scene);
            primaryStage.show();
            latch.notify();
        }
    }
    public static void KillThread(){
        Platform.setImplicitExit(true);
        Platform.exit();
    }
    public void terminate() {
        Platform.runLater(() -> primaryStage.close());
    }
}
