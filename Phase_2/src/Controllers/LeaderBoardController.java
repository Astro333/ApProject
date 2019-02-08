package Controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.HashMap;

public class LeaderBoardController {

    private HashMap<String, HBox> playersData;
    private Scene mainMenuScene;
    @FXML
    private VBox scoreContainer;

    void setMainMenuScene(Scene scene) {
        mainMenuScene = scene;
    }

    @FXML
    public void initialize() {
        playersData = new HashMap<>();
    }

    public void addPlayer(String playerName, int goldMoney, int money, int levelsFinished) {
        final Text number = new Text("#" + String.format("%02d", 1) + "");
        final TextField playerNameText = new TextField();
        final TextField goldMoneyText = new TextField();
        final TextField moneyText = new TextField();
        final TextField levelsFinishedText = new TextField();
        playerNameText.setText(playerName);
        goldMoneyText.setText("" + goldMoney);
        moneyText.setText("" + money);
        levelsFinishedText.setText("" + levelsFinished);
        playerNameText.setEditable(false);
        moneyText.setEditable(false);
        levelsFinishedText.setEditable(false);
        goldMoneyText.setEditable(false);

        playerNameText.setAlignment(Pos.CENTER);
        goldMoneyText.setAlignment(Pos.CENTER);
        moneyText.setAlignment(Pos.CENTER);
        levelsFinishedText.setAlignment(Pos.CENTER);

        HBox hBox = new HBox();
        hBox.getChildren().addAll(number, playerNameText, goldMoneyText, moneyText, levelsFinishedText);
        hBox.getStylesheets().add("CSSStyles/ScoreBoardStyle.css");
        playerNameText.getStyleClass().add("text-field-word");

        goldMoneyText.getStyleClass().add("numbers");
        moneyText.getStyleClass().add("numbers");
        levelsFinishedText.getStyleClass().add("numbers");

        number.getStyleClass().add("texts");
        hBox.setAlignment(Pos.CENTER_LEFT);
        scoreContainer.getChildren().add(hBox);
        playersData.put(playerName, hBox);
    }

    public void updatePlayerData(String name, int goldMoney, int money, int levelsFinished) {
        if (playersData.containsKey(name)) {
            HBox data = playersData.get(name);
            ((TextField) data.getChildren().get(2)).setText("" + goldMoney);
            ((TextField) data.getChildren().get(3)).setText("" + money);
            ((TextField) data.getChildren().get(4)).setText("" + levelsFinished);
        } else {
            addPlayer(name, goldMoney, money, levelsFinished);
        }
    }

    @FXML
    private void returnButtonAction() {
        Stage stage = ((Stage) (scoreContainer.getScene().getWindow()));
        stage.setScene(mainMenuScene);
        isOn = false;
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    private boolean isOn = false;

    public boolean isOn() {
        return isOn;
    }
}
