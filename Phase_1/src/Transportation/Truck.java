package Transportation;

import Utilities.Constants;

public class Truck extends TransportationTool{

    public Truck(byte maxLevel, byte level) {
        super(maxLevel, level);
        capacity = 20*(level+2);
    }

    @Override
    protected int calculateTimeToFinish() {
        return 20-level*5;
    }

    @Override
    public int getUpgradeCost() {
        return level == maxLevel ? Integer.MIN_VALUE : Constants.getElementLevelUpgradeCost("Truck", level+1);
    }

    @Override
    public boolean upgrade() {
        if (level < maxLevel) {
            ++level;
            capacity += 20;
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
