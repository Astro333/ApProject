package Structures;

import java.util.HashMap;


public class Depot {
    private int storedThingsVolume = 0;
    private int capacity;
    private byte level = 1;
    private final byte maxLevel;
    private final HashMap<String, Integer> thingsStored;

    public Depot(byte maxLevel) {
        this.maxLevel = maxLevel;
        thingsStored = new HashMap<>();
    }

    public boolean addAllStorable(String storable, int amount) {
        int unitVolume = Utilities.Constants.getProductSize(storable);
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

    public boolean addStorable(String storable) {
        int unitVolume = Utilities.Constants.getProductSize(storable);
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

    public boolean removeAllStorable(String type, int amount) {
        int unitVolume = Utilities.Constants.getProductSize(type);
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
            capacity += 10;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Depot:\n").append("Level = ").append(level).append(", MaxLevel = ").append(maxLevel).
               append(", capacity = ").append(capacity).
                append(", storedObjectsVolume = ").append(storedThingsVolume).append("\n");
        if(thingsStored.size() > 0) {
            s.append("Items:\n");
            for (String item : thingsStored.keySet()) {
                s.append(item).append(", amount = ").append(thingsStored.get(item)).append("\n");
            }
        }
        else
            s.append("No Items Inside.\n");
        return s.toString();
    }

    public int getUpgradeCost() {
        return 0;
    }

    public byte getLevel() {
        return 0;
    }
}
