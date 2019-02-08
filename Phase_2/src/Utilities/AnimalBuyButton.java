package Utilities;

import Interfaces.Destructible;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class AnimalBuyButton extends Button implements Destructible {
    private ViewSwitcher viewSwitcher;
    private int price;
    private AnimalType animalType;

    public AnimalBuyButton(String continent, String animalName) {
        super();
        this.animalType = AnimalType.getType(animalName);
        Gson gson = new GsonBuilder().registerTypeAdapter(Sprite.class, new SpriteDeserializer()).create();
        Reader reader = null;
        try {
            reader = new FileReader(new File("src/res/Buttons/AnimalBuyIcons/Config/config.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Sprite texture = gson.fromJson(gson.fromJson(reader, JsonObject.class).getAsJsonObject(continent).getAsJsonObject(animalName), Sprite.class);
        viewSwitcher = new ViewSwitcher(texture);
        viewSwitcher.getImageView().setPickOnBounds(false);
        viewSwitcher.getImageView().setTranslateX(4);
        viewSwitcher.getImageView().setTranslateY(25);
        setStyle("-fx-background-color: transparent;" +
                "-fx-font-family: Orbitron;" +
                "-fx-font-size: 9;" +
                "-fx-text-fill: White;" +
                "-fx-alignment: center;");
        setContentDisplay(ContentDisplay.TOP);
        setPickOnBounds(false);
        setGraphic(viewSwitcher.getImageView());

        disableProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                viewSwitcher.setIndex(3);
                setOpacity(1);
            } else {
                viewSwitcher.setIndex(0);
            }
        });
        setOnMousePressed(event -> viewSwitcher.setIndex(2));
        setOnMouseReleased(event -> {
            if (isHover()) {
                if(!isDisable()) {
                    viewSwitcher.setIndex(1);
                }
            } else {
                viewSwitcher.setIndex(0);
            }
        });
        setOnMouseEntered(event -> {
            if (!isPressed()) {
                viewSwitcher.setIndex(1);
            }
        });

        setOnMouseExited(event -> {
            if (!isPressed() && !isDisable()) {
                viewSwitcher.setIndex(0);
            }
        });
        setTranslateX(-4);
        setTranslateY(-25);
    }

    public AnimalType getAnimalType() {
        return animalType;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int val) {
        this.price = val;
        setText(String.valueOf(val));
    }

    public void destruct() {
        setOnMouseEntered(null);
        setOnMouseExited(null);
        setOnMouseReleased(null);
        setOnMousePressed(null);
        setOnAction(null);
    }
}
