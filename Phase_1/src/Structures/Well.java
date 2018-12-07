package Structures;

import Exceptions.IllegalConstructorArgumentException;

public class Well {

    private boolean isRefilling;
    private byte level = 0;
    private byte storedWater;
    private float waterAdditionFactor;
    private byte capacity;
    private int timeRemainedToRefill = -1;
    private final byte maxLevel;
    private final byte maxMaxLevel = 3;
    private final float WATER_UNIT_COST;

    public Well(byte maxLevel, float WATER_UNIT_COST) throws IllegalConstructorArgumentException {
        if (maxLevel > maxMaxLevel)
            throw new IllegalConstructorArgumentException();
        this.maxLevel = maxLevel;
        this.WATER_UNIT_COST = WATER_UNIT_COST;
    }

    public Well(byte maxLevel, byte level, float WATER_UNIT_COST) throws IllegalConstructorArgumentException {
        if (level > maxLevel || maxLevel > maxMaxLevel)
            throw new IllegalConstructorArgumentException();
        this.maxLevel = maxLevel;
        this.level = level;
        this.WATER_UNIT_COST = WATER_UNIT_COST;
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
            if(storedWater + waterAdditionFactor*turns >= capacity){
                storedWater = capacity;
                timeRemainedToRefill = -1;
                isRefilling = false;
            }
            else {
                storedWater += waterAdditionFactor*turns;
                timeRemainedToRefill -= turns;
            }
        }
    }

    public boolean isRefilling() {
        return isRefilling;
    }

    public boolean upgrade(){
        if(level < maxLevel){
            ++level;
            capacity +=10;
        }
        return false;
    }

    public int getUpgradePrice(){
        return 0;
    }

    public int getRefillPrice(){
        return (int) ((capacity - storedWater) * WATER_UNIT_COST);
    }

    private int calculateRefillingTime() {
        return 0;
    }
    public boolean refill(){
        if(!isRefilling && storedWater < capacity){
            isRefilling = true;
            timeRemainedToRefill = calculateRefillingTime();
            return true;
        }
        return false;
    }
}
