package Animals.Pet;

import Animals.Animal;
import Animals.Wild.Wild;
import Items.Item;
import Map.Map;

import java.util.HashSet;
import java.util.Iterator;

import static Utilities.Math_C.distance;

public class Dog extends Animal {

    private Wild target = null;

    public Dog(int x, int y) {
        super(x, y, 1, 1, AnimalType.Dog);
    }

    /**
     * @param map map it'sself
     * @return {x, y} return new position to map
     */

    public int[] updatePosition(Map map) {

        moveTowardDestination();
        HashSet<Wild> wilds = map.getWilds();

        //check if wild still exists
        if (!wilds.contains(target)) {
            target = null;
        }

        if (!wilds.contains(target)) {
            // if there is any wild in map
            if (wilds.size() > 0) {
                Iterator<Wild> it = wilds.iterator();
                target = it.next();
                while (it.hasNext()) {
                    Wild wild = it.next();
                    if (distance(x, y, wild.getX(), wild.getY()) < distance(x, y, target.getX(), target.getY())) {
                        target = wild;
                    }
                }
                destinationX = target.getX();
                destinationY = target.getY();
            } else {
                destinationX = random.nextInt(map.cellsWidth);
                destinationY = random.nextInt(map.cellsHeight);
            }
        }
        return new int[]{x, y};
    }
}
