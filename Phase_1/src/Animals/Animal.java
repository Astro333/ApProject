package Animals;

import Animals.Pet.Pet;
import Animals.Wild.Wild;
import Interfaces.Processable;
import Utilities.SUID;

import java.util.HashMap;
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

    protected Animal(int x, int y, int speed, int runningSpeed, AnimalType type) {
        id = SUID.generateId();
        this.x = x;
        this.y = y;
        this.type = type;
        this.speed = speed;
        this.runningSpeed = runningSpeed;
        this.random = new Random();
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("type : " + type + ", At : (" + x + ", " + y+")");
        if (this instanceof Pet) {
            s.append(", fullness : ").append(((Pet) this).getFullness());
        } else {
            s.append(", Status : ");
            if(((Wild)this).isCaged())
                s.append("Caged");
            else
                s.append("Free");
            s.append(", ID : ").append(id);
        }
        return s.toString();
    }

    public AnimalType getType() {
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
        BrownBear(true), WhiteBear(true), Grizzly(true), Lion(true), Jaguar(true),

        Cat(false), Dog(false),
        Sheep(false), GuineaFowl(false), Ostrich(false), Cow(false), Buffalo(false),
        Turkey(false), Chicken(false), Penguin(false), Llama(false), BrownCow(false), Walrus(false),
        Yak(false), KingPenguin(false), Goose(false), Goat(false);
        public final boolean IS_WILD;
        private static HashMap<String, AnimalType> animalTypeHashMap;
        private AnimalType(boolean IS_WILD){
            this.IS_WILD = IS_WILD;
        }
        static {
            animalTypeHashMap = new HashMap<>();
            for (AnimalType type : AnimalType.values()) {
                animalTypeHashMap.put(type.toString(), type);
            }
        }

        public static AnimalType getType(String name) {
            return animalTypeHashMap.getOrDefault(name, null);
        }
    }
}
