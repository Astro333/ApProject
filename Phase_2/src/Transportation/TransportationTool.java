package Transportation;


import Animals.Animal;
import Interfaces.Processable;
import Utilities.Constants;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.HashMap;

import static Items.Item.ItemType;

public abstract class TransportationTool {
    protected final byte maxLevel;
    protected final byte maxMaxLevel;
    protected byte level;
    protected int capacity;
    protected int turnsRemainedToFinishTask = -1;

    protected final HashMap<? super Processable, Integer> itemsInside;

    protected int itemsInsidePrice = 0;
    protected double itemsInsideVolume = 0;
    private transient final BooleanProperty isAtTask;
    TransportationTool(byte maxLevel, byte level){
        if(this instanceof Helicopter)
            maxMaxLevel = Constants.getElementMaxMaxLevel("Helicopter");
        else
            maxMaxLevel = Constants.getElementMaxMaxLevel("Truck");

        this.maxLevel = maxLevel;
        this.level = level;
        itemsInside = new HashMap<>();
        isAtTask = new SimpleBooleanProperty(this, "isAtTask", false);
    }

    public HashMap<? super Processable, Integer> getItemsInside() {
        return itemsInside;
    }

    public BooleanProperty isAtTaskProperty() {
        return isAtTask;
    }

    public int getTurnsRemainedToFinishTask() {
        return turnsRemainedToFinishTask;
    }

    public int getItemsInsidePrice(){
        return itemsInsidePrice;
    }

    public boolean isAtTask() {
        return isAtTask.get();
    }

    public void update(int turns){
        if(isAtTask()) {
            if (turnsRemainedToFinishTask - turns > 0) {
                turnsRemainedToFinishTask -= turns;
            }
            else {
                turnsRemainedToFinishTask = -1;
                isAtTask.set(false);
            }
        }
    }

    protected abstract int calculateTimeToFinish();

    public boolean go(){
        if(itemsInside.size() > 0){
            isAtTask.set(true);
            turnsRemainedToFinishTask = calculateTimeToFinish();
            return true;
        }
        return false;
    }

    public void clear(){
        itemsInside.clear();
        itemsInsideVolume = 0;
        itemsInsidePrice = 0;
    }
    public abstract int getUpgradeCost();
    public abstract boolean upgrade();
    public boolean hasCapacityFor(ItemType item, int amount){
        return Constants.getProductSize(item.toString()) * amount + itemsInsideVolume <= capacity;
    }

    public boolean hasCapacityFor(Animal.AnimalType type, int amount){
        return Constants.getAnimalDepotSize(type)*amount+itemsInsideVolume <= capacity;
    }
    public boolean addAll(ItemType item, int amount){
        if(hasCapacityFor(item, amount)) {
            if(this instanceof Helicopter)
                itemsInsidePrice += Constants.getProductBuyCost(item.toString())* amount;
            else
                itemsInsidePrice += Constants.getProductBuyCost(item.toString()) * amount/2;

            if(itemsInside.containsKey(item)){
                itemsInside.compute(item, (k, v) -> v + amount);
            }
            else {
                itemsInside.put(item, amount);
            }
            itemsInsideVolume += Constants.getProductSize(item.toString())*amount;
            return true;
        }
        return false;
    }

    public byte getLevel() {
        return level;
    }

    public abstract void printElements();
}
