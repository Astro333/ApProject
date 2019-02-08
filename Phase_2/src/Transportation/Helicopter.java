package Transportation;

import Interfaces.Processable;
import Utilities.Loader;
import Utilities.Sprite;
import Utilities.Utility;

import java.util.HashMap;
import java.util.Random;

import static Items.Item.ItemType;

public class Helicopter extends TransportationTool {

    private transient final Random random;
    public Helicopter(byte maxLevel, byte level) {
        super(maxLevel, level);
        random = new Random();
        capacity = level == 0 ? 40 : (1 + 2 * level) * 20;
        upgradeButton = Utility.createUpgradeButton();
        upgradeButton.setOnAction(event -> {
            int cost = getUpgradeCost();
            if (cost >= 0 && playerCoin.get() >= cost) {
                playerCoin.set(playerCoin.get() - cost);
                upgrade();
            }
        });
        upgradeButton.setLayoutX(65);
        upgradeButton.setLayoutY(130);
        upgradeButton.setViewOrder(-20);
        updateUpgradeButton();
        viewGraphic.getChildren().add(upgradeButton);
    }

    @Override
    protected int calculateTimeToFinish() {
        return 12 - level * 3;
    }

    @Override
    public int getUpgradeCost() {
        return level == maxLevel ? -1 : Loader.getElementLevelUpgradeCost("Helicopter", level + 1);
    }

    @Override
    public boolean upgrade() {
        if (level < maxLevel) {
            ++level;
            HashMap<Byte, Sprite> temp1 = miniMapTextures.get("Helicopter");
            viewGraphic.getChildren().clear();
            animation.clear();
            animation.addTexture(temp1.get(level), miniMapViews.get(level));
            miniMapGraphic.getChildren().clear();
            miniMapGraphic.getChildren().add(miniMapViews.get(level));
            view.setImage(images.get("Helicopter").get(level));
            viewGraphic.getChildren().add(view);
            updateUpgradeButton();
            viewGraphic.getChildren().add(upgradeButton);
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
                        ", Total Price : " + Loader.getProductBuyCost(type.toString()) * amount +
                        ", Total Volume : " + Loader.getProductSize(type.toString()) * amount);
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
}
