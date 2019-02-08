package Animals.Pets.Africa;

import Animals.Pets.Pet;
import Items.Item;
import Utilities.AnimalType;
import Utilities.State;
import Utilities.Sprite;
import Utilities.SpriteDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;

public class Buffalo extends Pet {

    private static final HashMap<State, Sprite> stateTextures;

    private static final Image picture = new Image(Buffalo.class.getResource("/res/Animals/Pets/Buffalo/Textures/pic.png").toExternalForm());

    static {
        HashMap<State, Sprite> temp = null;
        try {
            Reader reader = new FileReader(new File("src/res/Animals/Pets/Buffalo/States.json"));
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

    public Buffalo() {
        super(AnimalType.Buffalo);
        hungerIndicator.setHeight(3);
        hungerIndicator.setX(getX() - 50);
        hungerIndicator.setY(-8);
        hungerIndicator.viewOrderProperty().bind(graphic.viewOrderProperty());
    }

    @Override
    public Image getPicture() {
        return picture;
    }

    @Override
    public Item produce() {
        return new Item(Item.ItemType.Horn, getX(), getY());
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
