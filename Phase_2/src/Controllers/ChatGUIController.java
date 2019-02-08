package Controllers;

import Player.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;

public class ChatGUIController {

    private double deltaX, deltaY;
    private Client player;
    private HashMap<Integer, Socket> connected_Peers;

    @FXML
    private Button sendButton, attachFile;

    private Scene prevScene = null;
    private Scene currentScene = null;

    @FXML
    private TextField serverPortNumber;

    @FXML
    private TextArea message;

    @FXML
    private VBox messageContainer, peerContainer;

    @FXML
    private ScrollPane scroll;

    public void setPrevScene(Scene prevScene){
        this.prevScene = prevScene;
    }

    public void setPlayer(Client player) {
        this.player = player;
        serverPortNumber.setText("" + player.getServerPort());
        serverPortNumber.setEditable(false);
    }

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            message.setOnKeyPressed(event -> {
                if (message.getText().length() > 0) {
                    if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                        sendButtonAction();
                    }
                }
            });
            scroll.setFitToHeight(false);
            currentScene = message.getScene();
        });
    }

    public void displayStringMessage(String message, Pos pos, String playerName) {
        HBox hBox = new HBox();
        Label text = new Label(message.replaceAll("(.{30})", "$1\n"));
        text.getStyleClass().add("messages");
        hBox.getChildren().add(text);
        hBox.setAlignment(pos);
        text.setAlignment(Pos.TOP_LEFT);
        scroll.setFitToHeight(true);
        text.setMinHeight(Region.USE_PREF_SIZE);
        Platform.runLater(() -> {
            messageContainer.getChildren().add(hBox);
            messageContainer.getChildren().add(new Label());
        });
    }
    public void goToPrevScene(){
        Stage stage = (Stage) currentScene.getWindow();
        stage.setScene(prevScene);
    }
    @FXML
    private void attachFileOnAction() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Choose Your File");
        File file = fileChooser.showOpenDialog(message.getScene().getWindow());

        String fileType = Files.probeContentType(file.toPath());
        if (fileType != null) {
            switch (fileType.split("/")[0].trim()) {
                case "image":
                    player.sendImage(file);
                    break;
                case "text":
                    player.sendText(file);
                    break;
                case "audio":
                    player.sendAudio(file);
                    break;
                default:
                    player.sendOtherFile(file);
                    break;
            }
        }
    }

    @FXML
    private void sendButtonAction() {
        if (message.getText().length() > 0) {
            String s = message.getText();
            player.sendMessage(s);
            displayStringMessage(s, Pos.CENTER_RIGHT, player.getName());
            message.setText("");
        }
    }

    @FXML
    private void toolbarOnMousePressed(MouseEvent e) {
        deltaX = message.getScene().getWindow().getX() - e.getScreenX();
        deltaY = message.getScene().getWindow().getY() - e.getScreenY();
    }

    @FXML
    private void toolbarOnMouseDragged(MouseEvent e) {
        message.getScene().getWindow().setX(e.getScreenX() + deltaX);
        message.getScene().getWindow().setY(e.getScreenY() + deltaY);
    }

    public void displayImage(File image, Pos pos, String srcPlayer) {
        Image file = new Image(image.toURI().toString());
        ImageView view = new ImageView(file);
        HBox hBox = new HBox();
        view.setFitWidth(200);
        view.setFitHeight(160);
        hBox.setAlignment(pos);
        view.setOnMouseClicked(e -> {
            try {
                Desktop.getDesktop().open(image);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        hBox.getChildren().add(view);
        Platform.runLater(() -> {
            messageContainer.getChildren().add(hBox);
            messageContainer.getChildren().add(new Label());
        });

    }

    public void displayAudio(File file, Pos pos, String srcPlayer) {
        Media audio = new Media(file.toURI().toString());
        MediaPlayer player = new MediaPlayer(audio);
        HBox hBox = new HBox();
        Label text = new Label("\n" + file.getName() + "\n\n");
        text.getStyleClass().add("files");
        hBox.getChildren().add(text);
        hBox.setAlignment(pos);
        text.setAlignment(Pos.TOP_LEFT);
        Button start = new Button("►");
        start.setCursor(Cursor.HAND);
        start.getStyleClass().add("button");
        start.setPickOnBounds(false);
        System.out.println(player.getStatus());
        player.setVolume(1);
        start.setOnAction(new EventHandler<>() {
            private boolean toggle = false;
            @Override
            public void handle(ActionEvent event) {
                if (toggle) {
                    player.pause();
                    toggle = !toggle;
                    start.setText("►");
                } else {
                    toggle = true;
                    player.play();
                    start.setText("⏸");
                }
            }
        });
        hBox.getChildren().add(start);
        text.setOnMouseClicked(e -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        scroll.setFitToHeight(true);
        text.setMinHeight(Region.USE_PREF_SIZE);
        Platform.runLater(() -> {
            messageContainer.getChildren().add(hBox);
            messageContainer.getChildren().add(new Label());
        });
    }

    public void displayFile(File file, Pos pos, String srcPlayer) {
        HBox hBox = new HBox();
        Label text = new Label("\n" + file.getName() + "\n\n");
        text.getStyleClass().add("files");
        hBox.getChildren().add(text);
        hBox.setAlignment(pos);
        text.setAlignment(Pos.TOP_LEFT);
        text.setOnMouseClicked(e -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        scroll.setFitToHeight(true);
        text.setMinHeight(Region.USE_PREF_SIZE);
        Platform.runLater(() -> {
            messageContainer.getChildren().add(hBox);
            messageContainer.getChildren().add(new Label());
        });
    }
    private boolean isOn = true;
    public boolean isOn() {
        return isOn;
    }
}
