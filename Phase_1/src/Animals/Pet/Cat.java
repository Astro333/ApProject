package Animals.Pet;

import Animals.Animal;
import Items.Item;
import java.util.List;

public class Cat extends Animal {

    private final byte intelligence;

    public Cat(int x, int y, byte intelligence){
        super(x, y, AnimalType.Cat);
        this.intelligence = intelligence;
    }

    /**
     * @param items list of items in map
     * @return {x,y} return changed position to map
     * */

    public int[] changePosition(List<Item> items) {
        /*
        * implement the algorithm to find items based on intelligence
        * */
        return null;
    }
}
