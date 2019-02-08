package Controllers;

import Animals.Animal;
import Exceptions.PlayerNotFoundException;
import Interfaces.Processable;
import Levels.SaveData;
import Player.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;

public class PlayerMenuController {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Animal.class, new Utilities.AnimalDeserializer()).
            registerTypeAdapter(Processable.class, new Utilities.ProcessableDeserializer()).create();

    private String playerDataPath = null;
    private Player player = null;
    private Scene mainMenuScene = null;
    private Scene playerMenuScene = null;
    private Stage stage = null;
    private Scene shopScene = null;
    private ShopController shopController = null;

    @FXML
    private Button shopButton;

    @FXML
    private VBox levelsContainer;

    public void setMainMenuScene(Scene scene) {
        if (mainMenuScene == null) {
            mainMenuScene = scene;
        }
    }

    public void setPlayer(String name) throws PlayerNotFoundException {
        if (player == null) {
            try {
                player = Player.loadPlayer(name);
                playerDataPath = "PlayersData/Players/" + name;
            } catch (FileNotFoundException e) {
                throw new PlayerNotFoundException("Player Data Not Found!");
            }
        }
    }

    public void setPlayer(Player player) {
        this.player = player;
        playerDataPath = "PlayersData/Players/" + player.getName();
    }

    @FXML
    public void initialize() {
        for (int i = 0; i < levelsContainer.getChildren().size(); ++i) {
            HBox hBox = (HBox) levelsContainer.getChildren().get(i);
            for (int j = 0; j < hBox.getChildren().size(); ++j) {
                Button button = (Button) hBox.getChildren().get(j);
                button.setOnMouseClicked(event -> {
                    try {
                        runLevel(Integer.parseInt(button.getText()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        Platform.runLater(() -> {
            playerMenuScene = shopButton.getScene();
            stage = (Stage) playerMenuScene.getWindow();
        });
    }

    @FXML
    private void backToMainMenu() {
        Stage stage = (Stage) shopButton.getScene().getWindow();
        stage.setScene(mainMenuScene);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    private void runLevel(int levelId) throws FileNotFoundException {
        String path = "DefaultGameData/LevelsInfo/level_" + levelId + ".json";
        Pane root;
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/res/FxmlFiles/map.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            root = new Pane();
        }
        Scene levelScene = new Scene(root);
        LevelController controller = loader.getController();
        controller.setPrevScene(playerMenuScene);
        try {
            controller.setup((byte) levelId, player);
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.setScene(levelScene);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    private void printLevels() throws FileNotFoundException {
        int i = 1;
        File levelFile = new File("DefaultGameData/LevelsInfo/level_1.json");
        while (levelFile.exists()) {
            System.out.println("**********************************");
            System.out.println("##############");
            System.out.printf("## Level %02d ##\n", i);
            System.out.println("##############");
            Reader reader = new BufferedReader(new FileReader(levelFile));
            JsonObject object = gson.fromJson(reader, JsonObject.class);
            for (String s : object.keySet())
                System.out.println(s + " : " + object.get(s).toString());
            ++i;
            levelFile = new File("DefaultGameData/LevelsInfo/level_" + i + ".json");
        }
    }

    /*private void loadSavedGame(String jsonFileName) {
     *//*
     * find it in Player_Unfinished_Levels_Saves under pathToPlayerDataDirectory
     * *//*
        String path = playerDataPath + "/Player_Unfinished_Levels_Saves/" + jsonFileName;
        Reader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        SaveData saveData = gson.fromJson(reader, SaveData.class);
        try {
            final LevelController levelController = new LevelController(saveData, player);
            levelController.startProcessing();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/
    @FXML
    private void goToShop() {
        if (shopScene == null) {
            Pane pane = new Pane();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/res/FxmlFiles/shop.fxml"));
            try {
                pane = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            shopScene = new Scene(pane);
            shopController = loader.getController();
        }
        shopController.setup(player);
        Stage stage = (Stage) shopButton.getScene().getWindow();
        stage.setScene(shopScene);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }
}
