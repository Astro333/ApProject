package Transportation;

import Items.Item;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import Utilities.Constants;

import java.util.HashMap;

public abstract class TransportationTool {
    protected final byte maxLevel;
    protected final byte maxMaxLevel = 4;
    protected byte level;
    protected int capacity;
    private int timeRemainedToFinishTask = -1;
    private transient final BooleanProperty isAtTask;
    protected final HashMap<Item.ItemType, Integer> itemsInside;
    protected int itemsInsidePrice = 0;
    protected int itemsInsideVolume = 0;

    TransportationTool(byte maxLevel, byte level){
        this.maxLevel = maxLevel;
        this.level = level;
        itemsInside = new HashMap<>();
        isAtTask = new SimpleBooleanProperty(this, "isAtTask", false);
    }

    public BooleanProperty isAtTaskProperty() {
        return isAtTask;
    }


    public int getTimeRemainedToFinishTask() {
        return timeRemainedToFinishTask;
    }

    public int getItemsInsidePrice(){
        return itemsInsidePrice;
    }

    public boolean isAtTask() {
        return isAtTask.get();
    }

    public void update(int turns){
        if(isAtTask()) {
            if (timeRemainedToFinishTask - turns > 0) {
                timeRemainedToFinishTask -= turns;
            }
            else {
                timeRemainedToFinishTask = -1;
                isAtTask.set(false);
            }
        }
    }

    private int calculateTimeToFinish(){
        return 0;
    }
    public boolean go(){
        if(itemsInside.size() > 0){
            isAtTask.set(true);
            timeRemainedToFinishTask = calculateTimeToFinish();
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
    public boolean hasCapacityFor(Item.ItemType item, int amount){
        return Constants.getProductSize(item.toString()) * amount + itemsInsideVolume <= capacity;
    }
    public boolean addAll(Item.ItemType item, int amount){
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
            return true;
        }
        return false;
    }

    public byte getLevel() {
        return level;
    }
}
