package Controllers;

import java.util.Scanner;

public abstract class Controller {
    protected final Scanner scanner;

    protected Controller() {
        scanner = new Scanner(System.in);
    }

    public abstract void startProcessing();
}
