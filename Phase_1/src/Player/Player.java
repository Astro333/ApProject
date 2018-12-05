package Player;

import java.util.HashMap;

public class Player {

    private long money;
    private long goldMoney;
    private final byte[] levels;
    private final HashMap<String, Byte> gameElementsLevel;

    public long getId() {
        return id;
    }

    private final long id;
    private Player(byte[] levels, long id)
    {
        this.id = id;
        this.levels =  levels;
        gameElementsLevel = new HashMap<>();
    }
    public long getGoldMoney() {
        return goldMoney;
    }

    public void addGoldMoney(long amount) {
        this.goldMoney += amount;
    }

    public long getMoney() {
        return money;
    }

    public void addMoney(long amount){
        this.money += amount;
    }

    public void setLevelValue(byte value, int level){
        levels[level] = value;
    }

    public byte getGameElementLevel(String element){
        return gameElementsLevel.get(element);
    }

    public boolean incrementGameElementLevel(String element){
        if(gameElementsLevel.containsKey(element)) {
            gameElementsLevel.compute(element, (k, v) -> ++v);
            return true;
        }
        return false;
    }
}
