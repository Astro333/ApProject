package Structures;

public class Well {

    private boolean isRefilling;
    private byte level;
    private byte storedWater;
    private byte waterAdditionFactor;
    private byte capacity;
    private int timeRemainedToRefill = -1;
    private final byte maxLevel;
    private final byte WATER_UNIT_COST;

    public Well(byte maxLevel, byte WATER_UNIT_COST){
        this.maxLevel = maxLevel;
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
                isRefilling = false;
            }
            else {
                storedWater += waterAdditionFactor*turns;
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
        return (capacity-storedWater)*WATER_UNIT_COST;
    }

    public boolean refill(){
        if(!isRefilling && storedWater < capacity){
            isRefilling = true;
            return true;
        }
        return false;
    }
}
