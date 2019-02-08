package Utilities;

import Animals.Animal;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class AnimalDeserializer implements JsonDeserializer<Animal> {
    @Override
    public Animal deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            Class animalClass = Class.forName(
                    Loader.getAnimalClassPath(AnimalType.getType(jsonElement.getAsJsonObject().get("type").getAsString())));
            return jsonDeserializationContext.deserialize(jsonElement, animalClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
