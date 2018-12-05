package Controllers;

import Animals.Animal;
import Animals.Pet.Cat;
import Animals.Pet.Dog;
import Animals.Pet.Prairie.Cow;
import Animals.Pet.Africa.GuineaFowl;
import Animals.Pet.Africa.Ostrich;
import Animals.Pet.Prairie.Sheep;
import Animals.Wild.Wild;
import Interfaces.Processable;
import Items.Item;
import Levels.LevelData;
import Levels.SaveData;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
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

    {
        PLANT_REGEX = "plant\\s+\\d+\\s+\\d+";
        CAGE_REGEX = "cage\\s+\\d+\\s+\\d+";
        BUY_REGEX = "buy\\s+[a-z]+";
        PICKUP_REGEX = "pickup\\s+\\d+\\s+\\d+";
        WELL_REGEX = "well";
        START_WORKSHOP_REGEX = "start\\s+[a-z][a-z ]*";
        UPGRADE_REGEX = "upgrade\\s+[a-z][a-z ]*";
        PRINT_REGEX = "print\\s+[a-z][a-z ]*";
    }

    private int coinsCollected = 0;
    private final int BUY_PRICE_MULTIPLIER = 2;
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
                    coinsCollected += ((Truck) vehicle).getSoldGoodsMoney();
                } else if (vehicle instanceof Helicopter) {
                    HashMap<String, Integer> items = ((Helicopter) vehicle).getItemsInside();
                    for (String item : items.keySet()) {
                        depot.addAllStorable(item, items.get(item));
                    }
                    helicopter.clear();
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
                Cell mapCell = map.getCell(workshop.getDropZoneX(), workshop.getDropZoneY());
                for(int i = 0; i < amounts.length; ++i){
                    int j = 0;
                    if(outputs[i] instanceof Animal.AnimalType){
                        while (j < amounts[i]*amountProcessed){
                            Animal animal = getRandomlyLocatedAnimalInstance(outputs[i].toString().toLowerCase());
                            if(animal == null)
                                break;
                            animal.setX(workshop.getDropZoneX());
                            animal.setY(workshop.getDropZoneY());
                            mapCell.addAnimal(animal);
                            ++j;
                        }
                    }
                    else if(outputs[i] instanceof Item.ItemType){
                        while (j < amounts[i]*amountProcessed){
                            mapCell.addItem(new Item((Item.ItemType) outputs[i]));
                            ++j;
                        }
                    }
                }
            }
        }
    };

    /**
     * @param pathToLevelJsonFile       path to level json file.
     * @param player needed to determine max level for workshops, saveGame, etc.
     */
    public LevelController(String pathToLevelJsonFile, Player player) throws FileNotFoundException {
        /*
         * map, truck, helicopter, well and workShops are instantiated or set null based on levelJsonFile,
         * and maxLevel of each is set based on playerData
         * */
        Reader reader = new BufferedReader(new FileReader(pathToLevelJsonFile));
        Gson gson = new GsonBuilder().create();
        LevelData levelData = gson.fromJson(reader, LevelData.class);
        this.player = player;
        HashMap<String, Workshop> workshops = null;
        Map map = null;
        Well well = null;
        Helicopter helicopter = null;
        Truck truck = null;
        Depot depot = null;
        initiateLevelComponents(
                workshops,
                map,
                well,
                helicopter,
                truck,
                depot,
                levelData, player);
        this.map = map;
        this.well = well;
        this.truck = truck;
        this.helicopter = helicopter;
        this.workshops = workshops;
        levelLog = new StringBuilder();
        this.depot = depot;
        scanner = new Scanner(System.in);
        cageLevel = 2;
    }

    private void initiateLevelComponents(HashMap<String, Workshop> workshops, Map map, Well well, Helicopter helicopter, Truck truck, Depot depot, LevelData levelData, Player player) {
        if(levelData.getWorkshops().length == 0)
            workshops = null;
        else {
            workshops = new HashMap<>();
            for (String workshopName : levelData.getWorkshops()) {
                int maxLevel = player.getGameElementLevel(workshopName);
                Workshop workshop = null;
                try {
                    workshop = Workshop.getInstance(workshopName, maxLevel);
                }
                catch (FileNotFoundException e){
                    throw new RuntimeException("Workshop instantiation failed.");
                }
                workshops.put(workshopName, workshop);
            }
        }
        map = new Map();

    }

    public LevelController(SaveData saveData, Player player){
        this.player = player;
        map = null;
        well = null;
        truck = new Truck((byte) 5);
        helicopter = null;
        workshops = null;
        levelLog = new StringBuilder();
        depot = new Depot((byte) 5);
        scanner = new Scanner(System.in);
        cageLevel = 2;
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
                int cost = Constants.getProductBuyCost(s)*BUY_PRICE_MULTIPLIER;
                if(cost < 0) {
                    System.err.println("No Such Animal In Existence.");
                }
                else {
                    Animal animal = getRandomlyLocatedAnimalInstance(s);
                    if(coinsCollected >= cost){
                        if(animal != null) {
                            map.addAnimal(animal);
                            coinsCollected -= cost;
                            System.out.println(s + " Was Bought.");
                        }
                        else
                            System.err.println("No Such Animal In Existence.");
                    }
                    else
                        PRINT_NOT_ENOUGH_MONEY();
                }
            }
            else if (input.matches(WELL_REGEX)) {
                well();
            }
            else if (input.matches(UPGRADE_REGEX)) {
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
                    case "warehouse":
                        upgradeWarehouse();
                        break;
                }
                if (workshops.containsKey(name)) {
                    Workshop workshop = workshops.get(name);
                    int cost = workshop.getUpgradeCost();
                    if (coinsCollected >= cost) {
                        if (workshop.upgrade()) {
                            coinsCollected -= cost;
                            System.out.println(name+" was upgraded to level " + workshop.getLevel() + "\n");
                        } else {
                            System.err.println(name+" is At Maximum LevelData.");
                        }
                    } else {
                        System.err.println("Not Enough Money.");
                    }
                }
                else {
                    System.err.println("Invalid Argument For Upgrade Instruction.");
                }
            }
            else if (input.matches(START_WORKSHOP_REGEX)) {
                String name = input.replaceFirst("start\\s+", "");
                if(workshops.containsKey(name)){
                    if(!workshops.get(name).startWorking(depot)){
                        System.err.println("Not Enough Components for "+name+" to work.");
                    }
                }
                else
                    System.err.println("No Such Workshop In Existence.");
            }
            else if(input.matches(PRINT_REGEX)){
                String s = input.replaceFirst("print\\s+", "");
                switch (s){
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
                        for(Workshop workshop: workshops.values())
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
            }
            else {
                System.err.println("Invalid Command.");
            }
            input = scanner.nextLine().trim().toLowerCase();
        }
    }

    private void PRINT_INFO() {

    }

    public void detectDestructiveCollisions() {
        for(Cell[] cell1 : map.getCells()) {
            for (Cell cell : cell1) {
                for(Wild wild : cell.getWilds().values()){
                    if(wild.isCaged())
                        continue;
                    for(Item item : cell.getItems().values()) {
                        cell.removeItem(item.getId());
                    }
                    for(Animal animal : cell.getPets().values()){
                        wild.destroy(animal);
                        if(animal instanceof Dog){
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
        switch (type) {
            case "cat":
                return new Cat(randomGenerator.nextInt(map.cellsWidth),
                            randomGenerator.nextInt(map.cellsHeight), player.getGameElementLevel("Cat"));
            case "dog":
                    return new Dog(randomGenerator.nextInt(map.cellsWidth),
                            randomGenerator.nextInt(map.cellsHeight));
            case "cow":
                    return new Cow(randomGenerator.nextInt(map.cellsWidth),
                            randomGenerator.nextInt(map.cellsHeight));
            case "sheep":
                    return new Sheep(randomGenerator.nextInt(map.cellsWidth),
                            randomGenerator.nextInt(map.cellsHeight));
            case "guineafowl":
                    return new GuineaFowl(randomGenerator.nextInt(map.cellsWidth),
                            randomGenerator.nextInt(map.cellsHeight));
            case "ostrich":
                    return new Ostrich(randomGenerator.nextInt(map.cellsWidth),
                            randomGenerator.nextInt(map.cellsHeight));
            default:
                return null;
        }
    }

    private void PRINT_NOT_ENOUGH_MONEY(){
        System.err.println("Not Enough Money.");
    }

    private void well() {
        if(coinsCollected >= well.getRefillPrice()){
            if(!well.isRefilling()) {
                if (well.refill()) {
                    System.out.println("Well is Refilling.");
                } else
                    System.err.println("Well is Full.");
            }
            else
                System.err.println("Well is Already Refilling.");
        }
        else{
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
        if (coinsCollected >= helicopter.getItemsInsidePrices()) {
            if (helicopter.go()) {
                coinsCollected -= helicopter.getItemsInsidePrices();
                System.out.println("Helicopter is going. will return in "
                        +helicopter.getTimeRemainedToFinishTask()+" turns.\n");
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
            levelLog.append("Truck is going. will return in ").
                    append(truck.getTimeRemainedToFinishTask()).append(" turns.\n");
            return true;
        }
        levelLog.append("Nothing in Truck.\n");
        return false;
    }

    private void clearHelicopter() {
        helicopter.clear();
    }

    private void clearTruck() {
        truck.clear();
    }

    private void upgradeWell(){
        int cost = well.getUpgradePrice();
        if(coinsCollected >= cost) {
            if (well.upgrade()) {
                coinsCollected -= cost;
                System.out.println("Well was upgraded to level " + well.getLevel() + "\n");
            } else {
                System.err.println("Well is At Maximum LevelData.");
            }
        }
        else
            PRINT_NOT_ENOUGH_MONEY();
    }
    private void upgradeTruck(){
        int cost = truck.getUpgradeCost();
        if(coinsCollected >= cost){
            if(truck.upgrade()){
                coinsCollected -= cost;
                System.out.println("Truck was upgraded to level " + truck.getLevel() + "\n");
            }
            else {
                System.err.println("Truck is At Maximum LevelData.");
            }
        }
        else
            PRINT_NOT_ENOUGH_MONEY();
    }
    private void upgradeHelicopter(){
        int cost = helicopter.getUpgradeCost();
        if(coinsCollected >= cost){
            if(helicopter.upgrade()){
                coinsCollected -= cost;
                System.out.println("Helicopter was upgraded to level " + helicopter.getLevel() + "\n");
            }
            else {
                System.err.println("Helicopter is At Maximum LevelData.");
            }
        }
        else
            PRINT_NOT_ENOUGH_MONEY();

    }
    private void upgradeWarehouse() {
        int cost = depot.getUpgradeCost();
        if(coinsCollected >= cost){
            if(depot.upgrade()){
                coinsCollected -= cost;
                System.out.println("Warehouse was upgraded to level " + depot.getLevel() + "\n");
            }
            else {
                System.err.println("Warehouse is At Maximum LevelData.");
            }
        }
        else
            PRINT_NOT_ENOUGH_MONEY();

    }

    private boolean addItemsToTruck(String type, int count) {
        if (truck.hasCapacityFor(type, count)) {
            if (depot.removeAllStorable(type, count)) {
                truck.addAll(type, count);
                System.out.println(count+" of "+type+" was added to truck.\n");
                return true;
            }
            System.err.println("Not Enough Item(s) in Depot.\n");
        }
        System.err.println("Not enough of space in truck.\n");
        return false;
    }

    private boolean addItemsToHelicopter(String type, int amount) {
        if (helicopter.hasCapacityFor(type, amount)) {
            if (coinsCollected >= Constants.getProductBuyCost(type) * amount * BUY_PRICE_MULTIPLIER) {
                helicopter.addAll(type, amount);
                System.out.println(amount+" of "+type+" was added to helicopter buy list.");
                return true;
            }
            PRINT_NOT_ENOUGH_MONEY();
            return false;
        }
        System.err.println("Not enough of space in helicopter.");
        return false;
    }

    private void pickup(int x, int y) {
        boolean depotWasFilled = false;
        Cell cell = map.getCell(x, y);
            for (Item item : cell.getItems().values()) {
                if (!depotWasFilled) {
                    if (depot.addStorable(item.getType().toString())) {
                        cell.removeItem(item.getId());
                        System.out.println(item+" was picked up.\n");
                    } else {
                        System.err.println("can't pick "+item+", Depot is Full.\n");
                        depotWasFilled = true;
                    }
                }
                else {
                    System.err.println("can't pick "+item+", Depot is Full.\n");
                }
            }
            if(!depotWasFilled) {
                for (Wild wild : cell.getWilds().values()) {
                    if(wild.isCaged()){
                        if (!depotWasFilled) {
                            if (depot.addStorable(wild.getType().toString())) {
                                cell.removeAnimal(wild);
                            }
                            else {
                                depotWasFilled = true;
                                System.err.println("can't store " + wild.getType() + ", Depot is Full.");
                            }
                        } else {
                            System.err.println("can't store " + wild.getType() + ", Depot is Full.");
                        }
                    }
                }
            }
            else{
                for (Wild wild : cell.getWilds().values()) {
                    if(wild.isCaged()){
                        System.err.println("can't store "+wild.getType()+", Depot is Full.");
                    }
                }
            }
    }
}
