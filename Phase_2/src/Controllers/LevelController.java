package Controllers;

import Animals.Animal;
import Animals.Pets.Cat;
import Animals.Pets.Dog;
import Animals.Pets.Pet;
import Animals.Wilds.Wild;
import Buildings.Depot;
import Buildings.Well;
import Buildings.Workshop;
import Ground.GrassField;
import Interfaces.Processable;
import Items.Item;
import Levels.LevelData;
import Levels.RequirementsListener;
import Player.Player;
import Transportation.Helicopter;
import Transportation.TransportationTool;
import Transportation.Truck;
import Utilities.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LevelController {

    private boolean isSetup = false;
    private double deltaX, deltaY;
    private Scene prevScene = null;
    private boolean paused = false;
    private boolean levelIsFinished = false;
    public transient LevelData levelData; //don't serialize
    private transient GraphicalChronometer timer;
    private transient final Random random = new Random();//don't serialize
    private transient Player player;//only Serialize levelGameElementsLevel, name
    private ObservableMap<Processable, Integer> levelRequirements;//serialize
    private IntegerProperty coin;//serialize
    private HashMap<String, Workshop> workshops;//serialize
    private GrassField grassField;
    private Well well;
    private transient MoneyIndicator moneyIndicator;
    private Depot depot;
    private ObservableMap<AnimalType, Integer> animalsAmount;
    private HashMap<Long, Animal> pets;
    private HashMap<Long, Wild> wilds;
    private HashMap<Long, Item> items;
    private MediaPlayer MUSIC = new MediaPlayer(new Media(getClass().getResource("/res/CommonSounds/music.mp3").toExternalForm()));

    {
        MUSIC.setCycleCount(MediaPlayer.INDEFINITE);
        MUSIC.setVolume(0.8);
    }
    private double scale = 1;
    private double deltaX1;
    private double deltaY1;

    private DoubleProperty GENERAL_TIME_MULTIPLIER;

    private HBox animalsBuyButtons;

    private EllipseCollisionTester collisionTester = new EllipseCollisionTester(4);
    private HelicopterController helicopterController;
    private TruckController truckController;
    private Helicopter helicopter;
    private Truck truck;
    private Scene helicopterScene;
    private Scene truckScene;
    private Scene levelScene = null;
    private Stage stage;
    private ImageView roadView;
    private AimContainer aimContainer;
    @FXML
    private transient Pane background;

    private ScheduledExecutorService wildSpawnTimer;
    private ScheduledFuture<?> wildSpawnTask;

    private final Alert playerNotFound = new Alert(Alert.AlertType.CONFIRMATION, "Create New Player?", ButtonType.YES, ButtonType.NO);
    private final DialogPane dialogPane1 = playerNotFound.getDialogPane();

    {
        dialogPane1.lookupButton(ButtonType.NO).setTranslateX(-230);
        dialogPane1.lookupButton(ButtonType.NO).setTranslateY(5);
        dialogPane1.getStylesheets().add("CSSStyles/mainMenuStyle.css");
        dialogPane1.setHeaderText("Player Not Found!");

        dialogPane1.lookupButton(ButtonType.YES).setTranslateX(50);
        dialogPane1.lookupButton(ButtonType.YES).setTranslateY(5);
        playerNotFound.initStyle(StageStyle.UNDECORATED);

    }

    public void setup(byte levelNumber, Player player) throws IOException {
        if (!isSetup) {
            MUSIC.play();
            GENERAL_TIME_MULTIPLIER = new SimpleDoubleProperty(1);
            wildSpawnTimer = Executors.newScheduledThreadPool(1);
            wildSpawnTask = wildSpawnTimer.schedule(wildSpawner, (long) (10_000 / GENERAL_TIME_MULTIPLIER.get()), TimeUnit.MILLISECONDS);
            this.player = player;
            Reader reader = new BufferedReader(new FileReader("DefaultGameData/LevelsInfo/" + "level_" + levelNumber + ".json"));
            Gson gson = new GsonBuilder().registerTypeAdapter(Processable.class, new ProcessableDeserializer()).create();
            levelData = gson.fromJson(reader, LevelData.class);
            Item.setupTextures(levelData.getContinent());
            coin = new SimpleIntegerProperty(levelData.getStartMoney());
            levelRequirements = FXCollections.observableHashMap();

            JsonObject continentData = Loader.loadContinentData(levelData.getContinent());
            TransportationTool.setGeneralTimeMultiplier(GENERAL_TIME_MULTIPLIER);
            LinkedList<String> workshopsNames = new LinkedList<>(Arrays.asList(levelData.getWorkshops()));
            Workshop.setupTextures(workshopsNames);
            Depot.setPlayerCoin(coin);
            Workshop.setPlayerCoin(coin);
            Well.setPlayerCoin(coin);
            Well.setGeneralTimeMultiplier(GENERAL_TIME_MULTIPLIER);
            Workshop.setGeneralTimeMultiplier(GENERAL_TIME_MULTIPLIER);
            Wild.setCageLevel(player.getGameElementLevel("Cage"));
            int[] movementZone = gson.fromJson(continentData.get("movementZone"), int[].class);
            Animal.setMinX(movementZone[0]);
            Animal.setMinY(movementZone[1]);
            Animal.setMaxX(movementZone[2]);
            Animal.setMaxY(movementZone[3]);
            Animal.setGeneralTimeMultiplier(GENERAL_TIME_MULTIPLIER);
            pets = new HashMap<>();
            wilds = new HashMap<>();
            items = new HashMap<>();
            animalsAmount = FXCollections.observableHashMap();
            Dog.setWilds(wilds);
            Cat.setItems(items);
            animalsBuyButtons = Utility.createAnimalsMenu(levelData.getContinent());
            for (int i = 0; i < animalsBuyButtons.getChildren().size(); ++i) {
                AnimalBuyButton button = (AnimalBuyButton) animalsBuyButtons.getChildren().get(i);
                button.setOnAction(event -> buyAnimal(button.getAnimalType()));
                button.disableProperty().bind(coin.lessThan(button.getPrice()));
            }

            background.getChildren().add(animalsBuyButtons);

            RequirementsListener requirementsListener;

            for (Processable levelRequirement : levelData.getGoals().keySet())
                levelRequirements.put(levelRequirement, 0);
            if (levelData.getGoals().containsKey(Item.ItemType.Coin)) {
                requirementsListener = new RequirementsListener(this, true);
                coin.addListener(requirementsListener.getCoinChangeListener());
                levelRequirements.computeIfPresent(Item.ItemType.Coin, (k, v) -> levelData.getStartMoney());
            } else {
                requirementsListener = new RequirementsListener(
                        this, false);
            }
            animalsAmount.addListener(requirementsListener.getMapChangeListener());
            aimContainer = new AimContainer(levelRequirements, levelData.getGoals());
            aimContainer.setLayoutX(651);
            aimContainer.setLayoutY(448);
            aimContainer.setViewOrder(-19);
            background.getChildren().add(aimContainer);
            setupWorkshops(continentData);

            setupWell();
            setupGrassField(continentData);
            Pet.setGrassField(grassField);
            setupTimer();
            setupDepot(continentData, requirementsListener);
            setupTransportation(continentData);
            setupMoneyIndicator(continentData);
            Cat.setDepot(depot);
            setupZoom();
            background.getStylesheets().add(getClass().getResource("../res/Map/Africa/Styles/style.css").toExternalForm());
            collisionDetectionThread.start();
            isSetup = true;
            levelScene = background.getScene();
            Platform.runLater(() -> stage = (Stage) levelScene.getWindow());
            spawnStartingPets();
        }
    }

    private void setupTransportation(JsonObject continentData) {
        if (levelData.getHelicopterLevel() != null) {
            helicopter = new Helicopter((byte) player.getGameElementLevel("Helicopter"), levelData.getHelicopterLevel());
            helicopter.getViewGraphic().setLayoutX(continentData.getAsJsonObject("helicopter").get("layoutX").getAsInt());
            helicopter.getViewGraphic().setLayoutY(continentData.getAsJsonObject("helicopter").get("layoutY").getAsInt());
            helicopter.getViewGraphic().setViewOrder(-20);
            helicopter.getView().setOnMouseClicked(event -> {
                helicopterController.update();
                stage.setScene(helicopterScene);
                toggleMapElementsPause();
            });
            helicopter.getMiniMapGraphic().setLayoutX(continentData.getAsJsonObject("transportationService").
                    getAsJsonObject("helicopter").get("layoutX").getAsInt());
            helicopter.getMiniMapGraphic().setLayoutY(continentData.getAsJsonObject("transportationService").
                    getAsJsonObject("helicopter").get("layoutY").getAsInt());
            background.getChildren().add(helicopter.getMiniMapGraphic());
            helicopter.getMiniMapGraphic().setViewOrder(-20);
            background.getChildren().add(helicopter.getViewGraphic());
            helicopter.setPlayerCoin(coin);
            helicopter.statusProperty().addListener(helicopterStatusChangeListener);
            Pane root;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/res/FxmlFiles/helicopterMenu.fxml"));

            try {
                root = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                root = new Pane();
            }
            helicopterScene = new Scene(root);
            helicopterController = loader.getController();
            helicopterController.setPlayerCoin(coin);
            helicopterController.setup(this, helicopter);
            Platform.runLater(() -> helicopterController.setLevelScene(levelScene));
        } else {
            helicopterController = null;
            helicopter = null;
            helicopterScene = null;
        }
        if (levelData.getTruckLevel() != null) {
            truck = new Truck((byte) player.getGameElementLevel("Truck"), levelData.getTruckLevel());
            truck.getViewGraphic().setLayoutX(continentData.getAsJsonObject("truck").get("layoutX").getAsInt());
            truck.getViewGraphic().setLayoutY(continentData.getAsJsonObject("truck").get("layoutY").getAsInt());
            truck.getView().setOnMouseClicked(event -> {
                truckController.update();
                stage.setScene(truckScene);
                toggleMapElementsPause();
            });
            truck.getMiniMapGraphic().setLayoutX(continentData.getAsJsonObject("transportationService").
                    getAsJsonObject("truck").get("layoutX").getAsInt());
            truck.getMiniMapGraphic().setLayoutY(continentData.getAsJsonObject("transportationService").
                    getAsJsonObject("truck").get("layoutY").getAsInt());
            background.getChildren().add(truck.getMiniMapGraphic());
            truck.getMiniMapGraphic().setViewOrder(-20);
            truck.getViewGraphic().setViewOrder(-20);
            background.getChildren().add(truck.getViewGraphic());
            truck.setPlayerCoin(coin);
            truck.statusProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if (((newValue.intValue() & (~oldValue.intValue())) & State.FinishedJob.value) != 0) {
                        coin.set(coin.get() + truck.receiveSoldGoodsMoney());
                        truck.clear();
                        truck.statusProperty().set(newValue.intValue() & (~State.FinishedJob.value));
                    }
                }
            });

            Pane root;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/res/FxmlFiles/truckMenu.fxml"));

            try {
                root = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                root = new Pane();
            }
            truckScene = new Scene(root);
            truckController = loader.getController();
            truckController.setup(this, depot, pets, truck);
            Platform.runLater(() -> truckController.setLevelScene(levelScene));
        }
        roadView = new ImageView(new Image(getClass().getResource(
                "/res/Transportation/Road/Images/road" + levelData.getContinent() + ".png").toExternalForm()));
        roadView.setLayoutX(continentData.getAsJsonObject("road").get("layoutX").getAsInt());
        roadView.setLayoutY(continentData.getAsJsonObject("road").get("layoutY").getAsInt());
        background.getChildren().add(roadView);
    }


    private Runnable wildSpawner = new Runnable() {
        @Override
        public void run() {
            AnimalType[] types = AnimalType.getWilds().values().toArray(new AnimalType[0]);
            AnimalType type = types[random.nextInt(types.length)];
            Wild wild = (Wild) Animal.getInstance(type, levelData.getContinent());
            wild.scale(0.7);
            Platform.runLater(() -> {
                addAnimal(wild);
                wild.spawn(Animal.getMinX() + (Animal.getMaxX() - Animal.getMinX()) * random.nextDouble(),
                        Animal.getMinY() + (Animal.getMaxY() - Animal.getMinY()) * random.nextDouble());
                wild.statusProperty().addListener(wildStatusChangeListener);
                wild.getGraphic().setOnMouseClicked(event -> {
                    if (!wild.progressCaging()) {
                        if (depot.addStorable(Item.ItemType.getType("Caged" + wild.getType()))) {
                            wild.collect(Item.getDepotX(), Item.getDepotY());
                        } else {
                            Utility.FOOL_ACTION_SOUND.play();
                            depot.spark();
                        }
                    }
                });
            });
            wildSpawnTask = wildSpawnTimer.schedule(this, (long) (30_000 / GENERAL_TIME_MULTIPLIER.get()), TimeUnit.MILLISECONDS);
        }
    };

    private void setupZoom() {
        background.getScene().setOnScroll(event -> {
            if (event.isControlDown()) {
                scale = Math.max(1, scale + event.getDeltaY() / 200);
                background.setScaleX(scale);
                background.setScaleY(scale);
                background.setLayoutX(0);
                background.setLayoutY(0);
            }
        });
        background.setOnMouseDragged(e -> {
            if (e.isMiddleButtonDown()) {
                background.setLayoutX(Math.max(background.getWidth() * (1 - scale) / 2,
                        Math.min(-background.getWidth() * (1 - scale) / 2, e.getScreenX() - deltaX1)));
                background.setLayoutY(Math.max(background.getHeight() * (1 - scale) / 2,
                        Math.min(-background.getHeight() * (1 - scale) / 2, e.getScreenY() - deltaY1)));
            }
        });
        background.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.MIDDLE) {
                deltaX1 = (-background.getLayoutX() + e.getScreenX());
                deltaY1 = (-background.getLayoutY() + e.getScreenY());
            }
        });
    }


    private transient final ChangeListener<Number> itemStatusChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            Item item = (Item) ((IntegerProperty) observable).getBean();
            if (((newValue.intValue() & (~oldValue.intValue())) & State.Collected.value) != 0) {
                synchronized (items) {
                    items.remove(item.id);
                    background.getChildren().remove(item.getGraphic());
                    item.statusProperty().removeListener(this);
                }
            } else if (((newValue.intValue() & (~oldValue.intValue())) & State.Crack.value) != 0) {
                synchronized (items) {
                    items.remove(item.id);
                    background.getChildren().remove(item.getGraphic());
                    item.statusProperty().removeListener(this);
                }
            }
        }
    };
    private ChangeListener<? super Number> helicopterStatusChangeListener = new ChangeListener<Number>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            if (((newValue.intValue() & (~oldValue.intValue())) & State.FinishedJob.value) != 0) {
                for (Object o : helicopter.getItemsInside().keySet()) {
                    if (o instanceof Item.ItemType) {
                        int i = 0;
                        while (i < helicopter.getItemsInside().get(o)) {
                            Item item = new Item((Item.ItemType) o);
                            addItem(item);
                            Platform.runLater(() -> {
                                item.spawn(Animal.getMinX() + (Animal.getMaxX() - Animal.getMinX()) * random.nextDouble(),
                                        Animal.getMinY() + (Animal.getMaxY() - Animal.getMinY()) * random.nextDouble());
                                item.getView().setOnMouseClicked(event -> {
                                    if (depot.addStorable(item.getType())) {
                                        item.collect();
                                    } else {
                                        Utility.FOOL_ACTION_SOUND.play();
                                        depot.spark();
                                    }
                                });
                            });
                            ++i;
                        }
                    }
                }
                helicopter.clear();
                helicopter.statusProperty().set(newValue.intValue() & (~State.FinishedJob.value));
            }
        }
    };
    private AnimationTimer collisionDetectionThread = new AnimationTimer() {
        long prev = -1;

        @Override
        public void handle(long now) {
            if (prev == -1) {
                prev = now;
                return;
            }
            if ((now - prev) * GENERAL_TIME_MULTIPLIER.get() >= 250_000_000) {
                synchronized (wilds) {
                    if (wilds.size() > 0) {
                        Iterator<Wild> wildIterator = wilds.values().iterator();
                        while (wildIterator.hasNext()) {
                            Wild wild = wildIterator.next();
                            if (!wild.isCaged() && !wild.hasTossed()) {
                                double x0 = wild.getX();
                                double y0 = wild.getY();
                                double w0 = wild.hitBox.getRadiusX();
                                double h0 = wild.hitBox.getRadiusY();
                                synchronized (pets) {
                                    Iterator<Animal> petIterator = pets.values().iterator();
                                    while (petIterator.hasNext()) {
                                        Animal pet = petIterator.next();
                                        if ((pet.getStatus() & (State.Spawning.value)) == 0) {
                                            double x1 = pet.getX();
                                            double y1 = pet.getY();
                                            double w1 = pet.hitBox.getRadiusX();
                                            double h1 = pet.hitBox.getRadiusY();
                                            if (collisionTester.collide(x0, y0, w0, h0, x1, y1, w1, h1)) {
                                                if (pet instanceof Cat && !wild.hasTossed()) {
                                                    petIterator.remove();
                                                    ((Cat) pet).toss();
                                                    wild.setHasTossed(true);
                                                } else if (pet instanceof Dog) {
                                                    wildIterator.remove();
                                                    animalsAmount.computeIfPresent(wild.getType(), (k, v) -> v - 1);
                                                    wild.statusProperty().set(wild.getStatus() | State.MarkedToRemove.value);
                                                    Utility.BATTLE_SOUND.play();
                                                    petIterator.remove();
                                                    ((Dog) pet).enterFight();
                                                    break;
                                                } else if (!wild.hasTossed()) {
                                                    petIterator.remove();
                                                    ((Pet) pet).toss();
                                                    wild.setHasTossed(true);
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!wild.hasTossed()) {
                                    synchronized (items) {
                                        Iterator<Item> itemIterator = items.values().iterator();
                                        while (itemIterator.hasNext()) {
                                            Item item = itemIterator.next();
                                            if ((item.getStatus() & State.Spawning.value) == 0) {
                                                double x1 = item.getX();
                                                double y1 = item.getY();
                                                double w1 = item.hitBox.getRadiusX();
                                                double h1 = item.hitBox.getRadiusY();
                                                if (collisionTester.collide(x0, y0, w0, h0, x1, y1, w1, h1)) {
                                                    itemIterator.remove();
                                                    item.crack();
                                                    wild.setHasTossed(true);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                prev = now;
            }
        }
    };

    private ChangeListener<Number> wildStatusChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            Wild wild = (Wild) ((IntegerProperty) observable).getBean();
            if ((newValue.intValue() & (~oldValue.intValue()) & (State.Collected.value)) != 0) {
                background.getChildren().remove(wild.getGraphic());
                wild.statusProperty().removeListener(this);
                synchronized (wilds) {
                    wilds.remove(wild.id);
                    animalsAmount.computeIfPresent(wild.getType(), (k, v) -> v + 1);
                }
                wild.destruct();
            } else if ((newValue.intValue() & (~oldValue.intValue()) & (State.MarkedToRemove.value)) != 0) {
                background.getChildren().remove(wild.getGraphic());
                wild.statusProperty().removeListener(this);
                wild.destruct();
            }
        }
    };

    private ChangeListener<Number> petStatusChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            Pet pet = (Pet) ((IntegerProperty) observable).getBean();
            if ((newValue.intValue() & (~oldValue.intValue()) & (State.Death.value)) != 0) {
                System.err.println("pet died.");
                background.getChildren().remove(pet.getGraphic());
                pet.statusProperty().removeListener(this);
                synchronized (pets) {
                    pets.remove(pet.id);
                    animalsAmount.computeIfPresent(pet.getType(), (k, v) -> v - 1);
                }
            } else if ((newValue.intValue() & (~oldValue.intValue()) & State.Eat.value) != 0) {
                System.err.println("pet is eating");
            } else if ((newValue.intValue() & (~oldValue.intValue()) & State.MarkedToRemove.value) != 0) {
                pet.destruct();
                background.getChildren().remove(pet.getGraphic());
                pet.statusProperty().removeListener(this);
            } else if ((newValue.intValue() & (~oldValue.intValue()) & (State.Tossed.value)) != 0) {
                System.err.println("Pet Was Tossed");
                background.getChildren().remove(pet.getGraphic());
                pet.statusProperty().removeListener(this);
                synchronized (pets) {
                    pets.remove(pet.id);
                    animalsAmount.computeIfPresent(pet.getType(), (k, v) -> v - 1);
                }
            } else if ((newValue.intValue() & (~oldValue.intValue()) & (State.Produced.value)) != 0) {
                Item item = pet.produce();
                addItem(item);
                pet.statusProperty().set(newValue.intValue() & (~State.Produced.value));
                Platform.runLater(() -> {
                    item.spawn(pet.getX(), pet.getY());
                    item.getGraphic().setOnMouseClicked(event -> {
                        if (depot.addStorable(item.getType())) {
                            item.collect();
                        } else {
                            Utility.FOOL_ACTION_SOUND.play();
                            depot.spark();
                        }
                    });
                });
            }
        }
    };

    private synchronized void buyAnimal(AnimalType animalType) {
        synchronized (coin) {
            int price = Loader.getAnimalBuyCost(animalType);
            if (coin.get() >= price) {
                coin.set(coin.get() - price);
                Animal animal = Animal.getInstance(animalType, levelData.getContinent());
                animal.scale(0.7);
                animal.spawn(getRandomX(), getRandomY());
                if (animal instanceof Pet) {
                    animal.statusProperty().addListener(petStatusChangeListener);
                }
                addAnimal(animal);
            }
        }
    }

    private double getRandomX() {
        return Animal.getMinX() + (Animal.getMaxX() - Animal.getMinX()) * random.nextDouble();
    }

    private double getRandomY() {
        return Animal.getMinY() + (Animal.getMaxY() - Animal.getMinY()) * random.nextDouble();
    }

    private void setupGrassField(JsonObject continentData) {
        grassField = new GrassField(continentData.getAsJsonObject("grassField").get("centerX").getAsInt(),
                continentData.getAsJsonObject("grassField").get("centerY").getAsInt(),
                continentData.getAsJsonObject("grassField").get("radiusX").getAsDouble(),
                continentData.getAsJsonObject("grassField").get("radiusY").getAsDouble(),
                levelData.getContinent());
        grassField.setWell(well);
        for (int i = 0; i < grassField.getGrasses().length; ++i) {
            for (int j = 0; j < grassField.getGrasses()[i].length; ++j) {
                background.getChildren().add(grassField.getGrasses()[i][j].getGraphics());
            }
        }
    }

    private void spawnStartingPets() {
        for (int i = 0; i < levelData.getStartingPets().length; ++i) {
            AnimalType type = AnimalType.getType(levelData.getStartingPets()[i]);
            animalsAmount.put(type, levelData.getStartingPetsAmount()[i]);
            int j = 0;
            while (j < levelData.getStartingPetsAmount()[i]) {
                Animal animal = Animal.getInstance(type, levelData.getContinent());
                animal.scale(0.7);
                background.getChildren().add(animal.getGraphic());
                animal.spawn(getRandomX(), getRandomY());
                animal.statusProperty().addListener(petStatusChangeListener);
                pets.put(animal.id, animal);
                ++j;
            }
        }
    }

    private void setupWell() {
        well = new Well((byte) player.getGameElementLevel("Well"));
        background.getChildren().add(well.getGraphic());
    }

    private void setupTimer() {
        timer = new GraphicalChronometer(GENERAL_TIME_MULTIPLIER);
        timer.getGraphic().setViewOrder(-20);
        background.getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                toggleMapElementsPause();
                toggleMenu();
            }
        });
        Pane graphic = timer.getGraphic();
        Platform.runLater(() -> {
            graphic.setLayoutX(background.getWidth() - graphic.getWidth() + 2);
            graphic.setLayoutY(background.getHeight() - graphic.getHeight() + 2);
        });
        background.getChildren().add(timer.getGraphic());
        timer.start();
    }

    private boolean isPaused = false;
    private long timeRemainedToSpawnWild = -1;

    private Stage levelFinishedStage;
    private Label prize;

    {
        Platform.runLater(() -> {
            VBox levelFinishedRoot = new VBox(5);
            levelFinishedRoot.getStylesheets().add("/CSSStyles/pauseMenu.css");
            levelFinishedRoot.setId("back");
            prize = new Label();
            prize.setStyle("-fx-font-family: Orbitron;" +
                    "-fx-font-size: 23;");
            levelFinishedRoot.getChildren().add(prize);
            HBox box = new HBox();
            box.setSpacing(50);
            Button restartButton = new Button("Restart");

            box.getChildren().add(restartButton);
            Button playerMenu = new Button("Player Menu");
            box.getChildren().add(playerMenu);
            levelFinishedRoot.getChildren().add(box);
            playerMenu.setOnAction(new EventHandler<>() {
                @Override
                public void handle(ActionEvent event) {
                    levelFinishedStage.close();
                    goToPrevScene();
                    destruct();
                }
            });

            levelFinishedStage = new Stage(StageStyle.TRANSPARENT);

            levelFinishedStage.initOwner(stage);
            Scene scene = new Scene(levelFinishedRoot, Color.TRANSPARENT);
            levelFinishedStage.initModality(Modality.APPLICATION_MODAL);
            levelFinishedStage.setScene(scene);

            restartButton.setOnAction(event -> {
                background.setEffect(null);
                restart();
                levelFinishedStage.close();
            });
        });
    }

    private void restart() {
        destruct();
        try {
            setup(levelData.getLevelId(), player);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Stage pauseStage;

    {
        Platform.runLater(() -> {
            VBox pauseRoot = new VBox(5);
            pauseRoot.getStylesheets().add("/CSSStyles/pauseMenu.css");
            pauseRoot.setId("back");
            Button resume = new Button("Resume");
            pauseRoot.getChildren().add(resume);
            TextField textField = new TextField("rate = ");
            textField.setId("rateText");
            textField.setDisable(true);
            TextField rate = new TextField("1");
            rate.setTextFormatter(new TextFormatter<>(change -> {
                if (!change.isContentChange()) {
                    return change;
                }
                String s = change.getControlNewText();
                if (s.matches("([0-9]*\\.)?[0-9]*")) {
                    return change;
                }
                return null;
            }));
            rate.setOnKeyPressed(new EventHandler<>() {
                @Override
                public void handle(KeyEvent event) {
                    if (event.getCode() == KeyCode.ENTER) {
                        if (!rate.getText().matches("([0-9]+\\.)?[0-9]+"))
                            rate.setText("1");
                        GENERAL_TIME_MULTIPLIER.set(Double.parseDouble(rate.getText()));
                        pauseRoot.requestFocus();
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        pauseRoot.requestFocus();
                    }
                }
            });
            HBox box = new HBox();
            box.getChildren().addAll(textField, rate);
            pauseRoot.getChildren().add(box);
            Button playerMenu = new Button("Player Menu");
            pauseRoot.getChildren().add(playerMenu);
            playerMenu.setOnAction(new EventHandler<>() {
                @Override
                public void handle(ActionEvent event) {
                    pauseStage.close();
                    goToPrevScene();
                    destruct();
                }
            });

            pauseStage = new Stage(StageStyle.TRANSPARENT);

            pauseStage.initOwner(stage);
            Scene scene = new Scene(pauseRoot, Color.TRANSPARENT);
            pauseStage.initModality(Modality.APPLICATION_MODAL);
            pauseStage.setScene(scene);

            scene.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    toggleMapElementsPause();
                    toggleMenu();
                }
            });

            resume.setOnAction(event -> {
                toggleMapElementsPause();
                toggleMenu();
            });
        });
    }

    @SuppressWarnings("Duplicates")
    private void goToPrevScene() {
        stage.setScene(prevScene);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    void toggleMapElementsPause() {
        timer.toggle();
        well.togglePause();
        for (Animal pet : pets.values()) {
            pet.togglePause();
        }
        for (Wild wild : wilds.values()) {
            wild.togglePause();
        }
        for (Workshop workshop : workshops.values()) {
            workshop.togglePause();
        }
        if (helicopter != null) {
            helicopter.togglePause();
        }
        if (truck != null) {
            truck.togglePause();
        }
        if (isPaused) {
            isPaused = false;
            if (timeRemainedToSpawnWild > 0)
                wildSpawnTask = wildSpawnTimer.schedule(wildSpawner, timeRemainedToSpawnWild, TimeUnit.MILLISECONDS);
        } else {
            isPaused = true;
            if (!wildSpawnTask.isDone()) {
                wildSpawnTask.cancel(true);
                timeRemainedToSpawnWild = wildSpawnTask.getDelay(TimeUnit.MILLISECONDS);
            } else {
                timeRemainedToSpawnWild = -1;
            }
        }
    }

    private boolean menuShowing = false;

    private void toggleMenu() {
        if (menuShowing) {
            menuShowing = false;
            pauseStage.close();
            background.setEffect(null);
        } else {
            menuShowing = true;
            background.setEffect(new GaussianBlur());
            pauseStage.showAndWait();
        }
    }

    private void destruct() {
        for (Animal animal : pets.values()) {
            animal.destruct();
            animal.statusProperty().removeListener(petStatusChangeListener);
        }
        pets.clear();
        for (Wild wild : wilds.values()) {
            wild.destruct();
            wild.statusProperty().removeListener(wildStatusChangeListener);
        }
        wilds.clear();
        for (Workshop workshop : workshops.values()) {
            workshop.destruct();
            workshop.statusProperty().removeListener(workshopChangeListener);
        }
        workshops.clear();

        grassField.destruct();
        well.destruct();
        depot.destruct();
        moneyIndicator.destruct();

        wildSpawnTask.cancel(true);
        wildSpawnTimer.shutdownNow();
        animalsBuyButtons.getChildren().clear();
        for (Node button : animalsBuyButtons.getChildren()) {
            ((AnimalBuyButton) button).destruct();
        }
        background.getChildren().clear();
        MUSIC.stop();
        System.gc();
        isSetup = false;
    }

    private void setupMoneyIndicator(JsonObject continentData) {
        moneyIndicator = Utility.createMoneyIndicator();
        moneyIndicator.getGraphic().setLayoutX(continentData.getAsJsonObject("moneyIndicator").get("layoutX").getAsInt());
        moneyIndicator.getGraphic().setLayoutY(continentData.getAsJsonObject("moneyIndicator").get("layoutY").getAsInt());
        background.getChildren().add(moneyIndicator.getGraphic());
        moneyIndicator.setAmount(coin.get());
        coin.addListener((observable, oldValue, newValue) -> moneyIndicator.setAmount(coin.get()));
    }

    private void setupWorkshops(JsonObject continentData) {
        workshops = new HashMap<>();
        for (int i = 0; i < levelData.getWorkshops().length; ++i) {
            byte pos = levelData.getWorkshopsPosition()[i];
            Workshop workshop = new Workshop(levelData.getWorkshops()[i], pos,
                    levelData.getContinent(), player.getGameElementLevel(levelData.getWorkshops()[i] + "Workshop"), levelData.getWorkshopsLevel()[i]);
            workshops.put(workshop.getRealName(), workshop);
            background.getChildren().add(workshop.getGraphic());
            workshop.statusProperty().addListener(workshopChangeListener);
            workshop.getUpgradeButton().setLayoutX(continentData.getAsJsonObject("place" + (pos)).get("upgradeButtonX").getAsInt());
            workshop.getUpgradeButton().setLayoutY(continentData.getAsJsonObject("place" + (pos)).get("upgradeButtonY").getAsInt());
        }
    }

    private void setupDepot(JsonObject continentData, RequirementsListener requirementsListener) {
        depot = new Depot((byte) player.getGameElementLevel("Depot"), requirementsListener.getMapChangeListener());
        JsonObject depotData = continentData.getAsJsonObject("depot");
        depot.getUpgradeButton().setLayoutX(depotData.get("upgradeButtonX").getAsInt());
        depot.getUpgradeButton().setLayoutY(depotData.get("upgradeButtonY").getAsInt());
        Item.setDepotX(depotData.get("itemCollectionX").getAsInt());
        Item.setDepotY(depotData.get("itemCollectionY").getAsInt());
        depot.getGraphic().setLayoutX(depotData.get("layoutX").getAsInt());
        depot.getGraphic().setLayoutY(depotData.get("layoutY").getAsInt());

        depot.addAllStorable(Item.ItemType.Egg, 10);

        background.getChildren().add(depot.getGraphic());
    }

    private void addItem(Item item) {
        item.statusProperty().addListener(itemStatusChangeListener);
        synchronized (items) {
            items.put(item.id, item);
        }
        Platform.runLater(() -> {
            background.getChildren().add(item.getGraphic());
        });
    }

    private void addAnimal(Animal animal) {
        if (animal instanceof Wild) {
            wilds.put(animal.id, (Wild) animal);
        } else
            pets.put(animal.id, animal);
        if (animalsAmount.containsKey(animal.getType())) {
            animalsAmount.computeIfPresent(animal.getType(), (k, v) -> v + 1);
        } else {
            animalsAmount.put(animal.getType(), 1);
        }
        background.getChildren().add(animal.getGraphic());
    }

    private void removeItem(Item item) {
        items.remove(item.id);
        background.getChildren().remove(item.getGraphic());
    }

    private transient final ChangeListener<Number> workshopChangeListener = new ChangeListener<>() {
        @Override
        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            int changedBit = (newValue.intValue() & (~oldValue.intValue()));
            IntegerProperty integerProperty = ((IntegerProperty) observable);
            if ((changedBit & State.FinishedJob.value) != 0) {
                Workshop workshop = (Workshop) integerProperty.getBean();
                for (int i = 0; i < workshop.getOutputs().length; ++i) {
                    Processable p = workshop.getOutputs()[i];
                    if (p instanceof Item.ItemType) {
                        int j = 0;
                        while (j < workshop.getOutputsAmount()[i] * workshop.getProcessingMultiplier()) {
                            Item item = new Item((Item.ItemType) p, workshop.itemProduceX, workshop.itemProduceY);
                            addItem(item);
                            item.getGraphic().setOnMouseClicked(e -> {
                                if (depot.addStorable(item.getType())) {
                                    item.collect();
                                } else {
                                    Utility.FOOL_ACTION_SOUND.play();
                                    depot.spark();
                                }
                            });
                            item.spawn(workshop.dropZoneX + (1 - 2 * random.nextDouble()) * 17,
                                    workshop.dropZoneY + (1 - 2 * random.nextDouble()) * 17);

                            integerProperty.set(newValue.intValue() & (~State.FinishedJob.value));
                            ++j;
                        }
                    } else if (p instanceof AnimalType) {
                        int j = 0;
                        while (j < workshop.getOutputsAmount()[i] * workshop.getProcessingMultiplier()) {
                            Animal animal = Animal.getInstance((AnimalType) p, levelData.getContinent());
                            addAnimal(animal);
                            if (animal instanceof Pet) {
                                animal.statusProperty().addListener(petStatusChangeListener);
                            }
                            animal.scale(0.7);
                            animal.setX(workshop.itemProduceX);
                            animal.setY(workshop.itemProduceY);
                            Pane graphic = animal.getGraphic();
                            ScaleTransition st = new ScaleTransition();
                            st.setFromX(0.001);
                            st.setFromY(0.001);
                            st.setToX(0.7);
                            st.setToY(0.7);

                            double toX = workshop.dropZoneX - workshop.itemProduceX;
                            double toY = workshop.dropZoneY - workshop.itemProduceY;
                            long duration = (long) Math.sqrt(toX * toX + toY * toY);
                            st.setDuration(Duration.millis(duration));
                            st.playFromStart();
                            new AnimationTimer() {
                                long prev = System.nanoTime();

                                @Override
                                public void handle(long now) {
                                    double deltaT = (now - prev) / 1_000_000_000D;
                                    graphic.setLayoutY(graphic.getLayoutY() + 100 * deltaT);
                                    graphic.setLayoutY(graphic.getLayoutY() + 100 * deltaT);
                                    if (graphic.getLayoutY() >= workshop.dropZoneY - animal.hitBox.getCenterY()) {
                                        stop();
                                        animal.startMoving();
                                        animal.moveTowardRandomLocation();
                                    }
                                }
                            }.start();

                            integerProperty.set(newValue.intValue() & (~State.FinishedJob.value));
                            ++j;
                        }
                    }
                }
            } else if ((changedBit & State.Waiting.value) != 0) {
                Workshop workshop = (Workshop) integerProperty.getBean();
                synchronized (depot) {
                    int val = workshop.calculateAmountNeeded(depot);
                    if (val > 0) {
                        depot.sendItemsToWorkshops(workshop, background, val);
                    }
                }
            }
        }
    };

    public ObservableMap<Processable, Integer> getLevelRequirements() {
        return levelRequirements;
    }

    public void setPrevScene(Scene prevScene) {
        this.prevScene = prevScene;
    }

    public void setAchieved(Processable requirement) {
        int timePassed = timer.getSeconds();
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
                if (levelBestTime == Integer.MAX_VALUE) {
                    if (timePassed <= levelData.getGoldenTime()) {
                        prize.setTextFill(Color.GOLD);
                        prize.setText("Golden Prize : ");
                        playerPrize = levelData.getGoldenPrize();
                    } else if (timePassed <= levelData.getSilverTime()) {
                        prize.setTextFill(Color.SILVER);
                        prize.setText("Silver Prize : ");
                        playerPrize = levelData.getSilverPrize();
                    } else if (timePassed <= levelData.getBronzeTime()) {
                        prize.setTextFill(Color.BROWN);
                        prize.setText("Bronze Prize : ");
                        playerPrize = levelData.getBronzePrize();
                    } else {
                        prize.setText("Prize : ");
                        playerPrize = levelData.getPrize();
                    }
                } else if (timePassed <= levelData.getBronzeTime() && levelBestTime > levelData.getBronzeTime()) {
                    playerPrize = levelData.getBronzePrize();
                    System.out.print("Bronze Prize : ");
                    prize.setText("Bronze Prize : ");
                    prize.setTextFill(Color.BROWN);
                } else if (timePassed <= levelData.getSilverTime() && levelBestTime > levelData.getSilverTime()) {
                    playerPrize = levelData.getSilverPrize();
                    System.out.print("Silver Prize : ");
                    prize.setTextFill(Color.SILVER);
                    prize.setText("Silver Prize : ");
                } else if (timePassed <= levelData.getGoldenTime() && levelBestTime > levelData.getGoldenTime()) {
                    playerPrize = levelData.getGoldenPrize();
                    prize.setTextFill(Color.GOLD);
                    prize.setText("Golden Prize : ");
                    System.out.print("Golden Prize : ");
                } else {
                    prize.setText("Prize : ");
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
            prize.setText(prize.getText() + playerPrize + " ⬤");
            System.out.println("Prize : " + playerPrize + " Gold.");
            Utility.LEVEL_FINISHED.play();
            MUSIC.stop();
            Thread t = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() -> {
                    toggleMapElementsPause();
                    wildSpawnTimer.shutdownNow();
                    wildSpawnTask.cancel(true);
                    background.setEffect(new GaussianBlur());
                    levelFinishedStage.showAndWait();
                });
            });
            t.start();
            levelIsFinished = true;
        }
    }
}
