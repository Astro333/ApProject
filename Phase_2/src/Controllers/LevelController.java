package Controllers;

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
import Map.GraphicalCell;
import Map.Map;
import Map.MapGraphics;
import Player.Player;
import Structures.Depot;
import Structures.Well;
import Structures.Workshop;
import Transportation.Helicopter;
import Transportation.TransportationTool;
import Transportation.Truck;
import Utilities.Constants;
import Utilities.Pair;
import Utilities.ProcessableDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import static Items.Item.ItemType;

// this Controller is Used For Any LevelData Being Played
public class LevelController extends Controller {

    private transient final String PLANT_REGEX;
    private transient final String CAGE_REGEX;
    private transient final String BUY_REGEX;
    private transient final String PICKUP_REGEX;
    private transient final String WELL_REGEX;
    private transient final String START_WORKSHOP_REGEX;
    private transient final String UPGRADE_REGEX;
    private transient final String HELICOPTER_GO_REGEX;
    private transient final String TRUCK_GO_REGEX;
    private transient final String VEHICLE_ADD_REGEX;
    private transient final String PRINT_REGEX;
    private transient final String TURN_REGEX;
    private transient final String SAVE_REGEX;
    private transient final String SHOW_TRANSPORTATION_TOOL_MENU;

    {
        PLANT_REGEX = "(?i:plant\\s+\\d+\\s+\\d+)";
        CAGE_REGEX = "(?i:cage\\s+\\d+\\s+\\d+)";
        BUY_REGEX = "(?i:buy\\s+[a-z]+)";
        PICKUP_REGEX = "(?i:pickup\\s+\\d+\\s+\\d+)";
        WELL_REGEX = "(?i:well)";
        HELICOPTER_GO_REGEX = "(?i:helicopter\\s+go)";
        TRUCK_GO_REGEX = "(?i:truck\\s+go)";
        VEHICLE_ADD_REGEX = "(?i:((helicopter)|(truck))\\s+add\\s+[a-zA-Z]+\\s+\\d+)";
        START_WORKSHOP_REGEX = "(?i:start\\s+[a-z]+)";
        UPGRADE_REGEX = "(?i:upgrade\\s+[a-z]+)";
        PRINT_REGEX = "(?i:print\\s+[a-z]+)";
        TURN_REGEX = "(?i:turn\\s+[1-9]\\d*)";
        SHOW_TRANSPORTATION_TOOL_MENU = "(?i:show\\s+((truck)|(helicopter))\\s+menu)";
        SAVE_REGEX = "(?i:save\\s+game\\s+[0-9a-zA-Z\\-_]+.json)";
        String s = "s";
        s.matches(SAVE_REGEX);
    }

    private transient final String PLAYER_ADD_REGEX;
    private transient final String FILL_WELL_IMMEDIATELY;
    private transient final String ADD_ELEMENT_TO_REGEX;
    private transient final String ADD_ELEMENT_REGEX;

    {
        PLAYER_ADD_REGEX = "(?i:player.add\\s+[a-zA-Z]+\\s+\\d+)";
        FILL_WELL_IMMEDIATELY = "(?i:fill\\s+well\\s+immediately)";
        ADD_ELEMENT_TO_REGEX = "(?i:add\\s+[a-zA-Z]+\\s+\\d+\\s+to\\s+\\d+\\s+\\d+)";
        ADD_ELEMENT_REGEX = "(?i:add\\s+[a-zA-Z]+\\s+\\d+)";
        String s = "s";
        s.matches(ADD_ELEMENT_REGEX);
    }

    public transient final LevelData levelData; //don't serialize
    private transient final Random randomGenerator = new Random();//don't serialize
    private transient final Player player;//only Serialize levelGameElementsLevel, name

    private final IntegerProperty coin;//serialize
    private final Map map;//serialize
    private boolean levelIsFinished = false;//serialize

    private boolean cheatsEnabled = false;

    private final ObservableMap<Processable, Integer> levelRequirements;//serialize
    private final HashMap<String, Workshop> workshops;//serialize
    private final TransportationTool helicopter;// serialize
    private final TransportationTool truck;//serialize
    private final Well well;//serialize
    private final StringBuilder levelLog;//serialize
    private int timePassed;//serialize
    private final int cageLevel;//serialize
    private int timeRemainedToSpawnWild = 3;//serialize
    // this one is set when player first starts game
    private final HashMap<String, Byte> levelGameElementsLevel;

    private transient ChangeListener<Boolean> vehicleFinishedJob = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            if (oldValue) {
                TransportationTool vehicle = (TransportationTool) ((BooleanProperty) observable).getBean();
                if (vehicle instanceof Truck) {
                    int gold = ((Truck) vehicle).receiveSoldGoodsMoney();
                    coin.set(coin.get() + gold);
                    System.out.println("Truck brought " + gold + " coin.");
                } else if (vehicle instanceof Helicopter) {
                    System.out.println("Helicopter Dropping items:");
                    ((Helicopter) helicopter).dropItemsRandomly(map);
                }
            }
        }
    };
    private transient ChangeListener<Boolean> workShopFinishedJob = new ChangeListener<>() {
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
                            System.out.println(workshop.getDemonstrativeName() + " Produced " + animal.getType() +
                                    " At (" + dropZone.getKey() + ", " + dropZone.getValue() + ")");
                            ++j;
                        }
                    } else if (outputs[i] instanceof Item.ItemType) {
                        while (j < amounts[i] * processingMultiplier) {
                            map.addItem(new Item((Item.ItemType) outputs[i], dropZone.getKey(), dropZone.getValue()));
                            System.out.println(workshop.getDemonstrativeName() + " Produced " + outputs[i] +
                                    " At (" + dropZone.getKey() + ", " + dropZone.getValue() + ")");
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
        Gson gson = new GsonBuilder().registerTypeAdapter(Processable.class, new ProcessableDeserializer()).create();
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

        Depot depot = new Depot(player.getGameElementLevel("Depot"), requirementsListener.getMapChangeListener());
        Map map = new Map(depot, 10, 10, requirementsListener.getMapChangeListener());

        levelLog = new StringBuilder();
        this.player = player;
        this.levelGameElementsLevel = new HashMap<>(player.getGameElementsLevel());
        this.map = map;
        this.helicopter = getHelicopterInstance();
        this.truck = getTruckInstance();
        this.well = getWellInstance();
        this.workshops = getWorkshopsInstance();
        this.timePassed = 0;
        cageLevel = levelGameElementsLevel.get("Cage");

        if (helicopter != null) {
            helicopter.isAtTaskProperty().addListener(vehicleFinishedJob);
        }

        if (truck != null) {
            truck.isAtTaskProperty().addListener(vehicleFinishedJob);
        }
        if (workshops != null) {
            for (Workshop workshop : workshops.values()) {
                workshop.isAtTaskProperty().addListener(workShopFinishedJob);
            }
        }
        for (int i = 0; i < map.cellsWidth; ++i) {
            for (int j = 0; j < map.cellsHeight; ++j) {
                synchronized (MapGraphics.latch) {
                    map.getGraphics().getMapCells()[i][j].setOnMouseClicked(event -> {
                        GraphicalCell src = (GraphicalCell) event.getSource();
                        switch (event.getButton()) {
                            case PRIMARY:
                                plant(src.getPos_X(), src.getPos_Y());
                                break;
                            case SECONDARY:
                                System.out.println(map.getCell(src.getPos_X(), src.getPos_Y()));
                                break;
                        }
                    });
                }
            }
        }
    }

    public LevelController(SaveData saveData, Player player) throws FileNotFoundException {
        this.player = player;

        this.levelGameElementsLevel = saveData.getGameElementsLevel();
        this.coin = new SimpleIntegerProperty(saveData.getCoin());
        this.workshops = new HashMap<>();
        for (Workshop workshop : saveData.getWorkshops()) {
            workshops.put(workshop.getRealName(), workshop);
            workshop.isAtTaskProperty().set(workshop.getTimeToFinishTask() >= 0);
            workshop.isAtTaskProperty().addListener(workShopFinishedJob);
        }
        if (saveData.getHelicopter() != null) {
            this.helicopter = new Helicopter(saveData);
            this.helicopter.isAtTaskProperty().addListener(vehicleFinishedJob);
        } else
            this.helicopter = null;
        if (saveData.getTruck() != null) {
            this.truck = new Truck(saveData);
            this.truck.isAtTaskProperty().addListener(vehicleFinishedJob);
        } else
            this.truck = null;


        this.well = saveData.getWell();
        this.timePassed = saveData.getTimePassed();
        this.timeRemainedToSpawnWild = saveData.getTimeRemainedToSpawnWild();
        this.levelLog = new StringBuilder(saveData.getLevelLog());
        cageLevel = levelGameElementsLevel.get("Cage");
        Reader reader = new BufferedReader(new FileReader(saveData.getPathToLevelJsonFile()));
        Gson gson = new GsonBuilder().registerTypeAdapter(Processable.class, new ProcessableDeserializer()).create();
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
        this.map = new Map(saveData, requirementsListener.getMapChangeListener());

        for (int i = 0; i < map.cellsWidth; ++i) {
            for (int j = 0; j < map.cellsHeight; ++j) {
                synchronized (MapGraphics.latch) {
                    map.getGraphics().getMapCells()[i][j].setOnMouseClicked(event -> {
                        GraphicalCell src = (GraphicalCell) event.getSource();
                        switch (event.getButton()) {
                            case PRIMARY:
                                plant(src.getPos_X(), src.getPos_Y());
                                break;
                            case SECONDARY:
                                System.out.println(map.getCell(src.getPos_X(), src.getPos_Y()));
                                break;
                        }
                    });
                }
            }
        }
    }

    public ObservableMap<Processable, Integer> getLevelRequirements() {
        return levelRequirements;
    }

    private Helicopter getHelicopterInstance() {
        if (levelData.getHelicopterLevel() == null)
            return null;
        return new Helicopter(levelGameElementsLevel.get("Helicopter"),
                levelData.getHelicopterLevel());
    }

    private Truck getTruckInstance() {
        if (levelData.getTruckLevel() == null)
            return null;
        return new Truck(levelGameElementsLevel.get("Truck"),
                levelData.getTruckLevel());
    }

    private Well getWellInstance() {
        byte level = levelData.getWellLevel();
        byte maxLevel = levelGameElementsLevel.get("Well");
        Well well = null;
        try {
            well = new Well(maxLevel, level);
        } catch (IllegalConstructorArgumentException e) {
            e.printStackTrace();
        }
        return well;
    }

    private HashMap<String, Workshop> getWorkshopsInstance() {
        if (levelData.getWorkshops().length == 0) {
            return null;
        }
        HashMap<String, Workshop> workshops = new HashMap<>();
        String[] ws = levelData.getWorkshops();
        for (int i = 0; i < ws.length; ++i) {
            String workshopName = ws[i];
            int maxLevel = levelGameElementsLevel.get(workshopName);
            Workshop workshop;
            try {
                workshop = Workshop.getInstance(workshopName, maxLevel,
                        levelData.getWorkshopsPosition()[i], levelData.getContinent());
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Workshop instantiation failed.");
            }
            workshops.put(workshopName, workshop);
            workshop.isAtTaskProperty().addListener(workShopFinishedJob);
        }
        return workshops;
    }

    public void startProcessing() {
        String input = scanner.nextLine().trim();

        while (!(input.toLowerCase().equals("exit level"))) {
            if (input.matches(PLANT_REGEX)) {
                String[] s = input.replaceFirst("(?i:plant\\s+)", "").
                        split("\\s+");
                plant(Integer.parseInt(s[0]), Integer.parseInt(s[1]));

            } else if (input.matches(CAGE_REGEX)) {

                String[] s = input.replaceFirst("(?:cage\\s+)", "").
                        split("\\s+");
                cage(Integer.parseInt(s[0]), Integer.parseInt(s[1]));

            } else if (input.matches(PICKUP_REGEX)) {
                String[] s = input.replaceFirst("(?i:pickup\\s+)", "").
                        split("\\s+");
                pickup(Integer.parseInt(s[0]), Integer.parseInt(s[1]));

            } else if (input.matches(BUY_REGEX)) {
                String s = input.replaceFirst("(?i:buy\\s+)", "");
                int cost = Constants.getAnimalBuyCost(Animal.AnimalType.getType(s));
                if (cost < 0) {
                    System.err.println("No Such Animal In Existence.");
                } else {
                    Animal animal = getRandomlyLocatedAnimalInstance(s);
                    if (coin.get() >= cost) {
                        if (animal != null) {
                            map.addAnimal(animal);
                            coin.set(coin.get() - cost);
                            System.out.println(s + " Was Bought.");
                        } else
                            System.err.println("No Such Animal In Existence.");
                    } else
                        PRINT_NOT_ENOUGH_MONEY();
                }
            } else if (input.matches(WELL_REGEX)) {
                well();
            } else if (input.matches(UPGRADE_REGEX)) {
                String name = input.replaceFirst("(?i:upgrade\\s+)", "");
                switch (name.toLowerCase()) {
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
                            coin.set(coin.get() - cost);
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
                    if (workshops.get(name).startWorking(map.getDepot())) {
                        System.out.println(name + " is working.");
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
                    case "pets":
                        for (Animal animal : map.getPets())
                            System.out.println(animal);
                        break;

                    case "animals":
                        System.out.println("Pets:");
                        if (map.getPets().size() > 0) {
                            for (Animal animal : map.getPets()) {
                                System.out.println("\t" + animal);
                            }
                        } else
                            System.out.println("\tNone.");

                        System.out.println("Wilds:");
                        if (map.getWilds().size() > 0) {
                            for (Wild wild : map.getWilds()) {
                                System.out.println("\t" + wild);
                            }
                        } else
                            System.out.println("\tNone.");
                        break;
                    case "items":
                        System.out.println("Items:");
                        if (map.getItems().size() > 0) {
                            for (Item item : map.getItems()) {
                                System.out.println("\t" + item);
                            }
                        } else
                            System.out.println("\tNone.");
                        break;
                    case "depot":
                        System.out.println(map.getDepot());
                        break;
                    case "well":
                        System.out.println(well);
                        break;
                    case "workshops":
                        for (Workshop workshop : workshops.values())
                            System.out.println(workshop);
                        break;
                    case "truck":
                        System.out.println(truck);
                        break;
                    case "helicopter":
                        System.out.println(helicopter);
                        break;
                    case "money":
                        System.out.println("Money : " + coin.get());
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
                        input = scanner.nextLine().trim();
                        boolean setToGo = false;
                        while (!input.matches("exit helicopter")) {
                            if (input.matches("(?i:add\\s+[a-zA-Z]+\\s+\\d+)")) {
                                String[] s = input.split("\\s+");
                                int amount = Integer.parseInt(s[2]);
                                ItemType itemType = ItemType.getType(s[1]);
                                if (itemType == null || itemType == ItemType.Coin) {
                                    System.err.println("Invalid element type.");
                                } else {
                                    addItemsToHelicopter(itemType, amount);
                                }
                            } else if (input.matches("(?i:print\\s+items)")) {
                                helicopter.printElements();
                            } else if (input.matches("(?i:print)")) {
                                System.out.println(helicopter);
                            } else if (input.matches(HELICOPTER_GO_REGEX)) {
                                if (helicopterGo()) {
                                    setToGo = true;
                                    break;
                                }
                            } else if (input.toLowerCase().equals("help")) {
                                System.out.println("Commands:");
                                System.out.println("\t\"print items\"");
                                System.out.println("\t\"print\"");
                                System.out.println("\t\"add [Item_Type] [count]\"");
                                System.out.println("\t\"exit helicopter\"");
                            } else
                                System.err.println("Invalid Input");
                            input = scanner.nextLine().trim();
                        }
                        if (!setToGo)
                            clearHelicopter();
                    } else
                        System.err.println("This Level Lacks Helicopter.");
                } else {
                    if (truck != null) {
                        boolean setToGo = false;
                        input = scanner.nextLine().trim();
                        while (!input.matches("exit truck")) {
                            if (input.matches("(?i:add\\s+[a-zA-Z]+\\s+\\d+)")) {
                                String[] s = input.split("\\s+");
                                int amount = Integer.parseInt(s[2]);
                                Processable elementType = ItemType.getType(s[1]);
                                if (elementType == null)
                                    elementType = Animal.AnimalType.getType(s[1]);
                                if (elementType == null || elementType == ItemType.Coin) {
                                    System.err.println("Invalid element type.");
                                } else {
                                    if (elementType instanceof ItemType)
                                        addElementsToTruck((ItemType) elementType, amount);
                                    else
                                        addElementsToTruck((Animal.AnimalType) elementType, amount);
                                }
                            } else if (input.matches("(?i:print\\s+elements)")) {
                                truck.printElements();
                            } else if (input.matches("(?i:print)")) {
                                System.out.println(truck);
                            } else if (input.matches(TRUCK_GO_REGEX)) {
                                if (truckGo()) {
                                    setToGo = true;
                                    break;
                                }
                            } else if (input.toLowerCase().equals("help")) {
                                System.out.println("Commands:");
                                System.out.println("\t\"print elements\"");
                                System.out.println("\t\"print\"");
                                System.out.println("\t\"add [Item_Type|Animal_Type] [count]\"");
                                System.out.println("\t\"exit truck\"");
                            } else
                                System.err.println("Invalid Input");
                            input = scanner.nextLine().trim();
                        }
                        if (!setToGo)
                            clearTruck();
                    } else
                        System.err.println("This Level Lacks Truck.");
                }
            } else if (input.toLowerCase().equals("help")) {
                PRINT_HELP();
            } else if (input.toLowerCase().equals("enable cheats")) {
                cheatsEnabled = true;
            } else if (input.toLowerCase().equals("disable cheats")) {
                cheatsEnabled = false;
            } else if (input.matches(SAVE_REGEX)) {
                saveGame(input.split("\\s+")[2]);
            } else if (cheatsEnabled) {
                handleCheatCommands(input);
            } else {
                System.err.println("Invalid Command.");
            }
            if (levelIsFinished)
                return;
            input = scanner.nextLine().trim();
        }
        map.terminateGraphics();
    }

    private void saveGame(String jsonFileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SaveData data = new SaveData(map.cellsWidth, map.cellsHeight);
        data.setWell(well);
        map.fillSaveData(data);
        data.setCoin(coin.get());
        data.setHelicopter((Helicopter) helicopter);
        data.setTruck((Truck) truck);
        data.setLevelLog(levelLog.toString());
        data.setTimePassed(timePassed);
        data.setTimeRemainedToSpawnWild(timeRemainedToSpawnWild);
        data.setWorkshops(new LinkedList<>(workshops.values()));
        data.setGameElementsLevel(levelGameElementsLevel);
        data.setPathToLevelJsonFile("Phase_1/DefaultGameData/LevelsInfo/level_" + levelData.getLevelId() + ".json");
        data.setLevelRequirements(new HashMap<>(levelRequirements));
        try {
            Writer writer = new BufferedWriter(new FileWriter("Phase_1/PlayersData/" + player.getName() + "/Player_Unfinished_Levels_Saves/" + jsonFileName));
            gson.toJson(data, writer);
            writer.flush();
            writer.close();
            System.out.println("Saved Game Successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCheatCommands(String input) {
        if (input.matches(FILL_WELL_IMMEDIATELY)) {
            well.refill();
            well.update(well.getTimeRemainedToRefill() + 1);
        } else if (input.matches(ADD_ELEMENT_TO_REGEX)) {
            String[] s = input.split("\\s+");
            int amount = Integer.parseInt(s[2]);
            int x = Integer.parseInt(s[4]);
            int y = Integer.parseInt(s[5]);

            if (amount == 0) {
                return;
            }
            if (x >= map.cellsWidth || y >= map.cellsHeight) {
                System.err.println("index out of Map Exception.");
                return;
            }

            if (ItemType.getType(s[1]) != null) {
                ItemType type = ItemType.getType(s[1]);
                if (type == ItemType.Coin) {
                    System.err.println("No Such Element In Existence.");
                } else {
                    int i = 0;
                    while (i < amount) {
                        if (type.IS_ANIMAL) {
                            if (type.toString().startsWith("Caged")) {
                                Wild wild = (Wild) getRandomlyLocatedAnimalInstance(type.toString().replaceFirst("Caged", ""));
                                wild.setX(x);
                                wild.setY(y);
                                wild.setCaged(2 * cageLevel + 6);
                                map.addAnimal(wild);
                            } else {
                                map.addItem(new Item(type, x, y));
                            }
                        } else {
                            map.addItem(new Item(ItemType.getType(s[1]), x, y));
                        }
                        System.out.println(s[1] + " was added to (" + x + ", " + y + ")");
                        ++i;
                    }
                }
            } else if (Animal.AnimalType.getType(s[1]) != null) {
                int i = 0;
                while (i < amount) {
                    Animal animal = getRandomlyLocatedAnimalInstance(s[1]);
                    animal.setX(x);
                    animal.setY(y);
                    map.addAnimal(animal);
                    ++i;
                }
                System.out.println(s[1] + " was added to (" + x + ", " + y + ")");
            } else
                System.err.println("No Such Element In Existence.");
        } else if (input.matches(ADD_ELEMENT_REGEX)) {
            String[] s = input.split("\\s+");
            int amount = Integer.parseInt(s[2]);
            ItemType type = ItemType.getType(s[1]);
            if (type != null) {
                if (s[1].equals("Coin")) {
                    coin.set(coin.get() + amount);
                    System.out.println(amount + " of Coin Added.");
                } else {
                    if(map.getDepot().addAllStorable(type, amount)) {
                        System.out.println(amount + " of " + type + " was added to depot.");
                    } else
                        PRINT_DEPOT_FULL(type.toString());
                }
            }
        } else if (input.matches(PLAYER_ADD_REGEX)) {
            String s[] = input.split("\\s+");
            int amount = Integer.parseInt(s[2]);
            if (s[0].toLowerCase().equals("money")) {
                player.addMoney(amount);
            } else if (s[0].toLowerCase().equals("goldMoney")) {
                player.addGoldMoney(amount);
            } else
                System.err.println("No Such Element In Existence");
        } else
            System.err.println("Invalid Command");
    }

    private void PRINT_HELP() {
        String s = "Commands:\n" +
                "\t\"exit level\" : exit current level into player menu\n" +
                "\t\"[helicopter|truck] add [item_name] [count]\"\n" +
                "\t\"[helicopter|truck] clear\"\n" +
                "\t\"[helicopter|truck] go\"\n" +
                "\t\"cage [x y]\"\n" +
                "\t\"plant [x y]\"\n" +
                "\t\"pickup [x y]\"\n" +
                "\t\"buy [Animal_Name]\"\n" +
                "\t\"save game [json_file_Name.json]\"\n" +
                "\t\"upgrade [workshop_name|well|truck|helicopter|depot]\"\n" +
                "\t\"print [info|map|depot|well|items|workshops|truck|helicopter|money]\"\n" +
                "\t\"turn [n]\": turns game forward(not recommended for n -> Infinity)\n" +
                "";
        System.out.println(s);
    }

    private void update(int turns) {
        for (int i = 0; i < turns; ++i) {
            --timeRemainedToSpawnWild;
            if (timeRemainedToSpawnWild <= 0) {
                Wild wild1 = (Wild) getRandomlyLocatedAnimalInstance(Animal.AnimalType.BrownBear.toString());
                Wild wild2 = (Wild) getRandomlyLocatedAnimalInstance(Animal.AnimalType.Lion.toString());
                map.addAnimal(wild1);
                map.addAnimal(wild2);
                System.err.println(wild1.getType() + " was dropped on Map.");
                System.err.println(wild2.getType() + " was dropped on Map.");
                timeRemainedToSpawnWild = 10;
            }
            map.update();
            well.update(1);
            if (helicopter != null)
                helicopter.update(1);
            if (truck != null)
                truck.update(1);
            for (Workshop workshop : workshops.values())
                workshop.update(1);
        }
    }

    private void PRINT_INFO() {
        StringBuilder sb = new StringBuilder("Info:\n" +
                "\tMoney : " + coin.get() + "\n" +
                "\tTimePassed : " + timePassed + "\n" +
                "\tLevel Requirements:\n");
        HashMap<Processable, Integer> goals = levelData.getGoals();
        for (Processable processable : goals.keySet()) {
            sb.append("\t\t").append(processable).append(" : ").append(goals.get(processable));
            if (levelRequirements.containsKey(processable)) {
                sb.append(", Amount Accomplished : ").append(levelRequirements.get(processable));
            } else
                sb.append(", Goal Accomplished.");
            sb.append("\n");
        }
        System.out.print(sb.toString());
    }

    /**
     * @param type type of the animal selected to create
     */
    private Animal getRandomlyLocatedAnimalInstance(String type) {
        int x = randomGenerator.nextInt(map.cellsWidth);
        int y = randomGenerator.nextInt(map.cellsHeight);
        switch (type.toLowerCase()) {
            case "cat":
                return new Cat(x, y, levelGameElementsLevel.get("Cat"));
            case "dog":
                return new Dog(x, y);
        }

        String animalClassPath = Constants.getAnimalClassPath(Animal.AnimalType.getType(type));
        if (animalClassPath == null)
            return null;
        try {
            Class clazz = Class.forName(animalClassPath);
            Constructor constructor;
            if (clazz.isAssignableFrom(Pet.class)) {
                constructor = clazz.getDeclaredConstructor(int.class, int.class);
            } else {
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
                    coin.set(coin.get() - cost);
                } else
                    System.err.println("Well is Full.");
            } else
                System.err.println("Well is Already Refilling.");
        } else {
            PRINT_NOT_ENOUGH_MONEY();
        }
    }

    private synchronized void plant(int x, int y) {
        if (well.getStoredWater() > 0) {
            byte MAX_GRASS_AMOUNT = 10;
            map.setGrassInCell(x, y, MAX_GRASS_AMOUNT);
            well.decrementWaterLevel();
        } else
            System.err.println("Not Enough Water In Well.");
    }

    private void cage(int x, int y) {
        map.cageWilds(x, y, cageLevel * 2 + 6);
    }
    //Todo

    private boolean helicopterGo() {
        int cost = helicopter.getItemsInsidePrice();
        if (coin.get() >= cost) {
            if (helicopter.go()) {
                coin.set(coin.get() - cost);
                System.out.println("Helicopter is going. will return in "
                        + helicopter.getTurnsRemainedToFinishTask() + " turns.");
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

            for (Object o : truck.getItemsInside().keySet()) {
                Processable element = (Processable) o;
                int count = truck.getItemsInside().get(element);
                if (element instanceof Animal.AnimalType) {
                    Iterator<Animal> it = map.getPets().iterator();
                    Animal.AnimalType type = (Animal.AnimalType) element;
                    int i = 0;
                    while (it.hasNext() && i < count) {
                        Animal animal = it.next();
                        if (animal.getType() == type) {
                            it.remove();
                            map.removeAnimal(animal);
                            ++i;
                        }
                    }
                } else {
                    ItemType type = (ItemType) element;
                    map.getDepot().removeAllStorable(type, count);
                }
            }
            System.out.println("Truck is going. will return in " +
                    truck.getTurnsRemainedToFinishTask() + " turns.");
            return true;
        }
        System.err.println("Nothing in Truck.");
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
            System.err.println();
            if (well.upgrade()) {
                coin.set(coin.get() - cost);
                System.out.println("Well was upgraded to level " + well.getLevel());
            } else {
                System.err.println("Well is At Maximum Level.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private void upgradeTruck() {
        int cost = truck.getUpgradeCost();
        if (coin.get() >= cost) {
            if (truck.upgrade()) {
                coin.set(coin.get() - cost);
                System.out.println("Truck was upgraded to level " + truck.getLevel());
            } else {
                System.err.println("Truck is At Maximum Level.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private void upgradeHelicopter() {
        int cost = helicopter.getUpgradeCost();
        if (coin.get() >= cost) {
            if (helicopter.upgrade()) {
                coin.set(coin.get() - cost);
                System.out.println("Helicopter was upgraded to level " + helicopter.getLevel());
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
                coin.set(coin.get() - cost);
                System.out.println("Depot was upgraded to level " + depot.getLevel());
            } else {
                System.err.println("Depot is At Maximum Level.");
            }
        } else
            PRINT_NOT_ENOUGH_MONEY();
    }

    private boolean addElementsToTruck(ItemType type, int count) {
        if (truck.hasCapacityFor(type, count)) {
            if (map.getDepot().hasAll(type, count)) {
                truck.addAll(type, count);
                System.out.println(count + " of " + type + " was added to truck.");
                return true;
            }
            System.err.println("Not Enough Item(s) in Depot.");
            return false;
        }
        System.err.println("Not enough of space in truck.");
        return false;
    }

    private boolean addElementsToTruck(Animal.AnimalType type, int count) {
        if (type.IS_WILD) {
            System.err.println("Can't Sell Wild Animals!");
            return false;
        }
        if (truck.hasCapacityFor(type, count)) {
            if (map.getAnimalsAmount().containsKey(type)) {
                if (map.getAnimalsAmount().get(type) >= count) {
                    ((Truck) truck).addAll(type, count);
                    System.out.println(count + " of " + type + " was added to truck.");
                    return true;
                }
            }
            System.err.println("Not Enough Animals in Map.");
            return false;
        }
        System.err.println("Not enough of space in truck.");
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
        System.err.println("can't store " + item + ", Depot is Full.");
    }

    private void pickup(int x, int y) {
        boolean depotWasFilled = false;
        Cell cell = map.getCell(x, y);
        Iterator<Item> it = cell.getItems().values().iterator();
        while (it.hasNext()) {
            Item item = it.next();
            if (!depotWasFilled) {
                if (map.getDepot().addStorable(item.getType())) {
                    it.remove();
                    map.removeItem(item);
                    System.out.println(item + " was picked up.");
                } else {
                    PRINT_DEPOT_FULL(item.toString());
                    depotWasFilled = true;
                }
            } else {
                PRINT_DEPOT_FULL(item.toString());
            }
        }
        if (!depotWasFilled) {
            Iterator<Wild> wilds = cell.getWilds().values().iterator();
            while (wilds.hasNext()) {
                Wild wild = wilds.next();
                if (wild.isCaged()) {
                    if (!depotWasFilled) {
                        if (map.getDepot().addStorable((ItemType.getType("Caged" + wild.getType())))) {
                            wilds.remove();
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
                } else if (levelBestTime == Integer.MAX_VALUE) {
                    if (timePassed <= levelData.getGoldenTime()) {
                        playerPrize = levelData.getGoldenPrize();
                    } else if (timePassed <= levelData.getSilverTime()) {
                        playerPrize = levelData.getSilverPrize();
                    } else if (timePassed <= levelData.getBronzeTime()) {
                        playerPrize = levelData.getBronzePrize();
                    } else
                        playerPrize = levelData.getPrize();
                } else {
                    playerPrize = levelData.getPrize();
                }
            }
            player.addGoldMoney(playerPrize);
            player.addLevelTime(levelId, timePassed);
            try {
                Player.updatePlayer(player);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Saving Player Data Failed.");
            }
            System.out.println("Prize : " + playerPrize + " Gold.");
            levelIsFinished = true;
        }
    }
}
