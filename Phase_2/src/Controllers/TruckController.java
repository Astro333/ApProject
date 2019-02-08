package Controllers;

import Animals.Animal;
import Buildings.Depot;
import Interfaces.Processable;
import Items.Item;
import Transportation.Truck;
import Utilities.AnimalType;
import Utilities.State;
import Utilities.TruckProductBar;
import Utilities.Utility;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableMap;
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

import java.lang.annotation.Native;
import java.util.HashMap;
import java.util.Iterator;

public class TruckController {
    private Truck truck;
    private Depot depot;
    private HashMap<Long, Animal> pets;
    @FXML
    private VBox animalsContainer, itemsContainer;
    @FXML
    private Button cancel, ok;
    @FXML
    private Label moneyIndicator, volumeIndicator;

    private ImageView truckView;

    @FXML
    private Pane back;

    @FXML
    public void initialize() {
    }
    private void cancelAction() {
        for (int i = 0; i < itemsContainer.getChildren().size(); ++i) {
            TruckProductBar bar = (TruckProductBar) itemsContainer.getChildren().get(i);
            bar.reset();
        }
        itemsContainer.getChildren().clear();
        animalsContainer.getChildren().clear();
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
            TruckProductBar bar = (TruckProductBar) itemsContainer.getChildren().get(i);
            bar.clear();
        }
        itemsContainer.getChildren().clear();
        for (int i = 0; i < animalsContainer.getChildren().size(); ++i) {
            TruckProductBar bar = (TruckProductBar) animalsContainer.getChildren().get(i);
            bar.clear();
        }
        animalsContainer.getChildren().clear();
        Iterator<? super Processable> it = truck.getItemsInside().keySet().iterator();
        while (it.hasNext()) {
            Processable processable = (Processable) it.next();
            int amount = truck.getItemsInside().get(processable);
            if (processable instanceof Item.ItemType) {
                depot.removeAllStorable((Item.ItemType) processable, amount);
            } else {
                int j = 0;
                Iterator<Animal> animalIterator = pets.values().iterator();
                while (animalIterator.hasNext()){
                    Animal animal = animalIterator.next();
                    if (j < amount) {
                        if (animal.getType() == processable) {
                            animal.statusProperty().set(animal.getStatus() | State.MarkedToRemove.value);
                            animalIterator.remove();
                            ++j;
                        }
                    }
                }
            }
        }
        truck.go();
        goToPrevScene();
    }

    private Scene levelScene;

    public void setup(LevelController levelController, Depot depot, HashMap<Long, Animal> pets, Truck truck) {
        this.pets = pets;
        this.depot = depot;
        this.truck = truck;
        truckView = new ImageView();
        truckView.setTranslateX(524);
        truckView.setTranslateY(208);
        truckView.setStyle("-fx-fill: red;");
        back.getChildren().add(truckView);
        truckView.toBack();

        truck.itemsInsideVolumeProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                volumeIndicator.setText("☒ " + newValue.doubleValue() + " / " + truck.getCapacity());
            }
        });

        truck.itemsInsidePriceProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                moneyIndicator.setText("" + newValue.intValue());
            }
        });
        ok.setOnAction(event -> {
            okAction();
            levelController.toggleMapElementsPause();
        });
        cancel.setOnAction(event -> {
            cancelAction();
            levelController.toggleMapElementsPause();
        });
    }

    public void update() {
        volumeIndicator.setText("☒ " + truck.getItemsInsideVolume() + " / " + truck.getCapacity());
        truckView.setImage(new Image(getClass().getResource(
                "/res/Transportation/Truck/Textures/Menu/Images/0" + (truck.getLevel() + 1) + ".png").toExternalForm()));

        BooleanBinding bb = new SimpleBooleanProperty(true).not();
        for (Item.ItemType type : depot.getThingsStored().keySet()) {
            if (type != Item.ItemType.Coin && depot.getItemAmount(type) > 0) {
                TruckProductBar productBar = Utility.createTruckProductBar(type, depot.getThingsStored().get(type), truck);
                bb = bb.or(productBar.amountProperty().greaterThan(0));
                itemsContainer.getChildren().add(productBar);
            }
        }
        HashMap<AnimalType, Integer> animals = new HashMap<>();
        for (Animal pet : pets.values()) {
            if(pet.getType()!= AnimalType.Dog && pet.getType() != AnimalType.Cat) {
                if (animals.containsKey(pet.getType())) {
                    animals.compute(pet.getType(), (k, v) -> v + 1);
                } else {
                    animals.put(pet.getType(), 1);
                }
            }
        }
        for (AnimalType type : animals.keySet()) {
            TruckProductBar productBar = Utility.createTruckProductBar(type, animals.get(type), truck);
            bb = bb.or(productBar.amountProperty().greaterThan(0));
            animalsContainer.getChildren().add(productBar);
        }
        ok.disableProperty().unbind();
        ok.disableProperty().bind(bb.not());
    }

    public void setLevelScene(Scene levelScene) {
        this.levelScene = levelScene;
    }
}
