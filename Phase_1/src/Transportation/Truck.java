package Transportation;

import static Animals.Animal.AnimalType;

import Interfaces.Processable;
import Levels.SaveData;
import Utilities.Constants;

public class Truck extends TransportationTool{

    public Truck(byte maxLevel, byte level) {
        super(maxLevel, level);
        capacity = level == 0 ? 40 : (1 + 2 * level) * 20;
    }

    public Truck(SaveData saveData){
        this(saveData.getTruck().maxLevel, saveData.getTruck().level);
        Truck it = (Truck) saveData.getTruck();
        for(Object p : it.itemsInside.keySet())
            itemsInside.put((Processable) p, it.itemsInside.get(p));
        itemsInsidePrice = it.itemsInsidePrice;
        itemsInsideVolume = it.itemsInsideVolume;
        turnsRemainedToFinishTask = it.turnsRemainedToFinishTask;
        isAtTaskProperty().set(turnsRemainedToFinishTask >= 0);
    }

    @Override
    protected int calculateTimeToFinish() {
        return 20 - 5 * level;
    }

    @Override
    public int getUpgradeCost() {
        return level == maxLevel ? Integer.MIN_VALUE : Constants.getElementLevelUpgradeCost("Truck", level+1);
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

    public boolean addAll(AnimalType type, int amount){
        if(hasCapacityFor(type, amount)){
            itemsInsidePrice += Constants.getAnimalBuyCost(type) * amount/2;
            if(itemsInside.containsKey(type)){
                itemsInside.compute(type, (k, v) -> v + amount);
            }
            else {
                itemsInside.put(type, amount);
            }
            itemsInsideVolume += Constants.getAnimalDepotSize(type)*amount;
            return true;
        }
        return false;
    }

    @Override
    public void printElements() {
        if(itemsInside.size() > 0){
            System.out.println("Elements To Sell:");
            for(Object element : itemsInside.keySet()){
                int amount = itemsInside.get(element);
                if(element instanceof AnimalType){
                    System.out.println("\t"+element+" : "+amount+
                            ", Total Price : " + (Constants.getAnimalBuyCost((AnimalType) element)/2) * amount +
                            ", Total Volume : " + Constants.getAnimalDepotSize((AnimalType)element) * amount);
                } else {

                }
            }
        } else
            System.out.println("No Items Inside.");
    }

    public int receiveSoldGoodsMoney() {
        int temp = itemsInsidePrice;
        itemsInsidePrice = 0;
        return temp;
    }
}
