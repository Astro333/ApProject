import Controllers.MainMenuController;

public class Main {
    private static final String pathToPlayersInfoDirectory = "PlayersData";
    public static void main(String[] args) {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.startProcessing();
    }
}
