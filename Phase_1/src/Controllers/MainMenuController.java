package Controllers;

import Exceptions.PlayerNotFoundException;

import java.util.Scanner;

public class MainMenuController extends Controller {

    private final String PLAY_AS_REGEX;
    private final String SETTING_REGEX;
    private final String EXIT_GAME_REGEX;
    private final String NEW_PLAYER_REGEX;
    private final String PLAYER_NAME_REGEX;

    private final String pathToPlayerDataFolder = "../PlayersData/";

    {
        PLAY_AS_REGEX = "(?i:play\\s+as)\\s+[a-zA-Z0-9_]+";
        SETTING_REGEX = "(?i:show\\s+setting)";
        EXIT_GAME_REGEX = "(?i:exit\\s+game)";
        NEW_PLAYER_REGEX = "(?i:new player)";
        PLAYER_NAME_REGEX = "[a-zA-Z0-9_]+";
        String s = "a";
        s.matches(NEW_PLAYER_REGEX);
    }
    public MainMenuController(){
        super();
    }

    public void startProcessing(){
        String input = scanner.nextLine().trim();
        while (!input.matches(EXIT_GAME_REGEX)) {
            if (input.matches(PLAY_AS_REGEX)) {
                String playerName = input.split("\\s+")[2];
                play(playerName);
            }
            else if(input.matches(NEW_PLAYER_REGEX)){
                System.out.println("Enter Your Name:");
                input = scanner.nextLine().trim();
                while (true) {
                    if(!input.matches(PLAYER_NAME_REGEX)) {
                        System.err.println("Invalid Player Name, Try Again.\nEnter Your Name:");
                    }
                }
            }
            else if (input.matches(SETTING_REGEX)) {

            } else {
                System.err.println("Invalid Command");
            }
            input = scanner.nextLine().trim();
        }
    }

    /**
     * @param playerName this parameter with pathToPlayersInfoDirectory specifies
     *                  the directory in which player saved files exist.
     * */
    private void play(String playerName){
        try {

            PlayerMenuController playerMenuController = new PlayerMenuController(15238);
            playerMenuController.startProcessing();
        }
        catch (PlayerNotFoundException e){
            System.err.println("No Such Player In Existence.");
        }
        /*
        * in this method playerMenuController will be instantiated based on playerName.
        * must search list of players for player id then give player id to playerMenuController
        * */
    }
}
