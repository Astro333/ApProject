package Levels;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.HashMap;

public class LevelRequirementMetListener {
    private HashMap<String, IntegerProperty> requirements = new HashMap<>();
    private int[] val;
    private BooleanBinding requirementsMet = new  SimpleBooleanProperty(true).and(
            requirements.get(requirements.keySet().toArray(new String[0])[0]).greaterThanOrEqualTo(val[0])
    );
    {
        String s = "GuineaFowl";
        int i = 0;
        for(IntegerProperty integerProperty : requirements.values())
            requirementsMet.and(integerProperty.greaterThan(val[i++]));
        IntegerProperty x = requirements.getOrDefault(s, null);
        if(x != null){

        }
    }
}
