package Controllers;

import Exceptions.PlayerNotFoundException;

import java.util.Scanner;

public class MainMenuController extends Controller{

    private final Scanner scanner;

    public MainMenuController(){
        scanner = new Scanner(System.in);
    }

    public void startProcessing(){
        /*
        * in Phase_1, this method only handles "play as [Player_Name]" & "Exit Game" instructions;
        * */
    }

    /**
     * @param playerName this parameter with pathToPlayersInfoDirectory specifies
     *                  the directory in which player saved files exist.
     * */
    private void play(String playerName){
        try {
            PlayerMenuController playerMenuController = new PlayerMenuController(15238);
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
