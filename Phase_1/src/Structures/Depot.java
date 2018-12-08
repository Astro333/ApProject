package Structures;

import Interfaces.LevelRequirement;
import Utilities.Constants;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import static Items.Item.ItemType;


public class Depot {
    private float storedThingsVolume = 0;
    private int capacity = 40;
    private byte level = 1;
    private final byte maxLevel;
    private final ObservableMap<ItemType, Integer> thingsStored;

    public Depot(byte maxLevel, MapChangeListener<LevelRequirement, Integer> requirementMetListener) {
        this.maxLevel = maxLevel;
        thingsStored = FXCollections.observableHashMap();
        thingsStored.addListener(requirementMetListener);
    }

    public boolean addAllStorable(ItemType storable, int amount) {
        float unitVolume = Utilities.Constants.getProductSize(storable.toString());
        if(amount > 0 && storedThingsVolume + unitVolume*amount <= capacity) {
            if (thingsStored.containsKey(storable)) {
                thingsStored.compute(storable, (k, v) -> v + amount);
            } else {
                thingsStored.put(storable, amount);
            }
            storedThingsVolume += amount*unitVolume;
            return true;
        }
        return false;
    }

    public boolean addStorable(ItemType storable) {
        float unitVolume = Utilities.Constants.getProductSize(storable.toString());
        if (unitVolume + storedThingsVolume <= capacity) {
            if (thingsStored.containsKey(storable)) {
                thingsStored.put(storable, thingsStored.get(storable) + 1);
            } else {
                thingsStored.put(storable, 1);
            }
            storedThingsVolume += unitVolume;
            return true;
        }
        return false;
    }

    public boolean removeAllStorable(ItemType type, int amount) {
        float unitVolume = Utilities.Constants.getProductSize(type.toString());
        if (amount > 0 && thingsStored.containsKey(type)) {
            if (thingsStored.get(type) > amount) {
                thingsStored.compute(type, (k, v) -> v - amount);
                storedThingsVolume -= unitVolume*amount;
                return true;
            }
            else if (thingsStored.get(type) == amount) {
                thingsStored.remove(type);
                storedThingsVolume -= unitVolume*amount;
                return true;
            }
        }
        return false;
    }

    public boolean upgrade() {
        if (level < maxLevel) {
            ++level;
            capacity *= 2;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Depot:\n").append("Level = ").append(level).append(", MaxLevel = ").append(maxLevel).
               append(", capacity = ").append(capacity).
                append(", storedItemsVolume = ").append(storedThingsVolume).append("\n");
        if(thingsStored.size() > 0) {
            s.append("Items:\n");
            for (ItemType item : thingsStored.keySet()) {
                s.append(item).append(", amount = ").append(thingsStored.get(item)).append("\n");
            }
        }
        else
            s.append("No Items Inside.\n");
        return s.toString();
    }

    public int getUpgradeCost() {
        return Constants.getElementLevelUpgradeCost("Depot", level+1);
    }

    public byte getLevel() {
        return level;
    }
}
