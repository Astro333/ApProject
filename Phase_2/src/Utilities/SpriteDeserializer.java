package Utilities;

import com.google.gson.*;
import javafx.scene.image.Image;

import java.lang.reflect.Type;

public class SpriteDeserializer implements JsonDeserializer<Sprite> {
    @Override
    public Sprite deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        JsonArray arr;
        String s = object.get("image").getAsString();
        Image image = new Image(s);
        int[] frameSize = new int[2];
        arr = object.get("frameSize").getAsJsonArray();
        frameSize[0] = arr.get(0).getAsInt();
        frameSize[1] = arr.get(1).getAsInt();
        arr = object.get("offset").getAsJsonArray();
        int[] offset = new int[2];
        offset[0] = arr.get(0).getAsInt();
        offset[1] = arr.get(1).getAsInt();
        arr = object.get("relativeOffset").getAsJsonArray();
        int[] relativeOffset = new int[2];
        relativeOffset[0] = arr.get(0).getAsInt();
        relativeOffset[1] = arr.get(1).getAsInt();

        int framesCount = object.get("framesCount").getAsInt();
        int animationColumns = object.get("animationColumns").getAsInt();
        double animationLength = object.get("animationLength").getAsDouble();
        return new Sprite(image, offset, frameSize, relativeOffset, framesCount, animationColumns, animationLength);
    }
}