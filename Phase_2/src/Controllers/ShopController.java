package Controllers;

import Player.Player;
import Utilities.Loader;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.*;
import java.util.HashMap;

public class ShopController {

    @FXML
    private VBox elementContainer;

    private Scene prev = null;

    private Player player = null;

    private HashMap<String, String> elementsImagePath;

    @FXML
    public void initialize() {
        Gson gson = new Gson();
        elementsImagePath = new HashMap<>();
        Reader reader = null;
        try {
            reader = new FileReader(new File("src/res/Shop/loader.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JsonObject object = gson.fromJson(reader, JsonObject.class);
        for (String element : object.keySet()) {
            elementsImagePath.put(element, object.getAsJsonObject(element).get("path").getAsString());
        }
    }

    public void setPrevScene(Scene prev) {
        this.prev = prev;
    }

    public void setup(Player player) {
        this.player = player;

        int i = 0;
        HBox box = new HBox();
        for (String element : elementsImagePath.keySet()) {
            if ((i % 3) == 0) {
                box = new HBox();
                elementContainer.getChildren().add(box);
                box.setSpacing(5);
            }
            VBox vBox = new VBox();
            vBox.setId("element");
            vBox.setSpacing(5);
            vBox.setFillWidth(false);
            ImageView imageView = new ImageView();
            imageView.setFitWidth(186);
            imageView.setFitHeight(97.5);
            Button upgradeButton = new Button();
            updateButton(element, imageView, upgradeButton);
            vBox.getChildren().addAll(imageView, upgradeButton);
            upgradeButton.setOnAction(event -> {
                int cost1 = Loader.getElementMaxLevelUpgradeCost(element, player.getGameElementLevel(element) + 1);
                if (player.getGoldMoney() >= cost1) {
                    player.addGoldMoney(-cost1);
                    updateButton(element, imageView, upgradeButton);
                    try {
                        Player.updatePlayer(player);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            box.getChildren().add(vBox);
            ++i;
        }
    }

    private void updateButton(String element, ImageView imageView, Button button) {
        int level1 = player.getGameElementsLevel().get(element);
        System.out.println(element + " "+level1);
        int cost1 = Loader.getElementMaxLevelUpgradeCost(element, level1 + 1);

        if (cost1 >= 0) {
            button.setText(cost1 + " ⬤");
            String path = "/" + elementsImagePath.get(element) + "/0" + ((level1 + 1) + 1) + ".png";
            System.out.println(path);
            imageView.setImage(new Image(getClass().getResource(path).toExternalForm()));
            player.getGameElementsLevel().computeIfPresent(element, (k, v) -> v + 1);
        } else {
            button.setText("Max");
            imageView.setImage(new Image(getClass().getResource("/" + elementsImagePath.get(element) + "/0" + ((level1 + 1)) + ".png").toExternalForm()));
            button.setDisable(true);
        }
    }
}
