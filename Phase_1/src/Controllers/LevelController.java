package Controllers;

import static Items.Item.ItemType;

import Animals.Animal;
import Animals.Pet.Cat;
import Animals.Pet.Dog;
import Animals.Pet.Pet;
import Animals.Wild.Wild;
import Exceptions.IllegalConstructorArgumentException;
import Interfaces.Processable;
import Items.Item;
import Levels.LevelData;
import Levels.RequirementsListener;
import Levels.SaveData;
import Map.Cell;
import Map.Map;
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
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;


// this Controller is Used For Any LevelData Being Played
public class LevelController extends Controller {

    private transient final String PLANT_REGEX;
    private transient final String CAGE_REGEX;
    private transient final String BUY_REGEX;
    private transient final String PICKUP_REGEX;
    private transient final String WELL_REGEX;
    private transient final String START_WORKSHOP_REGEX;
    private transient final String UPGRADE_REGEX;
    private transient final String PRINT_REGEX;
    private transient final String TURN_REGEX;
    private transient final String SHOW_TRANSPORTATION_TOOL_MENU;

    {
        PLANT_REGEX = "plant\\s+\\d+\\s+\\d+";
        CAGE_REGEX = "cage\\s+\\d+\\s+\\d+";
        BUY_REGEX = "buy\\s+[a-z]+";
        PICKUP_REGEX = "pickup\\s+\\d+\\s+\\d+";
        WELL_REGEX = "well";
        START_WORKSHOP_REGEX = "start\\s+[a-z]+";
        UPGRADE_REGEX = "upgrade\\s+[a-z]+";
        PRINT_REGEX = "print\\s+[a-z]+";
        TURN_REGEX = "turn\\s+[1-9]\\d*";
        SHOW_TRANSPORTATION_TOOL_MENU = "show\\s+((truck)|(helicopter))\\s+menu";
    }

    private transient final LevelData levelData; //don't serialize
    private transient final Random randomGenerator = new Random();//don't serialize
    private transient final Player player;//only Serialize gameElementsLevel, name

    private final IntegerProperty coin;//serialize
    private final Map map;//serialize
    private boolean levelIsFinished = false;//serialize

    private final ObservableMap<Processable, Integer> levelRequirements;//serialize
    private final HashMap<String, Workshop> workshops;//serialize
    private final TransportationTool helicopter;// serialize
    private final TransportationTool truck;//serialize
    private final Well well;//serialize
    private final StringBuilder levelLog;//serialize
    private int timePassed;//serialize
    private final int cageLevel;//serialize
    private final HashMap<String, Byte> gameElementsLevel;
    private ChangeListener<Boolean> vehicleFinishedJob = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (oldValue) {
                TransportationTool vehicle = (TransportationTool) ((BooleanProperty) observable).getBean();
                if (vehicle instanceof Truck) {
                    int gold = ((Truck) vehicle).receiveSoldGoodsMoney();
                    coin.add(gold);
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
                int processingMultiplier = workshop.getProcessingMultiplier();
                Integer[] amounts = workshop.getOutputsAmount();
                Processable[] outputs = workshop.getOutputs();
                Pair<Integer, Integer> dropZone =
                        Constants.getWorkshopDropZone(levelData.getContinent(), workshop.getPosition());

                if (dropZone == null)
                    throw new RuntimeException("Why DropZone Was Null?");

                for (int i = 0; i < amounts.length; ++i) {
                    int j = 0;
                    if (outputs[i] instanceof Animal.AnimalType) {
                        while (j < amounts[i] * processingMultiplier) {
                            Animal animal = getRandomlyLocatedAnimalInstance(outputs[i].toString());
                            if (animal == null)
                                break;
                            animal.setX(dropZone.getKey());
                            animal.setY(dropZone.getValue());
                            map.addAnimal(animal);
                            ++j;
                        }
                    } else if (outputs[i] instanceof Item.ItemType) {
                        while (j < amounts[i] * processingMultiplier) {
                            map.addItem(new Item((Item.ItemType) outputs[i], dropZone.getKey(), dropZone.getValue()));
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
        super();
        Reader reader = new BufferedReader(new FileReader(pathToLevelJsonFile));
        Gson gson = new GsonBuilder().create();
        levelData = gson.fromJson(reader, LevelData.class);
        RequirementsListener requirementsListener;
        levelRequirements = FXCollections.observableHashMap();
        coin = new SimpleIntegerProperty(levelData.getStartMoney());
        for (Processable levelRequirement : levelData.getGoals().keySet())
            levelRequirements.put(levelRequirement, 0);
        if (levelData.getGoals().containsKey(ItemType.Coin)) {
            requirementsListener = new RequirementsListener(
                    this, true);
            coin.addListener(requirementsListener.getCoinChangeListener());
            levelRequirements.computeIfPresent(ItemType.Coin, (k, v) -> levelData.getStartMoney());
        } else
            requirementsListener = new RequirementsListener(
                    this, false);
        Map map;
        Depot depot = new Depot(player.getGameElementLevel("Depot"),
                requirementsListener.getMapChangeListener());
        map = new Map(depot, requirementsListener.getMapChangeListener());
        levelLog = new StringBuilder();

        this.player = player;
        this.gameElementsLevel = new HashMap<>(player.getGameElementsLevel());
        this.map = map;
        this.helicopter = getHelicopterInstance();
        this.truck = getTruckInstance();
        this.well = getWellInstance();
        this.workshops = getWorkshopsInstance();
        this.timePassed = 0;
        cageLevel = gameElementsLevel.get("Cage");

        if (helicopter != null) {
            helicopter.isAtTaskProperty().addListener(vehicleFinishedJob);
        }

        if (truck != null) {
            truck.isAtTaskProperty().addListener(vehicleFinishedJob);
        }
        if (workshops != null)
            for (Workshop workshop : workshops.values())
                workshop.isAtTaskProperty().addListener(workShopFinishedJob);
    }

    public LevelController(SaveData saveData, Player player) throws FileNotFoundException{
        this.gameElementsLevel = saveData.getGameElementsLevel();
        this.coin = new SimpleIntegerProperty(saveData.getCoin());
        this.workshops = new HashMap<>();
        for(Workshop workshop : saveData.getWorkshops())
            workshops.put(workshop.getRealName(), workshop);

        this.helicopter = saveData.getHelicopter();
        this.truck = saveData.getTruck();
        this.player = player;
        this.map = saveData.getMap();
        this.well = saveData.getWell();
        this.levelLog = new StringBuilder(saveData.getLevelLog());
        cageLevel = gameElementsLevel.get("Cage");
        Reader reader = new BufferedReader(new FileReader(saveData.getPathToLevelJsonFile()));
        Gson gson = new GsonBuilder().create();
        levelData = gson.fromJson(reader, LevelData.class);
        levelRequirements = FXCollections.observableMap(saveData.getLevelRequirements());
        RequirementsListener requirementsListener;
        if (levelData.getGoals().containsKey(ItemType.Coin)) {
            requirementsListener = new RequirementsListener(
                    this, true);
            coin.addListener(requirementsListener.getCoinChangeListener());
            levelRequirements.computeIfPresent(ItemType.Coin, (k, v) -> levelData.getStartMoney());
        } else
            requirementsListener = new RequirementsListener(
                    this, false);
        map.getDepot().addListener(requirementsListener.getMapChangeListener());
    }

    public ObservableMap<Processable, Integer> getLevelRequirements() {
        return levelRequirements;
    }

    private Helicopter getHelicopterInstance() {
        if (levelData.getHelicopterStartingLevel() == null)
            return null;
        return new Helicopter(gameElementsLevel.get("Helicopter"),
                levelData.getHelicopterStartingLevel());
    }

    private Truck getTruckInstance() {
        if (levelData.getTruckStartingLevel() == null)
            return null;
        return new Truck(gameElementsLevel.get("Truck"),
                levelData.getTruckStartingLevel());
    }

    private Well getWellInstance() {
        byte level = levelData.getWellStartingLevel();
        byte maxLevel = gameElementsLevel.get("Well");
        Well well = null;
        try {
            well = new Well(maxLevel, level);
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
            int maxLevel = gameElementsLevel.get(workshopName);
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

        while (!(input.equals("exit level"))) {
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
                    if (coin.get() >= cost) {
                        if (animal != null) {
                            map.addAnimal(animal);
                            coin.subtract(cost);
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
                    if (coin.get() >= cost) {
                        if (workshop.upgrade()) {
                            coin.subtract(cost);
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
                    if (!workshops.get(name).startWorking(map.getDepot())) {
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
                        System.out.print(map.getDepot());
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
                timePassed += turns;
                update(turns);
            } else if (input.matches(SHOW_TRANSPORTATION_TOOL_MENU)) {
                String type = input.split("\\s+")[1];
                if (type.equals("helicopter")) {
                    if (helicopter != null) {
                        while (!input.matches("exit helicopter")) {
                            input = scanner.nextLine().trim().toLowerCase();
                        }
                    } else
                        System.err.println("This Level Lacks Helicopter.");
                } else {
                    if (truck != null) {
                        input = scanner.nextLine().trim().toLowerCase();
                        while (!input.matches("exit truck")) {

                            input = scanner.nextLine().trim().toLowerCase();
                        }
                    } else
                        System.err.println("This Level Lacks Truck.");
                }
            } else {
                System.err.println("Invalid Command.");
            }
            if (levelIsFinished)
                return;
            input = scanner.nextLine().trim().toLowerCase();
        }
    }

    private void update(int turns) {
        for (int i = 0; i < turns; ++i) {
            map.update();
            for (Workshop workshop : workshops.values())
                workshop.update(1);
        }
    }

    private void PRINT_INFO() {

    }

    /**
     * @param type type of the animal selected to buy
     */
    private Animal getRandomlyLocatedAnimalInstance(String type) {
        int x = randomGenerator.nextInt(map.cellsWidth);
        int y = randomGenerator.nextInt(map.cellsHeight);
        switch (type) {
            case "cat":
                return new Cat(x, y, gameElementsLevel.get("Cat"));
            case "dog":
                return new Dog(x, y, gameElementsLevel.get("Dog"));
        }

        String animalClassPath = Constants.getAnimalClassPath(type);
        if (animalClassPath == null)
            return null;
        try {
            Class clazz = Class.forName(animalClassPath);
            Constructor constructor;
            if(clazz.isAssignableFrom(Pet.class)){
                constructor = clazz.getDeclaredConstructor(int.class, int.class);
            }
            else{
                constructor = clazz.getDeclaredConstructor(int.class, int.class);
            }
            constructor.setAccessible(true);
            Animal instance = (Animal) constructor.newInstance(x, y);
            constructor.setAccessible(false);
            return instance;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void PRINT_NOT_ENOUGH_MONEY() {
        System.err.println("Not Enough Money.");
    }

    private void well() {
        int cost = well.getRefillPrice();
        if (coin.get() >= cost) {
            if (!well.isRefilling()) {
                if (well.refill()) {
                    System.out.println("Well is Refilling.");
                    coin.subtract(cost);
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
            map.setGrassInCell(x, y, MAX_GRASS_AMOUNT);
            well.decrementWaterLevel();
        } else
            System.err.println("Not Enough Water In Well.\n");
    }

    private void cage(int x, int y) {
        map.cageWilds(x, y, cageLevel * 2);
    }
    //Todo
    private void saveGame(String jsonFileName) {

    }

    private boolean helicopterGo() {
        int cost = helicopter.getItemsInsidePrice();
        if (coin.get() >= cost) {
            if (helicopter.go()) {
                coin.subtract(cost);
                System.out.println("Helicopter is going. will return in "
                        + helicopter.getTurnsRemainedToFinishTask() + " turns.\n");
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
                    truck.getTurnsRemainedToFinishTask() + " turns.\n");
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
        if (coin.get() >= cost) {
            if (well.upgrade()) {
                coin.subtract(cost);
                System.out.println("Well was upgraded to level " + well.getLevel() + "\n");
            } else {
                System.err.println("Well is At Maximum Level.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private void upgradeTruck() {
        int cost = truck.getUpgradeCost();
        if (coin.get() >= cost && cost >= 0) {
            if (truck.upgrade()) {
                coin.subtract(cost);
                System.out.println("Truck was upgraded to level " + truck.getLevel() + "\n");
            } else {
                System.err.println("Truck is At Maximum Level.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private void upgradeHelicopter() {
        int cost = helicopter.getUpgradeCost();
        if (coin.get() >= cost && cost >= 0) {
            if (helicopter.upgrade()) {
                coin.subtract(cost);
                System.out.println("Helicopter was upgraded to level " + helicopter.getLevel() + "\n");
            } else {
                System.err.println("Helicopter is At Maximum Level.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private void upgradeDepot() {
        Depot depot = map.getDepot();
        int cost = depot.getUpgradeCost();
        if (coin.get() >= cost) {
            if (depot.upgrade()) {
                coin.subtract(cost);
                System.out.println("Depot was upgraded to level " + depot.getLevel() + "\n");
            } else {
                System.err.println("Depot is At Maximum Level.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private boolean addItemsToTruck(ItemType type, int count) {
        if (truck.hasCapacityFor(type, count)) {
            if (map.getDepot().removeAllStorable(type, count)) {
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
            if (coin.get() >= Constants.getProductBuyCost(type.toString()) * amount) {
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
                if (map.getDepot().addStorable(item.getType())) {
                    map.removeItem(item);
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
                        if (map.getDepot().addStorable((ItemType.valueOf("Caged" + wild.getType())))) {
                            map.removeAnimal(wild);
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

    public void setAchieved(Processable requirement) {
        // requirement graphics -> setAchieved
        levelRequirements.remove(requirement);
        if (levelRequirements.size() == 0) {
            System.err.println("LEVEL IS FINISHED.");
            byte levelId = levelData.getLevelId();
            LinkedList<Integer> levelTime = player.getLevelTime(levelId);
            player.addMoney(coin.get());
            int playerPrize;
            int levelBestTime = levelTime == null ? Integer.MAX_VALUE : levelTime.get(0);
            if (timePassed > levelBestTime)
                playerPrize = levelData.getPrize();
            else {
                if (timePassed <= levelData.getBronzeTime() && levelBestTime > levelData.getBronzeTime()) {
                    playerPrize = levelData.getBronzePrize();
                    System.out.print("Bronze Prize : ");
                } else if (timePassed <= levelData.getSilverTime() && levelBestTime > levelData.getSilverTime()) {
                    playerPrize = levelData.getSilverPrize();
                    System.out.print("Silver Prize : ");
                } else if (timePassed <= levelData.getGoldenTime() && levelBestTime > levelData.getGoldenTime()) {
                    playerPrize = levelData.getGoldenPrize();
                    System.out.print("Golden Prize : ");
                } else {
                    playerPrize = levelData.getPrize();
                    System.out.print("Prize : ");
                }
            }
            player.addGoldMoney(playerPrize);
            System.out.println(playerPrize + " Gold.");
            levelIsFinished = true;
        }
    }
}
