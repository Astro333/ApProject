package Map;

import Animals.Animal;
import Animals.Pet.Cat;
import Animals.Pet.Dog;
import Animals.Pet.Pet;
import Animals.Wild.Wild;
import Items.Item;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.*;

public class Map {

    private transient final ChangeListener<Boolean> deadAnimalListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            /*
             * in Phase 2 when graphics is added,
             * first must show "dying animal animation"
             * then remove the animal from map
             * */
            Animal dead = ((Animal)((BooleanProperty)observable).getBean());
            cells[dead.getX()][dead.getY()].removeAnimal(dead);
            if(dead instanceof Wild)
                wilds.remove(dead);
            else
                pets.remove(dead);
        }
    };
    private transient final ChangeListener<Boolean> noGrassInCell = new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            Cell cell = (Cell) ((BooleanProperty)observable).getBean();
            if(newValue){
                cellsWithGrass.remove(cell);
            }
            else{
                cellsWithGrass.add(cell);
            }
        }
    };

    private final Cell[][] cells;
    public final int cellsWidth = 10;
    public final int cellsHeight = 10;

    private transient final HashSet<Cell> cellsWithGrass;

    private final List<Animal> pets;
    private final List<Wild> wilds;
    private final List<Item> items;

    public Map(){
        cells = new Cell[cellsWidth][cellsHeight];
        wilds = new LinkedList<>();
        pets = new LinkedList<>();
        items = new LinkedList<>();
        cellsWithGrass = new HashSet<Cell>();
    }
    public void update(){
        for (int i = 0; i < cellsWidth; ++i){
            for(int j = 0; j < cellsHeight; ++j){
                for(Wild wild : cells[i][j].getWilds().values()){
                    int[] xy = wild.updatePosition();
                    cells[i][j].removeAnimal(wild);
                    cells[xy[0]][xy[1]].addAnimal(wild);
                }
                for(Animal animal : cells[i][j].getPets().values()){
                    int[] xy;
                    if(animal instanceof Pet) {
                        xy = ((Pet) animal).changePosition(cellsWithGrass, cells[i][j]);
                    }
                    else if(animal instanceof Dog){
                        xy = ((Dog) animal).changePosition(wilds);
                    }
                    else if(animal instanceof Cat){
                        xy = ((Cat) animal).changePosition(items);
                    }
                    else
                        throw new RuntimeException();

                    cells[i][j].removeAnimal(animal);
                    cells[xy[0]][xy[1]].addAnimal(animal);
                }
            }
        }
    }

    public Cell getCell(int x, int y){
        if(x < cells.length)
            if(y < cells[x].length)
                return cells[x][y];
        return null;
    }

    public void addAnimal(Animal animal){
        cells[animal.getX()][animal.getY()].addAnimal(animal);
        if(animal instanceof Wild)
            wilds.add((Wild) animal);
        else
            pets.add(animal);
        animal.isDeadProperty().addListener(deadAnimalListener);
    }

    public Cell[][] getCells(){
        return cells;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < cellsWidth; ++i){
            for(int j = 0; j < cellsHeight; ++j)
                sb.append(cells[i][j].toString());
        }
        return sb.toString();
    }
}
