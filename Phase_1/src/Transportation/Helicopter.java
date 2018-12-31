package Transportation;

import Interfaces.Processable;
import Items.Item;
import Levels.SaveData;
import Map.Map;
import Utilities.Constants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import static Items.Item.ItemType;

public class Helicopter extends TransportationTool {

    private transient final Random random;

    public Helicopter(byte maxLevel, byte level) {
        super(maxLevel, level);
        random = new Random();
        capacity = level == 0 ? 40 : (1 + 2 * level) * 20;
    }

    public Helicopter(SaveData saveData) {
        this(saveData.getHelicopter().maxLevel, saveData.getHelicopter().level);
        Helicopter it = (Helicopter) saveData.getHelicopter();
        for (Object p : it.itemsInside.keySet())
            itemsInside.put((Processable) p, it.itemsInside.get(p));
        itemsInsidePrice = it.itemsInsidePrice;
        itemsInsideVolume = it.itemsInsideVolume;
        turnsRemainedToFinishTask = it.turnsRemainedToFinishTask;
        isAtTaskProperty().set(turnsRemainedToFinishTask >= 0);
    }

    @Override
    protected int calculateTimeToFinish() {
        return 12 - level * 3;
    }

    @Override
    public int getUpgradeCost() {
        return level == maxLevel ? Integer.MIN_VALUE : Constants.getElementLevelUpgradeCost("Helicopter", level + 1);
    }

    @Override
    public boolean upgrade() {
        if (level < maxLevel) {
            ++level;
            capacity = (1 + 2 * level) * 20;
            return true;
        }
        return false;
    }

    @Override
    public void printElements() {
        if (itemsInside.size() > 0) {
            System.out.println("Items To Buy:");
            for (Object o : itemsInside.keySet()) {
                ItemType type = (ItemType) o;
                int amount = itemsInside.get(type);
                System.out.println("\t" + type + " : " + amount +
                        ", Total Price : " + Constants.getProductBuyCost(type.toString()) * amount +
                        ", Total Volume : " + Constants.getProductSize(type.toString()) * amount);
            }
            System.out.println("\tItems Volume : " + itemsInsideVolume);
            System.out.println("\tItems Price : " + itemsInsidePrice);
        } else
            System.out.println("No Items Inside.");
    }

    public HashMap<? super Processable, Integer> getItemsInside() {
        return itemsInside;
    }

    private int getRandomPosition(int max) {
        return random.nextInt(max);
    }

    public void dropItemsRandomly(Map map) {
        /*
         * must handle parachute animation in phase 2
         * */
        for (Object item : itemsInside.keySet()) {
            for (int i = itemsInside.get(item); i > 0; --i) {
                int pos_x = getRandomPosition(map.cellsWidth);
                int pos_y = getRandomPosition(map.cellsHeight);
                map.addItem(new Item((ItemType) item, pos_x, pos_y));
                System.out.printf("Dropped %s At (%d, %d)\n", item, pos_x, pos_y);
            }
        }
        itemsInside.clear();
    }
}
