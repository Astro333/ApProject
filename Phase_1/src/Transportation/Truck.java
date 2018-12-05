package Transportation;

public class Truck extends TransportationTool{
    public Truck(byte maxLevel){
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

    public int getSoldGoodsMoney(){
        int temp = itemsInsidePrices;
        itemsInsidePrices = 0;
        return temp;
    }
}
