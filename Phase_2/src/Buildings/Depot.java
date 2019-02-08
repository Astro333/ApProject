package Buildings;

import Interfaces.Destructible;
import Interfaces.Processable;
import Items.Item;
import Utilities.AnimalType;
import Utilities.Loader;
import Utilities.Utility;
import javafx.animation.AnimationTimer;
import javafx.animation.ParallelTransition;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.PopupWindow;
import javafx.util.Duration;

import java.util.HashMap;

import static Utilities.Utility.SQUARE_BUBBLE;

public class Depot implements Destructible {


    private static transient IntegerProperty playerCoin;
    private float storedThingsVolume = 0;
    private int capacity;
    private byte level = 0;
    private final byte maxLevel;

    public ObservableMap<Item.ItemType, Integer> getThingsStored() {
        return thingsStored;
    }

    private final ObservableMap<Item.ItemType, Integer> thingsStored;

    private final Pane graphic;
    private transient final AnimationTimer sparkAnimation;
    private final Button upgradeButton;

    private final HashMap<Byte, ImageView> views;

    public static void setPlayerCoin(IntegerProperty playerCoin) {
        Depot.playerCoin = playerCoin;
    }

    public Button getUpgradeButton() {
        return upgradeButton;
    }

    public Depot(byte maxLevel, MapChangeListener<Processable, Integer> requirementMetDetector) {
        this.maxLevel = maxLevel;
        this.capacity = 50;
        thingsStored = FXCollections.observableHashMap();
        thingsStored.addListener(requirementMetDetector);
        views = new HashMap<>();
        graphic = new Pane();
        sparkAnimation = Utility.setupSparkAnimation(graphic);
        graphic.setPickOnBounds(false);
        for (byte i = 0; i <= maxLevel; ++i) {
            ImageView view = new ImageView(new Image(getClass().getResource("/res/Depot/Textures/Images/0" + (i + 1) + ".png").toExternalForm()));
            views.put(i, view);
            view.setOnMouseEntered(event -> view.setEffect(Utility.lightAdjust));
            view.setOnMouseExited(event -> view.setEffect(null));
        }
        ImageView view = views.get((byte) 0);

        graphic.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                System.out.println(Depot.this.toString());
            }
        });
        upgradeButton = Utility.createUpgradeButton();
        updateButton();
        upgradeButton.setViewOrder(-2);
        upgradeButton.setOnAction(event -> {
            int cost = getUpgradeCost();
            if(cost >= 0 && playerCoin.get() >= cost) {
                playerCoin.set(playerCoin.get() - cost);
                Utility.UPGRADE_BUILDING.play();
                upgrade();
            }
        });
        graphic.getChildren().add(upgradeButton);
        playerCoin.addListener((observable, oldValue, newValue) -> {
            if (getUpgradeCost() > newValue.intValue()) {
                upgradeButton.setDisable(true);
            } else {
                upgradeButton.setDisable(false);
            }
        });
        graphic.viewOrderProperty().bind(graphic.layoutYProperty().multiply(-1));
        graphic.getChildren().add(view);
        Tooltip.install(graphic, createToolTip());
    }

    public Pane getGraphic() {
        return graphic;
    }

    private void updateButton() {
        int cost = getUpgradeCost();
        if (cost > 0) {
            upgradeButton.setText("🔨   " + cost + " ⬤");
        } else {
            upgradeButton.setVisible(false);
        }
    }

    /*public Depot(SaveData saveData, MapChangeListener<Processable, Integer> requirementMetListener) {
        this.maxLevel = saveData.getDepotMaxLevel();
        this.level = saveData.getDepotLevel();
        this.storedThingsVolume = saveData.getDepotStoredThingsVolume();
        this.capacity = level == 0 ? 50 : 150 * (1 << (level - 1));
        this.thingsStored = FXCollections.observableHashMap();
        this.thingsStored.putAll(saveData.getDepotItems());
        thingsStored.addListener(requirementMetListener);
    }*/

    /*public void fillSaveData(SaveData saveData) {
        saveData.setDepotLevel(level);
        saveData.setDepotMaxLevel(maxLevel);
        saveData.setDepotStoredThingsVolume(storedThingsVolume);
        saveData.setDepotItems(new HashMap<>(thingsStored));
    }*/

    public void addListener(MapChangeListener<Processable, Integer> requirementMetListener) {
        thingsStored.addListener(requirementMetListener);
    }

    public boolean addAllStorable(Item.ItemType storable, int amount) {
        float unitVolume = Loader.getProductSize(storable.toString());
        if (amount > 0 && storedThingsVolume + unitVolume * amount <= capacity) {
            if (thingsStored.containsKey(storable)) {
                thingsStored.compute(storable, (k, v) -> v + amount);
            } else {
                thingsStored.put(storable, amount);
            }
            storedThingsVolume += amount * unitVolume;
            return true;
        }
        return false;
    }

    public boolean addStorable(Item.ItemType storable) {
        float unitVolume = Loader.getProductSize(storable.toString());
        if (unitVolume + storedThingsVolume <= capacity) {
            if (thingsStored.containsKey(storable)) {
                thingsStored.put(storable, thingsStored.get(storable) + 1);
            } else {
                thingsStored.put(storable, 1);
            }
            storedThingsVolume += unitVolume;
            return true;
        }
        return false;
    }

    public boolean hasCapacityFor(Item.ItemType storable) {
        float unitVolume = Loader.getProductSize(storable.toString());
        return unitVolume + storedThingsVolume <= capacity;
    }

    public synchronized boolean removeAllStorable(Item.ItemType type, int amount) {
        float unitVolume = Loader.getProductSize(type.toString());
        if (amount > 0 && thingsStored.containsKey(type)) {
            if (thingsStored.get(type) >= amount) {
                thingsStored.compute(type, (k, v) -> v - amount);
                storedThingsVolume -= unitVolume * amount;
                return true;
            } else {
                System.err.println("Not Enough Items In Depot.");
                return false;
            }
        }
        return false;
    }

    public boolean hasAll(Item.ItemType item, int amount) {
        if (thingsStored.containsKey(item)) {
            return thingsStored.get(item) >= amount;
        }
        return false;
    }

    public int getItemAmount(Item.ItemType item) {
        return thingsStored.getOrDefault(item, 0);
    }

    public boolean upgrade() {
        int cost = getUpgradeCost();
        if (level < maxLevel && playerCoin.get() >= cost) {
            capacity = 150 * (1 << level);
            ++level;
            graphic.getChildren().clear();
            graphic.getChildren().add(views.get(level));
            graphic.getChildren().add(upgradeButton);
            updateButton();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Depot:\n").append("Level = ").append(level).append(", MaxLevel = ").append(maxLevel).
                append(", Capacity = ").append(capacity).
                append(", storedItemsVolume = ").append(storedThingsVolume).append("\n");
        if (thingsStored.size() > 0) {
            s.append("Items:\n");
            for (Item.ItemType item : thingsStored.keySet()) {
                s.append("\t").append(item).append(", Amount = ").append(thingsStored.get(item)).
                        append(", Unit Cost : ").append(Loader.getProductSaleCost(item.toString())).append("\n");
            }
            s.deleteCharAt(s.length() - 1);
        } else
            s.append("\tNo Items Inside.");
        return s.toString();
    }

    public int getUpgradeCost() {
        return level == maxLevel ? -1 : Loader.getElementLevelUpgradeCost("Depot", level + 1);
    }

    private Tooltip createToolTip() {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-font-size: 16px;"+
                "-fx-shape: \"" + SQUARE_BUBBLE + "\";" +
                "-fx-background-color: #f45942;");
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);

        Text name = new Text("Warehouse\n\nFor Storing\n\nProducts.\n\n");

        name.setFill(Color.GOLD);
        name.setFont(Font.font("Orbitron", 16));
        tooltip.setGraphic(name);
        return tooltip;
    }

    public byte getLevel() {
        return level;
    }

    public void spark() {
        sparkAnimation.start();
    }

    public synchronized void sendItemsToWorkshops(Workshop workshop, Pane background, int multiplier) {
        ParallelTransition pt = new ParallelTransition();
        int t = 0;
        for (int i = 0; i < workshop.getInputs().length; ++i) {
            Item.ItemType type = workshop.getInputs()[i];
            if(!removeAllStorable(type, workshop.getInputsAmount()[i] * multiplier)){
                throw new RuntimeException("invalid state Exception.");
            }
            int amount = 0;

            while (amount < workshop.getInputsAmount()[i] * multiplier){
                ParallelTransition tt = Item.createToWorkshopTransition(type, workshop.itemProduceX, workshop.itemProduceY);
                tt.setDelay(Duration.millis(t*100));
                ++t;
                background.getChildren().add(tt.getNode());
                pt.getChildren().add(tt);
                tt.setOnFinished(e->background.getChildren().remove(tt.getNode()));
                ++amount;
            }
        }
        pt.playFromStart();
        pt.setOnFinished(event -> workshop.startWorking());
    }

    @Override
    public void destruct() {

    }
}
