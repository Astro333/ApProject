package Controllers;

import Animals.Animal;
import Exceptions.PlayerNotFoundException;
import Interfaces.Processable;
import Levels.SaveData;
import Player.Player;
import Utilities.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.LinkedList;

public class PlayerMenuController extends Controller {

    private transient final String UPGRADE_REGEX;
    private transient final String RUN_LEVEL_REGEX;
    private transient final String PRINT_LEVELS_REGEX;
    private transient final String GO_TO_SHOP_REGEX;
    private transient final String EXIT_TO_MAIN_MENU_REGEX;
    private transient final String LOAD_SAVE_REGEX;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(Animal.class, new Utilities.AnimalDeserializer()).
            registerTypeAdapter(Processable.class, new Utilities.ProcessableDeserializer()).create();

    {
        UPGRADE_REGEX = "(?i:upgrade)\\s+[a-z]+";
        RUN_LEVEL_REGEX = "(?i:run\\s+[0-9]+)";
        LOAD_SAVE_REGEX = "(?i:load\\s+[a-zA-Z0-9-_]+\\.json)";
        GO_TO_SHOP_REGEX = "(?i:show\\s+shop)";
        EXIT_TO_MAIN_MENU_REGEX = "(?i:exit)";
        PRINT_LEVELS_REGEX = "(?i:print\\s+levels)";
        String s = "x";
        s.matches(EXIT_TO_MAIN_MENU_REGEX);
    }

    private final String playerDataPath;
    private final Player player;

    public PlayerMenuController(String playerName) throws PlayerNotFoundException {
        super();
        try {
            player = Player.loadPlayer(playerName);
            playerDataPath = "Phase_1/PlayersData/" + playerName;
        } catch (FileNotFoundException e) {
            throw new PlayerNotFoundException();
        }
    }

    public PlayerMenuController(Player player) {
        this.player = player;
        playerDataPath = "Phase_1/PlayersData/" + player.getName();
    }

    public void startProcessing() {
        String input = scanner.nextLine().trim();
        while (!input.matches(EXIT_TO_MAIN_MENU_REGEX)) {
            if (input.matches(RUN_LEVEL_REGEX)) {
                String[] s = input.split("\\s+");
                try {
                    runLevel(Integer.parseInt(s[1]));
                } catch (FileNotFoundException e) {
                    System.err.println("Level File Not Found.");
                }
            } else if(input.matches(LOAD_SAVE_REGEX)){
                String file = input.split("\\s+")[1];
                loadSavedGame(file);
            }else if (input.matches(GO_TO_SHOP_REGEX)) {
                goToShop();
            } else if (input.matches(PRINT_LEVELS_REGEX)) {
                try {
                    printLevels();
                } catch (FileNotFoundException e) {
                    System.err.println("Levels Not Found!");
                }
            } else if (input.equals("help")) {
                System.out.println("Commands:");
                System.out.println("\t\"Show Shop\"");
                System.out.println("\t\"[Element] info\"");
                System.out.println("\t\"exit\": exit to Main Menu");
                System.out.println("\t\"Run [Level_Number]\"");
                System.out.println("\t\"Load [path_to_save_data]\": load a saved game");
            } else
                System.err.println("Invalid Input");
            input = scanner.nextLine().trim();
        }
    }

    private void runLevel(int levelId) throws FileNotFoundException {
        String path = "Phase_1/DefaultGameData/LevelsInfo/level_" + levelId + ".json";
        LevelController levelController = new LevelController(path, player);
        levelController.startProcessing();
    }

    private void printLevels() throws FileNotFoundException {
        int i = 1;
        File levelFile = new File("Phase_1/DefaultGameData/LevelsInfo/level_1.json");
        while (levelFile.exists()) {
            System.out.println("**********************************");
            System.out.println("##############");
            System.out.printf("## Level %02d ##\n", i);
            System.out.println("##############");
            Reader reader = new BufferedReader(new FileReader(levelFile));
            JsonObject object = gson.fromJson(reader, JsonObject.class);
            for (String s : object.keySet())
                System.out.println(s + " : " + object.get(s).toString());
            ++i;
            levelFile = new File("Phase_1/DefaultGameData/LevelsInfo/level_" + i + ".json");
        }
    }

    private void loadSavedGame(String jsonFileName) {
        /*
         * find it in Player_Unfinished_Levels_Saves under pathToPlayerDataDirectory
         * */
        String path = playerDataPath + "/Player_Unfinished_Levels_Saves/" + jsonFileName;
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

    private void goToShop() {
        StringBuilder sb = new StringBuilder("Items:\n");
        LinkedList<String> upgradeableElements = new LinkedList<>();
        for (String element : player.getGameElementsLevel().keySet()) {
            int level = player.getGameElementLevel(element);
            sb.append(element).append(": Level = ").append(level).append(", ");
            int maxLevel = Constants.getElementMaxMaxLevel(element);
            if (level < maxLevel) {
                upgradeableElements.add(element);
                sb.append("MaxLevel = ").append(maxLevel).append(", ").
                        append("Upgrade Cost = ").append(Constants.getElementMaxLevelUpgradeCost(element, level + 1)).append("\n");
            } else
                sb.append("Item is At Max Level.\n");
        }
        System.out.println(sb);
        System.out.println("Options:");
        int i = 1;
        for (String s : upgradeableElements) {
            System.out.printf("%02d- Upgrade %s\n", i, s);
            ++i;
        }
        int exit = i;
        System.out.println(exit + "- Exit");
        i = scanner.nextInt();
        scanner.nextLine();
        while (i < exit) {
            if (i <= 0)
                System.err.println("Invalid Input.");
            else {
                String element = upgradeableElements.get(i - 1);
                int currentLevel = player.getGameElementLevel(element);
                int cost = Constants.getElementMaxLevelUpgradeCost(element, currentLevel + 1);
                if (player.getGoldMoney() >= cost) {
                    System.err.println("Are You Sure?(y/n)");
                    if (scanner.nextLine().trim().toLowerCase().equals("y")) {
                        if (player.incrementGameElementLevel(element)) {
                            player.addGoldMoney(-cost);
                            try {
                                Player.updatePlayer(player);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            System.out.println(element + " Was Upgraded to " + currentLevel + 1);
                        } else
                            System.err.println(element + " is At Maximum Level.");
                    }
                } else
                    System.err.println("Not Enough Money.");
            }
            i = scanner.nextInt();
        }
    }
}
