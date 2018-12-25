package Animals.Pet;

import Animals.Animal;
import Items.Item;
import Map.Map;

import java.util.HashSet;

public class Cat extends Animal {

    private final byte intelligence;

    public Cat(int x, int y, byte intelligence){
        super(x, y, 1, 1, AnimalType.Cat);
        this.intelligence = intelligence;
    }

    /**
     * @param map map
     * @return {x,y} return changed position to map
     * */

    public int[] updatePosition(Map map) {

        HashSet<Item> items = map.getItems();

        /*
         * write code to find nearest item based on intelligence and items HashSet
        * */

        return null;
    }
}
