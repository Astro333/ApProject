package Utilities;

import Interfaces.Processable;
import Items.Item;
import Transportation.Helicopter;
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

public class TruckProductBar extends HBox {
    private Processable processable;
    private Truck truck;
    private TextField amountField;
    private int amountAvail;
    public int getAmount() {
        return amount.get();
    }

    public IntegerProperty amountProperty() {
        return amount;
    }

    private IntegerProperty amount = new SimpleIntegerProperty(0);

    private int cost;

    public TruckProductBar(Processable processable, int amountAvail, Truck truck) {
        this.truck = truck;
        this.amountAvail = amountAvail;
        this.processable = processable;
        ImageView view = new ImageView();
        view.setFitHeight(48);
        view.setFitWidth(48);
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
                "-fx-font-size: 11;" +
                "-fx-text-fill: gold;" +
                "-fx-pref-width: 50;" +
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
            if (amount.get() + 1 <= amountAvail && hasCapacity(1)) {
                amount.set(Integer.parseInt(s));
            }
        });
        incrementButton.disableProperty().bind(amount.isEqualTo(amountAvail));
        incrementButton.setId("incrementButton");
        Button resetButton = new Button("🞩");
        resetButton.setId("resetButton");
        resetButton.setOnAction(event -> {
            amount.set(0);
        });
        cost = processable instanceof Item.ItemType ? Loader.getProductSaleCost(processable.toString()) :
                Loader.getAnimalBuyCost((AnimalType) processable)/2;
        amountField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (amountField.getText().equals(""))
                    amountField.setText("0");
                int addition = Integer.parseInt(amountField.getText()) - amount.get();
                if (amount.get() + addition <= amountAvail && hasCapacity(addition)) {
                    amount.set(amount.get() + addition);
                } else {
                    amountField.setText(""+amount.get());
                }
            }
        });
        amount.addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() >= 0) {
                    text.setText("" + cost * amount.get() + " ⬤");
                    amountField.setText("" + amount.get());
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
        if(processable instanceof AnimalType) {
            truck.addAll((AnimalType) processable, val);
        } else {
            truck.addAll((Item.ItemType) processable, val);
        }
    }

    private boolean hasCapacity(int val) {
        if(processable instanceof AnimalType) {
            return truck.hasCapacityFor((AnimalType) processable, val);
        } else {
            return truck.hasCapacityFor((Item.ItemType) processable, val);
        }
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
