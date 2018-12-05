package Structures;

import Interfaces.Processable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.*;
import java.util.Scanner;

public class Workshop {

    private final Processable[] inputs;
    private final Integer[] inputAmounts;

    private final Processable[] outputs;
    private final Integer[] outputsAmount;

    private final byte position;

    private int amountProcessed;
    private final String name;
    private final String pathToGraphicsFolder = null; //For Phase 1, it's null
    private final byte maxMaxLevel;

    private byte level;
    private final byte maxLevel;
    private long timeToFinishTask = -1;
    private transient BooleanProperty isAtTask = null;
    private int multiplier = 1; //this is dependent on Workshop level
    /**
     * @param inputs input types.
     * @param inputsAmount amount needed of each input.
     * @param outputs output types
     * @param outputsAmount amount produced of each output
     * @param position
     * @param name Workshop Name
     * @param maxLevel Workshop maxLevel                */

    private Workshop(Processable[] inputs, Integer[] inputsAmount,
                     Processable[] outputs, Integer[] outputsAmount,
                     byte position,
                     String name,
                     byte maxLevel,
                     byte maxMaxLevel
    )
    {
        this.inputs = inputs;
        this.inputAmounts = inputsAmount;
        this.outputs = outputs;
        this.outputsAmount = outputsAmount;
        this.position = position;
        this.name = name;
        this.maxLevel = maxLevel;
        this.maxMaxLevel = maxMaxLevel;
    }

    private void initiateVariables(){}

    public static Workshop getInstance(String workshopName, int maxLevel) throws FileNotFoundException {
        String workshopDataFile = "../DefaultGameData/DefaultWorkshops/"+workshopName+".json";

        Scanner scanner = new Scanner(new File(workshopDataFile));
        StringBuilder stringBuilder = new StringBuilder(scanner.useDelimiter("\\A").next());
        scanner.close();
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append("\"maxLevel\":").append(maxLevel).append("}");
        Gson gson = new GsonBuilder().create();
        Workshop workshop = gson.fromJson(stringBuilder.toString(), Workshop.class);
        workshop.isAtTask = new SimpleBooleanProperty(
                workshop, "isAtTask", workshop.timeToFinishTask != -1);
        return workshop;
    }

    public int getAmountProcessed() {
        return amountProcessed;
    }

    public void resetAmountProcessed(){
        amountProcessed = 0;
    }

    public Integer[] getOutputsAmount() {
        return outputsAmount;
    }

    public Processable[] getOutputs() {
        return outputs;
    }

    /**
     * @param turn turns to progress at work
     * */

    public void update(int turn){
        if(isAtTask()){
            if(timeToFinishTask > turn)
                timeToFinishTask -= turn;
            else {
                timeToFinishTask = -1;
                isAtTask.set(false);
            }
        }
    }

    /**
     * @param  storage reference of storage passed to workshop
     * @return success: if storage had enough of inputs required, return true
     * */

    public boolean startWorking(Depot storage){
        /*
        * Pseudo code:
        * forEach input:
        *   if (storage.hasAllInputs){
        *       storage.removeItem(input, amount)
        *   }
        * */
        amountProcessed = 1;
        return false;
    }

    public int getUpgradeCost(){
        return 0;
    }

    public boolean isAtTask() {
        return isAtTask.get();
    }

    public BooleanProperty isAtTaskProperty() {
        return isAtTask;
    }

    public boolean upgrade(){
        if(level < maxLevel) {
            ++level;
            ++multiplier;
            return true;
        }
        return false;
    }

    public byte getLevel() {
        return level;
    }
}
