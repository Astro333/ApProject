package Structures;

import Interfaces.Processable;
import Levels.SaveData;
import Utilities.Constants;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import java.util.HashMap;

import static Items.Item.ItemType;


public class Depot {
    private float storedThingsVolume = 0;
    private int capacity;
    private byte level = 0;
    private final byte maxLevel;
    private final ObservableMap<ItemType, Integer> thingsStored;

    public Depot(byte maxLevel, MapChangeListener<Processable, Integer> requirementMetListener) {
        this.maxLevel = maxLevel;
        this.capacity = 50;
        thingsStored = FXCollections.observableHashMap();
        thingsStored.addListener(requirementMetListener);
    }

    public Depot(SaveData saveData, MapChangeListener<Processable, Integer> requirementMetListener) {
        this.maxLevel = saveData.getDepotMaxLevel();
        this.level = saveData.getDepotLevel();
        this.storedThingsVolume = saveData.getDepotStoredThingsVolume();
        this.capacity = level == 0 ? 50 : 150 * (1 << (level - 1));
        this.thingsStored = FXCollections.observableHashMap();
        this.thingsStored.putAll(saveData.getDepotItems());
        thingsStored.addListener(requirementMetListener);
    }

    public void fillSaveData(SaveData saveData) {
        saveData.setDepotLevel(level);
        saveData.setDepotMaxLevel(maxLevel);
        saveData.setDepotStoredThingsVolume(storedThingsVolume);
        saveData.setDepotItems(new HashMap<>(thingsStored));
    }

    public void addListener(MapChangeListener<Processable, Integer> requirementMetListener) {
        thingsStored.addListener(requirementMetListener);
    }

    public boolean addAllStorable(ItemType storable, int amount) {
        float unitVolume = Utilities.Constants.getProductSize(storable.toString());
        if (amount > 0 && storedThingsVolume + unitVolume * amount <= capacity) {
            if (thingsStored.containsKey(storable)) {
                thingsStored.compute(storable, (k, v) -> v + amount);
            } else {
                thingsStored.put(storable, amount);
            }
            storedThingsVolume += amount * unitVolume;
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
            if (thingsStored.get(type) >= amount) {
                thingsStored.compute(type, (k, v) -> v - amount);
                storedThingsVolume -= unitVolume * amount;
                return true;
            } else {
                System.err.println("Not Enough Items In Depot.");
                return false;
            }
        }
        return false;
    }

    public boolean hasAll(ItemType item, int amount) {
        if (thingsStored.containsKey(item)) {
            return thingsStored.get(item) >= amount;
        }
        return false;
    }

    public int getItemAmount(ItemType item) {
        return thingsStored.getOrDefault(item, 0);
    }

    public boolean upgrade() {
        if (level < maxLevel) {
            capacity = 150 * (1 << level);
            ++level;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Depot:\n").append("Level = ").append(level).append(", MaxLevel = ").append(maxLevel).
                append(", Capacity = ").append(capacity).
                append(", storedItemsVolume = ").append(storedThingsVolume).append("\n");
        if (thingsStored.size() > 0) {
            s.append("Items:\n");
            for (ItemType item : thingsStored.keySet()) {
                s.append("\t").append(item).append(", Amount = ").append(thingsStored.get(item)).
                        append(", Unit Cost : ").append(Constants.getProductSaleCost(item.toString())).append("\n");
            }
            s.deleteCharAt(s.length() - 1);
        } else
            s.append("\tNo Items Inside.");
        return s.toString();
    }

    public int getUpgradeCost() {
        return Constants.getElementLevelUpgradeCost("Depot", level + 1);
    }

    public byte getLevel() {
        return level;
    }
}
