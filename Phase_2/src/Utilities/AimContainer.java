package Utilities;

import Interfaces.Processable;
import Items.Item;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class AimContainer extends HBox {
    private HashMap<Processable, Aim> aims;

    private static class Aim extends VBox {
        private Processable processable;
        private int amount;
        private int id;

        private Label text;

        private Aim(Processable processable, int amount, int id) {
            this.processable = processable;
            this.amount = amount;
            this.id = id;
            setStyle(
                    "-fx-alignment: center;" +
                            "-fx-spacing: 0;");
            Image image = processable instanceof AnimalType ?
                    new Image("/res/Products/Animals/Images/" + processable + ".png") :
                    Item.images.get(processable);
            ImageView view = new ImageView(image);
            view.setFitHeight(33);
            view.setFitWidth(33);
            getChildren().add(view);
            text = new Label("0\n━━\n" + amount);
            text.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: gold;" +
                            "-fx-font-family: IONA-U1;" +
                            "-fx-font-size: 13;" +
                            "-fx-alignment: center;" +
                            "-fx-text-alignment: center;");
            getChildren().add(text);

            setOnMouseEntered(event -> {
                setEffect(Utility.lightAdjust);
                text.setTextFill(Color.valueOf("#3277f9"));
            });
            setOnMouseExited(event -> {
                setEffect(null);
                text.setTextFill(Color.GOLD);
            });
        }

        private boolean isAchieved = false;

        void update(int val) {
            if (!isAchieved) {
                if (val >= amount) {
                    setAchieved();
                } else {
                    text.setText(val + "\n━━\n" + amount);
                }
            }
        }

        void setAchieved() {
            if (!isAchieved) {
                isAchieved = true;
                ImageView view = new ImageView(Utility.CHECK_MARK);
                view.setFitHeight(47);
                view.setFitWidth(44);
                getChildren().remove(text);
                getChildren().add(view);
            }
        }
    }

    public AimContainer(Processable[] processables, int[] amounts) {
        aims = new HashMap<>(aims.keySet().size());
        setStyle(
                "-fx-background-color: radial-gradient(radius 200%, #3277f9, derive(#de8c62, -30%), derive(burlywood, 30%));" +
                        "-fx-spacing: 20;" +
                        "-fx-alignment: center;" +
                        "-fx-padding: 10 10 10 10");
        for (int i = 0; i < processables.length; ++i) {
            Aim aim = new Aim(processables[i], amounts[i], i);
            aim.setOnMouseClicked(event -> System.out.println(aim.processable));
            aims.put(processables[i], aim);
            getChildren().add(aim);
        }
    }

    public AimContainer(ObservableMap<Processable, Integer> levelGoals, HashMap<Processable, Integer> initValue) {
        this.aims = new HashMap<>(initValue.keySet().size());
        setStyle(
                "-fx-background-color: radial-gradient(radius 200%, #3277f9, derive(#de8c62, -30%), derive(burlywood, 30%));" +
                        "-fx-spacing: 20;" +
                        "-fx-alignment: center;" +
                        "-fx-padding: 10 10 10 10");
        Iterator<Processable> it = initValue.keySet().iterator();
        for (int i = 0; it.hasNext(); ++i) {
            Processable processable = it.next();
            Aim aim = new Aim(processable, initValue.get(processable), i);
            aim.setOnMouseClicked(event -> System.out.println(aim.processable));
            this.aims.put(processable, aim);
            getChildren().add(aim);
            aim.update(levelGoals.get(processable));
        }
        levelGoals.addListener((MapChangeListener<Processable, Integer>) change -> {
            Processable processable = change.getKey();
            if (change.getValueAdded() != null) {
                aims.get(processable).update(change.getValueAdded().intValue());
            } else
                aims.get(processable).setAchieved();
        });
    }
}
