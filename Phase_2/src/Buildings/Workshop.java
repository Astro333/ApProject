package Buildings;

import Interfaces.Destructible;
import Interfaces.Processable;
import Items.Item;
import Utilities.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
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
import java.util.LinkedList;

import static Utilities.Utility.SQUARE_BUBBLE;

public class Workshop implements Destructible {
    private static IntegerProperty playerCoin;
    private static DoubleProperty GENERAL_TIME_MULTIPLIER;


    public static void setGeneralTimeMultiplier(DoubleProperty GENERAL_TIME_MULTIPLIER) {
        Workshop.GENERAL_TIME_MULTIPLIER = GENERAL_TIME_MULTIPLIER;
    }

    public static void setPlayerCoin(IntegerProperty playerCoin) {
        Workshop.playerCoin = playerCoin;
    }

    private static final HashMap<String, HashMap<Integer, Sprite>> textures;
    private Pane graphic;
    private SpriteAnimation animation;
    private transient Depot depot;
    private ImageView workshopView;

    public int getProcessingMultiplier() {
        return processingMultiplier;
    }

    private int processingMultiplier;
    private int multiplier = 1;

    public String getRealName() {
        return realName;
    }

    static {
        textures = new HashMap<>();
    }

    private final Item.ItemType[] inputs;

    private final Integer[] inputsAmount;

    public final double dropZoneX;
    public final double dropZoneY;

    private final Processable[] outputs;

    public int[] getOutputsAmount() {
        return outputsAmount;
    }

    private final int[] outputsAmount;

    private final byte[] productionTime;

    private final String realName;
    private final String demonstrativeName;

    private final byte maxMaxLevel;
    private final byte maxLevel;

    private byte level;

    public static void setupTextures(LinkedList<String> workshops) {
        textures.clear();
        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Sprite.class, new SpriteDeserializer()).create();
        HashMap<Integer, Sprite> temp;
        Type type = new TypeToken<HashMap<Integer, Sprite>>() {
        }.getType();
        for (String workshop : workshops) {
            try {
                Reader reader = new FileReader(new File("src/res/Workshops/" + workshop + "/Textures/Config/texture.json"));
                temp = gson.fromJson(reader, type);
                textures.put(workshop, temp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private final AnimationTimer workingTimer;
    public Double itemProduceX, itemProduceY;

    private static ColorAdjust adjust = new ColorAdjust(0, 0, 0.3, 0);

    private final Rectangle progressIndicator;
    private static Gson gson = new GsonBuilder().registerTypeAdapter(Processable.class, new ProcessableDeserializer()).create();

    private byte position;
    private String continent;
    private ImageView place;
    private ImageView subPicture;

    public Button getUpgradeButton() {
        return upgradeButton;
    }

    private Button upgradeButton;

    public Workshop(String name, byte position, String continent, int maxLevel, int level) {
        this.level = (byte) level;
        this.maxLevel = (byte) maxLevel;
        this.position = position;
        this.continent = continent;
        graphic = new Pane();
        Sprite texture = textures.get(name).get(level);

        int[] pos = Loader.getWorkshopConfig(continent, position);

        JsonObject data = Loader.loadWorkshopData(name, continent);
        demonstrativeName = data.get("demonstrativeName").getAsString();
        realName = name;
        inputs = gson.fromJson(data.get("inputs").getAsJsonArray(), Item.ItemType[].class);
        inputsAmount = gson.fromJson(data.get("inputsAmount").getAsJsonArray(), Integer[].class);
        outputs = gson.fromJson(data.get("outputs").getAsJsonArray(), Processable[].class);
        outputsAmount = gson.fromJson(data.get("outputsAmount").getAsJsonArray(), int[].class);
        productionTime = gson.fromJson(data.get("productionTime").getAsJsonArray(), byte[].class);
        maxMaxLevel = data.get("maxMaxLevel").getAsByte();

        itemProduceX = Loader.loadContinentData(continent).getAsJsonObject("place" + (position)).get("itemProduceX").getAsDouble();
        itemProduceY = Loader.loadContinentData(continent).getAsJsonObject("place" + (position)).get("itemProduceY").getAsDouble();

        upgradeButton = Utility.createUpgradeButton();
        upgradeButton.viewOrderProperty().bind(upgradeButton.layoutYProperty().add(100).multiply(-1));
        upgradeButton.setOnAction(event -> {
            int cost = getUpgradeCost();
            if (cost >= 0 && playerCoin.get() >= cost) {
                playerCoin.set(playerCoin.get() - cost);
                upgrade();
            }
        });

        playerCoin.addListener((observable, oldValue, newValue) -> {
            if (getUpgradeCost() > newValue.intValue()) {
                upgradeButton.setDisable(true);
            } else {
                upgradeButton.setDisable(false);
            }
        });

        workshopView = new ImageView(texture.getImage());
        workshopView.setLayoutX(pos[0] + texture.getRelativeOffset()[0]);
        workshopView.setLayoutY(pos[1] + texture.getRelativeOffset()[1] - texture.getFrameSize()[1]);
        progressIndicator = new Rectangle(workshopView.getLayoutX(), workshopView.getLayoutY() + 150, 6, 0);
        progressIndicator.setFill(Color.rgb(0, 255, 0));
        place = new ImageView(new Image(getClass().getResource(
                "../res/Map/" + continent + "/Textures/Images/Back/place" + position + ".png").toExternalForm()));
        place.setLayoutX(pos[2]);
        place.setLayoutY(pos[3]);

        subPicture = new ImageView(new Image(getClass().getResource(
                "../res/Map/" + continent + "/Textures/Images/Back/" + continent.toLowerCase() + "_0" + position + ".png")
                .toExternalForm()));
        subPicture.setLayoutX(pos[4]);
        subPicture.setLayoutY(pos[5]);
        dropZoneX = pos[6];
        dropZoneY = pos[7];
        animation = new SpriteAnimation(GENERAL_TIME_MULTIPLIER, texture, workshopView);
        animation.setRelativeRate(2);
        graphic.setPickOnBounds(false);
        graphic.getChildren().add(place);
        graphic.getChildren().add(subPicture);
        graphic.getChildren().add(workshopView);
        graphic.getChildren().add(progressIndicator);
        graphic.getChildren().add(upgradeButton);

        place.setViewOrder(-1);
        subPicture.setViewOrder(-2);
        workshopView.setViewOrder(-3);
        progressIndicator.setViewOrder(-4);

        animation.setCycleCount(Animation.INDEFINITE);
        animation.setInterpolator(Interpolator.LINEAR);
        workingTimer = setupWorkingTimer();
        workshopView.setOnMouseClicked(event -> {
            Utility.BUILDING_CLICK.play();
            if (progress < -0.99) {
                status.set(status.get() | State.Waiting.value);
            } else {
                progress += event.getClickCount() * 0.01;
            }
        });
        workshopView.setOnMouseEntered(event -> {
            workshopView.setEffect(adjust);
        });
        workshopView.setOnMouseExited(event -> workshopView.setEffect(null));

        updateButton();
        Tooltip tooltip = createToolTip();
        Tooltip.install(workshopView, tooltip);
    }

    private Tooltip createToolTip() {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle("-fx-font-size: 16px;" +
                "-fx-shape: \"" + SQUARE_BUBBLE + "\";" +
                "-fx-background-color: #3277f9;");
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);

        VBox vBox = new VBox();
        vBox.setSpacing(15);
        vBox.setAlignment(Pos.CENTER);
        Text name = new Text(demonstrativeName);
        name.setFill(Color.GOLD);
        name.setFont(Font.font("Orbitron", 16));
        vBox.getChildren().add(name);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        ImageView[] inputsViews = new ImageView[inputs.length];
        ImageView[] outputsViews = new ImageView[outputs.length];
        for (int i = 0; i < inputs.length; ++i) {
            Item.ItemType type = inputs[i];
            inputsViews[i] = new ImageView(Item.images.get(type));
            inputsViews[i].setTranslateY(-6);
            hBox.getChildren().add(inputsViews[i]);
        }
        Text text = new Text("➞");
        text.setFont(Font.font(20));
        text.setFill(Color.GOLD);
        text.setTranslateY(-5);
        hBox.getChildren().add(text);
        for (int i = 0; i < outputs.length; ++i) {
            Processable p = outputs[i];
            if (p instanceof AnimalType) {
                System.out.println(p);
                outputsViews[i] = new ImageView(new Image(getClass().getResource("/res/Products/Animals/Images/" + p.toString() + ".png").toExternalForm()));
            } else {
                outputsViews[i] = new ImageView(Item.images.get(p));
            }
            outputsViews[i].setTranslateY(-6);
            hBox.getChildren().add(outputsViews[i]);
        }
        vBox.getChildren().add(hBox);
        tooltip.setGraphic(vBox);
        tooltip.setText("");
        return tooltip;
    }

    public Integer[] getInputsAmount() {
        return inputsAmount;
    }

    public Item.ItemType[] getInputs() {
        return inputs;
    }

    private void updateButton() {
        int cost = getUpgradeCost();
        if (cost >= 0) {
            upgradeButton.setText("🔨   " + cost + " ⬤");
        } else {
            upgradeButton.setVisible(false);
        }
    }

    public Processable[] getOutputs() {
        return outputs;
    }


    public int getUpgradeCost() {
        return level == maxLevel ? Integer.MIN_VALUE : Loader.getElementLevelUpgradeCost(realName + "Workshop", level + 1);
    }

    private void switchTextures() {
        Sprite texture = textures.get(realName).get((int) level);
        workshopView.setImage(texture.getImage());
        int[] pos = Loader.getWorkshopConfig(continent, position);
        workshopView.setLayoutX(pos[0] + texture.getRelativeOffset()[0]);
        workshopView.setLayoutY(pos[1] + texture.getRelativeOffset()[1] - texture.getFrameSize()[1]);
        workshopView.setOnMouseClicked(event -> {
            Utility.BUILDING_CLICK.play();
            if (progress < -0.99) {
                status.set(status.get() | State.Waiting.value);
            } else {
                progress += event.getClickCount() * 0.01;
            }
        });
        workshopView.setOnMouseEntered(event -> {

            workshopView.setEffect(adjust);
        });
        workshopView.setOnMouseExited(event -> workshopView.setEffect(null));

        animation.clear();
        animation.addTexture(texture, workshopView);
        graphic.getChildren().clear();
        graphic.getChildren().addAll(place, subPicture, workshopView, progressIndicator, upgradeButton);
        place.setViewOrder(-1);
        subPicture.setViewOrder(-2);
        workshopView.setViewOrder(-3);
        progressIndicator.setViewOrder(-4);
        updateButton();
    }

    private void showInformation() {
        System.out.println(inputs[0]);
    }

    private void hideInformation() {

    }

    public boolean upgrade() {
        if (level < maxLevel) {
            Utility.UPGRADE_BUILDING.play();
            ++level;
            ++multiplier;
            timeToFinishTask = calculateTimeToFinishTask();
            switchTextures();
            return true;
        }
        return false;
    }

    public int calculateAmountNeeded(Depot depot) {
        if (progress < -0.9) {
            int multiplier = this.multiplier;
            int temp;
            for (int i = 0; i < inputs.length; ++i) {
                temp = depot.getItemAmount(inputs[i]) / inputsAmount[i];
                if (temp == 0) {
                    Utility.FOOL_ACTION_SOUND.play();
                    depot.spark();
                    status.set(status.get() & (~State.Waiting.value));
                    System.err.println("Not Enough Components in Depot.");
                    return -1;
                }
                if (temp < multiplier)
                    multiplier = temp;
            }
            processingMultiplier = multiplier;
            return processingMultiplier;
        }
        System.err.println("Another task is being done.");
        return -1;
    }

    public boolean startWorking() {
        if (processingMultiplier > 0) {
            status.set(State.Working.value);
            timeToFinishTask = calculateTimeToFinishTask();
            progress = 0;
            animation.playFromStart();
            workingTimer.start();
            return true;
        }
        return false;
    }

    private int timeToFinishTask;

    private int calculateTimeToFinishTask() {
        return productionTime[level];
    }

    private AnimationTimer setupWorkingTimer() {
        return new AnimationTimer() {
            long prev = -1;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now;
                    return;
                }
                double deltaProgress = (now - prev) * GENERAL_TIME_MULTIPLIER.get() / (1_000_000_000D * timeToFinishTask);
                if (progress + deltaProgress > 1) {
                    progressIndicator.setFill(Color.rgb(0, 0, 255));
                    status.set(status.get() | State.FinishedJob.value);
                    progress = -1;
                    animation.stop();
                    stop();
                    return;
                }
                progress += deltaProgress;
                progressIndicator.setHeight(progress * 113);
                progressIndicator.setTranslateY(-progress * 113);
                progressIndicator.setFill(Color.rgb((int) ((1 - progress) * 255), (int) (progress * 255), 0));
                prev = now;
            }

            @Override
            public void stop() {
                prev = -1;
                super.stop();
            }
        };
    }

    public int getStatus() {
        return status.get();
    }

    public IntegerProperty statusProperty() {
        return status;
    }

    private IntegerProperty status = new SimpleIntegerProperty(this, "status", 0);

    private double progress = -1;

    public Pane getGraphic() {
        return graphic;
    }

    public void destruct() {
        animation.stop();
        animation.clear();
        workingTimer.stop();
        depot = null;
        playerCoin = null;
    }

    private boolean isPaused = false;

    public void togglePause() {
        if (isPaused) {
            isPaused = false;
            if (progress >= 0) {
                animation.play();
                workingTimer.start();
            }
        } else {
            isPaused = true;
            if (progress >= 0) {
                animation.pause();
                workingTimer.stop();
            }
        }
    }
}
