import Controllers.MainMenuController;
import Map.MapGraphics;

public class Main {
    public static void main(String[] args) {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.startProcessing();
        MapGraphics.KillThread();
    }
}
