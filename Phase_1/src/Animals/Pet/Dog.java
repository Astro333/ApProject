package Animals.Pet;

import Animals.Animal;
import Animals.Wild.Wild;

import java.util.List;

public class Dog extends Animal {

    public Dog(int x, int y) {
        super(x, y, AnimalType.Dog);
    }

    public void kill(Wild wild){
        wild.setDead(true);
    }

    /**
     * @param wilds list of wild animals in map
     * @return {x, y} return new position to map
     * */

    public int[] changePosition(List<Wild> wilds){
        /*
        * find nearest wild animal and move towards it
        * */
        return null;
    }
}
