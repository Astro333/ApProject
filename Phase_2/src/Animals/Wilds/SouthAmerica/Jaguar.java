package Animals.Wilds.SouthAmerica;

import Animals.Wilds.Wild;
import Utilities.AnimalType;
import Utilities.State;
import Utilities.Sprite;
import Utilities.SpriteDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;

public class Jaguar extends Wild {
    private static final HashMap<State, Sprite> stateTextures;

    static {
        HashMap<State, Sprite> temp = null;
        try {
            Reader reader = new FileReader(new File("src/res/Animals/Wilds/Jaguar/Textures/Config/States.json"));

            Gson gson = new GsonBuilder().registerTypeAdapter(Sprite.class, new SpriteDeserializer()).create();
            Type type = new TypeToken<HashMap<State, Sprite>>() {
            }.getType();
            temp = gson.fromJson(reader, type);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        stateTextures = temp;

        stateTextures.put(State.MOVE_UP_RIGHT, stateTextures.get(State.MOVE_UP_LEFT));
        stateTextures.put(State.MOVE_RIGHT, stateTextures.get(State.MOVE_LEFT));
        stateTextures.put(State.MOVE_DOWN_RIGHT, stateTextures.get(State.MOVE_DOWN_LEFT));
    }

    public Jaguar() {
        super(AnimalType.Jaguar);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public synchronized void scale(double v) {
        for (String s : cagesViews.keySet()) {
            ImageView iv = cagesViews.get(s);
            double width = Wild.cages.get(s).getFrameSize()[0];
            double height = Wild.cages.get(s).getFrameSize()[1];
            iv.setTranslateX((v - 1) * (0.5 * width) + hitBox.getCenterX() * (1 - v));
            iv.setTranslateY((v - 1) * (0.5 * height) + hitBox.getCenterY() * (1 - v));
        }
        for (State s : statesViews.keySet()) {
            ImageView iv = statesViews.get(s);
            double width = stateTextures.get(s).getFrameSize()[0];
            double height = stateTextures.get(s).getFrameSize()[1];
            iv.setTranslateX((v - 1) * (0.5 * width) + hitBox.getCenterX() * (1 - v));
            iv.setTranslateY((v - 1) * (0.5 * height) + hitBox.getCenterY() * (1 - v));
        }
        scale.set(v);
        hitBox.setRadiusX(v * (HITBOX_RADIUS_X));
        hitBox.setRadiusY(v * (HITBOX_RADIUS_Y));
    }

    @Override
    protected HashMap<State, Sprite> getStateTextures() {
        return stateTextures;
    }

    @Override
    protected Sprite getTexture(State state) {
        return stateTextures.get(state);
    }
}
