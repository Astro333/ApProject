package Structures;

import Exceptions.IllegalConstructorArgumentException;
import Utilities.Constants;

public class Well {

    private boolean isRefilling;
    private byte level = 0;
    private byte storedWater;
    private byte capacity;

    private int timeRemainedToRefill = -1;

    private final byte maxLevel;
    private final byte maxMaxLevel = 3;
    public Well(byte maxLevel) throws IllegalConstructorArgumentException {
        if (maxLevel > maxMaxLevel)
            throw new IllegalConstructorArgumentException();
        this.maxLevel = maxLevel;
    }

    public Well(byte maxLevel, byte level) throws IllegalConstructorArgumentException {
        if (level > maxLevel || maxLevel > maxMaxLevel)
            throw new IllegalConstructorArgumentException();
        this.maxLevel = maxLevel;
        this.level = level;
        this.capacity = level == maxMaxLevel ? 100 : ((byte)(5 + level*(level+3)/2));
        storedWater = capacity;
    }

    public int getTimeRemainedToRefill() {
        return timeRemainedToRefill;
    }

    public byte getLevel() {
        return level;
    }

    public byte getStoredWater() {
        return storedWater;
    }

    public void decrementWaterLevel(){
        if(storedWater > 0)
            --storedWater;
    }

    public void update(int turns){
        if(isRefilling){
            timeRemainedToRefill -= turns;
            if(timeRemainedToRefill <= 0){
                storedWater = capacity;
                timeRemainedToRefill = -1;
                isRefilling = false;
            }
        }
    }

    public boolean isRefilling() {
        return isRefilling;
    }

    public boolean upgrade(){
        if(level < maxLevel){
            ++level;
            if(level == maxMaxLevel)
                capacity = 100;
            else
                capacity += (level+1);
        }
        return false;
    }

    public int getUpgradePrice(){
        return level == maxMaxLevel ? Integer.MAX_VALUE : Constants.getElementLevelUpgradeCost("Well",level+1);
    }

    public int getRefillPrice(){
        return level == maxMaxLevel ? 7 : 19-2*level;
    }

    private int calculateRefillingTime() {
        return level == maxMaxLevel ? 0 : 8 - 2*level;
    }
    public boolean refill(){
        if(!isRefilling && storedWater < capacity){
            isRefilling = true;
            timeRemainedToRefill = calculateRefillingTime();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Well:\n").
                append("\tLevel = ").append(level).append(", MaxLevel = ").append(maxLevel).append(", Status : ");
        if(isRefilling){
            s.append("Refilling, Time Remained = ").append(timeRemainedToRefill);
        } else if(storedWater == 0){
            s.append(" Empty");
        } else
            s.append(storedWater).append(" Water Unit Inside.");
        return s.toString();
    }
}
