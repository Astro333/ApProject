package Transportation;

import Utilities.Constants;

public class Truck extends TransportationTool{

    public Truck(byte maxLevel, byte level) {
        super(maxLevel, level);
    }

    @Override
    public int getUpgradeCost() {
        return Constants.getElementLevelUpgradeCost("Truck", getLevel() + 1);
    }

    @Override
    public boolean upgrade() {
        if (level < maxLevel) {
            ++level;
            ++capacity;
            return true;
        }
        return false;
    }

    public int receiveSoldGoodsMoney() {
        int temp = itemsInsidePrice;
        itemsInsidePrice = 0;
        return temp;
    }
}
