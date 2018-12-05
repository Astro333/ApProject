package Transportation;

import java.util.HashMap;

public class Helicopter extends TransportationTool{

    public Helicopter(byte maxLevel){
        super(maxLevel);
    }

    @Override
    public int getUpgradeCost() {
        return 0;
    }

    @Override
    public boolean upgrade() {
        return false;
    }

    public HashMap<String, Integer> getItemsInside(){
        return itemsInside;
    }
}
