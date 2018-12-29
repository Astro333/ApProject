package Levels;

import Controllers.LevelController;
import Interfaces.Processable;
import Items.Item;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;

public class RequirementsListener {
    private transient final MapChangeListener<Processable, Integer> mapChangeListener =
            new MapChangeListener<>() {
                @Override
                public void onChanged(Change<? extends Processable, ? extends Integer> change) {
                    if (levelController.getLevelRequirements().containsKey(change.getKey())) {
                        int newValue = change.getValueAdded();
                        levelController.getLevelRequirements().computeIfPresent(change.getKey(), (k, v)->newValue);
                        if (newValue >= levelController.levelData.getGoals().get(change.getKey())) {
                            levelController.getLevelRequirements().remove(change.getKey());
                            levelController.setAchieved(change.getKey());
                        }
                    }
                }
            };

    private transient final LevelController levelController;

    public ChangeListener<Number> getCoinChangeListener() {
        return coinChangeListener;
    }

    private transient final ChangeListener<Number> coinChangeListener;
    public RequirementsListener(LevelController levelController, boolean hasCoin) {
        this.levelController = levelController;
        if(hasCoin)
            coinChangeListener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                    if(newValue.intValue() >= levelController.getLevelRequirements().get(Item.ItemType.Coin)){
                        levelController.getLevelRequirements().remove(Item.ItemType.Coin);
                        levelController.setAchieved(Item.ItemType.Coin);
                    }
                }
            };

        else
            coinChangeListener = null;
    }
    public MapChangeListener<Processable, Integer> getMapChangeListener() {
        return mapChangeListener;
    }
}
