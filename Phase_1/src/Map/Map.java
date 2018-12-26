package Map;

import static Animals.Animal.AnimalType;

import Animals.Animal;
import Animals.Pet.Cat;
import Animals.Pet.Dog;
import Animals.Pet.Pet;
import Animals.Wild.Wild;
import Interfaces.Processable;
import Items.Item;
import Structures.Depot;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import java.lang.reflect.MalformedParametersException;
import java.util.HashSet;
import java.util.Iterator;

public class Map {

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
    private final ObservableMap<AnimalType, Integer> animalsAmount;
    private final Depot depot;

    public Map(Depot depot, MapChangeListener<Processable, Integer> mapChangeListener) {
        cells = new Cell[cellsWidth][cellsHeight];
        for (int i = 0; i < cellsWidth; ++i)
            for (int j = 0; j < cellsHeight; ++j) {
                cells[i][j] = new Cell(i, j);
                cells[i][j].noGrassProperty().addListener(noGrassInCell);
            }

        wilds = new HashSet<>();
        pets = new HashSet<>();
        items = new HashSet<>();
        this.depot = depot;
        cellsWithGrass = new HashSet<Cell>();
        animalsAmount = FXCollections.observableHashMap();
        animalsAmount.addListener(mapChangeListener);
    }

    public void update() {
        detectCollisions();
        for (int i = 0; i < cellsWidth; ++i) {
            for (int j = 0; j < cellsHeight; ++j) {
                Iterator<Wild> wildIterator = cells[i][j].getWilds().values().iterator();
                while (wildIterator.hasNext()) {
                    Wild wild = wildIterator.next();
                    int[] xy = wild.updatePosition(cellsWidth, cellsHeight);
                    wildIterator.remove();
                    if (xy == null) {
                        removeAnimal(wild);
                    } else {
                        cells[xy[0]][xy[1]].addAnimal(wild);
                    }
                }
                Iterator<Animal> petIterator = cells[i][j].getPets().values().iterator();
                while (petIterator.hasNext()) {
                    Animal pet = petIterator.next();
                    int[] xy;
                    if (pet instanceof Pet) {
                        xy = ((Pet) pet).updatePosition(this);
                    } else if (pet instanceof Dog) {
                        xy = ((Dog) pet).updatePosition(this);
                    } else if (pet instanceof Cat) {
                        xy = ((Cat) pet).updatePosition(this);
                    } else
                        throw new RuntimeException();
                    petIterator.remove();
                    cells[xy[0]][xy[1]].addAnimal(pet);
                }
            }
        }
    }

    private void detectCollisions() {
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                Iterator<Wild> wildIterator = cell.getWilds().values().iterator();
                while (wildIterator.hasNext()) {
                    Wild wild = wildIterator.next();
                    if (wild.isCaged() || wild.getTossingBuffer() >= 0)
                        continue;
                    if (cell.getPets().size() > 0) {
                        Animal thePet = cell.getPets().values().iterator().next();
                        removeAnimal(thePet);
                        wild.resetTossingBuffer();
                        if (thePet instanceof Dog) {
                            wildIterator.remove();
                            removeAnimal(wild);
                            break;
                        }
                    } else if (cell.getItems().size() > 0) {
                        removeItem(cell.getItems().values().iterator().next());
                        wild.resetTossingBuffer();
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

    public void addItem(Item item) {
        cells[item.getX()][item.getY()].addItem(item);
        items.add(item);
    }

    public void removeItem(Item item) {
        cells[item.getX()][item.getY()].removeItem(item);
        items.remove(item);
    }

    public void setGrassInCell(int x, int y, byte amount) {
        cells[x][y].setGrassInCell(amount);
    }

    public Cell getCell(int x, int y) {
        if (x < cells.length)
            if (y < cells[x].length)
                return cells[x][y];
        return null;
    }

    public void addAnimal(Animal animal) {
        cells[animal.getX()][animal.getY()].addAnimal(animal);

        if (animalsAmount.containsKey(animal.getType()))
            animalsAmount.compute(animal.getType(), (k, v) -> v + 1);
        else
            animalsAmount.put(animal.getType(), 1);

        if (animal instanceof Wild)
            wilds.add((Wild) animal);
        else
            pets.add(animal);
    }

    public Cell[][] getCells() {
        return cells;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cellsWidth; ++i) {
            for (int j = 0; j < cellsHeight; ++j)
                sb.append(cells[i][j].toString());
        }
        return sb.toString();
    }

    public void cageWilds(int x, int y, int timeToBreakCage) {
        cells[x][y].cageWilds(timeToBreakCage);
    }

    public Depot getDepot() {
        return depot;
    }

    public void removeAnimal(Animal animal) {
        cells[animal.getX()][animal.getY()].removeAnimal(animal);
        animalsAmount.compute(animal.getType(), (k, v) -> v - 1);
        if (animal instanceof Wild)
            wilds.remove(animal);
        else
            pets.remove(animal);
    }
}
