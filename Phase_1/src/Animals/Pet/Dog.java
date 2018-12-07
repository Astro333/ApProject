package Animals.Pet;

import Animals.Animal;
import Animals.Wild.Wild;
import Map.Map;

public class Dog extends Animal {

    private final byte level;

    public Dog(int x, int y, byte level) {
        super(x, y, AnimalType.Dog);
        this.level = level;
    }

    public byte getLevel() {
        return level;
    }

    public void kill(Wild wild){
        wild.setTossed(true);
    }

    /**
     * @param map map it'sself
     * @return {x, y} return new position to map
     * */

    public int[] updatePosition(Map map) {
        /*
        * find nearest wild animal and move towards it
        * */
        return null;
    }
}
