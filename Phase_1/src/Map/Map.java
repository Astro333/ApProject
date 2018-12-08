package Map;

import Animals.Animal;
import Animals.Pet.Cat;
import Animals.Pet.Dog;
import Animals.Pet.Pet;
import Animals.Wild.Wild;
import Interfaces.LevelRequirement;
import Items.Item;
import Structures.Depot;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import java.util.HashMap;
import java.util.HashSet;

public class Map {

    private transient final ChangeListener<Boolean> tossedElementListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            /*
             * in Phase 2 when graphics is added,
             * first must show "dying animal animation"
             * then remove the animal from map
             * */
            if(((BooleanProperty)observable).getBean() instanceof Item) {
                Item item = (Item) ((BooleanProperty) observable).getBean();
                removeItem(item);
            }
            else if(((BooleanProperty)observable).getBean() instanceof Animal) {
                Animal tossed = ((Animal) ((BooleanProperty) observable).getBean());
                removeAnimal(tossed);
            }
            else
                throw new RuntimeException("Fatal Error Occurred.");
        }
    };
    private transient final ChangeListener<Boolean> noGrassInCell = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            Cell cell = (Cell) ((BooleanProperty) observable).getBean();
            if (newValue) {
                cellsWithGrass.remove(cell);
            } else {
                cellsWithGrass.add(cell);
            }
        }
    };

    private final Cell[][] cells;
    public final int cellsWidth = 10;
    public final int cellsHeight = 10;

    private transient final HashSet<Cell> cellsWithGrass;
    private final HashSet<Animal> pets;
    private final HashSet<Wild> wilds;
    private final HashSet<Item> items;
    private final ObservableMap<Animal.AnimalType, Integer> animalsAmount;
    private final Depot depot;

    public Map(Depot depot, MapChangeListener<LevelRequirement, Integer> mapChangeListener){
        cells = new Cell[cellsWidth][cellsHeight];
        for(Cell[] cellRow : cells)
            for (Cell cell : cellRow)
                cell.noGrassProperty().addListener(noGrassInCell);

        wilds = new HashSet<>();
        pets = new HashSet<>();
        items = new HashSet<>();
        this.depot = depot;
        cellsWithGrass = new HashSet<Cell>();
        animalsAmount = FXCollections.observableHashMap();
        animalsAmount.addListener(mapChangeListener);
    }
    public void update(){
        detectCollisions();
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
                        xy = ((Pet) animal).updatePosition(this);
                    }
                    else if(animal instanceof Dog){
                        xy = ((Dog) animal).updatePosition(this);
                    }
                    else if(animal instanceof Cat){
                        xy = ((Cat) animal).updatePosition(this);
                    }
                    else
                        throw new RuntimeException();

                    cells[i][j].removeAnimal(animal);
                    cells[xy[0]][xy[1]].addAnimal(animal);
                }
            }
        }
    }

    private void detectCollisions() {
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                for (Wild wild : cell.getWilds().values()) {
                    if (wild.isCaged())
                        continue;
                    for (Item item : cell.getItems().values()) {
                        wild.destroy(item);
                    }
                    for (Animal animal : cell.getPets().values()) {
                        if(animal instanceof Cat){
                            for(Item item : cell.getItems().values()){
                                if(depot.addStorable(item.getType()))
                                    cell.removeItem(item);
                                else
                                    break;
                            }
                        }
                        wild.destroy(animal);
                        if (animal instanceof Dog) {
                            ((Dog) animal).kill(wild);
                        }
                    }
                }
            }
        }
    }


    public HashSet<Cell> getCellsWithGrass() {
        return cellsWithGrass;
    }

    public HashSet<Animal> getPets() {
        return pets;
    }

    public HashSet<Item> getItems() {
        return items;
    }

    public HashSet<Wild> getWilds() {
        return wilds;
    }

    public void addItem(Item item){
        cells[item.getX()][item.getY()].addItem(item);
        items.add(item);
        item.isTossedProperty().addListener(tossedElementListener);
    }

    public void removeItem(Item item){
        cells[item.getX()][item.getY()].removeItem(item);
        items.remove(item);
    }

    public void setGrassInCell(int x, int y, byte amount){
        cells[x][y].setGrassInCell(amount);
    }

    public Cell getCell(int x, int y){
        if(x < cells.length)
            if(y < cells[x].length)
                return cells[x][y];
        return null;
    }

    public void addAnimal(Animal animal){
        cells[animal.getX()][animal.getY()].addAnimal(animal);
        animalsAmount.compute(animal.getType(), (k, v) -> v+1);
        if(animal instanceof Wild)
            wilds.add((Wild) animal);
        else
            pets.add(animal);
        animal.isTossedProperty().addListener(tossedElementListener);
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

    public void cageWilds(int x, int y, int timeToBreakCage) {
        cells[x][y].cageWilds(timeToBreakCage);
    }

    public Depot getDepot(){
        return depot;
    }

    public void removeAnimal(Animal animal) {
        cells[animal.getX()][animal.getY()].removeAnimal(animal);
        animalsAmount.compute(animal.getType(), (k, v) -> v-1);
        if(animal instanceof Wild)
            wilds.remove(animal);
        else
            pets.remove(animal);
    }
}
