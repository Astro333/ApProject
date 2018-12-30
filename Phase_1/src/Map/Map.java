package Map;

import static Animals.Animal.AnimalType;

import Animals.Animal;
import Animals.Pet.Cat;
import Animals.Pet.Dog;
import Animals.Pet.Pet;
import Animals.Wild.Wild;
import Interfaces.Processable;
import Items.Item;
import Levels.SaveData;
import Structures.Depot;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Map {
    private transient final ChangeListener<Boolean> noGrassInCell = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            Cell cell = (Cell) ((BooleanProperty) observable).getBean();
            if (newValue) {
                graphics.getMapCells()[cell.getX()][cell.getY()].getCellBounds().setFill(Color.BLACK);
                cellsWithGrass.remove(cell);
            } else {
                graphics.getMapCells()[cell.getX()][cell.getY()].getCellBounds().setFill(Color.valueOf("#44c553"));
                cellsWithGrass.add(cell);
            }
        }
    };

    private final Cell[][] cells;
    public final int cellsWidth;
    public final int cellsHeight;

    private transient final HashSet<Cell> cellsWithGrass;
    private final HashSet<Animal> pets;
    private final HashSet<Wild> wilds;
    private final HashSet<Item> items;

    private final ObservableMap<AnimalType, Integer> animalsAmount;

    private final Depot depot;

    public MapGraphics getGraphics() {
        return graphics;
    }

    private transient MapGraphics graphics;

    public Map(Depot depot, int cellsWidth, int cellsHeight, MapChangeListener<Processable, Integer> mapChangeListener) {
        this.cellsHeight = cellsHeight;
        this.cellsWidth = cellsWidth;
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
        initializeGraphics();
    }

    public Map(SaveData saveData, MapChangeListener<Processable, Integer> mapChangeListener) {

        cellsHeight = saveData.getMapHeight();
        cellsWidth = saveData.getMapWidth();
        initializeGraphics();
        cells = new Cell[cellsWidth][cellsHeight];
        int[][] cellsGrassLevel = saveData.getCellsGrassLevel();

        cellsWithGrass = new HashSet<Cell>();
        for (int i = 0; i < cellsWidth; ++i)
            for (int j = 0; j < cellsHeight; ++j) {
                int grassLevel = cellsGrassLevel[i][j];
                cells[i][j] = new Cell(i, j);
                cells[i][j].setGrassInCell(grassLevel);
                cells[i][j].noGrassProperty().addListener(noGrassInCell);
                if (grassLevel > 0) {
                    cellsWithGrass.add(cells[i][j]);
                    graphics.getMapCells()[i][j].getCellBounds().setFill(Color.valueOf("#44c553"));
                }
            }

        wilds = new HashSet<>();
        pets = new HashSet<>();
        items = new HashSet<>();
        animalsAmount = FXCollections.observableHashMap();

        for (Animal animal : saveData.getAnimalsInMap()) {
            addAnimal(animal);
        }
        for (Item item : saveData.getItemsInMap()) {
            addItem(item);
        }
        depot = new Depot(saveData, mapChangeListener);
        Iterator<Wild> anim = wilds.iterator();
    }

    public void fillSaveData(SaveData saveData) {
        depot.fillSaveData(saveData);
        saveData.getAnimalsInMap().addAll(wilds);
        saveData.getAnimalsInMap().addAll(pets);
        saveData.getItemsInMap().addAll(items);
        int[][] cellsGrassLevel = saveData.getCellsGrassLevel();
        for (int i = 0; i < cellsGrassLevel.length; ++i) {
            for (int j = 0; j < cellsGrassLevel[i].length; ++j) {
                cellsGrassLevel[i][j] = cells[i][j].getGrassInCell();
            }
        }
    }

    private void initializeGraphics() {
        //initializing map graphics:
        final Object latch = MapGraphics.latch;
        if (MapGraphics.getCurrentInstance() == null) {
            new Thread(() -> Application.launch(MapGraphics.class, "Map5328449")).start();
            synchronized (latch) {
                try {
                    latch.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        graphics = MapGraphics.getCurrentInstance();
        graphics.setMap(this);
        try {
            synchronized (latch) {
                graphics.initialize();
                try {
                    latch.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObservableMap<AnimalType, Integer> getAnimalsAmount() {
        return animalsAmount;
    }

    public void update() {
        detectCollisions();
        Iterator<Wild> wildIterator = wilds.iterator();
        while (wildIterator.hasNext()) {
            Wild wild = wildIterator.next();
            int prevX = wild.getX();
            int prevY = wild.getY();
            cells[prevX][prevY].removeAnimal(wild);

            int[] newXY = wild.updatePosition(cellsWidth, cellsHeight);
            if (newXY == null) {
                wildIterator.remove();
                removeAnimal(wild);
                System.err.println(wild.getType() + " Broke Cage!");
            } else {
                cells[prevX][prevY].removeAnimal(wild);
                cells[newXY[0]][newXY[1]].addAnimal(wild);
            }
        }
        Iterator<Animal> petIterator = pets.iterator();
        while (petIterator.hasNext()) {
            Animal pet = petIterator.next();
            int prevX = pet.getX();
            int prevY = pet.getY();
            cells[prevX][prevY].removeAnimal(pet);
            int[] newXY;
            if (pet instanceof Pet) {
                newXY = ((Pet) pet).updatePosition(this);
                if (newXY == null) {
                    System.err.println(pet.getType() + " died out of Hunger.");
                    petIterator.remove();
                    removeAnimal(pet);
                    continue;
                }
            } else if (pet instanceof Dog) {
                newXY = ((Dog) pet).updatePosition(this);
            } else if (pet instanceof Cat) {
                newXY = ((Cat) pet).updatePosition(this);
            } else
                throw new RuntimeException();
            cells[newXY[0]][newXY[1]].addAnimal(pet);
        }
        graphics.updateElementsPosition();
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
                            System.out.println("Dog And " + wild.getType() + " collided!");
                            break;
                        }
                        System.out.println(wild.getType() + " Tossed " + thePet.getType());
                    } else if (cell.getItems().size() > 0) {
                        Item item = cell.getItems().values().iterator().next();
                        removeItem(item);
                        System.out.println(wild.getType() + " Tossed " + item.getType());
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
                sb.append(cells[i][j].toString()).append("\n");
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

    public void terminateGraphics() {
        graphics.terminate();
    }
}
