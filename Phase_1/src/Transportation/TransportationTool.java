package Transportation;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import Utilities.Constants;

import java.util.HashMap;

public abstract class TransportationTool {
    private final byte maxLevel;
    private byte level;
    private int capacity;
    private int timeRemainedToFinishTask = -1;
    private transient final BooleanProperty isAtTask;
    final HashMap<String, Integer> itemsInside;
    int itemsInsidePrices = 0;
    private int itemsInsideSize = 0;

    protected TransportationTool(byte maxLevel){
        this.maxLevel = maxLevel;
        itemsInside = new HashMap<>();
        isAtTask = new SimpleBooleanProperty(this, "isAtTask", false);
    }

    public BooleanProperty isAtTaskProperty() {
        return isAtTask;
    }


    public int getTimeRemainedToFinishTask() {
        return timeRemainedToFinishTask;
    }

    public int getItemsInsidePrices(){
        return itemsInsidePrices;
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
        /*
         * must check if there is any items to sell or buy first
         * must set timeRemainedToFinishTask Based on its level
         * must set isAtTask true
         * */
        if(itemsInside.size() > 0){
            isAtTask.set(true);
            timeRemainedToFinishTask = calculateTimeToFinish();
            return true;
        }
        return false;
    }

    public void clear(){
        itemsInside.clear();
        itemsInsidePrices = 0;
    }
    public abstract int getUpgradeCost();
    public abstract boolean upgrade();
    public boolean hasCapacityFor(String item, int amount){
        return Constants.getProductSize(item) * amount + itemsInsideSize <= capacity;
    }
    public boolean addAll(String item, int amount){
        if(hasCapacityFor(item, amount)) {
            if(this instanceof Helicopter)
                itemsInsidePrices += Constants.getProductBuyCost(item)* amount;
            else
                itemsInsidePrices += Constants.getProductSaleCost(item) * amount;

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
