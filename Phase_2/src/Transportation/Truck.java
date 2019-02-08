package Transportation;

import Items.Item;
import Utilities.AnimalType;
import Utilities.Loader;
import Utilities.Sprite;
import Utilities.Utility;

import java.util.HashMap;

public class Truck extends TransportationTool {

    public Truck(byte maxLevel, byte level) {
        super(maxLevel, level);
        capacity = level == 0 ? 40 : (1 + 2 * level) * 20;
        upgradeButton = Utility.createUpgradeButton();
        upgradeButton.setOnAction(event -> {
            int cost = getUpgradeCost();
            if (cost >= 0 && playerCoin.get() >= cost) {
                playerCoin.set(playerCoin.get() - cost);
                upgrade();
            }
        });
        upgradeButton.setLayoutX(35);
        upgradeButton.setLayoutY(100);
        upgradeButton.setViewOrder(-20);
        updateUpgradeButton();
        viewGraphic.getChildren().add(upgradeButton);
    }

    @Override
    protected int calculateTimeToFinish() {
        return 20 - 5 * level;
    }

    @Override
    public int getUpgradeCost() {
        return level == maxLevel ? -1 : Loader.getElementLevelUpgradeCost("Truck", level + 1);
    }

    @Override
    public boolean upgrade() {
        if (level < maxLevel) {
            ++level;
            HashMap<Byte, Sprite> temp1 = miniMapTextures.get("Truck");
            viewGraphic.getChildren().clear();
            animation.clear();
            animation.addTexture(temp1.get(level), miniMapViews.get(level));
            miniMapGraphic.getChildren().add(miniMapViews.get(level));
            view.setImage(images.get("Truck").get(level));
            viewGraphic.getChildren().add(view);
            viewGraphic.getChildren().add(upgradeButton);
            updateUpgradeButton();
            capacity = (1 + 2 * level) * 20;
            return true;
        }
        return false;
    }

    public boolean addAll(AnimalType type, int amount) {
        if (hasCapacityFor(type, amount)) {
            itemsInsidePrice.set(itemsInsidePrice.get() + Loader.getAnimalBuyCost(type) * amount / 2);
            if (itemsInside.containsKey(type)) {
                itemsInside.compute(type, (k, v) -> v + amount);
            } else {
                itemsInside.put(type, amount);
            }
            itemsInsideVolume.set(itemsInsideVolume.get()+Loader.getAnimalDepotSize(type) * amount);
            return true;
        }
        return false;
    }

    @Override
    public void printElements() {
        if (itemsInside.size() > 0) {
            System.out.println("Elements To Sell:");
            for (Object element : itemsInside.keySet()) {
                int amount = itemsInside.get(element);
                if (element instanceof AnimalType) {
                    System.out.println("\t" + element + " : " + amount +
                            ", Total Price : " + (Loader.getAnimalBuyCost((AnimalType) element) / 2) * amount +
                            ", Total Volume : " + Loader.getAnimalDepotSize((AnimalType) element) * amount);
                } else {
                    Item.ItemType type = (Item.ItemType) element;
                    System.out.println("\t" + type + " : " + amount +
                            ", Total Price : " + Loader.getProductSaleCost(type.toString()) * amount +
                            ", Total Volume : " + Loader.getProductSize(type.toString()) * amount);
                    System.out.println("\tItems Volume : " + itemsInsideVolume);
                    System.out.println("\tItems Price : " + itemsInsidePrice);
                }
            }
        } else
            System.out.println("No Items Inside.");
    }

    public int receiveSoldGoodsMoney() {
        int temp = itemsInsidePrice.get();
        itemsInsidePrice.set(0);
        return temp;
    }
}
