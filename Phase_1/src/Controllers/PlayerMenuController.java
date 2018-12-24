package Controllers;

import Animals.Animal;
import Exceptions.PlayerNotFoundException;
import Interfaces.Processable;
import Levels.SaveData;
import Player.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class PlayerMenuController extends Controller{

    private transient final String UPGRADE_REGEX;
    private transient final String RUN_LEVEL_REGEX;
    private transient final String GO_TO_SHOP_REGEX;
    private transient final String EXIT_TO_MAIN_MENU_REGEX;

    private final Gson gson = new GsonBuilder().registerTypeAdapter(Animal.class, new  Utilities.AnimalDeserializer()).
            registerTypeAdapter(Processable.class, new Utilities.ProcessableDeserializer()).create();
    {
        UPGRADE_REGEX = "(?i:upgrade)\\s+[a-z]+";
        RUN_LEVEL_REGEX = "(?i:((run)|(load))\\s+[0-9]+)";
        GO_TO_SHOP_REGEX = "(?i:show\\s+shop)";
        EXIT_TO_MAIN_MENU_REGEX = "(?i:exit)";
        String s = "x";
        s.matches(EXIT_TO_MAIN_MENU_REGEX);
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
            if(input.matches(RUN_LEVEL_REGEX)){
                String[] s = input.split("\\s+");
                if(s[0].toLowerCase().equals("run")){
                    runLevel(Integer.parseInt(s[1]));
                }
                else {
                    loadGame(s[1]);
                }
            }
            input = scanner.nextLine().trim();
        }
    }

    private void runLevel(int levelId){
        /*
        * validate levelFile and instantiate a new LevelController based on level file
        * */
    }

    private void printLevels(){
    }

    private void loadGame(String jsonFileName){
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
        /*
        * show shop options in here
        * */
    }
}
