package Controllers;

import Player.Host;
import Player.Client;
import Player.Player;
import Utilities.PasswordSkin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jasypt.util.password.BasicPasswordEncryptor;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;

public class MainMenuController {

    private Gson gson;
    private BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
    private Reader reader;
    private HashMap<String, String> players;

    private Stage stage = null;
    private Scene mainMenuScene = null;

    private Player currentPlayer;

    private final TextInputDialog passGetter = new TextInputDialog();
    private final TextInputDialog passSetter = new TextInputDialog();
    private final Alert playerNotFound = new Alert(Alert.AlertType.CONFIRMATION, "Create New Player?", ButtonType.YES, ButtonType.NO);
    private final DialogPane dialogPane1 = playerNotFound.getDialogPane();
    private final Alert wrongPassword = new Alert(Alert.AlertType.ERROR, null, ButtonType.OK);
    private final DialogPane dialogPane2 = wrongPassword.getDialogPane();
    private final Alert multiPlayerAlert = new Alert(Alert.AlertType.CONFIRMATION, "Play As Host?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
    private final DialogPane dialogPane3 = multiPlayerAlert.getDialogPane();
    {
        dialogPane1.lookupButton(ButtonType.NO).setTranslateX(-230);
        dialogPane1.lookupButton(ButtonType.NO).setTranslateY(5);

        dialogPane1.lookupButton(ButtonType.YES).setTranslateX(50);
        dialogPane1.lookupButton(ButtonType.YES).setTranslateY(5);

        dialogPane1.getStylesheets().add("CSSStyles/mainMenuStyle.css");
        dialogPane2.getStylesheets().add("CSSStyles/mainMenuStyle.css");
        dialogPane3.getStylesheets().add("CSSStyles/mainMenuStyle.css");

        dialogPane1.setId("warningDialog");
        dialogPane2.setId("warningDialog");
        dialogPane3.setId("warningDialog");

        playerNotFound.initStyle(StageStyle.UNDECORATED);
        wrongPassword.initStyle(StageStyle.UNDECORATED);
        dialogPane1.setHeaderText("Player Not Found!");
        dialogPane2.setHeaderText("Wrong Password!");
    }

    @FXML
    private TextField playerName;

    @FXML
    private Button soloButton, multiPlayerButton, switchPlayerButton, settingButton, exitButton;

    @FXML
    private void multiPlayerButtonAction(){

        Pane root;
        FXMLLoader loader = new FXMLLoader();

        multiPlayerAlert.showAndWait();
        Client client;
        if(multiPlayerAlert.getResult() == ButtonType.YES){
            Host host = new Host(1234);
            return;
        } else if(multiPlayerAlert.getResult() == ButtonType.NO){
            client = new Client(currentPlayer, 1234, "localHost");
        } else
            return;
        ChatGUIController controller;
        try {
            loader.setLocation(getClass().getResource("/res/FxmlFiles/ChatGUI.fxml"));
            root = loader.load();
            controller = loader.getController();
            controller.setPlayer(client);
            controller.setPrevScene(mainMenuScene);
            client.setChatGuiController(controller);
        } catch (IOException e) {
            e.printStackTrace();
            root = new Pane();
        }
        Scene playerMenuScene = new Scene(root);
        stage.setScene(playerMenuScene);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    @FXML
    private void soloButtonAction() {
        Pane root;
        FXMLLoader loader = new FXMLLoader();

        PlayerMenuController controller;
        try {
            loader.setLocation(getClass().getResource("/res/FxmlFiles/playerMenu.fxml"));
            root = loader.load();
            controller = loader.getController();
            controller.setPlayer(playerName.getText());
            controller.setMainMenuScene(mainMenuScene);
        } catch (IOException e) {
            e.printStackTrace();
            root = new Pane();
        }
        Scene playerMenuScene = new Scene(root);
        stage.setScene(playerMenuScene);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    @FXML
    private void exitButtonAction() {
        Platform.exit();
        System.exit(0);
    }

    private final EventHandler<KeyEvent> textFieldEnterPressed = new EventHandler<>() {
        @Override
        public void handle(KeyEvent event) {
            if (event.getCode() == KeyCode.ENTER) {
                String name = playerName.getText();
                if (name.length() >= 3) {
                    if (!players.containsKey(name)) {
                        playerNotFound.showAndWait();
                        if (playerNotFound.getResult() == ButtonType.YES) {
                            passSetter.showAndWait();
                            passSetter.getEditor().setText("");
                            if(passSetter.getResult() == null){
                                playerName.setText("");
                                return;
                            }
                            String pass = passSetter.getResult();
                            players.put(name, passwordEncryptor.encryptPassword(pass));
                            try {
                                Writer writer = new FileWriter(new File("PlayersData/players.json"));
                                gson.toJson(players, writer);
                                writer.close();
                                currentPlayer = Player.create(name);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            exitButton.requestFocus();
                        } else {
                            playerName.setText("");
                            return;
                        }
                    } else {
                        passGetter.showAndWait();
                        passGetter.getEditor().setText("");
                        if (passGetter.getResult() == null) {
                            playerName.setText("");
                            return;
                        }
                        try {
                            currentPlayer = Player.loadPlayer(name);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        exitButton.requestFocus();
                    }
                    soloButton.setDisable(false);
                    multiPlayerButton.setDisable(false);
                    exitButton.requestFocus();
                }
            }
        }
    };

    @FXML
    public void initialize() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            reader = new FileReader(new File("PlayersData/players.json"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        players = gson.fromJson(reader, type);
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        playerName.textProperty().addListener((observable, oldValue, newValue) -> {
            multiPlayerButton.setDisable(true);
            soloButton.setDisable(true);
        });
        playerName.setOnKeyPressed(textFieldEnterPressed);
        playerName.setTextFormatter(new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }
            String text = change.getControlNewText();
            if (text.matches("[a-zA-Z0-9\\-_]*")) {
                return change;
            }
            return null;
        }));

        setupTextInputDialog(passGetter);
        setupTextInputDialog(passSetter);
        passGetter.getEditor().setSkin(new PasswordSkin(passGetter.getEditor()));
        passSetter.getEditor().setSkin(new PasswordSkin(passSetter.getEditor()));
        passGetter.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION,
                event -> {
                    if (!passwordEncryptor.checkPassword(passGetter.getEditor().getText(), players.get(playerName.getText()))) {
                        wrongPassword.showAndWait();
                        passGetter.getEditor().setText("");
                        event.consume();
                    }
                }
        );
        Platform.runLater(() -> {
            mainMenuScene = exitButton.getScene();
            stage = (Stage) mainMenuScene.getWindow();
            mainMenuScene.getRoot().setOnMouseClicked(event -> exitButton.requestFocus());
        });
    }

    private void setupTextInputDialog(TextInputDialog dialog) {
        dialog.initStyle(StageStyle.UNDECORATED);
        dialog.getDialogPane().getStylesheets().add("CSSStyles/mainMenuStyle.css");
        dialog.getDialogPane().setId("warningDialog");
        dialog.getDialogPane().setHeaderText("Enter Your Password");
        dialog.getEditor().setPromptText("Password");
        dialog.getEditor().setId("passwordField");
        dialog.getDialogPane().lookupButton(ButtonType.OK).setTranslateX(-55);
        dialog.getDialogPane().lookupButton(ButtonType.OK).disableProperty().bind(
                dialog.getEditor().textProperty().length().lessThan(3));
    }
}
