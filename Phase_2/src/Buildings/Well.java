package Buildings;

import Interfaces.Destructible;
import Utilities.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.PopupWindow;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;

import static Utilities.Utility.SQUARE_BUBBLE;

public class Well implements Destructible {

    private static DoubleProperty generalTimeMultiplier;
    private boolean isRefilling;
    private byte level = 0;
    private byte storedWater;
    private byte capacity;
    private AnimationTimer sparkAnimation;
    private Button upgradeButton;

    private static final AudioClip REFILL_SOUND = new AudioClip(Well.class.getResource("/res/Well/Sounds/action_well_water.mp3").toExternalForm());

    public static void setGeneralTimeMultiplier(DoubleProperty generalTimeMultiplier) {
        Well.generalTimeMultiplier = generalTimeMultiplier;
    }

    private transient static final HashMap<Byte, Sprite> textures;
    private transient final HashMap<Byte, Image> images;
    private transient final Pane graphic;
    private ImageView view;

    static {
        Type type = new TypeToken<HashMap<Byte, Sprite>>() {
        }.getType();
        Gson gson = new GsonBuilder().registerTypeAdapter(Sprite.class, new SpriteDeserializer()).create();
        Reader reader = null;
        try {
            reader = new FileReader(new File("src/res/Well/Textures/Config/config.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        textures = gson.fromJson(reader, type);
    }

    private static IntegerProperty playerCoin;

    public static void setPlayerCoin(IntegerProperty playerCoin) {
        Well.playerCoin = playerCoin;
    }

    private transient final SpriteAnimation animation;
    private transient final AnimationTimer refillTimer;
    private transient final Rectangle progressIndicator;
    private double timeRemainedToRefill = -1;

    private final byte maxLevel;
    private final byte maxMaxLevel = 3;

    public Well(byte maxLevel) {
        graphic = new Pane();
        images = new HashMap<>();
        progressIndicator = setupProgressIndicator();
        this.capacity = level == maxMaxLevel ? 100 : ((byte) (5 + level * (level + 3) / 2));
        refillTimer = createRefillTimer();
        for (byte i = 0; i <= maxLevel; ++i) {
            Image image = textures.get(i).getImage();
            images.put(i, image);
        }

        view = new ImageView(images.get(level));
        sparkAnimation = Utility.setupSparkAnimation(view);
        setupViewEffect(view);
        graphic.getChildren().add(view);
        graphic.getChildren().add(progressIndicator);
        graphic.setLayoutX(350);
        graphic.setLayoutY(40);
        storedWater = capacity;
        animation = new SpriteAnimation(generalTimeMultiplier, textures.get(level), view);
        animation.setRelativeRate(2);
        animation.setCycleCount(Animation.INDEFINITE);
        this.maxLevel = maxLevel;

        upgradeButton = Utility.createUpgradeButton();
        setupUpgradeButton();
        Tooltip.install(view, createToolTip());
    }

    public Well(byte maxLevel, byte level) {
        this.maxLevel = maxLevel;
        this.level = level;
        this.capacity = level == maxMaxLevel ? 100 : ((byte) (5 + level * (level + 3) / 2));
        graphic = new Pane();

        progressIndicator = setupProgressIndicator();
        refillTimer = createRefillTimer();
        images = new HashMap<>();
        for (byte i = 0; i <= maxLevel; ++i) {
            Image image = textures.get(i).getImage();
            images.put(i, image);
        }
        view = new ImageView(images.get(level));
        setupViewEffect(view);
        sparkAnimation = Utility.setupSparkAnimation(view);
        graphic.getChildren().add(view);
        graphic.getChildren().add(progressIndicator);
        graphic.setLayoutX(350);
        graphic.setLayoutY(40);
        animation = new SpriteAnimation(generalTimeMultiplier, textures.get(level), view);
        animation.setRelativeRate(2);
        animation.setCycleCount(Animation.INDEFINITE);
        storedWater = capacity;
        upgradeButton = Utility.createUpgradeButton();
        setupUpgradeButton();
        Tooltip.install(view, createToolTip());
    }

    private Tooltip createToolTip() {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-font-size: 16px;" +
                "-fx-shape: \"" + SQUARE_BUBBLE + "\";" +
                "-fx-background-color: #f45942;");
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);

        Text name = new Text("Well!\n");

        name.setFill(Color.GOLD);
        name.setFont(Font.font("Orbitron", 16));
        tooltip.setGraphic(name);
        return tooltip;
    }

    private boolean isPaused = false;

    public void togglePause() {
        if (isPaused) {
            isPaused = false;
            if (isRefilling) {
                refillTimer.start();
                animation.play();
            }
        } else {
            isPaused = true;
            if (isRefilling) {
                refillTimer.stop();
                animation.pause();
            }
        }
    }

    private void setupUpgradeButton() {
        upgradeButton.setLayoutX(120);
        upgradeButton.setLayoutY(100);
        graphic.getChildren().add(upgradeButton);
        upgradeButton.viewOrderProperty().bind(upgradeButton.layoutYProperty().add(100).multiply(-1));
        upgradeButton.setOnAction(event -> {
            int cost = getUpgradeCost();
            if (cost >= 0 && playerCoin.get() >= cost) {
                upgrade();
                playerCoin.set(playerCoin.get() - cost);
            }
        });

        playerCoin.addListener((observable, oldValue, newValue) -> {
            if (getUpgradeCost() > newValue.intValue()) {
                upgradeButton.setDisable(true);
            } else {
                upgradeButton.setDisable(false);
            }
        });
        updateButton();
    }

    public Button getUpgradeButton() {
        return upgradeButton;
    }

    private void setupViewEffect(ImageView view) {
        view.setOnMouseClicked(event -> {
            int cost = getRefillPrice();
            if (playerCoin.get() >= cost) {
                if (refill()) {
                    playerCoin.set(playerCoin.get() - cost);
                }
            }
        });
        view.setOnMouseEntered(event -> view.setEffect(Utility.lightAdjust));
        view.setOnMouseExited(event -> view.setEffect(null));
    }

    public Pane getGraphic() {
        return graphic;
    }

    private Rectangle setupProgressIndicator() {
        Rectangle rectangle = new Rectangle(graphic.getLayoutX() + 20, graphic.getLayoutY() + 113, 3, 113);
        rectangle.setTranslateY(-113);
        rectangle.setFill(Color.rgb(0, 255, 0));
        return rectangle;
    }

    private AnimationTimer createRefillTimer() {
        return new AnimationTimer() {
            private long prev = -1;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now;
                    return;
                }
                double deltaProgress = (now - prev) * generalTimeMultiplier.get() / (timeRemainedToRefill * 1_000_000_000D);
                progress += deltaProgress;

                if (progress < 1) {
                    progressIndicator.setHeight(progress * 113);
                    progressIndicator.setTranslateY(-progress * 113);
                    progressIndicator.setFill(Color.rgb((int) ((1 - progress) * 255), (int) (progress * 255), 0));
                } else {
                    storedWater = capacity;
                    REFILL_SOUND.stop();
                    animation.stop();
                    progress = -1;
                    isRefilling = false;
                    stop();
                    return;
                }
                prev = now;
            }

            @Override
            public void stop() {
                prev = -1;
                super.stop();
            }

            @Override
            public void start() {
                if (progress < 0) {
                    progress = storedWater * 1D / capacity;
                }
                super.start();
            }
        };

    }

    private double progress = -1;

    public double getTimeRemainedToRefill() {
        return timeRemainedToRefill;
    }

    public byte getLevel() {
        return level;
    }

    public byte getStoredWater() {
        return storedWater;
    }

    public boolean useWater() {
        if (storedWater > 0 && !isRefilling) {
            --storedWater;
            double val = (storedWater * 1D / capacity);
            progressIndicator.setHeight(113 * val);
            progressIndicator.setTranslateY(-progressIndicator.getHeight());
            progressIndicator.setFill(Color.rgb((int) (255 * (1 - val)), (int) (255 * val), 0));
            return true;
        }
        return false;
    }

    public boolean isRefilling() {
        return isRefilling;
    }

    private void updateButton() {
        int cost = getUpgradeCost();
        if (cost >= 0) {
            upgradeButton.setText("🔨   " + cost + " ⬤");
        } else {
            upgradeButton.setVisible(false);
        }
    }

    public boolean upgrade() {
        if (level < maxLevel) {
            Utility.UPGRADE_BUILDING.play();
            ++level;
            graphic.getChildren().clear();
            animation.clear();
            view.setImage(images.get(level));
            animation.addTexture(textures.get(level), view);
            graphic.getChildren().addAll(view, progressIndicator, upgradeButton);
            updateButton();
            timeRemainedToRefill = calculateRefillingTime();
            capacity += (level + 1);
        }
        return false;
    }

    public int getUpgradeCost() {
        return level == maxLevel ? -1 : Loader.getElementLevelUpgradeCost("Well", level + 1);
    }

    public int getRefillPrice() {
        return level == maxMaxLevel ? 7 : 19 - 2 * level;
    }

    private int calculateRefillingTime() {
        return level == maxMaxLevel ? 1 : 8 - 2 * level;
    }

    public boolean refill() {
        if (!isRefilling && storedWater < capacity) {
            REFILL_SOUND.play();
            isRefilling = true;
            animation.playFromStart();
            refillTimer.start();
            timeRemainedToRefill = calculateRefillingTime();
            return true;
        }
        return false;
    }


    public void spark() {
        sparkAnimation.start();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Well:\n").
                append("\tLevel = ").append(level).append(", MaxLevel = ").append(maxLevel).append(", Status : ");
        if (isRefilling) {
            s.append("Refilling, Time Remained = ").append(timeRemainedToRefill);
        } else if (storedWater == 0) {
            s.append(" Empty");
        } else
            s.append(storedWater).append(" Water Unit Inside.");
        return s.toString();
    }

    @Override
    public void destruct() {
        refillTimer.stop();
        graphic.getChildren().clear();
    }
}
