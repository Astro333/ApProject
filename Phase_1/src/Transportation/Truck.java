package Transportation;

public class Truck extends TransportationTool{

    private Truck(){
        super();
    }

    @Override
    public int getUpgradeCost() {
        return 0;
    }

    @Override
    public boolean upgrade() {
        return false;
    }

    public int receiveSoldGoodsMoney(){
        int temp = itemsInsidePrices;
        itemsInsidePrices = 0;
        return temp;
    }
}
