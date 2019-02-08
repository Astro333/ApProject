package Utilities;

import Interfaces.Processable;
import Items.Item;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class ProcessableDeserializer implements JsonDeserializer<Processable> {
    @Override
    public Processable deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Processable var = AnimalType.getType(jsonElement.getAsString());
        if(var != null)
            return var;
        return Item.ItemType.getType(jsonElement.getAsString());
    }
}