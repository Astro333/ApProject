package Player;

import java.util.*;

public class Player {

    private long money;
    private long goldMoney;
    private final HashMap<Byte, LinkedList<Integer>> levelsTime = null;
    private final HashMap<String, Byte> gameElementsLevel = null;
    private final Long id = null;

    public long getId() {
        return id;
    }

    private Player() {}

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

    public LinkedList<Integer> getLevelTime(byte levelId){
        return levelsTime.getOrDefault(levelId, null);
    }

    public void addLevelTime(byte levelId, int time){
        if(levelsTime.containsKey(levelId)) {
            int i = 0;
            while (levelsTime.get(levelId).get(i) < time)
                ++i;
            levelsTime.get(levelId).add(i, time);
        }
        else {
            levelsTime.put(levelId, new LinkedList<>());
            levelsTime.get(levelId).add(time);
        }
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
