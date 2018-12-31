package Animals.Pet;

import Animals.Animal;
import Animals.Wild.Wild;
import Map.Map;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import static Utilities.Math_C.distance;

public class Dog extends Animal {

    private Long targetId;

    public Dog(int x, int y) {
        super(x, y, 1, 1, AnimalType.Dog);
    }

    private Dog(){}

    /**
     * @param map map it'sself
     * @return {x, y} return new position to map
     */

    public int[] updatePosition(Map map) {

        moveTowardDestination();
        moveTowardDestination();

        HashSet<Wild> wilds = map.getWilds();
        Wild target = map.getCells()[destinationX][destinationY].getWilds().get(targetId);
        //check if wild still exists
        if (!wilds.contains(target)) {
            targetId = null;
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
                targetId = target.getId();
            } else {
                destinationX = random.nextInt(map.cellsWidth);
                destinationY = random.nextInt(map.cellsHeight);
            }
        }
        return new int[]{x, y};
    }

    public Long getTargetId() {
        return targetId;
    }
}
