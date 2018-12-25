package Structures;

import Interfaces.Processable;
import Items.Item;
import static Items.Item.ItemType;
import Utilities.Constants;
import Utilities.Pair;
import Utilities.ProcessableDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Workshop {

    private final ItemType[] inputs = null;
    private final Integer[] inputsAmount = null;

    private final Processable[] outputs = null;
    private final Integer[] outputsAmount = null;

    private final Byte position = null;

    private final Float pos_x = null;
    private final Float pos_y = null;

    private final String realName = null;
    private final String demonstrativeName = null;

    private final Byte maxMaxLevel = null;
    private final Byte maxLevel = null;
    private byte level;
    private int processingMultiplier;

    private int multiplier = 1; //this is dependent on Workshop level
    private long timeToFinishTask = -1;
    private transient static final Gson gson = new GsonBuilder().registerTypeAdapter(Processable.class,
            new ProcessableDeserializer()).create();

    private transient final BooleanProperty isAtTask;
    private Workshop(){
        this.isAtTask = new SimpleBooleanProperty(this, "isAtTask", false);
    }

    public String getRealName() {
        return realName;
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
        return gson.fromJson(stringBuilder.toString(), Workshop.class);
    }

    public byte getPosition() {
        return position;
    }

    public int getProcessingMultiplier() {
        return processingMultiplier;
    }

    public void resetAmountProcessed(){
        processingMultiplier = 0;
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
     * @param  depot reference of depot passed to workshop
     * @return success: if depot had enough of inputs required, return true
     * */

    public boolean startWorking(Depot depot){
        /*
        * Pseudo code:
        * forEach input:
        *   if (depot.hasAllInputs){
        *       depot.removeItem(input, amount)
        *   }
        * */
        int multiplier = this.multiplier;
        int temp;
        for(int i = 0; i < inputs.length; ++i){
            temp = depot.getItemAmount(inputs[i])/inputsAmount[i];
            if(temp == 0)
                return false;
            if(temp < multiplier)
                multiplier = temp;
        }
        processingMultiplier = multiplier;
        isAtTaskProperty().set(true);
        timeToFinishTask = calculateTimeToFinishTask();
        return true;
    }

    private int calculateTimeToFinishTask(){
        return 5;
    }

    public int getUpgradeCost(){
        return Constants.getElementLevelUpgradeCost(realName, level+1);
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
