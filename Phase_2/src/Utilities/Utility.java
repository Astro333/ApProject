package Utilities;

import Interfaces.Processable;
import Transportation.Helicopter;
import Transportation.Truck;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class Utility {

    public static final AudioClip FOOL_ACTION_SOUND = new AudioClip(Utility.class.getResource("/res/CommonSounds/fool_action.mp3").toExternalForm());
    public static final AudioClip UPGRADE_BUILDING = new AudioClip(Utility.class.getResource("/res/CommonSounds/action_upgrade.mp3").toExternalForm());
    public static final AudioClip BUILDING_CLICK = new AudioClip(Utility.class.getResource("/res/CommonSounds/house_click.mp3").toExternalForm());
    public static final AudioClip BATTLE_SOUND = new AudioClip(Utility.class.getResource("/res/CommonSounds/battle.mp3").toExternalForm());
    public static final AudioClip LEVEL_FINISHED = new AudioClip(Utility.class.getResource("/res/CommonSounds/fanfare_level_complete.mp3").toExternalForm());
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(Sprite.class, new SpriteDeserializer()).create();
    public static final Image CHECK_MARK = new Image(Utility.class.getResource("/res/other/checkMark.png").toExternalForm());
    public static final ColorAdjust lightAdjust = new ColorAdjust(0, 0, 0.2, 0);
    public static AnimationTimer setupSparkAnimation(Node node) {
        return new AnimationTimer() {
            long prev = -1;
            int times = 0;

            boolean isStart = false;

            @Override
            public void handle(long now) {
                if (prev == -1) {
                    prev = now;
                    return;
                }
                if (times < 8) {
                    if (now - prev >= 200_000_000) {
                        if (times % 2 == 0) {
                            node.setEffect(lightAdjust);
                        } else
                            node.setEffect(null);
                        ++times;
                        prev = now;
                    }
                } else
                    stop();
            }

            @Override
            public void start() {
                if (!isStart) {
                    isStart = true;
                    super.start();
                }
            }

            @Override
            public void stop() {
                if (isStart) {
                    times = 0;
                    isStart = false;
                    super.stop();
                }
            }
        };
    }

    public static final String SQUARE_BUBBLE =
            "M24 1h-24v16.981h4v5.019l7-5.019h13z";

    public static Button createUpgradeButton(){
        Button button = new Button();
        button.getStylesheets().add(Utility.class.getResource("/Utilities/CssStyles/upgradeButtonStyle.css").toExternalForm());
        return button;
    }

    public static HelicopterProductBar createHelicopterItemBar(Processable processable, Helicopter helicopter){
        HelicopterProductBar productBar = new HelicopterProductBar(processable, helicopter);
        productBar.setFillHeight(false);
        productBar.setAlignment(Pos.CENTER_LEFT);
        productBar.setSpacing(10);
        productBar.setStyle("-fx-border-width: 1 1 1 1;" +
                "-fx-border-color: Orange;");
        return productBar;
    }

    public static HBox createAnimalsMenu(String continent){
        HBox hBox = new HBox();
        hBox.setPrefWidth(Region.USE_PREF_SIZE);
        Reader reader = null;
        try {
            reader = new FileReader(new File("src/res/Buttons/AnimalBuyIcons/Config/config.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JsonObject buttons = gson.fromJson(reader, JsonObject.class).getAsJsonObject(continent);
        for(String animal : buttons.keySet()){
            AnimalBuyButton button = new AnimalBuyButton(continent, animal);
            hBox.getChildren().add(button);
            button.setPrice(Loader.getAnimalBuyCost(AnimalType.getType(animal)));
        }
        return hBox;
    }

    public static TruckProductBar createTruckProductBar(Processable processable, int amountAvail, Truck truck) {
        TruckProductBar productBar = new TruckProductBar(processable, amountAvail, truck);
        productBar.setFillHeight(false);
        productBar.setAlignment(Pos.CENTER_LEFT);
        productBar.setSpacing(7);
        productBar.setStyle("-fx-border-width: 1 1 1 1;" +
                "-fx-border-color: Orange;");
        return productBar;
    }

    public static MoneyIndicator createMoneyIndicator(){
        return new MoneyIndicator();
    }
}
