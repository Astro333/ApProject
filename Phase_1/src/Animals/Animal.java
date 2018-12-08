package Animals;

import Interfaces.LevelRequirement;
import Interfaces.Processable;
import Utilities.SUID;
import javafx.beans.property.BooleanProperty;

public abstract class Animal {

    protected int x, y;
    protected int destinationX, destinationY;
    private transient BooleanProperty isTossed;
    private final Long id;
    private final AnimalType type;


    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    protected Animal(int x, int y, AnimalType type){
        id = SUID.generateId();
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void setTossed(boolean isTossed) {
        this.isTossed.set(isTossed);
    }

    public AnimalType getType(){
        return type;
    }

    public BooleanProperty isTossedProperty() {
        return isTossed;
    }

    public boolean isDead() {
        return isTossed.get();
    }

    public long getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDestinationX() {
        return destinationX;
    }

    public int getDestinationY() {
        return destinationY;
    }

    public enum AnimalType implements Processable, LevelRequirement {
        BrownBear, WhiteBear, Grizzly, Lion, Jaguar,

        Cat, Dog, Sheep, GuineaFowl, Ostrich, Cow, Buffalo,
        Turkey, Chicken, Penguin, Llama, BrownCow, Walrus ,
        Yak, KingPenguin,Goose,Goat
    }
}
