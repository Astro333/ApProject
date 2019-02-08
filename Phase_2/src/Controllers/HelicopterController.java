package Controllers;

import Items.Item;
import Transportation.Helicopter;
import Utilities.HelicopterProductBar;
import Utilities.Utility;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class HelicopterController {
    private Helicopter helicopter;
    @FXML
    private VBox itemsContainer;
    @FXML
    private Button cancel, ok;
    private IntegerProperty playerCoin;
    @FXML
    private Label moneyIndicator, volumeIndicator;

    private ImageView helicopterView;

    @FXML
    private Pane back;

    @FXML
    public void initialize() {
    }

    private void cancelAction() {
        for (int i = 0; i < itemsContainer.getChildren().size(); ++i) {
            HelicopterProductBar bar = (HelicopterProductBar) itemsContainer.getChildren().get(i);
            bar.reset();
        }
        goToPrevScene();
    }

    @SuppressWarnings("Duplicates")
    private void goToPrevScene() {
        Stage stage = (Stage) cancel.getScene().getWindow();
        stage.setScene(levelScene);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    private void okAction() {
        for (int i = 0; i < itemsContainer.getChildren().size(); ++i) {
            HelicopterProductBar bar = (HelicopterProductBar) itemsContainer.getChildren().get(i);
            bar.clear();
        }
        helicopter.go();
        goToPrevScene();
    }

    private Scene levelScene;

    public void setup(LevelController levelController, Helicopter helicopter) {
        this.helicopter = helicopter;
        helicopterView = new ImageView();
        helicopterView.setTranslateX(584 - 216);
        helicopterView.setTranslateY(429 - 164);
        helicopterView.setStyle("-fx-fill: red;");
        back.getChildren().add(helicopterView);
        update();
        moneyIndicator.textProperty().bind(playerCoin.asString());
        BooleanBinding bb = new SimpleBooleanProperty(true).not();
        for (Item.ItemType type : Item.ItemType.values()) {
            if (!type.IS_ANIMAL && type != Item.ItemType.Coin) {
                HelicopterProductBar productBar = Utility.createHelicopterItemBar(type, helicopter);
                bb = bb.or(productBar.amountProperty().greaterThan(0));
                productBar.setPlayerCoin(playerCoin);
                itemsContainer.getChildren().add(productBar);
            }
        }
        helicopter.itemsInsideVolumeProperty().addListener((observable, oldValue, newValue) -> volumeIndicator.setText("☒ "+newValue.doubleValue()+" / "+helicopter.getCapacity()));
        ok.disableProperty().bind(bb.not());
        ok.setOnAction(event -> {
            levelController.toggleMapElementsPause();
            okAction();
        });
        cancel.setOnAction(event -> {
            levelController.toggleMapElementsPause();
            cancelAction();
        });
    }
    public void update(){
        volumeIndicator.setText("☒ "+helicopter.getItemsInsideVolume()+" / "+helicopter.getCapacity());
        helicopterView.setImage(new Image(getClass().getResource(
                "/res/Transportation/Helicopter/Textures/Menu/Images/0"+(helicopter.getLevel()+1)+".png").toExternalForm()));
    }
    public void setPlayerCoin(IntegerProperty playerCoin) {
        this.playerCoin = playerCoin;
    }

    public void setLevelScene(Scene levelScene) {
        this.levelScene = levelScene;
    }
}
