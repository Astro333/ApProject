package Controllers;

import static Items.Item.ItemType;

import Animals.Animal;
import Animals.Pet.Cat;
import Animals.Pet.Dog;
import Animals.Pet.Prairie.Cow;
import Animals.Pet.Africa.GuineaFowl;
import Animals.Pet.Africa.Ostrich;
import Animals.Pet.Prairie.Sheep;
import Animals.Wild.Wild;
import Exceptions.IllegalConstructorArgumentException;
import Exceptions.SaveDataInvalidException;
import Interfaces.Processable;
import Items.Item;
import Levels.LevelData;
import Map.Map;
import Map.Cell;
import Player.Player;
import Structures.Depot;
import Structures.Well;
import Structures.Workshop;
import Transportation.Helicopter;
import Transportation.TransportationTool;
import Transportation.Truck;
import Utilities.Constants;
import Utilities.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

// this Controller is Used For Any LevelData Being Played
public class LevelController {
    private final Scanner scanner;

    private final String PLANT_REGEX;
    private final String CAGE_REGEX;
    private final String BUY_REGEX;
    private final String PICKUP_REGEX;
    private final String WELL_REGEX;
    private final String START_WORKSHOP_REGEX;
    private final String UPGRADE_REGEX;
    private final String PRINT_REGEX;
    private final String TURN_REGEX;

    {
        PLANT_REGEX = "plant\\s+\\d+\\s+\\d+";
        CAGE_REGEX = "cage\\s+\\d+\\s+\\d+";
        BUY_REGEX = "buy\\s+[a-z]+";
        PICKUP_REGEX = "pickup\\s+\\d+\\s+\\d+";
        WELL_REGEX = "well";
        START_WORKSHOP_REGEX = "start\\s+[a-z][a-z ]*";
        UPGRADE_REGEX = "upgrade\\s+[a-z][a-z ]*";
        PRINT_REGEX = "print\\s+[a-z][a-z ]*";
        TURN_REGEX = "turn\\s+[1-9]\\d*";
        String x = "s";
        x.matches(TURN_REGEX);
    }

    private int coinsCollected = 0;
    private final LevelData levelData;
    private final Map map;
    private final HashMap<String, Workshop> workshops;
    private final TransportationTool helicopter;
    private final TransportationTool truck;
    private final Well well;
    private final StringBuilder levelLog;
    private final Depot depot;
    private final int cageLevel;
    private final Random randomGenerator = new Random();
    private final Player player;
    private ChangeListener<Boolean> vehicleFinishedJob = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (oldValue) {
                TransportationTool vehicle = (TransportationTool) ((BooleanProperty) observable).getBean();
                if (vehicle instanceof Truck) {
                    coinsCollected += ((Truck) vehicle).receiveSoldGoodsMoney();
                } else if (vehicle instanceof Helicopter) {
                    ((Helicopter) helicopter).dropItemsRandomly(map);
                }
            }
        }
    };
    private ChangeListener<Boolean> workShopFinishedJob = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (oldValue) {
                Workshop workshop = (Workshop) (((BooleanProperty) observable).getBean());
                int amountProcessed = workshop.getAmountProcessed();
                Integer[] amounts = workshop.getOutputsAmount();
                Processable[] outputs = workshop.getOutputs();
                Pair<Integer, Integer> dropZone =
                        Constants.getWorkshopDropZone(levelData.getContinent(), workshop.getPosition());

                if(dropZone == null)
                    throw new RuntimeException("Why DropZone Was Null?");

                Cell mapCell = map.getCell(dropZone.getKey(), dropZone.getValue());
                for (int i = 0; i < amounts.length; ++i) {
                    int j = 0;
                    if (outputs[i] instanceof Animal.AnimalType) {
                        while (j < amounts[i] * amountProcessed) {
                            Animal animal = getRandomlyLocatedAnimalInstance(outputs[i].toString().toLowerCase());
                            if (animal == null)
                                break;
                            animal.setX(dropZone.getKey());
                            animal.setY(dropZone.getValue());
                            mapCell.addAnimal(animal);
                            ++j;
                        }
                    } else if (outputs[i] instanceof Item.ItemType) {
                        while (j < amounts[i] * amountProcessed) {
                            mapCell.addItem(new Item((Item.ItemType) outputs[i]));
                            ++j;
                        }
                    }
                }
            }
        }
    };

    /**
     * @param pathToLevelJsonFile path to level json file.
     * @param player              needed to determine max level for workshops, saveGame, etc.
     */
    public LevelController(String pathToLevelJsonFile, Player player) throws FileNotFoundException {
        scanner = new Scanner(System.in);

        Reader reader = new BufferedReader(new FileReader(pathToLevelJsonFile));
        Gson gson = new GsonBuilder().create();

        levelData = gson.fromJson(reader, LevelData.class);
        Map map;
        map = new Map();
        levelLog = new StringBuilder();

        this.player = player;
        this.map = map;
        this.well = getWellInstance();
        this.truck = getTruckInstance();
        this.helicopter = getHelicopterInstance();
        this.workshops = getWorkshopsInstance();
        this.depot = new Depot(player.getGameElementLevel("Depot"));

        cageLevel = player.getGameElementLevel("Cage");

        if(helicopter != null)
            helicopter.isAtTaskProperty().addListener(vehicleFinishedJob);
        if(truck != null)
            truck.isAtTaskProperty().addListener(vehicleFinishedJob);
        if(workshops != null)
            for (Workshop workshop : workshops.values())
                workshop.isAtTaskProperty().addListener(workShopFinishedJob);
    }

    public LevelController(Player player, String pathToSaveData) throws SaveDataInvalidException {
        this.player = player;
        levelData = null;
        map = null;
        well = null;
        truck = null;
        helicopter = null;
        workshops = null;
        levelLog = new StringBuilder();
        depot = new Depot((byte) 5);
        scanner = new Scanner(System.in);
        cageLevel = 2;
    }

    private Helicopter getHelicopterInstance() {
        if (levelData.getHelicopterStartingLevel() == null)
            return null;
        return new Helicopter(player.getGameElementLevel("Helicopter"),
                levelData.getHelicopterStartingLevel());
    }

    private Truck getTruckInstance() {
        if (levelData.getTruckStartingLevel() == null)
            return null;
        return new Truck(player.getGameElementLevel("Truck"),
                levelData.getTruckStartingLevel());
    }

    private Well getWellInstance() {
        byte level = levelData.getWellStartingLevel();
        byte maxLevel = player.getGameElementLevel("Well");
        Well well = null;
        try {
            well = new Well(maxLevel, level, 3);
        } catch (IllegalConstructorArgumentException e) {
            e.printStackTrace();
        }
        return well;
    }

    private HashMap<String, Workshop> getWorkshopsInstance() {
        HashMap<String, Workshop> workshops;
        if (levelData.getWorkshops().length == 0) {
            return null;
        }
        workshops = new HashMap<>();
        String[] ws = levelData.getWorkshops();
        for (int i = 0; i < ws.length; ++i) {
            String workshopName = ws[i];
            int maxLevel = player.getGameElementLevel(workshopName);
            Workshop workshop;
            try {
                workshop = Workshop.getInstance(workshopName, maxLevel,
                        levelData.getWorkshopsPosition()[i], levelData.getContinent());
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Workshop instantiation failed.");
            }
            workshops.put(workshopName, workshop);
        }
        return workshops;
    }

    public void startProcessing() {
        String input = scanner.nextLine().trim().toLowerCase();

        while (!input.equals("exit level")) {
            if (input.matches(PLANT_REGEX)) {
                String[] s = input.replaceFirst("plant\\s+", "").
                        split("\\s+");
                plant(Integer.parseInt(s[0]), Integer.parseInt(s[1]));

            } else if (input.matches(CAGE_REGEX)) {

                String[] s = input.replaceFirst("cage\\s+", "").
                        split("\\s+");
                cage(Integer.parseInt(s[0]), Integer.parseInt(s[1]));

            } else if (input.matches(PICKUP_REGEX)) {
                String[] s = input.replaceFirst("pickup\\s+", "").
                        split("\\s+");
                pickup(Integer.parseInt(s[0]), Integer.parseInt(s[1]));

            } else if (input.matches(BUY_REGEX)) {
                String s = input.replaceFirst("buy\\s+", "");
                int cost = Constants.getProductBuyCost(s);
                if (cost < 0) {
                    System.err.println("No Such Animal In Existence.");
                } else {
                    Animal animal = getRandomlyLocatedAnimalInstance(s);
                    if (coinsCollected >= cost) {
                        if (animal != null) {
                            map.addAnimal(animal);
                            coinsCollected -= cost;
                            System.out.println(s + " Was Bought.");
                        } else
                            System.err.println("No Such Animal In Existence.");
                    } else
                        PRINT_NOT_ENOUGH_MONEY();
                }
            } else if (input.matches(WELL_REGEX)) {
                well();
            } else if (input.matches(UPGRADE_REGEX)) {
                String name = input.replaceFirst("upgrade\\s+", "");
                switch (name) {
                    case "well":
                        upgradeWell();
                        break;
                    case "truck":
                        upgradeTruck();
                        break;
                    case "helicopter":
                        upgradeHelicopter();
                        break;
                    case "depot":
                        upgradeDepot();
                        break;
                }
                if (workshops.containsKey(name)) {
                    Workshop workshop = workshops.get(name);
                    int cost = workshop.getUpgradeCost();
                    if (coinsCollected >= cost) {
                        if (workshop.upgrade()) {
                            coinsCollected -= cost;
                            System.out.println(name + " was upgraded to level " + workshop.getLevel() + "\n");
                        } else {
                            System.err.println(name + " is At Maximum LevelData.");
                        }
                    } else {
                        System.err.println("Not Enough Money.");
                    }
                } else {
                    System.err.println("Invalid Argument For Upgrade Instruction.");
                }
            } else if (input.matches(START_WORKSHOP_REGEX)) {
                String name = input.replaceFirst("start\\s+", "");
                if (workshops.containsKey(name)) {
                    if (!workshops.get(name).startWorking(depot)) {
                        System.err.println("Not Enough Components for " + name + " to work.");
                    }
                } else
                    System.err.println("No Such Workshop In Existence.");
            } else if (input.matches(PRINT_REGEX)) {
                String s = input.replaceFirst("print\\s+", "");
                switch (s) {
                    case "info":
                        PRINT_INFO();
                        break;
                    case "map":
                        System.out.print(map);
                        break;
                    case "warehouse":
                        System.out.print(depot);
                        break;
                    case "well":
                        System.out.print(well);
                        break;
                    case "workshops":
                        for (Workshop workshop : workshops.values())
                            System.out.print(workshop);
                        break;
                    case "truck":
                        System.out.print(truck);
                        break;
                    case "helicopter":
                        System.out.print(helicopter);
                        break;
                    default:
                        System.err.println("Invalid Argument For Print Instruction.");
                        break;
                }
            } else if (input.matches(TURN_REGEX)) {
                int turns = Integer.parseInt(input.split("\\s+")[1]);
                update(turns);
            } else {
                System.err.println("Invalid Command.");
            }
            input = scanner.nextLine().trim().toLowerCase();
        }
    }

    private void update(int turns) {
        for (int i = 0; i < turns; ++i) {
            map.update();
            helicopter.update(1);
            truck.update(1);
            for (Workshop workshop : workshops.values())
                workshop.update(1);
        }
    }

    private void PRINT_INFO() {

    }

    public void detectDestructiveCollisions() {
        for (Cell[] cell1 : map.getCells()) {
            for (Cell cell : cell1) {
                for (Wild wild : cell.getWilds().values()) {
                    if (wild.isCaged())
                        continue;
                    for (Item item : cell.getItems().values()) {
                        cell.removeItem(item.getId());
                    }
                    for (Animal animal : cell.getPets().values()) {
                        wild.destroy(animal);
                        if (animal instanceof Dog) {
                            ((Dog) animal).kill(wild);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param type type of the animal selected to buy
     */
    private Animal getRandomlyLocatedAnimalInstance(String type) {
        int x = randomGenerator.nextInt(map.cellsWidth);
        int y = randomGenerator.nextInt(map.cellsHeight);
        switch (type) {
            case "cat":
                return new Cat(x, y, player.getGameElementLevel("Cat"));
            case "dog":
                return new Dog(x, y, player.getGameElementLevel("Dog"));
        }

        String animalClassName = Constants.getAnimalClassName(type);
        if(animalClassName == null)
            return null;
        String packageName = "Animals.Pet."+levelData.getContinent()+".";
        try {
            Class clazz = Class.forName(packageName+animalClassName);
            Constructor constructor = clazz.getDeclaredConstructor(int.class, int.class);
            constructor.setAccessible(true);
            Animal instance = (Animal) constructor.newInstance(x, y);
            constructor.setAccessible(false);
            return instance;
        }
        catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
               InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void PRINT_NOT_ENOUGH_MONEY() {
        System.err.println("Not Enough Money.");
    }

    private void well() {
        if (coinsCollected >= well.getRefillPrice()) {
            if (!well.isRefilling()) {
                if (well.refill()) {
                    System.out.println("Well is Refilling.");
                    coinsCollected -= well.getRefillPrice();
                } else
                    System.err.println("Well is Full.");
            } else
                System.err.println("Well is Already Refilling.");
        } else {
            PRINT_NOT_ENOUGH_MONEY();
        }
    }

    private void plant(int x, int y) {
        if (well.getStoredWater() > 0) {
            byte MAX_GRASS_AMOUNT = 6;
            map.getCell(x, y).setGrassInCell(MAX_GRASS_AMOUNT);
            well.decrementWaterLevel();
        } else
            System.err.println("Not Enough Water In Well.\n");
    }

    private void cage(int x, int y) {
        map.getCell(x, y).cageWilds(cageLevel * 2);
    }

    private void saveGame(String jsonFileName) {
        /*
         * save data AS Json File under Player_Unfinished_Levels_Saves
         * */
    }

    private boolean helicopterGo() {
        if (coinsCollected >= helicopter.getItemsInsidePrice()) {
            if (helicopter.go()) {
                coinsCollected -= helicopter.getItemsInsidePrice();
                System.out.println("Helicopter is going. will return in "
                        + helicopter.getTimeRemainedToFinishTask() + " turns.\n");
                return true;
            }
            System.err.println("Nothing Set To Buy.\n");
            return false;
        }
        PRINT_NOT_ENOUGH_MONEY();
        return false;
    }

    private boolean truckGo() {
        if (truck.go()) {
            System.out.println("Truck is going. will return in " +
                    truck.getTimeRemainedToFinishTask() + " turns.\n");
            return true;
        }
        System.err.println("Nothing in Truck.\n");
        return false;
    }

    private void clearHelicopter() {
        helicopter.clear();
    }

    private void clearTruck() {
        truck.clear();
    }

    private void upgradeWell() {
        int cost = well.getUpgradePrice();
        if (coinsCollected >= cost) {
            if (well.upgrade()) {
                coinsCollected -= cost;
                System.out.println("Well was upgraded to level " + well.getLevel() + "\n");
            } else {
                System.err.println("Well is At Maximum LevelData.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private void upgradeTruck() {
        int cost = truck.getUpgradeCost();
        if (coinsCollected >= cost) {
            if (truck.upgrade()) {
                coinsCollected -= cost;
                System.out.println("Truck was upgraded to level " + truck.getLevel() + "\n");
            } else {
                System.err.println("Truck is At Maximum LevelData.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private void upgradeHelicopter() {
        int cost = helicopter.getUpgradeCost();
        if (coinsCollected >= cost) {
            if (helicopter.upgrade()) {
                coinsCollected -= cost;
                System.out.println("Helicopter was upgraded to level " + helicopter.getLevel() + "\n");
            } else {
                System.err.println("Helicopter is At Maximum LevelData.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private void upgradeDepot() {
        int cost = depot.getUpgradeCost();
        if (coinsCollected >= cost) {
            if (depot.upgrade()) {
                coinsCollected -= cost;
                System.out.println("Depot was upgraded to level " + depot.getLevel() + "\n");
            } else {
                System.err.println("Depot is At Maximum Level.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private boolean addItemsToTruck(ItemType type, int count) {
        if (truck.hasCapacityFor(type, count)) {
            if (depot.removeAllStorable(type, count)) {
                truck.addAll(type, count);
                System.out.println(count + " of " + type + " was added to truck.\n");
                return true;
            }
            System.err.println("Not Enough Item(s) in Depot.\n");
        }
        System.err.println("Not enough of space in truck.\n");
        return false;
    }

    private boolean addItemsToHelicopter(ItemType type, int amount) {
        if (helicopter.hasCapacityFor(type, amount)) {
            if (coinsCollected >= Constants.getProductBuyCost(type.toString()) * amount) {
                helicopter.addAll(type, amount);
                System.out.println(amount + " of " + type + " was added to helicopter buy list.");
                return true;
            }
            PRINT_NOT_ENOUGH_MONEY();
            return false;
        }
        System.err.println("Not enough of space in helicopter.");
        return false;
    }

    private void PRINT_DEPOT_FULL(String item) {
        System.err.println("can't store " + item + ", Depot is Full.\n");
    }

    private void pickup(int x, int y) {
        boolean depotWasFilled = false;
        Cell cell = map.getCell(x, y);
        for (Item item : cell.getItems().values()) {
            if (!depotWasFilled) {
                if (depot.addStorable(item.getType())) {
                    cell.removeItem(item.getId());
                    System.out.println(item + " was picked up.\n");
                } else {
                    PRINT_DEPOT_FULL(item.toString());
                    depotWasFilled = true;
                }
            } else {
                PRINT_DEPOT_FULL(item.toString());
            }
        }
        if (!depotWasFilled) {
            for (Wild wild : cell.getWilds().values()) {
                if (wild.isCaged()) {
                    if (!depotWasFilled) {
                        if (depot.addStorable(ItemType.valueOf("Caged" + wild.getType().toString()))) {
                            cell.removeAnimal(wild);
                        } else {
                            depotWasFilled = true;
                            PRINT_DEPOT_FULL(wild.toString());
                        }
                    } else {
                        PRINT_DEPOT_FULL(wild.toString());
                    }
                }
            }
        } else {
            for (Wild wild : cell.getWilds().values()) {
                if (wild.isCaged()) {
                    PRINT_DEPOT_FULL("Caged" + wild.toString());
                }
            }
        }
    }
}
