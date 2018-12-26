package Animals;

import Interfaces.Processable;
import Utilities.SUID;

import java.util.Random;

public abstract class Animal {

    protected int x, y;
    protected final int speed;// for Phase 2
    protected final int runningSpeed;// for Phase 2
    protected int destinationX, destinationY;
    private final Long id;
    private final AnimalType type;
    protected transient Random random;

    protected void moveTowardDestination() {
        if (x != destinationX && y != destinationY) {
            if (random.nextBoolean()) {
                if (x < destinationX)
                    ++x;
                else
                    --x;
            } else {
                if (y < destinationY)
                    ++y;
                else
                    --y;
            }
        } else if (x != destinationX) {
            if (x < destinationX)
                ++x;
            else
                --x;
        } else if (y != destinationY) {
            if (y < destinationY)
                ++y;
            else
                --y;
        }
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    protected Animal(int x, int y, int speed, int runningSpeed, AnimalType type){
        id = SUID.generateId();
        this.x = x;
        this.y = y;
        this.type = type;
        this.speed = speed;
        this.runningSpeed = runningSpeed;
    }


    public AnimalType getType(){
        return type;
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

    public enum AnimalType implements Processable {
        BrownBear, WhiteBear, Grizzly, Lion, Jaguar,

        Cat, Dog,
        Sheep, GuineaFowl, Ostrich, Cow, Buffalo,
        Turkey, Chicken, Penguin, Llama, BrownCow, Walrus,
        Yak, KingPenguin, Goose, Goat
    }
}
