package Player;

import Utilities.Loader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;

public class Player implements Serializable{

    private int money;
    private int goldMoney;

    private final HashMap<Byte, LinkedList<Integer>> levelsTime = null;
    private final HashMap<String, Integer> gameElementsLevel = null;

    private final String name = null;

    private Player(){}

    public byte[] serialize(){
        /*
         * int nameLength,
         * String name,
         * int goldMoney,
         * int money
         * int levelsFinished
         * */
        String name = getName();
        ByteBuffer buffer = ByteBuffer.allocate(4 + name.length() + 4 + 4 + 4);
        buffer.putInt(name.length());// name length
        buffer.put(name.getBytes(StandardCharsets.UTF_8));// name
        buffer.putInt(goldMoney);
        buffer.putInt(money);
        buffer.putInt(getLevelsFinished());
        return buffer.array();
    }

    public static Player create(String name) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Reader reader = new BufferedReader(new FileReader("PlayersData/Players/newPlayerProgress.json"));
        JsonObject object = gson.fromJson(reader, JsonObject.class);
        object.remove("name");
        object.addProperty("name", name);

        String playerDataDir = "PlayersData/Players/"+name+"/";

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
        Reader reader = new BufferedReader(new FileReader("PlayersData/Players/"+name+"/progress.json"));
        JsonObject object = gson.fromJson(reader, JsonObject.class);
        return gson.fromJson(object, Player.class);
    }

    public static void updatePlayer(Player player) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Writer writer = new BufferedWriter(new FileWriter("PlayersData/Players/"+player.getName()+"/progress.json"));
        gson.toJson(player, writer);
        writer.flush();
        writer.close();
    }

    public HashMap<String, Integer> getGameElementsLevel() {
        return gameElementsLevel;
    }

    public static boolean exists(String playerName) {
        return new File("PlayersData/Players/"+playerName).exists();
    }

    public String getName() {
        return name;
    }

    public int getGoldMoney() {
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

    public int getLevelsFinished(){
        return levelsTime.size();
    }

    public void addLevelTime(byte levelId, int time){
        if(levelsTime.containsKey(levelId)) {
            int i = 0;
            while (levelsTime.get(levelId).get(i) < time) {
                ++i;
                if(i == levelsTime.size())
                    break;
            }
            levelsTime.get(levelId).add(i, time);
        }
        else {
            levelsTime.put(levelId, new LinkedList<>());
            levelsTime.get(levelId).add(time);
        }
    }

    public int getGameElementLevel(String element){
        return gameElementsLevel.get(element);
    }

    public boolean incrementGameElementLevel(String element){
        if(gameElementsLevel.containsKey(element)) {
            if(Loader.getElementMaxMaxLevel(element) > getGameElementLevel(element)) {
                gameElementsLevel.compute(element, (k, v) -> ++v);
                return true;
            }
        }
        return false;
    }
}
