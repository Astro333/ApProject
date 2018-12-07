package Transportation;

import Items.Item;
import Map.Map;

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
    public int getUpgradeCost() {
        return 0;
    }

    @Override
    public boolean upgrade() {
        return false;
    }

    public HashMap<ItemType, Integer> getItemsInside(){
        return itemsInside;
    }

    public void dropItemsRandomly(Map map) {
        for(ItemType item : itemsInside.keySet()){
            for(int i = itemsInside.get(item); i > 0; --i) {
                int pos_x = random.nextInt(map.cellsWidth);
                int pos_y = random.nextInt(map.cellsHeight);
                map.getCell(pos_x, pos_y).addItem(new Item(item));
            }
        }
        itemsInside.clear();
    }
}
