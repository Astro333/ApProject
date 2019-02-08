package Utilities;

import Interfaces.Processable;
import Items.Item;
import Transportation.Helicopter;
import Transportation.TransportationTool;
import Transportation.Truck;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

public class HelicopterProductBar extends HBox {
    private Processable processable;
    private Helicopter helicopter;
    private TextField amountField;

    public int getAmount() {
        return amount.get();
    }

    public IntegerProperty amountProperty() {
        return amount;
    }

    private IntegerProperty amount = new SimpleIntegerProperty(0);

    private IntegerProperty playerCoin;
    private int cost;

    public void setPlayerCoin(IntegerProperty playerCoin) {
        this.playerCoin = playerCoin;
    }

    public HelicopterProductBar(Processable processable, Helicopter helicopter) {
        this.helicopter = helicopter;
        this.processable = processable;
        ImageView view = new ImageView();
        view.setScaleX(0.8);
        view.setScaleY(0.8);
        if (processable instanceof AnimalType) {
            view.setImage(new Image(Utility.class.getResource(
                    "/res/Products/Animals/Images/" + processable.toString() + ".png").toExternalForm()));
        } else {
            view.setImage(Item.images.get(processable));
        }
        TextField text = new TextField("0 ⬤");
        text.setDisable(true);
        text.setOpacity(1);
        text.setAlignment(Pos.CENTER);
        text.setStyle("-fx-font-family: IONA-U1;" +
                "-fx-font-size: 12;" +
                "-fx-text-fill: gold;" +
                "-fx-pref-width: 62;" +
                "-fx-background-color: #3277f9;");
        amountField = new TextField("0");
        amountField.setId("amountIndicator");
        amountField.setTextFormatter(new TextFormatter<>(change -> {
            if (!change.isContentChange()) {
                return change;
            }
            String s = change.getControlNewText();
            if (s.matches("[0-9]*")) {
                return change;
            }
            return null;
        }));
        Button incrementButton = new Button("🞣");
        incrementButton.setOnAction(event -> {
            String s = amountField.getText().equals("") ? "1" : "" + (Integer.parseInt(amountField.getText()) + 1);

            if (playerCoin.get() >= cost && hasCapacity(1)) {
                amount.set(Integer.parseInt(s));
            }
        });
        incrementButton.setId("incrementButton");
        Button resetButton = new Button("🞩");
        resetButton.setId("resetButton");
        resetButton.setOnAction(event -> {
            amount.set(0);
        });
        cost = Loader.getProductBuyCost(processable.toString());
        amountField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (amountField.getText().equals(""))
                    amountField.setText("0");
                int addition = Integer.parseInt(amountField.getText()) - amount.get();
                if (playerCoin.get() >= addition * cost && hasCapacity(addition)) {
                    amount.set(amount.get() + addition);
                }
            }
        });
        amount.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() >= 0) {
                    text.setText("" + cost * amount.get() + " ⬤");
                    amountField.setText("" + amount.get());
                    playerCoin.set(playerCoin.get() - (newValue.intValue() - oldValue.intValue()) * cost);
                    addAll(newValue.intValue() - oldValue.intValue());
                } else if (newValue.intValue() == -1) {
                    text.setText("0 ⬤");
                    amountField.setText("0");
                    amount.removeListener(this);
                    amount.set(0);
                    amount.addListener(this);
                }
            }
        });
        getChildren().add(view);
        getChildren().add(text);
        getChildren().add(amountField);
        getChildren().add(incrementButton);
        getChildren().add(resetButton);
    }

    private void addAll(int val) {
        helicopter.addAll((Item.ItemType) processable, val);
    }

    private boolean hasCapacity(int val) {
        return helicopter.hasCapacityFor((Item.ItemType) processable, val);
    }

    public void reset() {
        amount.set(0);
    }

    public Processable getProcessable() {
        return processable;
    }

    public void clear() {
        amount.set(-1);
    }
}
