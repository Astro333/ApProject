package Transportation;

import Items.Item;
import Map.Map;
import Utilities.Constants;

import java.util.HashMap;
import java.util.Random;

import static Items.Item.ItemType;

public class Helicopter extends TransportationTool{

    private final Random random;

    public Helicopter(byte maxLevel, byte level){
        super(maxLevel, level);
        random = new Random();
    }

    @Override
    protected int calculateTimeToFinish() {
        return level == 0 ? 120 : 140-level*40;
    }

    @Override
    public int getUpgradeCost() {
        return level == maxLevel ? Integer.MIN_VALUE : Constants.getElementLevelUpgradeCost("Helicopter", level+1);
    }

    @Override
    public boolean upgrade() {
        return false;
    }

    public HashMap<ItemType, Integer> getItemsInside(){
        return itemsInside;
    }

    private int getRandomPosition(int max){
        return random.nextInt(max);
    }

    public void dropItemsRandomly(Map map) {
        /*
        * must handle parachute animation in phase 2
        * */
        for(ItemType item : itemsInside.keySet()){
            for(int i = itemsInside.get(item); i > 0; --i) {
                int pos_x = getRandomPosition(map.cellsWidth);
                int pos_y = getRandomPosition(map.cellsHeight);
                map.addItem(new Item(item, pos_x, pos_y));
            }
        }
        itemsInside.clear();
    }
}
