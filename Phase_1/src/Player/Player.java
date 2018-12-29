package Player;

import Utilities.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = new BufferedReader(new FileReader("Phase_1/PlayersData/newPlayerProgress.json"));
        JsonObject object = gson.fromJson(reader, JsonObject.class);
        object.remove("name");
        object.addProperty("name", name);

        String playerDataDir = "Phase_1/PlayersData/"+name+"/";

        new File(playerDataDir).mkdir();
        new File(playerDataDir+"Player_Custom_Workshops/").mkdir();
        new File(playerDataDir+"Player_Unfinished_Levels_Saves/").mkdir();
        Writer writer = new BufferedWriter(new FileWriter(playerDataDir+"progress.json"));
        gson.toJson(object, writer);
        writer.flush();
        writer.close();
        reader.close();

        return gson.fromJson(object, Player.class);
    }

    public static Player loadPlayer(String name) throws FileNotFoundException {
        Gson gson = new Gson();
        Reader reader = new BufferedReader(new FileReader("Phase_1/PlayersData/"+name+"/progress.json"));
        JsonObject object = gson.fromJson(reader, JsonObject.class);
        object.remove("name");
        object.addProperty("name", name);
        return gson.fromJson(object, Player.class);
    }

    public static void updatePlayer(Player player) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Writer writer = new BufferedWriter(new FileWriter("Phase_1/PlayersData/"+player.getName()+"/progress.json"));
        gson.toJson(player, writer);
        writer.flush();
        writer.close();
    }

    public HashMap<String, Byte> getGameElementsLevel() {
        return gameElementsLevel;
    }

    public static boolean exists(String playerName) {
        return new File("Phase_1/PlayersData/"+playerName).exists();
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
            if(Constants.getElementMaxMaxLevel(element) > getGameElementLevel(element)) {
                gameElementsLevel.compute(element, (k, v) -> ++v);
                return true;
            }
        }
        return false;
    }
}
