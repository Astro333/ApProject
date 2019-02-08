package Transportation;


import Interfaces.Processable;
import Utilities.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;

import static Items.Item.ItemType;

public abstract class TransportationTool {
    protected static AudioClip TRUCK_CAME = new AudioClip(TransportationTool.class.getResource(
            "/res/Transportation/Sounds/car_came.mp3").toExternalForm());
    protected static AudioClip HELICOPTER_FLYING = new AudioClip(TransportationTool.class.getResource(
            "/res/Transportation/Sounds/helicopter_flyin.mp3").toExternalForm());
    protected final byte maxLevel;
    protected final byte maxMaxLevel;
    protected byte level;

    protected int capacity;

    protected IntegerProperty playerCoin;

    protected Button upgradeButton;

    protected final HashMap<? super Processable, Integer> itemsInside;

    private static DoubleProperty GENERAL_TIME_MULTIPLIER;

    protected static HashMap<String, HashMap<Byte, Image>> images;
    protected final HashMap<Byte, ImageView> miniMapViews;
    protected static HashMap<String, HashMap<Byte, Sprite>> miniMapTextures;
    protected final Pane miniMapGraphic;
    protected final Pane viewGraphic;
    protected final SpriteAnimation animation;
    protected ImageView view;
    private double progress = -1;
    private int timeToFinishTask = -1;
    private String type;

    private final AnimationTimer taskTimer;

    public int getItemsInsidePrice() {
        return itemsInsidePrice.get();
    }

    public IntegerProperty itemsInsidePriceProperty() {
        return itemsInsidePrice;
    }

    protected IntegerProperty itemsInsidePrice = new SimpleIntegerProperty(0);

    static {
        miniMapTextures = new HashMap<>();
        images = new HashMap<>();
        Gson gson = new GsonBuilder().registerTypeAdapter(Sprite.class, new SpriteDeserializer()).create();
        Type type = new TypeToken<HashMap<Byte, Sprite>>() {
        }.getType();
        HashMap<Byte, Sprite> temp;
        Reader reader = null;
        try {
            reader = new FileReader(new File("src/res/Transportation/Helicopter/Textures/UI/Config/config.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        temp = gson.fromJson(reader, type);
        miniMapTextures.put("Helicopter", temp);
        try {
            reader = new FileReader(new File("src/res/Transportation/Truck/Textures/UI/Config/config.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        temp = gson.fromJson(reader, type);
        miniMapTextures.put("Truck", temp);
        HashMap<Byte, Image> temp2 = new HashMap<>();
        for (byte i = 0; i < 4; ++i) {
            temp2.put(i, new Image(TransportationTool.class.getResource("/res/Transportation/Helicopter/Textures/Service/Images/0" + (i + 1) + ".png").toExternalForm()));
        }
        images.put("Helicopter", temp2);
        temp2 = new HashMap<>();
        for (byte i = 0; i < 4; ++i) {
            temp2.put(i, new Image(TransportationTool.class.getResource("/res/Transportation/Truck/Textures/Service/Images/0" + (i + 1) + ".png").toExternalForm()));
        }
        images.put("Truck", temp2);
    }

    public void setPlayerCoin(IntegerProperty playerCoin) {
        this.playerCoin = playerCoin;
    }

    public int getCapacity() {
        return capacity;
    }

    protected void updateUpgradeButton() {
        int cost = getUpgradeCost();
        if (cost >= 0) {
            upgradeButton.setText("🔨   " + cost + " ⬤");
        } else {
            upgradeButton.setVisible(false);
        }
    }

    public double getItemsInsideVolume() {
        return itemsInsideVolume.get();
    }

    public DoubleProperty itemsInsideVolumeProperty() {
        return itemsInsideVolume;
    }

    protected DoubleProperty itemsInsideVolume = new SimpleDoubleProperty(0);

    public int getStatus() {
        return status.get();
    }

    public IntegerProperty statusProperty() {
        return status;
    }

    protected transient final IntegerProperty status;

    public static void setGeneralTimeMultiplier(DoubleProperty GENERAL_TIME_MULTIPLIER) {
        TransportationTool.GENERAL_TIME_MULTIPLIER = GENERAL_TIME_MULTIPLIER;
    }

    TransportationTool(byte maxLevel, byte level) {
        miniMapViews = new HashMap<>();
        HashMap<Byte, Sprite> temp1 = this instanceof Helicopter ? miniMapTextures.get("Helicopter") : miniMapTextures.get("Truck");
        HashMap<Byte, Image> temp2 = this instanceof Helicopter ? images.get("Helicopter") : images.get("Truck");

        if (this instanceof Helicopter) {
            this.type = "Helicopter";
            maxMaxLevel = Loader.getElementMaxMaxLevel("Helicopter");
        } else {
            this.type = "Truck";
            maxMaxLevel = Loader.getElementMaxMaxLevel("Truck");
        }
        animation = new SpriteAnimation(GENERAL_TIME_MULTIPLIER, Duration.millis(1000));
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRelativeRate(3);
        for (Byte b : temp1.keySet()) {
            miniMapViews.put(b, new ImageView(temp1.get(b).getImage()));
        }
        viewGraphic = new Pane();
        viewGraphic.setPickOnBounds(false);
        miniMapGraphic = new Pane();
        miniMapGraphic.setVisible(false);
        miniMapGraphic.setPickOnBounds(false);
        miniMapGraphic.getChildren().add(miniMapViews.get(level));
        this.maxLevel = maxLevel;
        this.level = level;
        animation.addTexture(temp1.get(level), miniMapViews.get(level));
        view = new ImageView(images.get(type).get(level));
        view.setOnMouseEntered(event -> view.setEffect(Utility.lightAdjust));
        view.setOnMouseExited(event -> view.setEffect(null));
        viewGraphic.getChildren().add(view);
        itemsInside = new HashMap<>();
        status = new SimpleIntegerProperty(this, "status", 0);
        taskTimer = setupTaskTimer();
    }

    public ImageView getView() {
        return view;
    }

    private AnimationTimer setupTaskTimer() {
        return new AnimationTimer() {
            long prev = -1;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now;
                    return;
                }
                double deltaProgress = (now - prev) * GENERAL_TIME_MULTIPLIER.get() / (timeToFinishTask * 1_000_000_000D);
                progress += deltaProgress;
                if (progress < 0.5) {
                    miniMapGraphic.setTranslateX(270 * progress);
                } else if (progress < 1) {
                    miniMapGraphic.setScaleX(1);
                    miniMapGraphic.setTranslateX(270 * (1 - progress));
                } else {
                    status.set((status.get() & (~State.Working.value)) | State.FinishedJob.value);
                    if (type.equals("Truck"))
                        TRUCK_CAME.play();
                    progress = -1;
                    viewGraphic.setVisible(true);
                    miniMapGraphic.setVisible(false);
                    animation.stop();
                    stop();
                    return;
                }
                prev = now;
            }

            @Override
            public void start() {
                timeToFinishTask = calculateTimeToFinish();
                miniMapGraphic.setScaleX(-1);
                super.start();
            }

            @Override
            public void stop() {
                prev = -1;
                super.stop();
            }
        };
    }

    public Pane getMiniMapGraphic() {
        return miniMapGraphic;
    }

    public Pane getViewGraphic() {
        return viewGraphic;
    }

    public HashMap<? super Processable, Integer> getItemsInside() {
        return itemsInside;
    }

    protected abstract int calculateTimeToFinish();

    public boolean go() {
        if (itemsInside.size() > 0) {
            if (type.equals("Helicopter"))
                HELICOPTER_FLYING.play();
            status.set(status.get() | State.Working.value);
            progress = 0;
            animation.playFromStart();
            viewGraphic.setVisible(false);
            miniMapGraphic.setVisible(true);
            taskTimer.start();
            return true;
        }
        return false;
    }

    public SpriteAnimation getAnimation() {
        return animation;
    }

    public void clear() {
        itemsInside.clear();
        itemsInsideVolume.set(0);
        itemsInsidePrice.set(0);
    }

    public abstract int getUpgradeCost();

    public abstract boolean upgrade();

    public boolean hasCapacityFor(ItemType item, int amount) {
        return Loader.getProductSize(item.toString()) * amount + itemsInsideVolume.get() <= capacity;
    }

    public boolean hasCapacityFor(AnimalType type, int amount) {
        return Loader.getAnimalDepotSize(type) * amount + itemsInsideVolume.get() <= capacity;
    }

    public boolean addAll(ItemType item, int amount) {
        if (hasCapacityFor(item, amount)) {
            if (this instanceof Helicopter)
                itemsInsidePrice.set(itemsInsidePrice.get() + Loader.getProductBuyCost(item.toString()) * amount);
            else
                itemsInsidePrice.set(itemsInsidePrice.get() + Loader.getProductBuyCost(item.toString()) * amount / 2);

            if (itemsInside.containsKey(item)) {
                itemsInside.compute(item, (k, v) -> v + amount);
            } else {
                itemsInside.put(item, amount);
            }
            itemsInsideVolume.set(itemsInsideVolume.get() + Loader.getProductSize(item.toString()) * amount);
            return true;
        }
        return false;
    }

    public byte getLevel() {
        return level;
    }

    public abstract void printElements();
    private boolean isPaused = false;
    public void togglePause() {
        if(isPaused){
            if(progress >= 0) {
                taskTimer.start();
                animation.play();
            }
        } else {
            if(progress >= 0) {
                taskTimer.stop();
                animation.pause();
            }
        }
        isPaused = !isPaused;
    }
}
