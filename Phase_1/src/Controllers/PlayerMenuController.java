package Controllers;

import Exceptions.PlayerNotFoundException;
import Player.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Scanner;

public class PlayerMenuController {

    private final String UPGRADE_REGEX;

    private final String playerDataPath;
    private final Player player;
    private final Scanner scanner;

    public PlayerMenuController(long playerId) throws PlayerNotFoundException {
        String playerDataPath = "../PlayersData/"+playerId;
        try {
            Reader reader = new BufferedReader(new FileReader(playerDataPath));
            Gson gson = new GsonBuilder().create();
            player = gson.fromJson(reader, Player.class);
            this.playerDataPath = playerDataPath;
            scanner = new Scanner(System.in);
        }
        catch (FileNotFoundException e){
            throw new PlayerNotFoundException();
        }
    }

    public void startProcessing(){
        /*
        * in here, player's commands are handled
        * */
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
    }

    private void goToShop(){
        /*
        * show shop options in here
        * */
    }
}