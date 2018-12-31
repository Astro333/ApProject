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

    private final Byte[] productionTime = null;

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

    public static Workshop getInstance(String workshopName, int maxLevel,
                                       byte workshopPosition, String continent) throws FileNotFoundException {
        workshopName = workshopName.replace("Workshop", "");
        String workshopDataFile = "DefaultGameData/DefaultWorkshops/"+workshopName+"/"+workshopName+"Workshop.json";

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
    public long getTimeToFinishTask() {
        return timeToFinishTask;
    }

    public String getRealName() {
        return realName;
    }

    public String getDemonstrativeName() {
        return demonstrativeName;
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
        if(!isAtTask.get()) {
            int multiplier = this.multiplier;
            int temp;
            for (int i = 0; i < inputs.length; ++i) {
                temp = depot.getItemAmount(inputs[i]) / inputsAmount[i];
                if (temp == 0) {
                    System.err.println("Not Enough Components in Depot.");
                    return false;
                }
                if (temp < multiplier)
                    multiplier = temp;
            }
            processingMultiplier = multiplier;
            for (int i = 0; i < inputs.length; ++i) {
                depot.removeAllStorable(inputs[i], inputsAmount[i] * processingMultiplier);
                System.out.println(demonstrativeName + " took " + inputsAmount[i] * multiplier + " " + inputs[i] + " from Depot.");
            }
            isAtTaskProperty().set(true);
            timeToFinishTask = calculateTimeToFinishTask();
            return true;
        }
        System.err.println("Another task is being done.");
        return false;
    }

    private int calculateTimeToFinishTask(){
        return productionTime[level];
    }

    public int getUpgradeCost(){
        return level == maxMaxLevel ? Integer.MIN_VALUE : Constants.getElementLevelUpgradeCost(realName, level+1);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("*********************************\n");
        sb.append(demonstrativeName).append(" :\n").
                append("Inputs: ");
        for(int i = 0; i < inputs.length; ++i){
            sb.append(inputs[i]).append(" : ").append(inputsAmount[i]);
            if(i != inputs.length-1)
                sb.append(", ");
        }
        sb.append("\n").append("Outputs: ");

        for(int i = 0; i < outputs.length; ++i){
            sb.append(outputs[i]).append(" : ").append(outputsAmount[i]);
            if(i != outputs.length-1)
                sb.append(", ");
        }
        sb.append("\n");
        sb.append("Level: ").append(level).append(", MaxLevel: ").append(maxLevel).append(", TaskTime: ").append(calculateTimeToFinishTask()).append("\n");
        if(isAtTask()){
            sb.append("Time to finish current Task: ").append(timeToFinishTask).append("\n");
        } else
            sb.append("No Task At Hand.\n");
        sb.append("*********************************");
        return sb.toString();
    }
}
