package Utilities;

import Animals.Animal;
import Animals.Pet.Pet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

public class AnimalDeserializer implements JsonDeserializer<Animal> {
    @Override
    public Animal deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            Class animalClass = Class.forName(
                    Constants.getAnimalClassPath(Animal.AnimalType.getType(jsonElement.getAsJsonObject().get("type").getAsString())));
            Constructor constructor;
            if (animalClass.isAssignableFrom(Pet.class)) {
                constructor = animalClass.getDeclaredConstructor(int.class, int.class);
            } else {
                constructor = animalClass.getDeclaredConstructor(int.class, int.class);
            }
            constructor.setAccessible(true);
            int x = jsonElement.getAsJsonObject().get("x").getAsInt();
            int y = jsonElement.getAsJsonObject().get("y").getAsInt();
            Animal instance = (Animal) constructor.newInstance(x, y);
            constructor.setAccessible(false);
            return instance;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }
}
