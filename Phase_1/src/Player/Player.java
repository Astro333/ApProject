package Player;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.*;

public class Player {

    private long money;
    private long goldMoney;

    private final HashMap<Byte, LinkedList<Integer>> levelsTime = null;
    private final HashMap<String, Byte> gameElementsLevel = null;

    private final String name = null;

    private Player(){}

    public static Player create(String name) throws IOException {
        Gson gson = new Gson();
        Reader reader = new BufferedReader(new FileReader("PlayersData/newPlayerProgress.json"));
        JsonObject object = gson.fromJson(reader, JsonObject.class);
        object.remove("name");
        object.addProperty("name", name);

        String playerDataDir = "../PlayersData/"+name+"/";

        new File(playerDataDir).mkdirs();
        new File(playerDataDir+"Player_Custom_Workshops/").mkdirs();
        new File(playerDataDir+"Player_Unfinished_Levels_Saves/").mkdirs();

        return gson.fromJson(object, Player.class);
    }

    public static Player loadPlayer(String name) throws FileNotFoundException {
        Gson gson = new Gson();
        Reader reader = new BufferedReader(new FileReader("PlayersData/"+name+"/progress.json"));
        JsonObject object = gson.fromJson(reader, JsonObject.class);
        object.remove("name");
        object.addProperty("name", name);
        return gson.fromJson(object, Player.class);
    }

    public HashMap<String, Byte> getGameElementsLevel() {
        return gameElementsLevel;
    }

    public static boolean exists(String playerName) {
        return new File("PlayersData/"+playerName).exists();
    }

    public String getName() {
        return name;
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
