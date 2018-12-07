package Structures;

import Interfaces.Processable;
import Utilities.Constants;
import Utilities.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Workshop {

    private final Processable[] inputs = null;
    private final Integer[] inputAmounts = null;

    private final Processable[] outputs = null;
    private final Integer[] outputsAmount = null;

    private final Byte position = null;

    private final Float pos_x = null;
    private final Float pos_y = null;

    private final String name = null;
    private final Byte maxMaxLevel = null;
    private final Byte maxLevel = null;
    private byte level;

    private int amountProcessed;
    private int multiplier = 1; //this is dependent on Workshop level
    private long timeToFinishTask = -1;

    private transient final BooleanProperty isAtTask;

    private Workshop(){
        this.isAtTask = new SimpleBooleanProperty(this, "isAtTask", false);
    }

    public static Workshop getInstance(String workshopName, int maxLevel,
                                       byte workshopPosition, String continent) throws FileNotFoundException {
        String workshopDataFile = "../DefaultGameData/DefaultWorkshops/"+workshopName+".json";

        Scanner scanner = new Scanner(new File(workshopDataFile));
        StringBuilder stringBuilder = new StringBuilder(scanner.useDelimiter("\\A").next());
        scanner.close();
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        stringBuilder.append(",\"maxLevel\":").append(maxLevel).
                append(",\"position\":").append(workshopPosition);
        Pair<Integer, Integer> pos = Constants.getWorkshopPosition(continent, workshopPosition);
        stringBuilder.append(",\"pos_x\":").append(pos.getKey());
        stringBuilder.append(",\"pos_y\":").append(pos.getValue());

        stringBuilder.append("}");
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(stringBuilder.toString(), Workshop.class);
    }

    /*public static Workshop getInstance(String workshopName, int maxLevel){

    }*/

    public byte getPosition() {
        return position;
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
        for(Processable processable : inputs){

        }
        amountProcessed = 1;
        return false;
    }

    public int getUpgradeCost(){
        return Constants.getElementLevelUpgradeCost(name, level+1);
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
