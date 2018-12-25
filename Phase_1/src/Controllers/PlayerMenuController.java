package Controllers;

import Animals.Animal;
import Exceptions.PlayerNotFoundException;
import Interfaces.Processable;
import Levels.SaveData;
import Player.Player;
import Utilities.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.LinkedList;

public class PlayerMenuController extends Controller{

    private transient final String UPGRADE_REGEX;
    private transient final String RUN_LEVEL_REGEX;
    private transient final String PRINT_LEVELS_REGEX;
    private transient final String GO_TO_SHOP_REGEX;
    private transient final String EXIT_TO_MAIN_MENU_REGEX;

    private final Gson gson = new GsonBuilder().registerTypeAdapter(Animal.class, new  Utilities.AnimalDeserializer()).
            registerTypeAdapter(Processable.class, new Utilities.ProcessableDeserializer()).create();
    {
        UPGRADE_REGEX = "(?i:upgrade)\\s+[a-z]+";
        RUN_LEVEL_REGEX = "(?i:((run)|(load))\\s+[0-9]+)";
        GO_TO_SHOP_REGEX = "(?i:show\\s+shop)";
        EXIT_TO_MAIN_MENU_REGEX = "(?i:exit)";
        PRINT_LEVELS_REGEX = "(?i:print\\s+levels)";
        String s = "x";
        s.matches(PRINT_LEVELS_REGEX);
    }

    private final String playerDataPath;
    private final Player player;

    public PlayerMenuController(String playerName) throws PlayerNotFoundException {
        super();
        try {
            player = Player.loadPlayer(playerName);
            playerDataPath = "../PlayersData/"+playerName;
        }
        catch (FileNotFoundException e){
            throw new PlayerNotFoundException();
        }
    }

    public PlayerMenuController(Player player){
        this.player = player;
        playerDataPath = "../PlayersData/"+player.getName();
    }

    public void startProcessing(){
        String input = scanner.nextLine().trim();
        while (!input.matches(EXIT_TO_MAIN_MENU_REGEX)){
            if(input.matches(RUN_LEVEL_REGEX)) {
                String[] s = input.split("\\s+");
                if (s[0].toLowerCase().equals("run")) {
                    try {
                        runLevel(Integer.parseInt(s[1]));
                    } catch (FileNotFoundException e) {
                        System.err.println("Level File Not Found.");
                    }
                } else {
                    loadSavedGame(s[1]);
                }
            } else if(input.matches(GO_TO_SHOP_REGEX)){
                goToShop();
            } else if(input.matches(PRINT_LEVELS_REGEX)){
                try {
                    printLevels();
                } catch (FileNotFoundException e) {
                    System.err.println("Levels Not Found!");
                }
            }
            input = scanner.nextLine().trim();
        }
    }

    private void runLevel(int levelId) throws FileNotFoundException {
        String path = "DefaultGameData/LevelsInfo/level_"+levelId+".json";
        Reader reader = new BufferedReader(new FileReader(path));

    }

    private void printLevels() throws FileNotFoundException {
        Reader reader = new BufferedReader(new FileReader("DefaultGameData/LevelsInfo/level_1.json"));
        System.out.println(gson.fromJson(reader, JsonObject.class));
    }

    private void loadSavedGame(String jsonFileName){
        /*
        * find it in Player_Unfinished_Levels_Saves under pathToPlayerDataDirectory
        * */
        String path = playerDataPath+"Player_Unfinished_Levels_Saves"+jsonFileName+".json";
        Reader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        SaveData saveData = gson.fromJson(reader, SaveData.class);
        try {
            final Controller levelController = new LevelController(saveData, player);
            levelController.startProcessing();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void goToShop(){
        StringBuilder sb = new StringBuilder("Items:\n");
        LinkedList<String> upgradeableElements = new LinkedList<>();
        for(String element : player.getGameElementsLevel().keySet()){
            int level = player.getGameElementLevel(element);
            sb.append(element).append(": Level = ").append(level).append(", ");
            int maxLevel = Constants.getElementMaxMaxLevel(element);
            if(level < maxLevel){
                upgradeableElements.add(element);
                sb.append("MaxLevel = ").append(maxLevel).append(", ").
                append("Upgrade Cost = ").append(Constants.getElementMaxLevelUpgradeCost(element, level+1)).append("\n");
            } else
                sb.append("Item is At Max Level.\n");
        }
        System.out.println(sb);
        System.out.println("Options:\n");
        int i = 1;
        for(String s : upgradeableElements){
            System.out.printf("%d- Upgrade %s\n", i, s);
            ++i;
        }
        int exit = i;
        System.out.println(exit+"- Exit");
        i = scanner.nextInt();
        while (i < exit){
            if(i <= 0)
                System.err.println("Invalid Input.");
            else {
                String element = upgradeableElements.get(i-1);
                int currentLevel = player.getGameElementLevel(element);
                int cost = Constants.getElementMaxLevelUpgradeCost(element, currentLevel+1);
                if(player.getGoldMoney() >= cost){
                    System.err.println("Are You Sure?(y/n)");
                    element = scanner.nextLine().trim().toLowerCase();
                    if(element.equals("y")){
                        if(player.incrementGameElementLevel(element))
                            System.out.println("Upgrade Successful.");
                        else
                            System.err.println("Element is At Maximum Level.");
                    }
                } else
                    System.err.println("Not Enough Money.");
            }
            i = scanner.nextInt();
        }
    }
}
